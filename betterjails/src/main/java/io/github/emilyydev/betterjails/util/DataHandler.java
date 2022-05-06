//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2021 emilyy-dev
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
import com.google.common.base.Strings;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.api.impl.model.jail.ApiJail;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

public class DataHandler {

  public static final String FIELD_UNJAILED = "unjailed";
  public static final String FIELD_LASTLOCATION = "lastlocation";
  public static final String FIELD_GROUP = "group";
  public static final String FIELD_UUID = "uuid";
  public static final String FIELD_NAME = "name";
  public static final String FIELD_JAIL = "jail";
  public static final String FIELD_JAILEDBY = "jailedby";
  public static final String FIELD_SECONDSLEFT = "secondsleft";

  public final File playerDataFolder;
  private final BetterJailsPlugin plugin;
  private final Server server;
  private final Set<String> jailedPlayerNames = new HashSet<>();
  private final Set<UUID> jailedPlayerUuids = new HashSet<>();
  private final Map<String, Jail> jails = new HashMap<>();
  private final Map<UUID, YamlConfiguration> yamlsJailedPlayers = new HashMap<>();
  private final Map<UUID, Long> playersJailedUntil = new HashMap<>();
  private final File jailsFile;
  private Location backupLocation;
  private final File subcommandsFile;
  private YamlConfiguration jailsYaml;
  private YamlConfiguration subcommandsYaml;

  public DataHandler(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
    this.server = plugin.getServer();
    this.jailsFile = new File(plugin.getDataFolder(), "jails.yml");
    this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
    this.subcommandsFile = new File(plugin.getDataFolder(), "subcommands.yml");
  }

  public void init() throws IOException {
    String world = plugin.getConfig().getString("backupLocation.world");
    if (world == null) {
      world = this.server.getWorlds().stream()
          .findAny()
          .map(World::getName)
          .orElseThrow(() -> new NoSuchElementException("No valid world could be found"));
      plugin.getLogger().warning("Error in config.yml: Couldn't retrieve backupLocation.world");
      plugin.getLogger().warning("Choosing world \"" + world + "\" by default.");
    }

    this.backupLocation = new Location(this.server.getWorld(world),
        plugin.getConfig().getDouble("backupLocation.x"),
        plugin.getConfig().getDouble("backupLocation.y"),
        plugin.getConfig().getDouble("backupLocation.z"),
        (float) plugin.getConfig().getDouble("backupLocation.yaw"),
        (float) plugin.getConfig().getDouble("backupLocation.pitch"));

    plugin.getDataFolder().mkdir();
    loadJails();

    this.playerDataFolder.mkdir();

    if (!this.subcommandsFile.exists()) {
      plugin.saveResource("subcommands.yml", false);
    }

    this.subcommandsYaml = YamlConfiguration.loadConfiguration(this.subcommandsFile);

    alertNewConfigAvailable();

    final File[] files = this.playerDataFolder.listFiles();
    if (files != null) {
      final long now = System.currentTimeMillis();
      for (final File file : files) {
        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        final UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
        if (plugin.getConfig().getBoolean("offlineTime")) {
          this.yamlsJailedPlayers.put(uuid, yaml);
          if (yaml.get(FIELD_LASTLOCATION, this.backupLocation) != this.backupLocation) {
            this.playersJailedUntil.put(uuid, now + yaml.getLong(FIELD_SECONDSLEFT, 0L) * 1000L);
          }
        }

        final String name = yaml.getString(FIELD_NAME);
        if (name != null) {
          this.jailedPlayerNames.add(name.toLowerCase(Locale.ROOT));
        }

        this.jailedPlayerUuids.add(uuid);
      }
    }
  }

  public Map<UUID, YamlConfiguration> getAllJailedPlayers() {
    return this.yamlsJailedPlayers;
  }

  private void loadJails() throws IOException {
    this.jailsFile.createNewFile();
    this.jailsYaml = YamlConfiguration.loadConfiguration(this.jailsFile);

    for (final String key : this.jailsYaml.getKeys(false)) {
      this.jails.put(key.toLowerCase(Locale.ROOT), new ApiJail(key.toLowerCase(Locale.ROOT), (Location) this.jailsYaml.get(key)));
    }
  }

