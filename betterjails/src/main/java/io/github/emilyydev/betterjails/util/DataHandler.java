//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2024 emilyy-dev
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package io.github.emilyydev.betterjails.util;

import com.earth2me.essentials.User;
import com.github.fefo.betterjails.api.event.jail.JailCreateEvent;
import com.github.fefo.betterjails.api.event.jail.JailDeleteEvent;
import com.github.fefo.betterjails.api.event.plugin.PluginSaveDataEvent;
import com.github.fefo.betterjails.api.event.prisoner.PlayerImprisonEvent;
import com.github.fefo.betterjails.api.event.prisoner.PrisonerReleaseEvent;
import com.github.fefo.betterjails.api.model.jail.Jail;
import com.github.fefo.betterjails.api.model.prisoner.Prisoner;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import com.google.common.collect.ImmutableList;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.api.impl.model.jail.ApiJail;
import io.github.emilyydev.betterjails.config.BetterJailsConfiguration;
import io.github.emilyydev.betterjails.config.SubCommandsConfiguration;
import io.github.emilyydev.betterjails.dataupgrade.DataUpgrader;
import io.github.emilyydev.betterjails.dataupgrade.V1ToV2;
import io.github.emilyydev.betterjails.interfaces.permission.PermissionInterface;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class DataHandler {

  public static final String IS_RELEASED_FIELD = "released";
  public static final String LAST_LOCATION_FIELD = "last-location";
  public static final String GROUP_FIELD = "group";
  public static final String EXTRA_GROUPS_FIELD = "extra-groups";
  public static final String UUID_FIELD = "uuid";
  public static final String NAME_FIELD = "name";
  public static final String JAIL_FIELD = "jail";
  public static final String JAILED_BY_FIELD = "jailed-by";
  public static final String SECONDS_LEFT_FIELD = "seconds-left";
  public static final String TOTAL_SENTENCE_TIME = "total-sentence-time";

  private static final List<DataUpgrader> DATA_UPGRADERS =
      ImmutableList.of(
          new V1ToV2(),
          DataUpgrader.TAIL
      );

  public final Path playerDataFolder;
  private final BetterJailsPlugin plugin;
  private final BetterJailsConfiguration config;
  private final SubCommandsConfiguration subCommands;
  private final Server server;
  private final Set<String> prisonerNames = new HashSet<>();
  private final Set<UUID> prisonerIds = new HashSet<>();
  private final Map<String, Jail> jails = new HashMap<>();
  private final Map<UUID, YamlConfiguration> prisonersMap = new HashMap<>();
  private final Map<UUID, Long> playersJailedUntil = new HashMap<>();
  private final Path jailsFile;
  private Location backupLocation;
  private YamlConfiguration jailsYaml;

  public DataHandler(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
    this.config = plugin.configuration();
    this.subCommands = plugin.subCommands();
    this.server = plugin.getServer();

    final Path pluginDir = plugin.getPluginDir();
    this.jailsFile = pluginDir.resolve("jails.yml");
    this.playerDataFolder = pluginDir.resolve("playerdata");
  }

  public void init() throws IOException, InvalidConfigurationException {
    this.backupLocation = this.config.backupLocation().mutable();

    Files.createDirectories(this.playerDataFolder);
    loadJails();
    alertNewConfigAvailable();

    final long now = System.currentTimeMillis();
    try (final Stream<Path> s = Files.list(this.playerDataFolder)) {
      s.forEach(file -> {
        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
        migratePrisonerData(yaml, file);
        final UUID uuid = UUID.fromString(file.getFileName().toString().replace(".yml", ""));
        if (this.config.considerOfflineTime()) {
          this.prisonersMap.put(uuid, yaml);
          if (yaml.get(LAST_LOCATION_FIELD, this.backupLocation) != this.backupLocation) {
            this.playersJailedUntil.put(uuid, now + yaml.getLong(SECONDS_LEFT_FIELD, 0L) * 1000L);
          }
        }

        final String name = yaml.getString(NAME_FIELD);
        if (name != null) {
          this.prisonerNames.add(name.toLowerCase(Locale.ROOT));
        }

        this.prisonerIds.add(uuid);
      });
    }
  }

  private void loadJails() throws IOException {
    if (Files.notExists(this.jailsFile)) {
      Files.createFile(this.jailsFile);
    }

    this.jailsYaml = YamlConfiguration.loadConfiguration(this.jailsFile.toFile());
    for (final String key : this.jailsYaml.getKeys(false)) {
      final String lowerCaseKey = key.toLowerCase(Locale.ROOT);
      this.jails.put(lowerCaseKey, new ApiJail(lowerCaseKey, (Location) this.jailsYaml.get(key)));
    }
  }

  public Set<UUID> getPrisonerIds() {
    return Collections.unmodifiableSet(this.prisonerIds);
  }

  public boolean isPlayerJailed(final UUID uuid) {
    return this.prisonerIds.contains(uuid);
  }

  public boolean isPlayerJailed(final String playerName) {
    return this.prisonerNames.contains(playerName.toLowerCase(Locale.ROOT));
  }

  public YamlConfiguration retrieveJailedPlayer(final UUID uuid) {
    if (!isPlayerJailed(uuid)) {
      final YamlConfiguration config = new YamlConfiguration();
      config.set("version", DataUpgrader.VERSION);
      V1ToV2.setVersionWarning(config);
      return config;
    }

    if (this.prisonersMap.containsKey(uuid)) {
      return this.prisonersMap.get(uuid);
    } else {
      final Path playerFile = this.playerDataFolder.resolve(uuid + ".yml");
      final YamlConfiguration config = new YamlConfiguration();
      try {
        config.load(playerFile.toFile());
        migratePrisonerData(config, playerFile);
      } catch (final IOException exception) {
        if (!(exception instanceof FileNotFoundException)) {
          this.plugin.getLogger().log(Level.SEVERE, "Couldn't read file " + playerFile, exception);
        }
      } catch (final InvalidConfigurationException exception) {
        this.plugin.getLogger().log(Level.SEVERE, "Invalid YAML configuration in file " + playerFile, exception);
      }

      return config;
    }
  }

  public void loadJailedPlayer(final UUID uuid, final YamlConfiguration jailedPlayer) {
    this.prisonersMap.put(uuid, jailedPlayer);
  }

  public void unloadJailedPlayer(final UUID uuid) {
    this.prisonersMap.remove(uuid);
    this.playersJailedUntil.remove(uuid);
  }

  public Map<String, Jail> getJails() {
    return this.jails;
  }

  public @Nullable Jail getJail(final String name) {
    return this.jails.get(name.toLowerCase(Locale.ROOT));
  }

  public CompletableFuture<Void> addJail(final String name, final Location location) {
    final String lowerCaseName = name.toLowerCase(Locale.ROOT);
    this.jails.computeIfAbsent(lowerCaseName, key -> new ApiJail(key, location))
        .location(ImmutableLocation.copyOf(location));
    this.jailsYaml.set(lowerCaseName, location);
    this.plugin.eventBus().post(JailCreateEvent.class, name, ImmutableLocation.copyOf(location));
    return FileIO.writeString(this.jailsFile, this.jailsYaml.saveToString());
  }

  public CompletableFuture<Void> removeJail(final String name) {
    final String lowerCaseName = name.toLowerCase(Locale.ROOT);
    final Jail jail = this.jails.remove(lowerCaseName);
    this.jailsYaml.set(name, null); // just in case...
    this.jailsYaml.set(lowerCaseName, null);
    this.plugin.eventBus().post(JailDeleteEvent.class, jail);
    return FileIO.writeString(this.jailsFile, this.jailsYaml.saveToString());
  }

  public boolean addJailedPlayer(final OfflinePlayer player, final String jailName, final UUID jailer, final @Nullable String jailerName, final long secondsLeft) {
    final Location alternativeLastLocation = player.isOnline() ? player.getPlayer().getLocation() : this.backupLocation;
    return addJailedPlayer(player, jailName, jailer, jailerName, secondsLeft, true, alternativeLastLocation);
  }

  public boolean addJailedPlayer(
      final OfflinePlayer player,
      final String jailName,
      final UUID jailer,
      final @Nullable String jailerName,
      final long secondsLeft,
      final boolean teleport,
      final Location alternativeLastLocation
  ) {
    final UUID prisonerUuid = player.getUniqueId();
    final YamlConfiguration yaml = retrieveJailedPlayer(prisonerUuid);
    final Jail jail = getJail(jailName);
    final boolean jailExists = jail != null;
    final boolean isPlayerOnline = player.isOnline();
    final Player online = player.getPlayer();
    final boolean isPlayerJailed = isPlayerJailed(prisonerUuid);

    if (!jailExists) {
      return false;
    }

    yaml.set(UUID_FIELD, prisonerUuid.toString());
    yaml.set(NAME_FIELD, player.getName());
    yaml.set(JAIL_FIELD, jailName.toLowerCase(Locale.ROOT));
    if (jailerName != null) {
      yaml.set(JAILED_BY_FIELD, jailerName);
    }
    yaml.set(SECONDS_LEFT_FIELD, secondsLeft);
    yaml.set(TOTAL_SENTENCE_TIME, secondsLeft);
    yaml.set(IS_RELEASED_FIELD, false);

    if (isPlayerOnline && !isPlayerJailed) {
      assert online != null;
      yaml.set(LAST_LOCATION_FIELD, alternativeLastLocation);

      if (teleport) {
        Teleport.teleportAsync(online, jail.location().mutable());
      }

      this.prisonersMap.put(prisonerUuid, yaml);

      final SubCommandsConfiguration.SubCommands subCommands = this.subCommands.onJail();
      subCommands.executeAsPrisoner(this.server, online, yaml.getString(JAILED_BY_FIELD, ""));
      subCommands.executeAsConsole(this.server, online, yaml.getString(JAILED_BY_FIELD, ""));
    } else if (!isPlayerOnline && !isPlayerJailed) {
      yaml.set(LAST_LOCATION_FIELD, this.backupLocation);

      if (this.config.considerOfflineTime()) {
        this.prisonersMap.put(prisonerUuid, yaml);
      }

    } else if (isPlayerOnline) {
      assert online != null;
      Location lastLocation = (Location) yaml.get(LAST_LOCATION_FIELD, null);
      if (lastLocation == null) {
        yaml.set(LAST_LOCATION_FIELD, this.backupLocation);
        lastLocation = this.backupLocation;
      }

      if (lastLocation.equals(this.backupLocation)) {
        yaml.set(LAST_LOCATION_FIELD, alternativeLastLocation);
      }

      if (teleport) {
        Teleport.teleportAsync(online, jail.location().mutable());
      }

      this.prisonersMap.put(prisonerUuid, yaml);
    }

    final PermissionInterface permissionInterface = this.plugin.permissionInterface();
    if (yaml.getString(GROUP_FIELD) == null || yaml.getBoolean(IS_RELEASED_FIELD, true)) {
      permissionInterface.fetchPrimaryGroup(player).thenCombineAsync(permissionInterface.fetchParentGroups(player), (primaryGroup, parentGroups) -> {
        yaml.set(GROUP_FIELD, primaryGroup);
        yaml.set(EXTRA_GROUPS_FIELD, ImmutableList.copyOf(parentGroups));

        return permissionInterface.setPrisonerGroup(player, jailer, jailerName);
      }, this.plugin).thenCompose(stage -> stage).exceptionally(exception -> {
        if (permissionInterface != PermissionInterface.NULL) {
          this.plugin.getLogger().log(Level.SEVERE, null, exception);
        }

        return null;
      }).thenComposeAsync(v -> {
        final CompletableFuture<Void> saveFuture = FileIO.writeString(this.playerDataFolder.resolve(prisonerUuid + ".yml"), yaml.saveToString());
        return saveFuture.exceptionally(ex -> {
          this.plugin.getLogger().log(Level.SEVERE, null, ex);
          return null;
        });
      }, this.plugin);
    }

    if (!isPlayerJailed) {
      final String name = player.getName();
      if (name != null) {
        this.prisonerNames.add(name.toLowerCase(Locale.ROOT));
      }

      this.prisonerIds.add(prisonerUuid);
    }

    if (isPlayerOnline) {
      final long jailedUntil = System.currentTimeMillis() + secondsLeft * 1000L;
      this.playersJailedUntil.put(prisonerUuid, jailedUntil);
    }

    if (this.plugin.essentials != null) {
      final User user = this.plugin.essentials.getUser(prisonerUuid);
      if (user != null) {
        user.setJailed(true);
        if (isPlayerOnline) {
          user.setJailTimeout(this.playersJailedUntil.get(prisonerUuid));
        }
      }
    }

    final Prisoner prisoner = this.plugin.api().getPrisonerManager().getPrisoner(prisonerUuid);
    this.plugin.eventBus().post(PlayerImprisonEvent.class, prisoner);
    return true;
  }

  public boolean releaseJailedPlayer(final OfflinePlayer player, final UUID source, final @Nullable String sourceName, final boolean teleport) {
    final UUID prisonerUuid = player.getUniqueId();
    if (!isPlayerJailed(prisonerUuid)) {
      return false;
    }

    final YamlConfiguration yaml = retrieveJailedPlayer(prisonerUuid);
    final Path playerFile = this.playerDataFolder.resolve(prisonerUuid + ".yml");

    final Prisoner prisoner = this.plugin.api().getPrisonerManager().getPrisoner(prisonerUuid);

    final PermissionInterface permissionInterface = this.plugin.permissionInterface();
    permissionInterface.setParentGroups(player, yaml.getStringList(EXTRA_GROUPS_FIELD), source, sourceName)
        .whenComplete((ignored, exception) -> {
          if (exception != null && permissionInterface != PermissionInterface.NULL) {
            this.plugin.getLogger().log(Level.SEVERE, null, exception);
          }
        });

    if (player.isOnline() || player instanceof Player) {
      final Player online = player.isOnline() ? player.getPlayer() : (Player) player;
      assert online != null;
      if (teleport) {
        final Location lastLocation = (Location) yaml.get(LAST_LOCATION_FIELD, this.backupLocation);
        if (!lastLocation.equals(this.backupLocation)) {
          Teleport.teleportAsync(online, lastLocation);
        }
      }

      this.prisonersMap.remove(prisonerUuid);
      try {
        Files.deleteIfExists(playerFile);
      } catch (final IOException ex) {
        this.plugin.getLogger().log(Level.WARNING, "Could not delete prisoner file " + playerFile, ex);
      }

      this.prisonerNames.remove(online.getName().toLowerCase(Locale.ROOT));
      this.prisonerIds.remove(prisonerUuid);
      this.playersJailedUntil.remove(prisonerUuid);

      final SubCommandsConfiguration.SubCommands subCommands = this.subCommands.onRelease();
      subCommands.executeAsPrisoner(this.server, online, yaml.getString(JAILED_BY_FIELD, ""));
      subCommands.executeAsConsole(this.server, online, yaml.getString(JAILED_BY_FIELD, ""));
    } else {
      if (yaml.getBoolean(IS_RELEASED_FIELD, false)) {
        return true;
      }

      if (yaml.get(LAST_LOCATION_FIELD, this.backupLocation).equals(this.backupLocation)) {
        this.prisonersMap.remove(prisonerUuid);
        try {
          Files.deleteIfExists(playerFile);
        } catch (final IOException ex) {
          this.plugin.getLogger().log(Level.WARNING, "Could not delete prisoner file " + playerFile, ex);
        }

        final String name = player.getName();
        if (name != null) {
          this.prisonerNames.remove(name.toLowerCase(Locale.ROOT));
        }

        this.prisonerIds.remove(prisonerUuid);
        this.playersJailedUntil.remove(prisonerUuid);
      } else {
        yaml.set(IS_RELEASED_FIELD, true);
        FileIO.writeString(playerFile, yaml.saveToString()).exceptionally(ex -> {
          this.plugin.getLogger().log(Level.SEVERE, null, ex);
          return null;
        });
      }
    }

    if (this.plugin.essentials != null) {
      final User user = this.plugin.essentials.getUser(prisonerUuid);
      if (user != null && user.isJailed()) {
        user.setJailTimeout(0L);
        user.setJailed(false);
      }
    }

    this.plugin.eventBus().post(PrisonerReleaseEvent.class, prisoner);
    return true;
  }

  public long getSecondsLeft(final UUID uuid, final int fallback) {
    if (this.playersJailedUntil.containsKey(uuid)) {
      return (this.playersJailedUntil.get(uuid) - System.currentTimeMillis()) / 1000L;
    } else {
      return retrieveJailedPlayer(uuid).getLong(SECONDS_LEFT_FIELD, fallback);
    }
  }

  public boolean isReleased(final UUID uuid, final boolean def) {
    return retrieveJailedPlayer(uuid).getBoolean(IS_RELEASED_FIELD, def);
  }

  public String getName(final UUID uuid, final @Nullable String def) {
    return retrieveJailedPlayer(uuid).getString(NAME_FIELD, def);
  }

  public String getJail(final UUID uuid, final @Nullable String def) {
    return retrieveJailedPlayer(uuid).getString(JAIL_FIELD, def);
  }

  public String getJailer(final UUID uuid, final @Nullable String def) {
    return retrieveJailedPlayer(uuid).getString(JAILED_BY_FIELD, def);
  }

  public Location getLastLocation(final UUID uuid) {
    return (Location) retrieveJailedPlayer(uuid).get(LAST_LOCATION_FIELD, this.backupLocation);
  }

  public String getPrimaryGroup(final UUID uuid, final @Nullable String def) {
    return retrieveJailedPlayer(uuid).getString(GROUP_FIELD, def);
  }

  public List<String> getAllParentGroups(final UUID uuid) {
    return retrieveJailedPlayer(uuid).getStringList(EXTRA_GROUPS_FIELD);
  }

  public void updateSecondsLeft(final UUID uuid) {
    final OfflinePlayer player = this.server.getOfflinePlayer(uuid);
    if (this.config.considerOfflineTime() && !getLastLocation(uuid).equals(this.backupLocation) || player.isOnline()) {
      retrieveJailedPlayer(uuid).set(SECONDS_LEFT_FIELD, getSecondsLeft(uuid, 0));
    }
  }

  public CompletableFuture<Void> save() {
    // A Jail's location can be changed...
    this.jails.forEach((name, jail) -> this.jailsYaml.set(name.toLowerCase(Locale.ROOT), jail.location().mutable()));
    CompletableFuture<Void> cf = FileIO.writeString(this.jailsFile, this.jailsYaml.saveToString());

    for (final Map.Entry<UUID, YamlConfiguration> entry : this.prisonersMap.entrySet()) {
      final UUID key = entry.getKey();
      final YamlConfiguration value = entry.getValue();
      value.set(SECONDS_LEFT_FIELD, getSecondsLeft(key, 0));
      final Path playerFile = this.playerDataFolder.resolve(key + ".yml");
      final String str = value.saveToString();
      cf = cf.thenCompose(v -> FileIO.writeString(playerFile, str).whenComplete((v1, ex) -> {
        if (ex != null) {
          this.plugin.getLogger().log(Level.SEVERE, null, ex);
        }
      }));
    }

    this.plugin.eventBus().post(PluginSaveDataEvent.class);
    return cf;
  }

  public void reload() throws IOException {
    this.backupLocation = this.config.backupLocation().mutable();
    this.jailsYaml = YamlConfiguration.loadConfiguration(this.jailsFile.toFile());
    for (final String key : this.jailsYaml.getKeys(false)) {
      final String lowerCaseKey = key.toLowerCase(Locale.ROOT);
      this.jails.put(lowerCaseKey, new ApiJail(lowerCaseKey, (Location) this.jailsYaml.get(key)));
    }

    this.prisonersMap.clear();
    this.playersJailedUntil.clear();
    final long now = System.currentTimeMillis();
    if (this.config.considerOfflineTime()) {
      try (final Stream<Path> s = Files.list(this.playerDataFolder)) {
        s.forEach(file -> {
          final UUID uuid = UUID.fromString(file.getFileName().toString().replace(".yml", ""));
          final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
          migratePrisonerData(yaml, file);
          this.prisonersMap.put(uuid, yaml);
          this.playersJailedUntil.put(uuid, now + yaml.getLong(SECONDS_LEFT_FIELD) * 1000L);
        });
      }
    } else {
      for (final Player player : this.server.getOnlinePlayers()) {
        if (isPlayerJailed(player.getUniqueId())) {
          final YamlConfiguration yaml = retrieveJailedPlayer(player.getUniqueId());
          this.prisonersMap.put(player.getUniqueId(), yaml);
          this.playersJailedUntil.put(player.getUniqueId(), now + yaml.getLong(SECONDS_LEFT_FIELD) * 1000L);
        }
      }
    }

    if (this.config.permissionHookEnabled()) {
      this.config.prisonerPermissionGroup().ifPresent(prisonerGroup ->
          this.plugin.resetPermissionInterface(
              PermissionInterface.determinePermissionInterface(this.plugin, prisonerGroup)
          )
      );
    } else {
      this.plugin.resetPermissionInterface(PermissionInterface.NULL);
    }
  }

  public void timer() {
    final Iterator<Map.Entry<UUID, YamlConfiguration>> iterator = this.prisonersMap.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<UUID, YamlConfiguration> entry = iterator.next();
      final UUID key = entry.getKey();
      final YamlConfiguration value = entry.getValue();
      final Location location = (Location) value.get(LAST_LOCATION_FIELD, this.backupLocation);
      final boolean released = value.getBoolean(IS_RELEASED_FIELD, false);

      if (location.equals(this.backupLocation)) {
        if (released) {
          iterator.remove();

          final Path playerFile = this.playerDataFolder.resolve(key + ".yml");
          try {
            Files.deleteIfExists(playerFile);
          } catch (final IOException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Could not delete prisoner file " + playerFile, ex);
          }

          final String name = value.getString(NAME_FIELD);
          if (name != null) {
            this.prisonerNames.remove(name.toLowerCase(Locale.ROOT));
          }

          this.prisonerIds.remove(key);
          this.playersJailedUntil.remove(key);
        }
        continue;
      }

      if (released || getSecondsLeft(key, 0) <= 0) {
        releaseJailedPlayer(this.server.getOfflinePlayer(key), Util.NIL_UUID, "timer", true);
      }
    }
  }

  // TODO keep this or perform some kind of automatic migration?
  private void alertNewConfigAvailable() throws IOException, InvalidConfigurationException {
    final YamlConfiguration bundledConfig = new YamlConfiguration();
    try (
        final InputStream in = this.plugin.getResource("config.yml");
        final Reader reader = new InputStreamReader(Objects.requireNonNull(in, "bundled config not present"), StandardCharsets.UTF_8)
    ) {
      bundledConfig.load(reader);
    }

    final FileConfiguration existingConfig = this.plugin.getConfig();
    if (!bundledConfig.getKeys(true).equals(existingConfig.getKeys(true))) {
      this.plugin.getLogger().warning("New config.yml found!");
      this.plugin.getLogger().warning("Make sure to make a backup of your settings before deleting your current config.yml!");
    }
  }

  private void migratePrisonerData(final YamlConfiguration config, final Path file) {
    boolean changed = false;
    final int version = config.getInt("version", 1);
    if (version > DataUpgrader.VERSION) {
      this.plugin.getLogger().warning("Prisoner file " + file + " is from a newer version of BetterJails");
      this.plugin.getLogger().warning("The plugin will continue to load it, but it may not function properly, errors might show up and data could be lost");
      this.plugin.getLogger().warning("!!! Consider updating BetterJails !!!");
      return;
    }

    for (final DataUpgrader upgrader : DATA_UPGRADERS.subList(version - 1, DATA_UPGRADERS.size())) {
      changed |= upgrader.upgrade(config, this.plugin);
    }

    if (changed) {
      FileIO.writeString(file, config.saveToString()).exceptionally(ex -> {
        this.plugin.getLogger().log(Level.WARNING, "Could not save player data file " + file, ex);
        return null;
      });
    }
  }
}
