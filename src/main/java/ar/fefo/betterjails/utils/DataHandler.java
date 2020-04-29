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
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class DataHandler {
    private final ConcurrentHashMap<UUID, YamlConfiguration> yamlsOnlineJailedPlayers = new ConcurrentHashMap<>();
    private final Vector<Integer> cachedJailedPlayersNameHashes = new Vector<>();
    private final Main main;
    private final File jailsFile;
    private final Hashtable<String, Jail> jails = new Hashtable<>();
    public File playerDataFolder;
    private Location backupLocation;
    private YamlConfiguration yamlJails;

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

        jailsFile = new File(this.main.getDataFolder(), "jails.yml");
        if (!jailsFile.getParentFile().exists())
            if (!jailsFile.toPath().getParent().toFile().mkdirs())
                throw new IOException("Could not create data folder.");
        loadJails();

        playerDataFolder = new File(this.main.getDataFolder(), "playerdata" + File.separator);
        if (!playerDataFolder.exists())
            if (!playerDataFolder.mkdirs())
                throw new IOException("Could not create player data folder.");

        upgradeDataFiles();
        alertNewConfigAvailable();

        if (main.getConfig().getBoolean("offlineTime")) {
            File[] files = playerDataFolder.listFiles();
            if (files != null)
                for (File file : files) {
                    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                    yamlsOnlineJailedPlayers.put(UUID.fromString(file.getName().replace(".yml", "")), yaml);
                    cachedJailedPlayersNameHashes.add(yaml.getString("name", "").toUpperCase().hashCode());
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

        if (yamlsOnlineJailedPlayers.containsKey(uuid)) {
            return yamlsOnlineJailedPlayers.get(uuid);
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

    public void loadJailedPlayer(UUID uuid, YamlConfiguration jailedPlayer) { yamlsOnlineJailedPlayers.put(uuid, jailedPlayer); }

    public void unloadJailedPlayer(UUID uuid) { yamlsOnlineJailedPlayers.remove(uuid); }

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
        YamlConfiguration yaml = retrieveJailedPlayer(player.getUniqueId());
        Jail jail = getJail(jailName);
        boolean jailExists = jail != null;
        boolean isPlayerOnline = player.isOnline();
        boolean isPlayerJailed = isPlayerJailed(player.getUniqueId());

        if (!jailExists)
            return false;

        yaml.set("uuid", player.getUniqueId().toString());
        yaml.set("name", player.getName());
        yaml.set("jail", jailName);
        if (jailer != null)
            yaml.set("jailedby", jailer);
        yaml.set("secondsleft", secondsLeft);
        yaml.set("unjailed", false);

        if (isPlayerOnline && !isPlayerJailed) {
            yaml.set("lastlocation", ((Player)player).getLocation());

            ((Player)player).teleport(jail.getLocation());
            yamlsOnlineJailedPlayers.put(player.getUniqueId(), yaml);

        } else if (!isPlayerOnline && !isPlayerJailed) {
            yaml.set("lastlocation", backupLocation);

            if (main.getConfig().getBoolean("offlineTime"))
                yamlsOnlineJailedPlayers.put(player.getUniqueId(), yaml);

        } else if (isPlayerOnline) {
            Location lastLocation = (Location)yaml.get("lastlocation");
            if (lastLocation == null) {
                yaml.set("lastlocation", backupLocation);
                lastLocation = backupLocation;
            }

            if (lastLocation.equals(backupLocation)) {
                yaml.set("lastlocation", ((Player)player).getLocation());
            }
            ((Player)player).teleport(jail.getLocation());
            yamlsOnlineJailedPlayers.put(player.getUniqueId(), yaml);
        }

        if (main.getConfig().getBoolean("changeGroup")) {
            main.getServer().getScheduler().runTaskAsynchronously(main, new Runnable() {
                @Override
                public void run() {
                    if (main.perm != null && (yaml.getString("group") == null || yaml.getBoolean("unjailed"))) {
                        String group = main.perm.getPrimaryGroup(null, player);
                        yaml.set("group", group);
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

        if (main.ess != null) {
            User user = main.ess.getUser(player.getUniqueId());
            if (user != null) {
                user.setJailed(true);
                if (player.isOnline())
                    user.setJailTimeout(Instant.now().toEpochMilli() + secondsLeft * 1000);
            }
        }

        return true;
    }

    public boolean removeJailedPlayer(@NotNull UUID uuid) {
        if (!isPlayerJailed(uuid))
            return false;

        YamlConfiguration yaml = retrieveJailedPlayer(uuid);
        File playerFile = new File(playerDataFolder, uuid + ".yml");

        OfflinePlayer player = main.getServer().getOfflinePlayer(uuid);

        if (main.perm != null && main.getConfig().getBoolean("changeGroup")) {
            String group = yaml.getString("group");
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
            Location lastLocation = (Location)yaml.get("lastlocation", backupLocation);

            if (lastLocation.equals(backupLocation))
                lastLocation = ((Player)player).getLocation();

            ((Player)player).teleport(lastLocation);
            yamlsOnlineJailedPlayers.remove(uuid);
            playerFile.delete();
            cachedJailedPlayersNameHashes.remove(Integer.valueOf(player.getName().toUpperCase().hashCode()));
        } else {
            if (yaml.getBoolean("unjailed"))
                return true;

            if (yaml.get("lastlocation", backupLocation).equals(backupLocation)) {
                yamlsOnlineJailedPlayers.remove(uuid);
                playerFile.delete();
                cachedJailedPlayersNameHashes.remove(Integer.valueOf(player.getName().toUpperCase().hashCode()));
            } else {
                yaml.set("unjailed", true);
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

    public void save() throws IOException {
        yamlJails.save(jailsFile);

        for (Map.Entry<UUID, YamlConfiguration> entry : yamlsOnlineJailedPlayers.entrySet()) {
            UUID k = entry.getKey();
            YamlConfiguration v = entry.getValue();
            try {
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

        yamlsOnlineJailedPlayers.clear();
        if (main.getConfig().getBoolean("offlineTime")) {
            File[] files = playerDataFolder.listFiles();
            if (files != null)
                for (File file : files)
                    yamlsOnlineJailedPlayers.put(UUID.fromString(file.getName().replace(".yml", "")), YamlConfiguration.loadConfiguration(file));
        } else {
            for (Player player : main.getServer().getOnlinePlayers()) {
                if (isPlayerJailed(player.getUniqueId()))
                    yamlsOnlineJailedPlayers.put(player.getUniqueId(), retrieveJailedPlayer(player.getUniqueId()));
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
        for (Map.Entry<UUID, YamlConfiguration> entry : yamlsOnlineJailedPlayers.entrySet()) {
            UUID k = entry.getKey();
            YamlConfiguration v = entry.getValue();
            Location loc = ((Location)v.get("lastlocation", backupLocation));
            boolean unjailed = v.getBoolean("unjailed");

            if (loc.equals(backupLocation) &&
                unjailed) {
                yamlsOnlineJailedPlayers.remove(k);
                new File(playerDataFolder, k + ".yml").delete();
                cachedJailedPlayersNameHashes.remove(Integer.valueOf(v.getString("name", "").toUpperCase().hashCode()));
            } else if (loc.equals(backupLocation))
                continue;

            int secondsLeft = v.getInt("secondsleft");
            if (secondsLeft <= 0 || unjailed)
                removeJailedPlayer(k);
            else
                v.set("secondsleft", --secondsLeft);
        }
    }

    private void upgradeDataFiles() throws IOException {
        File oldPlayerDataFile = new File(this.main.getDataFolder(), "jailed_players.yml");
        if (oldPlayerDataFile.exists()) {
            YamlConfiguration oldPlayerData = YamlConfiguration.loadConfiguration(oldPlayerDataFile);
            for (String key : oldPlayerData.getConfigurationSection("players").getKeys(false)) {
                ConfigurationSection section = oldPlayerData.getConfigurationSection("players." + key);

                YamlConfiguration newPlayerData = new YamlConfiguration();
                newPlayerData.set("uuid", key);
                newPlayerData.set("name", section.getString("name"));
                newPlayerData.set("jail", section.getString("jail"));
                newPlayerData.set("secondsleft", section.getInt("secondsLeft"));
                newPlayerData.set("unjailed", section.getBoolean("unjailed"));

                String w = section.getString("lastlocation.world");
                Location l = new Location(main.getServer()
                                              .getWorld(w != null ? w : "world"),
                                          section.getDouble("lastlocation.x"),
                                          section.getDouble("lastlocation.y"),
                                          section.getDouble("lastlocation.z"),
                                          (float)section.getDouble("lastlocation.yaw"),
                                          (float)section.getDouble("lastlocation.pitch"));
                newPlayerData.set("lastlocation", l);

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
