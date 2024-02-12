//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2022 emilyy-dev
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
import com.google.common.collect.ImmutableMap;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.api.impl.model.jail.ApiJail;
import io.github.emilyydev.betterjails.config.BetterJailsConfiguration;
import io.github.emilyydev.betterjails.config.SubCommandsConfiguration;
import io.github.emilyydev.betterjails.interfaces.permission.PermissionInterface;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class DataHandler {

  public static final String IS_RELEASED_FIELD = "released";
  public static final String LAST_LOCATION_FIELD = "last-location";
  public static final String GROUP_FIELD = "group";
  public static final String EXTRA_GROUPS_FIELD = "extra-groups";
  public static final String UUID_FIELD = "uuid";
  public static final String NAME_FIELD = "name";
  public static final String JAIL_FIELD = "jail";
  public static final String JAILED_BY_FIELD = "jailed-by";
  public static final String SECONDS_LEFT_FIELD = "seconds-left";

  @Deprecated private static final String LEGACY_UNJAILED_FIELD = "unjailed";
  @Deprecated private static final String LEGACY_LASTLOCATION_FIELD = "lastlocation";
  @Deprecated private static final String LEGACY_JAILEDBY_FIELD = "jailedby";
  @Deprecated private static final String LEGACY_SECONDSLEFT_FIELD = "secondsleft";

  @Deprecated private static final List<Map<String, String>> DATA_FIELD_MIGRATION_MAPS =
      ImmutableList.of(
          ImmutableMap.<String, String>builder()
              .put(LEGACY_UNJAILED_FIELD, IS_RELEASED_FIELD)
              .put(LEGACY_LASTLOCATION_FIELD, LAST_LOCATION_FIELD)
              .put(LEGACY_JAILEDBY_FIELD, JAILED_BY_FIELD)
              .put(LEGACY_SECONDSLEFT_FIELD, SECONDS_LEFT_FIELD)
              .build()
      );

  public final File playerDataFolder;
  private final BetterJailsPlugin plugin;
  private final BetterJailsConfiguration config;
  private final SubCommandsConfiguration subCommands;
  private final Server server;
  private final Set<String> jailedPlayerNames = new HashSet<>();
  private final Set<UUID> jailedPlayerUuids = new HashSet<>();
  private final Map<String, Jail> jails = new HashMap<>();
  private final Map<UUID, YamlConfiguration> yamlsJailedPlayers = new HashMap<>();
  private final Map<UUID, Long> playersJailedUntil = new HashMap<>();
  private final File jailsFile;
  private Location backupLocation;
  private YamlConfiguration jailsYaml;

  public DataHandler(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
    this.config = plugin.configuration();
    this.subCommands = plugin.subCommands();
    this.server = plugin.getServer();

    final File dataFolder = plugin.getDataFolder();
    this.jailsFile = new File(dataFolder, "jails.yml");
    this.playerDataFolder = new File(dataFolder, "playerdata");
  }

  public void init() throws IOException, InvalidConfigurationException {
    this.backupLocation = this.config.backupLocation().mutable();

    this.playerDataFolder.mkdirs();
    loadJails();

    alertNewConfigAvailable();

    final long now = System.currentTimeMillis();
    for (final File file : this.playerDataFolder.listFiles()) {
      final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
      migratePrisonerData(yaml, file);
      final UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
      if (this.config.considerOfflineTime()) {
        this.yamlsJailedPlayers.put(uuid, yaml);
        if (yaml.get(LAST_LOCATION_FIELD, this.backupLocation) != this.backupLocation) {
          this.playersJailedUntil.put(uuid, now + yaml.getLong(SECONDS_LEFT_FIELD, 0L) * 1000L);
        }
      }

      final String name = yaml.getString(NAME_FIELD);
      if (name != null) {
        this.jailedPlayerNames.add(name.toLowerCase(Locale.ROOT));
      }

      this.jailedPlayerUuids.add(uuid);
    }
  }

  public Map<UUID, YamlConfiguration> getAllJailedPlayers() {
    return this.yamlsJailedPlayers;
  }

  private void loadJails() throws IOException {
    this.jailsFile.createNewFile();
    this.jailsYaml = YamlConfiguration.loadConfiguration(this.jailsFile);

    for (final String key : this.jailsYaml.getKeys(false)) {
      final String lowerCaseKey = key.toLowerCase(Locale.ROOT);
      this.jails.put(lowerCaseKey, new ApiJail(lowerCaseKey, (Location) this.jailsYaml.get(key)));
    }
  }

  public boolean isPlayerJailed(final UUID uuid) {
    return this.jailedPlayerUuids.contains(uuid);
  }

  public boolean isPlayerJailed(final String playerName) {
    return this.jailedPlayerNames.contains(playerName.toLowerCase(Locale.ROOT));
  }

  public YamlConfiguration retrieveJailedPlayer(final UUID uuid) {
    if (!isPlayerJailed(uuid)) {
      return new YamlConfiguration();
    }

    if (this.yamlsJailedPlayers.containsKey(uuid)) {
      return this.yamlsJailedPlayers.get(uuid);
    } else {
      final File playerFile = new File(this.playerDataFolder, uuid + ".yml");
      final YamlConfiguration config = new YamlConfiguration();
      try {
        config.load(playerFile);
        migratePrisonerData(config, playerFile);
      } catch (final IOException exception) {
        if (!(exception instanceof FileNotFoundException)) {
          this.plugin.getLogger().severe("Couldn't read file " + playerFile.getAbsolutePath());
          exception.printStackTrace();
        }
      } catch (final InvalidConfigurationException exception) {
        this.plugin.getLogger().severe("Invalid YAML configuration in file " + playerFile.getAbsolutePath());
        exception.printStackTrace();
      }

      return config;
    }
  }

  public void loadJailedPlayer(final UUID uuid, final YamlConfiguration jailedPlayer) {
    this.yamlsJailedPlayers.put(uuid, jailedPlayer);
  }

  public void unloadJailedPlayer(final UUID uuid) {
    this.yamlsJailedPlayers.remove(uuid);
    this.playersJailedUntil.remove(uuid);
  }

  public Map<String, Jail> getJails() {
    return this.jails;
  }

  public @Nullable Jail getJail(final String name) {
    return this.jails.get(name.toLowerCase(Locale.ROOT));
  }

  public void addJail(final String name, final Location location) throws IOException {
    final String lowerCaseName = name.toLowerCase(Locale.ROOT);
    this.jails.computeIfAbsent(lowerCaseName, key -> new ApiJail(key, location))
        .location(ImmutableLocation.copyOf(location));
    this.jailsYaml.set(lowerCaseName, location);
    this.jailsYaml.save(this.jailsFile);

    this.plugin.eventBus().post(JailCreateEvent.class, name, ImmutableLocation.copyOf(location));
  }

  public void removeJail(final String name) throws IOException {
    final String lowerCaseName = name.toLowerCase(Locale.ROOT);
    final Jail jail = this.jails.remove(lowerCaseName);
    this.jailsYaml.set(name, null); // just in case...
    this.jailsYaml.set(lowerCaseName, null);
    this.jailsYaml.save(this.jailsFile);

    this.plugin.eventBus().post(JailDeleteEvent.class, jail);
  }

  public boolean addJailedPlayer(
      final OfflinePlayer player,
      final String jailName,
      final UUID jailer,
      final @Nullable String jailerName,
      final long secondsLeft
  ) throws IOException {
    final YamlConfiguration yaml = retrieveJailedPlayer(player.getUniqueId());
    final Jail jail = getJail(jailName);
    final boolean jailExists = jail != null;
    final boolean isPlayerOnline = player.isOnline();
    final Player online = player.getPlayer();
    final boolean isPlayerJailed = isPlayerJailed(player.getUniqueId());

    if (!jailExists) {
      return false;
    }

    yaml.set(UUID_FIELD, player.getUniqueId().toString());
    yaml.set(NAME_FIELD, player.getName());
    yaml.set(JAIL_FIELD, jailName.toLowerCase(Locale.ROOT));
    if (jailerName != null) {
      yaml.set(JAILED_BY_FIELD, jailerName);
    }
    yaml.set(SECONDS_LEFT_FIELD, secondsLeft);
    yaml.set(IS_RELEASED_FIELD, false);

    if (isPlayerOnline && !isPlayerJailed) {
      yaml.set(LAST_LOCATION_FIELD, online.getLocation());

      online.teleport(jail.location().mutable());
      this.yamlsJailedPlayers.put(player.getUniqueId(), yaml);

      final SubCommandsConfiguration.SubCommands subCommands = this.subCommands.onJail();
      subCommands.executeAsPrisoner(this.server, online, yaml.getString(JAILED_BY_FIELD, ""));
      subCommands.executeAsConsole(this.server, online, yaml.getString(JAILED_BY_FIELD, ""));
    } else if (!isPlayerOnline && !isPlayerJailed) {
      yaml.set(LAST_LOCATION_FIELD, this.backupLocation);

      if (this.config.considerOfflineTime()) {
        this.yamlsJailedPlayers.put(player.getUniqueId(), yaml);
      }

    } else if (isPlayerOnline) {
      Location lastLocation = (Location) yaml.get(LAST_LOCATION_FIELD, null);
      if (lastLocation == null) {
        yaml.set(LAST_LOCATION_FIELD, this.backupLocation);
        lastLocation = this.backupLocation;
      }

      if (lastLocation.equals(this.backupLocation)) {
        yaml.set(LAST_LOCATION_FIELD, online.getLocation());
      }
      online.teleport(jail.location().mutable());
      this.yamlsJailedPlayers.put(player.getUniqueId(), yaml);
    }

    final PermissionInterface permissionInterface = this.plugin.permissionInterface();
    if (yaml.getString(GROUP_FIELD) == null || yaml.getBoolean(IS_RELEASED_FIELD, true)) {
      permissionInterface.fetchPrimaryGroup(player).thenCombine(permissionInterface.fetchParentGroups(player), (primaryGroup, parentGroups) -> {
        yaml.set(GROUP_FIELD, primaryGroup);
        yaml.set(EXTRA_GROUPS_FIELD, ImmutableList.copyOf(parentGroups));

        return permissionInterface.setPrisonerGroup(player, jailer, jailerName);
      }).thenCompose(stage -> stage).whenComplete(($, exception) -> {
        if (exception != null && permissionInterface != PermissionInterface.NULL) {
          exception.printStackTrace();
        }

        try {
          yaml.save(new File(this.playerDataFolder, player.getUniqueId() + ".yml"));
        } catch (final IOException ex) {
          ex.printStackTrace();
        }
      });
    }

    if (!isPlayerJailed) {
      final String name = player.getName();
      if (name != null) {
        this.jailedPlayerNames.add(name.toLowerCase(Locale.ROOT));
      }

      this.jailedPlayerUuids.add(player.getUniqueId());
    }

    if (player.isOnline()) {
      final long jailedUntil = System.currentTimeMillis() + secondsLeft * 1000L;
      this.playersJailedUntil.put(player.getUniqueId(), jailedUntil);
    }

    if (this.plugin.essentials != null) {
      final User user = this.plugin.essentials.getUser(player.getUniqueId());
      if (user != null) {
        user.setJailed(true);
        if (player.isOnline()) {
          user.setJailTimeout(this.playersJailedUntil.get(player.getUniqueId()));
        }
      }
    }

    final Prisoner prisoner = this.plugin.api().getPrisonerManager().getPrisoner(player.getUniqueId());
    this.plugin.eventBus().post(PlayerImprisonEvent.class, prisoner);
    return true;
  }

  public boolean releaseJailedPlayer(final UUID prisonerUuid, final UUID source, final @Nullable String sourceName) {
    if (!isPlayerJailed(prisonerUuid)) {
      return false;
    }

    final YamlConfiguration yaml = retrieveJailedPlayer(prisonerUuid);
    final File playerFile = new File(this.playerDataFolder, prisonerUuid + ".yml");

    final OfflinePlayer player = this.server.getOfflinePlayer(prisonerUuid);
    final Prisoner prisoner = this.plugin.api().getPrisonerManager().getPrisoner(player.getUniqueId());

    final PermissionInterface permissionInterface = this.plugin.permissionInterface();
    permissionInterface.setParentGroups(player, yaml.getStringList(EXTRA_GROUPS_FIELD), source, sourceName)
        .whenComplete(($, exception) -> {
          if (exception != null && permissionInterface != PermissionInterface.NULL) {
            exception.printStackTrace();
          }
        });

    if (player.isOnline()) {
      final Player online = player.getPlayer();
      Location lastLocation = (Location) yaml.get(LAST_LOCATION_FIELD, this.backupLocation);

      if (lastLocation.equals(this.backupLocation)) {
        lastLocation = online.getLocation();
      }

      online.teleport(lastLocation);
      this.yamlsJailedPlayers.remove(prisonerUuid);
      playerFile.delete();
      this.jailedPlayerNames.remove(online.getName().toLowerCase(Locale.ROOT));
      this.jailedPlayerUuids.remove(prisonerUuid);
      this.playersJailedUntil.remove(prisonerUuid);

      final SubCommandsConfiguration.SubCommands subCommands = this.subCommands.onRelease();
      subCommands.executeAsPrisoner(this.server, online, yaml.getString(JAILED_BY_FIELD, ""));
      subCommands.executeAsConsole(this.server, online, yaml.getString(JAILED_BY_FIELD, ""));
    } else {
      if (yaml.getBoolean(IS_RELEASED_FIELD, false)) {
        return true;
      }

      if (yaml.get(LAST_LOCATION_FIELD, this.backupLocation).equals(this.backupLocation)) {
        this.yamlsJailedPlayers.remove(prisonerUuid);
        playerFile.delete();
        final String name = player.getName();
        if (name != null) {
          this.jailedPlayerNames.remove(name.toLowerCase(Locale.ROOT));
        }

        this.jailedPlayerUuids.remove(prisonerUuid);
        this.playersJailedUntil.remove(prisonerUuid);
      } else {
        yaml.set(IS_RELEASED_FIELD, true);
        try {
          yaml.save(playerFile);
        } catch (final IOException exception) {
          exception.printStackTrace();
        }
      }
    }

    if (this.plugin.essentials != null) {
      final User user = this.plugin.essentials.getUser(player.getUniqueId());
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
    if (
        this.config.considerOfflineTime() &&
        !getLastLocation(uuid).equals(this.backupLocation) || player.isOnline()
    ) {
      retrieveJailedPlayer(uuid).set(SECONDS_LEFT_FIELD, getSecondsLeft(uuid, 0));
    }
  }

  public void save() throws IOException {
    // A Jail's location can be changed...
    this.jails.forEach((name, jail) -> this.jailsYaml.set(name.toLowerCase(Locale.ROOT), jail.location().mutable()));
    this.jailsYaml.save(this.jailsFile);

    for (final Map.Entry<UUID, YamlConfiguration> entry : this.yamlsJailedPlayers.entrySet()) {
      final UUID key = entry.getKey();
      final YamlConfiguration value = entry.getValue();
      try {
        value.set(SECONDS_LEFT_FIELD, getSecondsLeft(key, 0));
        value.save(new File(this.playerDataFolder, key + ".yml"));
      } catch (final IOException exception) {
        exception.printStackTrace();
      }
    }

    this.plugin.eventBus().post(PluginSaveDataEvent.class);
  }

  public void reload() throws IOException {
    this.backupLocation = this.config.backupLocation().mutable();
    this.jailsYaml = YamlConfiguration.loadConfiguration(this.jailsFile);
    for (final String key : this.jailsYaml.getKeys(false)) {
      final String lowerCaseKey = key.toLowerCase(Locale.ROOT);
      this.jails.put(lowerCaseKey, new ApiJail(lowerCaseKey, (Location) this.jailsYaml.get(key)));
    }

    this.yamlsJailedPlayers.clear();
    this.playersJailedUntil.clear();
    final long now = System.currentTimeMillis();
    if (this.config.considerOfflineTime()) {
      for (final File file : this.playerDataFolder.listFiles()) {
        final UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        migratePrisonerData(yaml, file);
        this.yamlsJailedPlayers.put(uuid, yaml);
        this.playersJailedUntil.put(uuid, now + yaml.getLong(SECONDS_LEFT_FIELD) * 1000L);
      }
    } else {
      for (final Player player : this.server.getOnlinePlayers()) {
        if (isPlayerJailed(player.getUniqueId())) {
          final YamlConfiguration yaml = retrieveJailedPlayer(player.getUniqueId());
          this.yamlsJailedPlayers.put(player.getUniqueId(), yaml);
          this.playersJailedUntil.put(player.getUniqueId(), now + yaml.getLong(SECONDS_LEFT_FIELD) * 1000L);
        }
      }
    }

    if (this.config.permissionHookEnabled()) {
      this.config.prisonerPermissionGroup().ifPresent(prisonerGroup ->
          this.plugin.resetPermissionInterface(
              PermissionInterface.determinePermissionInterface(this.server, prisonerGroup)
          )
      );
    } else {
      this.plugin.resetPermissionInterface(PermissionInterface.NULL);
    }
  }

  public void timer() {
    final Iterator<Map.Entry<UUID, YamlConfiguration>> iterator = this.yamlsJailedPlayers.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<UUID, YamlConfiguration> entry = iterator.next();
      final UUID key = entry.getKey();
      final YamlConfiguration value = entry.getValue();
      final Location location = (Location) value.get(LAST_LOCATION_FIELD, this.backupLocation);
      final boolean released = value.getBoolean(IS_RELEASED_FIELD, false);

      if (location.equals(this.backupLocation)) {
        if (released) {
          iterator.remove();
          new File(this.playerDataFolder, key + ".yml").delete();
          final String name = value.getString(NAME_FIELD);
          if (name != null) {
            this.jailedPlayerNames.remove(name.toLowerCase(Locale.ROOT));
          }

          this.jailedPlayerUuids.remove(key);
          this.playersJailedUntil.remove(key);
        }
        continue;
      }

      if (released || getSecondsLeft(key, 0) <= 0) {
        releaseJailedPlayer(key, Util.NIL_UUID, "timer");
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

  private void migratePrisonerData(final YamlConfiguration config, final File file) throws IOException {
    boolean wasChanged = false;
    for (final Map<String, String> dataFieldMigrationMap : DATA_FIELD_MIGRATION_MAPS) {
      for (final Map.Entry<String, String> dataFieldMigrationEntry : dataFieldMigrationMap.entrySet()) {
        final String oldField = dataFieldMigrationEntry.getKey();
        final String newField = dataFieldMigrationEntry.getValue();
        if (config.contains(oldField)) {
          if (!config.contains(newField)) {
            config.set(newField, config.get(oldField));
          }

          config.set(oldField, null);
          wasChanged = true;
        }
      }
    }

    if (wasChanged) {
      config.save(file);
    }
  }
}
