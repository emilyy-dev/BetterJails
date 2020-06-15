package me.fefo.betterjails.utils;

import com.earth2me.essentials.User;
import me.fefo.betterjails.Main;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public final class DataHandler {
  private static final String PLAYERDATA_FIELD_UUID = "uuid";
  private static final String PLAYERDATA_FIELD_NAME = "name";
  private static final String PLAYERDATA_FIELD_JAIL = "jail";
  private static final String PLAYERDATA_FIELD_JAILEDBY = "jailedby";
  private static final String PLAYERDATA_FIELD_SECONDSLEFT = "secondsleft";
  private static final String PLAYERDATA_FIELD_UNJAILED = "unjailed";
  private static final String PLAYERDATA_FIELD_LASTLOCATION = "lastlocation";
  private static final String PLAYERDATA_FIELD_GROUP = "group";
  public final File playerDataFolder;
  private final ConcurrentHashMap<UUID, YamlConfiguration> yamlsJailedPlayers = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<UUID, Long> playersJailedUntil = new ConcurrentHashMap<>();
  private final Vector<Integer> cachedJailedPlayersNameHashes = new Vector<>();
  private final Hashtable<String, Jail> jails = new Hashtable<>();
  private final File subcommandsFile;
  private final File jailsFile;
  private final boolean useYaml;
  private final SQLConnector conn;
  private final String tableprefix;
  private final Main main;
  private YamlConfiguration yamlJails;
  private YamlConfiguration subcommandsYaml;
  private Location backupLocation;

  public DataHandler(@NotNull Main main) throws IOException, SQLException {
    this.main = main;

    String world = main.getConfig().getString(Main.CONFIG_FIELD_BACKUP_LOCATION + ".world");
    if (world == null || main.getServer().getWorld(world) == null) {
      world = main.getServer().getWorlds().get(0).getName();
      main.getLogger().warning("Error in config.yml: Couldn't retrieve \"" + Main.CONFIG_FIELD_BACKUP_LOCATION + ".world\"");
      main.getLogger().warning("Choosing world \"" + world + "\" by default.");
    }
    backupLocation = new Location(main.getServer().getWorld(world),
                                  main.getConfig().getDouble(Main.CONFIG_FIELD_BACKUP_LOCATION + ".x"),
                                  main.getConfig().getDouble(Main.CONFIG_FIELD_BACKUP_LOCATION + ".y"),
                                  main.getConfig().getDouble(Main.CONFIG_FIELD_BACKUP_LOCATION + ".z"),
                                  (float)main.getConfig().getDouble(Main.CONFIG_FIELD_BACKUP_LOCATION + ".yaw"),
                                  (float)main.getConfig().getDouble(Main.CONFIG_FIELD_BACKUP_LOCATION + ".pitch"));

    jailsFile = new File(main.getDataFolder(), "jails.yml");
    main.getDataFolder().mkdir();
    loadJails();

    playerDataFolder = new File(main.getDataFolder(), "playerdata");
    playerDataFolder.mkdir();

    subcommandsFile = new File(main.getDataFolder(), "subcommands.yml");
    if (!subcommandsFile.exists()) {
      main.saveResource("subcommands.yml", false);
    }
    subcommandsYaml = YamlConfiguration.loadConfiguration(subcommandsFile);

    upgradeDataFiles();
    alertNewConfigAvailable();

    final boolean useMySQL = main.getConfig().getBoolean(Main.CONFIG_FIELD_USE_MYSQL, false);
    if (useMySQL) {
      useYaml = false;
      final String host, port, database, user, passwd;
      host = main.getConfig().getString("mysql.host", null);
      port = main.getConfig().getString("mysql.port", null);
      database = main.getConfig().getString("mysql.schema", null);
      user = main.getConfig().getString("mysql.user", null);
      passwd = main.getConfig().getString("mysql.passwd", null);
      tableprefix = main.getConfig().getString("mysql.tablePrefix", null);
      conn = new SQLConnector(host + ":" + port,
                              database,
                              user,
                              passwd);

      if (conn.openSession()) {
        String tableCreationStatement = "CREATE TABLE IF NOT EXISTS `" + database + "`.`" + tableprefix + "jails` (" +
                                        "`name` VARCHAR(32) NOT NULL," +
                                        "`location` BLOB NOT NULL)";
        conn.SQLStatementNoReturn(tableCreationStatement, (Object[])null);

        tableCreationStatement = "CREATE TABLE IF NOT EXISTS `" + database + "`.`" + tableprefix + "playerdata` (" +
                                 "`uuid_msb` BIGINT UNSIGNED NOT NULL," +
                                 "`uuid_lsb` BIGINT UNSIGNED NOT NULL," +
                                 "`name` VARCHAR(32) NOT NULL," +
                                 "`jail` VARCHAR(32) NOT NULL," +
                                 "`jailedby` VARCHAR(32) NOT NULL," +
                                 "`secondsleft` BIGINT NOT NULL," +
                                 "`unjailed` BIT(1) NOT NULL," +
                                 "`lastlocation` BLOB NOT NULL," +
                                 "`group` VARCHAR(32) NOT NULL)";
        conn.SQLStatementNoReturn(tableCreationStatement, (Object[])null);
        conn.closeSession();
      } else {
        conn.closeSession();
        throw new SQLException("Could not connect to the database");
      }
    } else {
      useYaml = true;
      tableprefix = null;
      conn = null;

      File[] files = playerDataFolder.listFiles();
      if (files != null) {
        long nowMillis = Instant.now().toEpochMilli();
        for (File file : files) {
          YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
          UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
          if (main.getConfig().getBoolean(Main.CONFIG_FIELD_OFFLINE_TIME)) {
            yamlsJailedPlayers.put(uuid, yaml);
            if (!yaml.get(PLAYERDATA_FIELD_LASTLOCATION, backupLocation).equals(backupLocation)) {
              playersJailedUntil.put(uuid, nowMillis + yaml.getInt(PLAYERDATA_FIELD_SECONDSLEFT, 0) * 1000L);
            }
          }
          cachedJailedPlayersNameHashes.add(yaml.getString(PLAYERDATA_FIELD_NAME, "").toUpperCase().hashCode());
        }
      }
    }
  }

  private void loadJails() throws IOException {
    jailsFile.createNewFile();
    yamlJails = YamlConfiguration.loadConfiguration(jailsFile);

    for (String key : yamlJails.getKeys(false)) {
      jails.put(key, new Jail(key, (Location)yamlJails.get(key)));
    }
  }

  public boolean isPlayerJailed(UUID uuid) { return isPlayerJailed(main.getServer().getOfflinePlayer(uuid).getName()); }

  public boolean isPlayerJailed(@NotNull String playerName) { return cachedJailedPlayersNameHashes.contains(playerName.toUpperCase().hashCode()); }

  @NotNull
  public YamlConfiguration retrieveJailedPlayer(UUID uuid) {
    if (!isPlayerJailed(uuid)) {
      return new YamlConfiguration();
    }

    if (yamlsJailedPlayers.containsKey(uuid)) {
      return yamlsJailedPlayers.get(uuid);
    } else {
      File playerFile = new File(playerDataFolder, uuid + ".yml");
      YamlConfiguration retYaml = new YamlConfiguration();
      try {
        retYaml.load(playerFile);
      } catch (IOException e) {
        if (!(e instanceof FileNotFoundException)) {
          main.getLogger().severe("Couldn't read file " + playerFile.getAbsolutePath());
          e.printStackTrace();
        }
      } catch (InvalidConfigurationException e) {
        main.getLogger().severe("Invalid YAML configuration in file " + playerFile.getAbsolutePath());
        e.printStackTrace();
      }

      return retYaml;
    }
  }

  public void loadJailedPlayer(UUID uuid, YamlConfiguration jailedPlayer) { yamlsJailedPlayers.put(uuid, jailedPlayer); }

  public void unloadJailedPlayer(UUID uuid) {
    yamlsJailedPlayers.remove(uuid);
    playersJailedUntil.remove(uuid);
  }

  public Hashtable<String, Jail> getJails() { return jails; }

  public int getTotalJailedPlayers() { return cachedJailedPlayersNameHashes.size(); }

  @Nullable
  public Jail getJail(String name) { return jails.get(name); }

  public void addJail(String name, Location location) throws IOException {
    jails.put(name, new Jail(name, location));
    yamlJails.set(name, location);
    yamlJails.save(jailsFile);
  }

  public void removeJail(String name) throws IOException {
    jails.remove(name);
    yamlJails.set(name, null);
    yamlJails.save(jailsFile);
  }

  // TODO: reorder when to change the group of the player so it's after both being jailed and unjailed.
  public boolean addJailedPlayer(@NotNull OfflinePlayer player,
                                 @NotNull String jailName,
                                 @Nullable String jailer,
                                 long secondsLeft) throws IOException {
    final YamlConfiguration yaml = retrieveJailedPlayer(player.getUniqueId());
    final Jail jail = getJail(jailName);
    final boolean jailExists = jail != null;
    final boolean isPlayerOnline = player.isOnline();
    final boolean isPlayerJailed = isPlayerJailed(player.getUniqueId());

    if (!jailExists) {
      return false;
    }

    yaml.set(PLAYERDATA_FIELD_UUID, player.getUniqueId().toString());
    yaml.set(PLAYERDATA_FIELD_NAME, player.getName());
    yaml.set(PLAYERDATA_FIELD_JAIL, jailName);
    if (jailer != null) {
      yaml.set(PLAYERDATA_FIELD_JAILEDBY, jailer);
    }
    yaml.set(PLAYERDATA_FIELD_SECONDSLEFT, secondsLeft);
    yaml.set(PLAYERDATA_FIELD_UNJAILED, false);

    if (isPlayerOnline && !isPlayerJailed) {
      yaml.set(PLAYERDATA_FIELD_LASTLOCATION, ((Player)player).getLocation());

      ((Player)player).teleport(jail.getLocation());
      yamlsJailedPlayers.put(player.getUniqueId(), yaml);

      List<String> asPrisoner = subcommandsYaml.getStringList("on-jail.as-prisoner");
      List<String> asConsole = subcommandsYaml.getStringList("on-jail.as-console");
      for (String cmd : asPrisoner) {
        if (!cmd.equals("")) {
          ((Player)player).performCommand(cmd.replace("{player}", yaml.getString(PLAYERDATA_FIELD_JAILEDBY, ""))
                                             .replace("{prisoner}", player.getName()));
        }
      }
      for (String cmd : asConsole) {
        if (!cmd.equals("")) {
          main.getServer().dispatchCommand(main.getServer().getConsoleSender(),
                                           cmd.replace("{player}", yaml.getString(PLAYERDATA_FIELD_JAILEDBY, ""))
                                              .replace("{prisoner}", player.getName()));
        }
      }
    } else if (!isPlayerOnline && !isPlayerJailed) {
      yaml.set(PLAYERDATA_FIELD_LASTLOCATION, backupLocation);

      if (main.getConfig().getBoolean(Main.CONFIG_FIELD_OFFLINE_TIME)) {
        yamlsJailedPlayers.put(player.getUniqueId(), yaml);
      }

    } else if (isPlayerOnline) {
      Location lastLocation = (Location)yaml.get(PLAYERDATA_FIELD_LASTLOCATION, null);
      if (lastLocation == null) {
        yaml.set(PLAYERDATA_FIELD_LASTLOCATION, backupLocation);
        lastLocation = backupLocation;
      }

      if (lastLocation.equals(backupLocation)) {
        yaml.set(PLAYERDATA_FIELD_LASTLOCATION, ((Player)player).getLocation());
      }
      ((Player)player).teleport(jail.getLocation());
      yamlsJailedPlayers.put(player.getUniqueId(), yaml);
    }

    if (main.getConfig().getBoolean(Main.CONFIG_FIELD_CHANGE_GROUP)) {
      main.getServer().getScheduler().runTaskAsynchronously(main, new Runnable() {
        @Override
        public void run() {
          if (main.permManager != null && (yaml.getString(PLAYERDATA_FIELD_GROUP, null) == null || yaml.getBoolean(PLAYERDATA_FIELD_UNJAILED, true))) {
            String group = main.permManager.getPrimaryGroup(null, player);
            yaml.set(PLAYERDATA_FIELD_GROUP, group);
            try {
              yaml.save(new File(playerDataFolder, player.getUniqueId().toString() + ".yml"));
            } catch (IOException e) {
              e.printStackTrace();
            }
            main.permManager.playerRemoveGroup(null, player, group);
            main.permManager.playerAddGroup(null, player, main.prisonerGroup);
          }
        }
      });
    } else {
      yaml.save(new File(playerDataFolder, player.getUniqueId().toString() + ".yml"));
    }

    if (!isPlayerJailed) {
      cachedJailedPlayersNameHashes.add(player.getName().toUpperCase().hashCode());
    }

    if (player.isOnline()) {
      final long jailedUntil = Instant.now().toEpochMilli() + secondsLeft * 1000L;
      playersJailedUntil.put(player.getUniqueId(), jailedUntil);
    }

    if (main.essentials != null) {
      User user = main.essentials.getUser(player.getUniqueId());
      if (user != null) {
        user.setJailed(true);
        if (player.isOnline()) {
          user.setJailTimeout(playersJailedUntil.get(player.getUniqueId()));
        }
      }
    }

    return true;
  }

  // TODO: reorder when to change the group of the player so it's after both being jailed and unjailed.
  public boolean removeJailedPlayer(@NotNull UUID uuid) {
    if (!isPlayerJailed(uuid)) {
      return false;
    }

    final YamlConfiguration yaml = retrieveJailedPlayer(uuid);
    final File playerFile = new File(playerDataFolder, uuid + ".yml");

    final OfflinePlayer player = main.getServer().getOfflinePlayer(uuid);

    if (main.permManager != null && main.getConfig().getBoolean(Main.CONFIG_FIELD_CHANGE_GROUP)) {
      final String group = yaml.getString(PLAYERDATA_FIELD_GROUP, null);
      main.getServer().getScheduler().runTaskAsynchronously(main, new Runnable() {
        @Override
        public void run() {
          if (main.permManager.getPrimaryGroup(null, player).equalsIgnoreCase(group)) {
            return;
          }
          main.permManager.playerRemoveGroup(null, player, main.prisonerGroup);
          main.permManager.playerAddGroup(null, player, group != null ? group : "default");
        }
      });
    }

    if (player.isOnline()) {
      Location lastLocation = (Location)yaml.get(PLAYERDATA_FIELD_LASTLOCATION, backupLocation);

      if (lastLocation.equals(backupLocation)) {
        lastLocation = ((Player)player).getLocation();
      }

      ((Player)player).teleport(lastLocation);
      yamlsJailedPlayers.remove(uuid);
      playerFile.delete();
      cachedJailedPlayersNameHashes.remove(Integer.valueOf(player.getName().toUpperCase().hashCode()));
      playersJailedUntil.remove(uuid);

      List<String> asPrisoner = subcommandsYaml.getStringList("on-release.as-prisoner");
      List<String> asConsole = subcommandsYaml.getStringList("on-release.as-console");
      for (String cmd : asPrisoner) {
        if (!cmd.equals("")) {
          ((Player)player).performCommand(cmd.replace("{player}", yaml.getString(PLAYERDATA_FIELD_JAILEDBY, ""))
                                             .replace("{prisoner}", player.getName()));
        }
      }
      for (String cmd : asConsole) {
        if (!cmd.equals("")) {
          main.getServer().dispatchCommand(main.getServer().getConsoleSender(),
                                           cmd.replace("{player}", yaml.getString(PLAYERDATA_FIELD_JAILEDBY, ""))
                                              .replace("{prisoner}", player.getName()));
        }
      }
    } else {
      if (yaml.getBoolean(PLAYERDATA_FIELD_UNJAILED, false)) {
        return true;
      }

      if (yaml.get(PLAYERDATA_FIELD_LASTLOCATION, backupLocation).equals(backupLocation)) {
        yamlsJailedPlayers.remove(uuid);
        playerFile.delete();
        cachedJailedPlayersNameHashes.remove(Integer.valueOf(player.getName().toUpperCase().hashCode()));
        playersJailedUntil.remove(uuid);
      } else {
        yaml.set(PLAYERDATA_FIELD_UNJAILED, true);
        try {
          yaml.save(playerFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    if (main.essentials != null) {
      User user = main.essentials.getUser(player.getUniqueId());
      if (user != null && user.isJailed()) {
        user.setJailTimeout(0);
        user.setJailed(false);
      }
    }

    return true;
  }

  public int getSecondsLeft(@NotNull UUID uuid, int def) {
    if (playersJailedUntil.containsKey(uuid)) {
      return (int)((playersJailedUntil.get(uuid) - Instant.now().toEpochMilli()) / 1000);
    } else {
      return retrieveJailedPlayer(uuid).getInt(PLAYERDATA_FIELD_SECONDSLEFT, def);
    }
  }
  public boolean getUnjailed(@NotNull UUID uuid, boolean def) { return retrieveJailedPlayer(uuid).getBoolean(PLAYERDATA_FIELD_UNJAILED, def); }
  public String getName(@NotNull UUID uuid, @Nullable String def) { return retrieveJailedPlayer(uuid).getString(PLAYERDATA_FIELD_NAME, def); }
  public String getJail(@NotNull UUID uuid, @Nullable String def) { return retrieveJailedPlayer(uuid).getString(PLAYERDATA_FIELD_JAIL, def); }
  public String getJailer(@NotNull UUID uuid, @Nullable String def) { return retrieveJailedPlayer(uuid).getString(PLAYERDATA_FIELD_JAILEDBY, def); }
  public Location getLastLocation(@NotNull UUID uuid) { return ((Location)retrieveJailedPlayer(uuid).get(PLAYERDATA_FIELD_LASTLOCATION, backupLocation)); }
  public String getGroup(@NotNull UUID uuid, @Nullable String def) { return retrieveJailedPlayer(uuid).getString(PLAYERDATA_FIELD_GROUP, def); }

  public void updateSecondsLeft(@NotNull UUID uuid) {
    OfflinePlayer player = main.getServer().getOfflinePlayer(uuid);
    if ((main.getConfig().getBoolean(Main.CONFIG_FIELD_OFFLINE_TIME) &&
         !getLastLocation(uuid).equals(backupLocation)) ||
        player.isOnline()) {
      retrieveJailedPlayer(uuid).set(PLAYERDATA_FIELD_SECONDSLEFT, getSecondsLeft(uuid, 0));
    }
  }

  public void save() throws IOException {
    yamlJails.save(jailsFile);

    for (Map.Entry<UUID, YamlConfiguration> entry : yamlsJailedPlayers.entrySet()) {
      UUID k = entry.getKey();
      YamlConfiguration v = entry.getValue();
      try {
        v.set(PLAYERDATA_FIELD_SECONDSLEFT, getSecondsLeft(k, 0));
        v.save(new File(playerDataFolder, k + ".yml"));
      } catch (IOException e) {
        main.getLogger().severe(e.getMessage());
        e.printStackTrace();
      }
    }
  }

  public void reload() {
    main.reloadConfig();

    String unjailWorld = main.getConfig().getString(Main.CONFIG_FIELD_BACKUP_LOCATION + ".world");
    if (unjailWorld == null) {
      unjailWorld = main.getServer().getWorlds().get(0).getName();
      main.getLogger().warning("Error in config.yml: Couldn't retrieve " + Main.CONFIG_FIELD_BACKUP_LOCATION + ".world");
      main.getLogger().warning("Choosing world \"" + unjailWorld + "\" by default.");
    }
    backupLocation = new Location(main.getServer().getWorld(unjailWorld),
                                  main.getConfig().getDouble(Main.CONFIG_FIELD_BACKUP_LOCATION + ".x"),
                                  main.getConfig().getDouble(Main.CONFIG_FIELD_BACKUP_LOCATION + ".y"),
                                  main.getConfig().getDouble(Main.CONFIG_FIELD_BACKUP_LOCATION + ".z"),
                                  (float)main.getConfig().getDouble(Main.CONFIG_FIELD_BACKUP_LOCATION + ".yaw"),
                                  (float)main.getConfig().getDouble(Main.CONFIG_FIELD_BACKUP_LOCATION + ".pitch"));

    yamlJails = YamlConfiguration.loadConfiguration(jailsFile);

    for (String key : yamlJails.getKeys(false)) {
      jails.put(key, new Jail(key, (Location)yamlJails.get(key)));
    }

    subcommandsYaml = YamlConfiguration.loadConfiguration(subcommandsFile);

    yamlsJailedPlayers.clear();
    playersJailedUntil.clear();
    long nowMillis = Instant.now().toEpochMilli();
    if (main.getConfig().getBoolean(Main.CONFIG_FIELD_OFFLINE_TIME)) {
      File[] files = playerDataFolder.listFiles();
      if (files != null) {
        for (File file : files) {
          UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
          YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
          yamlsJailedPlayers.put(uuid, yaml);
          playersJailedUntil.put(uuid, nowMillis + yaml.getLong(PLAYERDATA_FIELD_SECONDSLEFT) * 1000L);
        }
      }
    } else {
      for (Player player : main.getServer().getOnlinePlayers()) {
        if (isPlayerJailed(player.getUniqueId())) {
          YamlConfiguration yaml = retrieveJailedPlayer(player.getUniqueId());
          yamlsJailedPlayers.put(player.getUniqueId(), yaml);
          playersJailedUntil.put(player.getUniqueId(), nowMillis + yaml.getLong(PLAYERDATA_FIELD_SECONDSLEFT) * 1000L);
        }
      }
    }

    if (main.getConfig().getBoolean(Main.CONFIG_FIELD_CHANGE_GROUP)) {
      if (main.getServer().getPluginManager().getPlugin("Vault") != null) {
        RegisteredServiceProvider<Permission> rsp = main.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
          main.getLogger().severe("There was an error while hooking with Vault!");
          main.getLogger().severe("Group changing feature will not be used!");
        } else {
          main.permManager = rsp.getProvider();
          main.prisonerGroup = main.getConfig().getString(Main.CONFIG_FIELD_PRISONER_GROUP);
        }
      } else {
        main.getLogger().warning("Option \"" + Main.CONFIG_FIELD_CHANGE_GROUP + "\" in config.yml is set to true, yet Vault wasn't found!");
        main.getLogger().warning("Group changing feature will not be used!");
      }
    }
  }

  public void timer() {
    for (Map.Entry<UUID, YamlConfiguration> entry : yamlsJailedPlayers.entrySet()) {
      UUID k = entry.getKey();
      YamlConfiguration v = entry.getValue();
      Location loc = ((Location)v.get(PLAYERDATA_FIELD_LASTLOCATION, backupLocation));
      boolean unjailed = v.getBoolean(PLAYERDATA_FIELD_UNJAILED, false);

      if (loc.equals(backupLocation) &&
          unjailed) {
        yamlsJailedPlayers.remove(k);
        new File(playerDataFolder, k + ".yml").delete();
        cachedJailedPlayersNameHashes.remove(Integer.valueOf(v.getString(PLAYERDATA_FIELD_NAME, "").toUpperCase().hashCode()));
        playersJailedUntil.remove(k);
        continue;
      } else if (loc.equals(backupLocation)) {
        continue;
      }

      if (unjailed || getSecondsLeft(k, 0) <= 0) {
        removeJailedPlayer(k);
      }
    }
  }

  private void upgradeDataFiles() throws IOException {
    File oldPlayerDataFile = new File(main.getDataFolder(), "jailed_players.yml");
    if (oldPlayerDataFile.exists()) {
      YamlConfiguration oldPlayerData = YamlConfiguration.loadConfiguration(oldPlayerDataFile);
      for (String key : oldPlayerData.getConfigurationSection("players").getKeys(false)) {
        ConfigurationSection section = oldPlayerData.getConfigurationSection("players." + key);

        YamlConfiguration newPlayerData = new YamlConfiguration();
        newPlayerData.set(PLAYERDATA_FIELD_UUID, key);
        newPlayerData.set(PLAYERDATA_FIELD_NAME, section.getString(PLAYERDATA_FIELD_NAME));
        newPlayerData.set(PLAYERDATA_FIELD_JAIL, section.getString(PLAYERDATA_FIELD_JAIL));
        newPlayerData.set(PLAYERDATA_FIELD_SECONDSLEFT, section.getInt("secondsLeft"));
        newPlayerData.set(PLAYERDATA_FIELD_UNJAILED, section.getBoolean(PLAYERDATA_FIELD_UNJAILED));

        String w = section.getString(PLAYERDATA_FIELD_LASTLOCATION + ".world");
        Location l = new Location(main.getServer()
                                      .getWorld(w != null ? w : "world"),
                                  section.getDouble(PLAYERDATA_FIELD_LASTLOCATION + ".x"),
                                  section.getDouble(PLAYERDATA_FIELD_LASTLOCATION + ".y"),
                                  section.getDouble(PLAYERDATA_FIELD_LASTLOCATION + ".z"),
                                  (float)section.getDouble(PLAYERDATA_FIELD_LASTLOCATION + ".yaw"),
                                  (float)section.getDouble(PLAYERDATA_FIELD_LASTLOCATION + ".pitch"));
        newPlayerData.set(PLAYERDATA_FIELD_LASTLOCATION, l);

        newPlayerData.save(new File(playerDataFolder, key + ".yml"));
      }

      oldPlayerDataFile.delete();
    }
  }

  private void alertNewConfigAvailable() throws IOException {
    InputStream newConfig = main.getResource("config.yml");
    assert newConfig != null;
    YamlConfiguration newYaml = YamlConfiguration.loadConfiguration(new InputStreamReader(newConfig));
    newConfig.close();

    FileConfiguration oldConfig = main.getConfig();

    if (newYaml.getKeys(true).hashCode() != oldConfig.getKeys(true).hashCode()) {
      main.getLogger().warning("New config.yml found!");
      main.getLogger().warning("Make sure to make a backup of your settings before deleting your current config.yml!");
    }
  }
}
