package ar.fefo.betterjails.utils;

import ar.fefo.betterjails.Main;
import com.earth2me.essentials.User;
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

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataHandler {
    private static final String FIELD_UUID = "uuid";
    private static final String FIELD_NAME = "name";
    private final Vector<Integer> cachedJailedPlayersNameHashes = new Vector<>();
    private final Main main;
    private final File jailsFile;
    private final Hashtable<String, Jail> jails = new Hashtable<>();
    private static final String FIELD_JAIL = "jail";
    private static final String FIELD_JAILEDBY = "jailedby";
    private static final String FIELD_SECONDSLEFT = "secondsleft";
    private Location backupLocation;
    private YamlConfiguration yamlJails;
    private static final String FIELD_UNJAILED = "unjailed";
    private static final String FIELD_LASTLOCATION = "lastlocation";
    private static final String FIELD_GROUP = "group";
    public final File playerDataFolder;
    private final ConcurrentHashMap<UUID, YamlConfiguration> yamlsJailedPlayers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> playersJailedUntil = new ConcurrentHashMap<>();
    private final File subcommandsFile;
    private YamlConfiguration subcommandsYaml;

    public DataHandler(@NotNull Main main) throws IOException {
        this.main = main;

        String world = main.getConfig().getString("backupLocation.world");
        if (world == null) {
            world = main.getServer().getWorlds().get(0).getName();
            main.getLogger().warning("Error in config.yml: Couldn't retrieve backupLocation.world");
            main.getLogger().warning("Choosing world \"" + world + "\" by default.");
        }
        backupLocation = new Location(main.getServer().getWorld(world),
                                      main.getConfig().getDouble("backupLocation.x"),
                                      main.getConfig().getDouble("backupLocation.y"),
                                      main.getConfig().getDouble("backupLocation.z"),
                                      (float)main.getConfig().getDouble("backupLocation.yaw"),
                                      (float)main.getConfig().getDouble("backupLocation.pitch"));

        jailsFile = new File(main.getDataFolder(), "jails.yml");
        main.getDataFolder().mkdir();
        loadJails();

        playerDataFolder = new File(main.getDataFolder(), "playerdata");
        playerDataFolder.mkdir();

        subcommandsFile = new File(main.getDataFolder(), "subcommands.yml");
        if (!subcommandsFile.exists())
            main.saveResource("subcommands.yml", false);
        subcommandsYaml = YamlConfiguration.loadConfiguration(subcommandsFile);

        upgradeDataFiles();
        alertNewConfigAvailable();

        File[] files = playerDataFolder.listFiles();
        if (files != null) {
            long nowMillis = Instant.now().toEpochMilli();
            for (File file : files) {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                if (main.getConfig().getBoolean("offlineTime")) {
                    yamlsJailedPlayers.put(uuid, yaml);
                    if (!yaml.get(FIELD_LASTLOCATION, backupLocation).equals(backupLocation))
                        playersJailedUntil.put(uuid, nowMillis + yaml.getInt(FIELD_SECONDSLEFT, 0) * 1000L);
                }
                cachedJailedPlayersNameHashes.add(yaml.getString(FIELD_NAME, "").toUpperCase().hashCode());
            }
        }
    }

    private void loadJails() throws IOException {
        jailsFile.createNewFile();
        yamlJails = YamlConfiguration.loadConfiguration(jailsFile);

        for (String key : yamlJails.getKeys(false))
            jails.put(key, new Jail(key, (Location)yamlJails.get(key)));
    }

    public boolean isPlayerJailed(UUID uuid) { return isPlayerJailed(main.getServer().getOfflinePlayer(uuid).getName()); }

    public boolean isPlayerJailed(@NotNull String playerName) { return cachedJailedPlayersNameHashes.contains(playerName.toUpperCase().hashCode()); }

    @NotNull
    public YamlConfiguration retrieveJailedPlayer(UUID uuid) {
        if (!isPlayerJailed(uuid))
            return new YamlConfiguration();

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

    public boolean addJailedPlayer(@NotNull OfflinePlayer player,
                                   @NotNull String jailName,
                                   @Nullable String jailer,
                                   long secondsLeft) throws IOException {
        final YamlConfiguration yaml = retrieveJailedPlayer(player.getUniqueId());
        final Jail jail = getJail(jailName);
        final boolean jailExists = jail != null;
        final boolean isPlayerOnline = player.isOnline();
        final boolean isPlayerJailed = isPlayerJailed(player.getUniqueId());

        if (!jailExists)
            return false;

        yaml.set(FIELD_UUID, player.getUniqueId().toString());
        yaml.set(FIELD_NAME, player.getName());
        yaml.set(FIELD_JAIL, jailName);
        if (jailer != null)
            yaml.set(FIELD_JAILEDBY, jailer);
        yaml.set(FIELD_SECONDSLEFT, secondsLeft);
        yaml.set(FIELD_UNJAILED, false);

        if (isPlayerOnline && !isPlayerJailed) {
            yaml.set(FIELD_LASTLOCATION, ((Player)player).getLocation());

            ((Player)player).teleport(jail.getLocation());
            yamlsJailedPlayers.put(player.getUniqueId(), yaml);

            List<String> asPrisoner = subcommandsYaml.getStringList("on-jail.as-prisoner");
            List<String> asConsole = subcommandsYaml.getStringList("on-jail.as-console");
            for (String cmd : asPrisoner) {
                if (!cmd.equals(""))
                    ((Player)player).performCommand(cmd.replace("{player}", yaml.getString(FIELD_JAILEDBY, ""))
                                                       .replace("{prisoner}", player.getName()));
            }
            for (String cmd : asConsole) {
                if (!cmd.equals(""))
                    main.getServer().dispatchCommand(main.getServer().getConsoleSender(),
                                                     cmd.replace("{player}", yaml.getString(FIELD_JAILEDBY, ""))
                                                        .replace("{prisoner}", player.getName()));
            }
        } else if (!isPlayerOnline && !isPlayerJailed) {
            yaml.set(FIELD_LASTLOCATION, backupLocation);

            if (main.getConfig().getBoolean("offlineTime"))
                yamlsJailedPlayers.put(player.getUniqueId(), yaml);

        } else if (isPlayerOnline) {
            Location lastLocation = (Location)yaml.get(FIELD_LASTLOCATION, null);
            if (lastLocation == null) {
                yaml.set(FIELD_LASTLOCATION, backupLocation);
                lastLocation = backupLocation;
            }

            if (lastLocation.equals(backupLocation)) {
                yaml.set(FIELD_LASTLOCATION, ((Player)player).getLocation());
            }
            ((Player)player).teleport(jail.getLocation());
            yamlsJailedPlayers.put(player.getUniqueId(), yaml);
        }

        if (main.getConfig().getBoolean("changeGroup")) {
            main.getServer().getScheduler().runTaskAsynchronously(main, new Runnable() {
                @Override
                public void run() {
                    if (main.perm != null && (yaml.getString(FIELD_GROUP, null) == null || yaml.getBoolean(FIELD_UNJAILED, true))) {
                        String group = main.perm.getPrimaryGroup(null, player);
                        yaml.set(FIELD_GROUP, group);
                        try {
                            yaml.save(new File(playerDataFolder, player.getUniqueId().toString() + ".yml"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        main.perm.playerRemoveGroup(null, player, group);
                        main.perm.playerAddGroup(null, player, main.prisonerGroup);
                    }
                }
            });
        } else {
            yaml.save(new File(playerDataFolder, player.getUniqueId().toString() + ".yml"));
        }

        if (!isPlayerJailed)
            cachedJailedPlayersNameHashes.add(player.getName().toUpperCase().hashCode());

        if (player.isOnline()) {
            final long jailedUntil = Instant.now().toEpochMilli() + secondsLeft * 1000L;
            playersJailedUntil.put(player.getUniqueId(), jailedUntil);
        }

        if (main.ess != null) {
            User user = main.ess.getUser(player.getUniqueId());
            if (user != null) {
                user.setJailed(true);
                if (player.isOnline())
                    user.setJailTimeout(playersJailedUntil.get(player.getUniqueId()));
            }
        }

        return true;
    }

    public boolean removeJailedPlayer(@NotNull UUID uuid) {
        if (!isPlayerJailed(uuid))
            return false;

        final YamlConfiguration yaml = retrieveJailedPlayer(uuid);
        final File playerFile = new File(playerDataFolder, uuid + ".yml");

        final OfflinePlayer player = main.getServer().getOfflinePlayer(uuid);

        if (main.perm != null && main.getConfig().getBoolean("changeGroup")) {
            final String group = yaml.getString(FIELD_GROUP, null);
            main.getServer().getScheduler().runTaskAsynchronously(main, new Runnable() {
                @Override
                public void run() {
                    if (main.perm.getPrimaryGroup(null, player).equalsIgnoreCase(group))
                        return;
                    main.perm.playerRemoveGroup(null, player, main.prisonerGroup);
                    main.perm.playerAddGroup(null, player, group != null ? group : "default");
                }
            });
        }

        if (player.isOnline()) {
            Location lastLocation = (Location)yaml.get(FIELD_LASTLOCATION, backupLocation);

            if (lastLocation.equals(backupLocation))
                lastLocation = ((Player)player).getLocation();

            ((Player)player).teleport(lastLocation);
            yamlsJailedPlayers.remove(uuid);
            playerFile.delete();
            cachedJailedPlayersNameHashes.remove(Integer.valueOf(player.getName().toUpperCase().hashCode()));
            playersJailedUntil.remove(uuid);

            List<String> asPrisoner = subcommandsYaml.getStringList("on-release.as-prisoner");
            List<String> asConsole = subcommandsYaml.getStringList("on-release.as-console");
            for (String cmd : asPrisoner) {
                if (!cmd.equals(""))
                    ((Player)player).performCommand(cmd.replace("{player}", yaml.getString(FIELD_JAILEDBY, ""))
                                                       .replace("{prisoner}", player.getName()));
            }
            for (String cmd : asConsole) {
                if (!cmd.equals(""))
                    main.getServer().dispatchCommand(main.getServer().getConsoleSender(),
                                                     cmd.replace("{player}", yaml.getString(FIELD_JAILEDBY, ""))
                                                        .replace("{prisoner}", player.getName()));
            }
        } else {
            if (yaml.getBoolean(FIELD_UNJAILED, false))
                return true;

            if (yaml.get(FIELD_LASTLOCATION, backupLocation).equals(backupLocation)) {
                yamlsJailedPlayers.remove(uuid);
                playerFile.delete();
                cachedJailedPlayersNameHashes.remove(Integer.valueOf(player.getName().toUpperCase().hashCode()));
                playersJailedUntil.remove(uuid);
            } else {
                yaml.set(FIELD_UNJAILED, true);
                try {
                    yaml.save(playerFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (main.ess != null) {
            User user = main.ess.getUser(player.getUniqueId());
            if (user != null && user.isJailed()) {
                user.setJailTimeout(0);
                user.setJailed(false);
            }
        }

        return true;
    }

    public int getSecondsLeft(@NotNull UUID uuid, int def) {
        if (playersJailedUntil.containsKey(uuid))
            return (int)((playersJailedUntil.get(uuid) - Instant.now().toEpochMilli()) / 1000);
        else
            return retrieveJailedPlayer(uuid).getInt(FIELD_SECONDSLEFT, def);
    }
    public boolean getUnjailed(@NotNull UUID uuid, boolean def) { return retrieveJailedPlayer(uuid).getBoolean(FIELD_UNJAILED, def); }
    public String getName(@NotNull UUID uuid, @Nullable String def) { return retrieveJailedPlayer(uuid).getString(FIELD_NAME, def); }
    public String getJail(@NotNull UUID uuid, @Nullable String def) { return retrieveJailedPlayer(uuid).getString(FIELD_JAIL, def); }
    public String getJailer(@NotNull UUID uuid, @Nullable String def) { return retrieveJailedPlayer(uuid).getString(FIELD_JAILEDBY, def); }
    public Location getLastLocation(@NotNull UUID uuid) { return ((Location)retrieveJailedPlayer(uuid).get(FIELD_LASTLOCATION, backupLocation)); }
    public String getGroup(@NotNull UUID uuid, @Nullable String def) { return retrieveJailedPlayer(uuid).getString(FIELD_GROUP, def); }

    public void updateSecondsLeft(@NotNull UUID uuid) {
        OfflinePlayer player = main.getServer().getOfflinePlayer(uuid);
        if ((main.getConfig().getBoolean("offlineTime") &&
             !getLastLocation(uuid).equals(backupLocation)) ||
            player.isOnline()) {
            retrieveJailedPlayer(uuid).set(FIELD_SECONDSLEFT, getSecondsLeft(uuid, 0));
        }
    }

    public void save() throws IOException {
        yamlJails.save(jailsFile);

        for (Map.Entry<UUID, YamlConfiguration> entry : yamlsJailedPlayers.entrySet()) {
            UUID k = entry.getKey();
            YamlConfiguration v = entry.getValue();
            try {
                v.set(FIELD_SECONDSLEFT, getSecondsLeft(k, 0));
                v.save(new File(playerDataFolder, k + ".yml"));
            } catch (IOException e) {
                main.getLogger().severe(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void reload() {
        main.reloadConfig();

        String unjailWorld = main.getConfig().getString("backupLocation.world");
        if (unjailWorld == null) {
            unjailWorld = main.getServer().getWorlds().get(0).getName();
            main.getLogger().warning("Error in config.yml: Couldn't retrieve backupLocation.world");
            main.getLogger().warning("Choosing world \"" + unjailWorld + "\" by default.");
        }
        backupLocation = new Location(main.getServer().getWorld(unjailWorld),
                                      main.getConfig().getDouble("backupLocation.x"),
                                      main.getConfig().getDouble("backupLocation.y"),
                                      main.getConfig().getDouble("backupLocation.z"),
                                      (float)main.getConfig().getDouble("backupLocation.yaw"),
                                      (float)main.getConfig().getDouble("backupLocation.pitch"));

        yamlJails = YamlConfiguration.loadConfiguration(jailsFile);

        for (String key : yamlJails.getKeys(false))
            jails.put(key, new Jail(key, (Location)yamlJails.get(key)));

        subcommandsYaml = YamlConfiguration.loadConfiguration(subcommandsFile);

        yamlsJailedPlayers.clear();
        playersJailedUntil.clear();
        long nowMillis = Instant.now().toEpochMilli();
        if (main.getConfig().getBoolean("offlineTime")) {
            File[] files = playerDataFolder.listFiles();
            if (files != null)
                for (File file : files) {
                    UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                    yamlsJailedPlayers.put(uuid, yaml);
                    playersJailedUntil.put(uuid, nowMillis + yaml.getLong(FIELD_SECONDSLEFT) * 1000L);
                }
        } else {
            for (Player player : main.getServer().getOnlinePlayers()) {
                if (isPlayerJailed(player.getUniqueId())) {
                    YamlConfiguration yaml = retrieveJailedPlayer(player.getUniqueId());
                    yamlsJailedPlayers.put(player.getUniqueId(), yaml);
                    playersJailedUntil.put(player.getUniqueId(), nowMillis + yaml.getLong(FIELD_SECONDSLEFT) * 1000L);
                }
            }
        }

        if (main.getConfig().getBoolean("changeGroup")) {
            if (main.getServer().getPluginManager().getPlugin("Vault") != null) {
                RegisteredServiceProvider<Permission> rsp = main.getServer().getServicesManager().getRegistration(Permission.class);
                if (rsp == null) {
                    main.getLogger().severe("There was an error while hooking with Vault!");
                    main.getLogger().severe("Group changing feature will not be used!");
                } else {
                    main.perm = rsp.getProvider();
                    main.prisonerGroup = main.getConfig().getString("prisonerGroup");
                }
            } else {
                main.getLogger().warning("Option \"changeGroup\" in config.yml is set to true, yet Vault wasn't found!");
                main.getLogger().warning("Group changing feature will not be used!");
            }
        }
    }

    public void timer() {
        for (Map.Entry<UUID, YamlConfiguration> entry : yamlsJailedPlayers.entrySet()) {
            UUID k = entry.getKey();
            YamlConfiguration v = entry.getValue();
            Location loc = ((Location)v.get(FIELD_LASTLOCATION, backupLocation));
            boolean unjailed = v.getBoolean(FIELD_UNJAILED, false);

            if (loc.equals(backupLocation) &&
                unjailed) {
                yamlsJailedPlayers.remove(k);
                new File(playerDataFolder, k + ".yml").delete();
                cachedJailedPlayersNameHashes.remove(Integer.valueOf(v.getString(FIELD_NAME, "").toUpperCase().hashCode()));
                playersJailedUntil.remove(k);
                continue;
            } else if (loc.equals(backupLocation))
                continue;

            if (unjailed || getSecondsLeft(k, 0) <= 0)
                removeJailedPlayer(k);
        }
    }

    private void upgradeDataFiles() throws IOException {
        File oldPlayerDataFile = new File(main.getDataFolder(), "jailed_players.yml");
        if (oldPlayerDataFile.exists()) {
            YamlConfiguration oldPlayerData = YamlConfiguration.loadConfiguration(oldPlayerDataFile);
            for (String key : oldPlayerData.getConfigurationSection("players").getKeys(false)) {
                ConfigurationSection section = oldPlayerData.getConfigurationSection("players." + key);

                YamlConfiguration newPlayerData = new YamlConfiguration();
                newPlayerData.set(FIELD_UUID, key);
                newPlayerData.set(FIELD_NAME, section.getString(FIELD_NAME));
                newPlayerData.set(FIELD_JAIL, section.getString(FIELD_JAIL));
                newPlayerData.set(FIELD_SECONDSLEFT, section.getInt("secondsLeft"));
                newPlayerData.set(FIELD_UNJAILED, section.getBoolean(FIELD_UNJAILED));

                String w = section.getString(FIELD_LASTLOCATION + ".world");
                Location l = new Location(main.getServer()
                                              .getWorld(w != null ? w : "world"),
                                          section.getDouble(FIELD_LASTLOCATION + ".x"),
                                          section.getDouble(FIELD_LASTLOCATION + ".y"),
                                          section.getDouble(FIELD_LASTLOCATION + ".z"),
                                          (float)section.getDouble(FIELD_LASTLOCATION + ".yaw"),
                                          (float)section.getDouble(FIELD_LASTLOCATION + ".pitch"));
                newPlayerData.set(FIELD_LASTLOCATION, l);

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