  public boolean isPlayerJailed(final UUID uuid) {
    return this.jailedPlayerUuids.contains(uuid);
  }

  public boolean isPlayerJailed(final String playerName) {
    return this.jailedPlayerNames.contains(playerName.toLowerCase(Locale.ROOT));
  }

  public @NotNull YamlConfiguration retrieveJailedPlayer(final UUID uuid) {
    if (!isPlayerJailed(uuid)) {
      return new YamlConfiguration();
    }

    if (this.yamlsJailedPlayers.containsKey(uuid)) {
      return this.yamlsJailedPlayers.get(uuid);
    } else {
      final File playerFile = new File(this.playerDataFolder, uuid + ".yml");
      final YamlConfiguration retYaml = new YamlConfiguration();
      try {
        retYaml.load(playerFile);
      } catch (final IOException exception) {
        if (!(exception instanceof FileNotFoundException)) {
          this.plugin.getLogger().severe("Couldn't read file " + playerFile.getAbsolutePath());
          exception.printStackTrace();
        }
      } catch (final InvalidConfigurationException exception) {
        this.plugin.getLogger().severe("Invalid YAML configuration in file " + playerFile.getAbsolutePath());
        exception.printStackTrace();
      }

      return retYaml;
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
    this.jails.computeIfAbsent(name.toLowerCase(Locale.ROOT), key -> new ApiJail(key, location))
        .location(ImmutableLocation.copyOf(location));
    this.jailsYaml.set(name.toLowerCase(Locale.ROOT), location);
    this.jailsYaml.save(this.jailsFile);

    this.plugin.getEventBus().post(JailCreateEvent.class, name, ImmutableLocation.copyOf(location));
  }

  public void removeJail(final String name) throws IOException {
    final Jail jail = this.jails.remove(name.toLowerCase(Locale.ROOT));
    this.jailsYaml.set(name, null); // jic...
    this.jailsYaml.set(name.toLowerCase(Locale.ROOT), null);
    this.jailsYaml.save(this.jailsFile);

    this.plugin.getEventBus().post(JailDeleteEvent.class, jail);
  }

  public boolean addJailedPlayer(final @NotNull OfflinePlayer player, final @NotNull String jailName,
      final @Nullable String jailer, final long secondsLeft)
      throws IOException {
    final YamlConfiguration yaml = retrieveJailedPlayer(player.getUniqueId());
    final Jail jail = getJail(jailName);
    final boolean jailExists = jail != null;
    final boolean isPlayerOnline = player.isOnline();
    final Player online = player.getPlayer();
    final boolean isPlayerJailed = isPlayerJailed(player.getUniqueId());

    if (!jailExists) {
      return false;
    }

    yaml.set(FIELD_UUID, player.getUniqueId().toString());
    yaml.set(FIELD_NAME, player.getName());
    yaml.set(FIELD_JAIL, jailName.toLowerCase(Locale.ROOT));
    if (jailer != null) {
      yaml.set(FIELD_JAILEDBY, jailer);
    }
    yaml.set(FIELD_SECONDSLEFT, secondsLeft);
    yaml.set(FIELD_UNJAILED, false);

    if (isPlayerOnline && !isPlayerJailed) {
      yaml.set(FIELD_LASTLOCATION, online.getLocation());

      online.teleport(jail.location().mutable());
      this.yamlsJailedPlayers.put(player.getUniqueId(), yaml);

      final List<String> asPrisoner = this.subcommandsYaml.getStringList("on-jail.as-prisoner");
      final List<String> asConsole = this.subcommandsYaml.getStringList("on-jail.as-console");
      for (final String cmd : asPrisoner) {
        if (!cmd.equals("")) {
          online.performCommand(cmd.replace("{player}", yaml.getString(FIELD_JAILEDBY, ""))
              .replace("{prisoner}", player.getName()));
        }
      }
      for (final String cmd : asConsole) {
        if (!cmd.equals("")) {
          this.server.dispatchCommand(this.server.getConsoleSender(),
              cmd.replace("{player}", yaml.getString(FIELD_JAILEDBY, ""))
                  .replace("{prisoner}", player.getName()));
        }
      }
    } else if (!isPlayerOnline && !isPlayerJailed) {
      yaml.set(FIELD_LASTLOCATION, this.backupLocation);

      if (this.plugin.getConfig().getBoolean("offlineTime")) {
        this.yamlsJailedPlayers.put(player.getUniqueId(), yaml);
      }

    } else if (isPlayerOnline) {
      Location lastLocation = (Location) yaml.get(FIELD_LASTLOCATION, null);
      if (lastLocation == null) {
        yaml.set(FIELD_LASTLOCATION, this.backupLocation);
        lastLocation = this.backupLocation;
      }

      if (lastLocation.equals(this.backupLocation)) {
        yaml.set(FIELD_LASTLOCATION, online.getLocation());
      }
      online.teleport(jail.location().mutable());
      this.yamlsJailedPlayers.put(player.getUniqueId(), yaml);
    }

    if (this.plugin.permsInterface != null) {
      this.server.getScheduler().runTaskAsynchronously(this.plugin, () -> {
        if (yaml.getString(FIELD_GROUP, null) == null || yaml.getBoolean(FIELD_UNJAILED, true)) {
          final String group = this.plugin.permsInterface.getPrimaryGroup(null, player);
          yaml.set(FIELD_GROUP, group);
          try {
            yaml.save(new File(this.playerDataFolder, player.getUniqueId().toString() + ".yml"));
          } catch (final IOException exception) {
            exception.printStackTrace();
          }
          this.plugin.permsInterface.playerRemoveGroup(null, player, group);
          this.plugin.permsInterface.playerAddGroup(null, player, this.plugin.prisonerGroup);
        }
      });
    } else {
      yaml.save(new File(this.playerDataFolder, player.getUniqueId().toString() + ".yml"));
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

    final Prisoner prisoner = this.plugin.getApi().getPrisonerManager().getPrisoner(player.getUniqueId());
    this.plugin.getEventBus().post(PlayerImprisonEvent.class, prisoner);
    return true;
  }

  public boolean removeJailedPlayer(final @NotNull UUID uuid) {
    if (!isPlayerJailed(uuid)) {
      return false;
    }

    final YamlConfiguration yaml = retrieveJailedPlayer(uuid);
    final File playerFile = new File(this.playerDataFolder, uuid + ".yml");

    final OfflinePlayer player = this.server.getOfflinePlayer(uuid);
    final Prisoner prisoner = this.plugin.getApi().getPrisonerManager().getPrisoner(player.getUniqueId());

    if (this.plugin.permsInterface != null) {
      final String group = yaml.getString(FIELD_GROUP, null);
      this.server.getScheduler().runTaskAsynchronously(this.plugin, () -> {
        if (this.plugin.permsInterface.getPrimaryGroup(null, player).equalsIgnoreCase(group)) {
          return;
        }
        this.plugin.permsInterface.playerRemoveGroup(null, player, this.plugin.prisonerGroup);
        this.plugin.permsInterface.playerAddGroup(null, player, group != null ? group : "default");
      });
    }

    if (player.isOnline()) {
      final Player online = player.getPlayer();
      Location lastLocation = (Location) yaml.get(FIELD_LASTLOCATION, this.backupLocation);

      if (lastLocation.equals(this.backupLocation)) {
        lastLocation = online.getLocation();
      }

      online.teleport(lastLocation);
      this.yamlsJailedPlayers.remove(uuid);
      playerFile.delete();
      this.jailedPlayerNames.remove(player.getName().toLowerCase(Locale.ROOT));
      this.jailedPlayerUuids.remove(uuid);
      this.playersJailedUntil.remove(uuid);

      final List<String> commandsAsPrisoner = this.subcommandsYaml.getStringList("on-release.as-prisoner");
      final List<String> commandsAsConsole = this.subcommandsYaml.getStringList("on-release.as-console");
      for (final String command : commandsAsPrisoner) {
        if (!Strings.isNullOrEmpty(command)) {
          ((Player) player).performCommand(command.replace("{player}", yaml.getString(FIELD_JAILEDBY, ""))
              .replace("{prisoner}", player.getName()));
        }
      }
      for (final String command : commandsAsConsole) {
        if (!Strings.isNullOrEmpty(command)) {
          this.server.dispatchCommand(this.server.getConsoleSender(),
              command.replace("{player}", yaml.getString(FIELD_JAILEDBY, ""))
                  .replace("{prisoner}", player.getName()));
        }
      }
    } else {
      if (yaml.getBoolean(FIELD_UNJAILED, false)) {
        return true;
      }

      if (yaml.get(FIELD_LASTLOCATION, this.backupLocation).equals(this.backupLocation)) {
        this.yamlsJailedPlayers.remove(uuid);
        playerFile.delete();
        final String name = player.getName();
        if (name != null) {
          this.jailedPlayerNames.remove(name.toLowerCase(Locale.ROOT));
        }

        this.jailedPlayerUuids.remove(uuid);
        this.playersJailedUntil.remove(uuid);
      } else {
        yaml.set(FIELD_UNJAILED, true);
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

    this.plugin.getEventBus().post(PrisonerReleaseEvent.class, prisoner);
    return true;
  }

  public long getSecondsLeft(final @NotNull UUID uuid, final int fallback) {
    if (this.playersJailedUntil.containsKey(uuid)) {
      return (this.playersJailedUntil.get(uuid) - System.currentTimeMillis()) / 1000L;
    } else {
      return retrieveJailedPlayer(uuid).getLong(FIELD_SECONDSLEFT, fallback);
    }
  }

  public boolean getUnjailed(final @NotNull UUID uuid, final boolean def) {
    return retrieveJailedPlayer(uuid).getBoolean(FIELD_UNJAILED, def);
  }

  public String getName(final @NotNull UUID uuid, final @Nullable String def) {
    return retrieveJailedPlayer(uuid).getString(FIELD_NAME, def);
  }

  public String getJail(final @NotNull UUID uuid, final @Nullable String def) {
    return retrieveJailedPlayer(uuid).getString(FIELD_JAIL, def);
  }

  public String getJailer(final @NotNull UUID uuid, final @Nullable String def) {
    return retrieveJailedPlayer(uuid).getString(FIELD_JAILEDBY, def);
  }

  public Location getLastLocation(final @NotNull UUID uuid) {
    return (Location) retrieveJailedPlayer(uuid).get(FIELD_LASTLOCATION, this.backupLocation);
  }

  public String getGroup(final @NotNull UUID uuid, final @Nullable String def) {
    return retrieveJailedPlayer(uuid).getString(FIELD_GROUP, def);
  }

  public void updateSecondsLeft(final @NotNull UUID uuid) {
    final OfflinePlayer player = this.server.getOfflinePlayer(uuid);
    if (this.plugin.getConfig().getBoolean("offlineTime")
        && !getLastLocation(uuid).equals(this.backupLocation)
        || player.isOnline()) {
      retrieveJailedPlayer(uuid).set(FIELD_SECONDSLEFT, getSecondsLeft(uuid, 0));
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
        value.set(FIELD_SECONDSLEFT, getSecondsLeft(key, 0));
        value.save(new File(this.playerDataFolder, key + ".yml"));
      } catch (final IOException exception) {
        exception.printStackTrace();
      }
    }

    this.plugin.getEventBus().post(PluginSaveDataEvent.class);
  }

  public void reload() {
    this.plugin.reloadConfig();

    String unjailWorld = this.plugin.getConfig().getString("backupLocation.world");
    if (unjailWorld == null) {
      unjailWorld = this.server.getWorlds().get(0).getName();
      this.plugin.getLogger().warning("Error in config.yml: Couldn't retrieve backupLocation.world");
      this.plugin.getLogger().warning("Choosing world \"" + unjailWorld + "\" by default.");
    }
    this.backupLocation = new Location(this.server.getWorld(unjailWorld),
        this.plugin.getConfig().getDouble("backupLocation.x"),
        this.plugin.getConfig().getDouble("backupLocation.y"),
        this.plugin.getConfig().getDouble("backupLocation.z"),
        (float) this.plugin.getConfig().getDouble("backupLocation.yaw"),
        (float) this.plugin.getConfig().getDouble("backupLocation.pitch"));

    this.jailsYaml = YamlConfiguration.loadConfiguration(this.jailsFile);

    for (final String key : this.jailsYaml.getKeys(false)) {
      this.jails.put(key.toLowerCase(Locale.ROOT), new ApiJail(key.toLowerCase(Locale.ROOT), (Location) this.jailsYaml.get(key)));
    }

    this.subcommandsYaml = YamlConfiguration.loadConfiguration(this.subcommandsFile);

    this.yamlsJailedPlayers.clear();
    this.playersJailedUntil.clear();
    final long now = System.currentTimeMillis();
    if (this.plugin.getConfig().getBoolean("offlineTime")) {
      final File[] files = this.playerDataFolder.listFiles();
      if (files != null) {
        for (final File file : files) {
          final UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
          final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
          this.yamlsJailedPlayers.put(uuid, yaml);
          this.playersJailedUntil.put(uuid, now + yaml.getLong(FIELD_SECONDSLEFT) * 1000L);
        }
      }
    } else {
      for (final Player player : this.server.getOnlinePlayers()) {
        if (isPlayerJailed(player.getUniqueId())) {
          final YamlConfiguration yaml = retrieveJailedPlayer(player.getUniqueId());
          this.yamlsJailedPlayers.put(player.getUniqueId(), yaml);
          this.playersJailedUntil.put(player.getUniqueId(), now + yaml.getLong(FIELD_SECONDSLEFT) * 1000L);
        }
      }
    }

    if (this.plugin.getConfig().getBoolean("changeGroup")) {
      this.plugin.prisonerGroup = this.plugin.getConfig().getString("prisonerGroup");
    } else {
      this.plugin.permsInterface = null;
      this.plugin.prisonerGroup = null;
    }
  }

  public void timer() {
    final Iterator<Map.Entry<UUID, YamlConfiguration>> iterator = this.yamlsJailedPlayers.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<UUID, YamlConfiguration> entry = iterator.next();
      final UUID key = entry.getKey();
      final YamlConfiguration value = entry.getValue();
      final Location location = (Location) value.get(FIELD_LASTLOCATION, this.backupLocation);
      final boolean unjailed = value.getBoolean(FIELD_UNJAILED, false);

      if (location.equals(this.backupLocation)) {
        if (unjailed) {
          iterator.remove();
          new File(this.playerDataFolder, key + ".yml").delete();
          final String name = value.getString(FIELD_NAME);
          if (name != null) {
            this.jailedPlayerNames.remove(name.toLowerCase(Locale.ROOT));
          }

          this.jailedPlayerUuids.remove(key);
          this.playersJailedUntil.remove(key);
        }
        continue;
      }

      if (unjailed || getSecondsLeft(key, 0) <= 0) {
        removeJailedPlayer(key);
      }
    }
  }

  private void alertNewConfigAvailable() throws IOException {
    try (final InputStream bundledConfig = this.plugin.getResource("config.yml")) {
      final YamlConfiguration newYaml = YamlConfiguration.loadConfiguration(new InputStreamReader(bundledConfig));
      final FileConfiguration existingConfig = this.plugin.getConfig();

      if (newYaml.getKeys(true).hashCode() != existingConfig.getKeys(true).hashCode()) {
        this.plugin.getLogger().warning("New config.yml found!");
        this.plugin.getLogger().warning("Make sure to make a backup of your settings before deleting your current config.yml!");
      }
    }
  }
}
