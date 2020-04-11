package ar.fefo.betterjails.utils;

import ar.fefo.betterjails.Main;
import com.earth2me.essentials.User;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DataHandler {
    public File playerDataFolder;
    private Location backupLocation;
    private Main main;
    private File jailsFile;
    private YamlConfiguration yamlJails;
    private HashMap<String, Jail> jails = new HashMap<>();
    private HashMap<UUID, YamlConfiguration> yamlsOnlineJailedPlayers = new HashMap<>();

    public DataHandler(@NotNull Main main) throws IOException {
        this.main = main;

        String world = main.getConfig().getString("backupLocation.world");
        if (world == null) {
            world = main.getServer().getWorlds().get(0).getName();
            Bukkit.getLogger().log(Level.WARNING, "Error in config.yml: Couldn't retrieve backupLocation.world");
            Bukkit.getLogger().log(Level.WARNING, "Choosing world \"" + world + "\" by default.");
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
        if (!playerDataFolder.exists()) {
            if (!playerDataFolder.mkdirs()) {
                throw new IOException("Could not create player data folder.");
            }
        }

        upgradeDataFiles();
        alertNewConfigAvailable();

        if (main.getConfig().getBoolean("offlineTime")) {
            File[] files = playerDataFolder.listFiles();
            if (files != null)
                for (File file : files)
                    yamlsOnlineJailedPlayers.put(UUID.fromString(file.getName().replace(".yml", "")), YamlConfiguration.loadConfiguration(file));
        }
    }

    private void loadJails() throws IOException {
        jailsFile.createNewFile();
        yamlJails = YamlConfiguration.loadConfiguration(jailsFile);

        for (String key : yamlJails.getKeys(false))
            jails.put(key, new Jail(key, (Location)yamlJails.get(key)));
    }

    @Nullable
    public YamlConfiguration retrieveJailedPlayer(UUID uuid) {
        if (yamlsOnlineJailedPlayers.containsKey(uuid)) {
            return yamlsOnlineJailedPlayers.get(uuid);
        } else {
            FilenameFilter filter = (dir, name) -> name.equalsIgnoreCase(uuid + ".yml");
            File[] matchingFiles = playerDataFolder.listFiles(filter);
            if (matchingFiles == null || matchingFiles.length == 0)
                return null;
            return YamlConfiguration.loadConfiguration(matchingFiles[0]);
        }
    }

    public void loadJailedPlayer(UUID uuid, YamlConfiguration jailedPlayer) { yamlsOnlineJailedPlayers.put(uuid, jailedPlayer); }

    public void unloadJailedPlayer(UUID uuid) { yamlsOnlineJailedPlayers.remove(uuid); }

    public HashMap<String, Jail> getJails() { return jails; }

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
        boolean isPlayerJailed = yaml != null;

        if (!jailExists)
            return false;

        if (yaml == null)
            yaml = new YamlConfiguration();
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
            YamlConfiguration finalYaml = yaml;
            Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
                @Override
                public void run() {
                    if (main.perm != null && (finalYaml.getString("group") == null || finalYaml.getBoolean("unjailed"))) {
                        String group = main.perm.getPrimaryGroup(null, player);
                        finalYaml.set("group", group);
                        try {
                            finalYaml.save(new File(playerDataFolder, player.getUniqueId().toString() + ".yml"));
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

        if (main.ess != null) {
            User user = main.ess.getUser(player.getUniqueId());
            if (user != null)
                user.setJailed(true);
        }

        return true;
    }

    public boolean removeJailedPlayer(@NotNull UUID uuid) {
        FilenameFilter filter = (dir, name) -> name.equalsIgnoreCase(uuid + ".yml");
        File[] files = playerDataFolder.listFiles(filter);
        if (files == null || files.length == 0)
            return false;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(files[0]);

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        if (yaml.getBoolean("unjailed") && !player.isOnline())
            return true;

        if (main.perm != null && main.getConfig().getBoolean("changeGroup")) {
            String group = yaml.getString("group");
            Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
                @Override
                public void run() {
                    main.perm.playerRemoveGroup(null, player, main.prisonerGroup);
                    main.perm.playerAddGroup(null, player, group != null ? group : "default");
                }
            });
        }

        if (player.isOnline()) {
            Location lastLocation = (Location)yaml.get("lastlocation");

            if (lastLocation == null)
                lastLocation = backupLocation;
            else if (lastLocation.equals(backupLocation))
                lastLocation = ((Player)player).getLocation();

            ((Player)player).teleport(lastLocation);
            new File(playerDataFolder, uuid + ".yml").delete();
            yamlsOnlineJailedPlayers.remove(uuid);
        } else {
            yaml.set("unjailed", true);
            try {
                yaml.save(files[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (main.ess != null) {
            User user = main.ess.getUser(player.getUniqueId());
            if (user != null)
                user.setJailed(false);
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
                Bukkit.getLogger().log(Level.SEVERE, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void reload() {
        main.reloadConfig();

        String unjailWorld = main.getConfig().getString("backupLocation.world");
        if (unjailWorld == null) {
            unjailWorld = main.getServer().getWorlds().get(0).getName();
            Bukkit.getLogger().log(Level.WARNING, "Error in config.yml: Couldn't retrieve backupLocation.world");
            Bukkit.getLogger().log(Level.WARNING, "Choosing world \"" + unjailWorld + "\" by default.");
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
            for (Player player : Bukkit.getOnlinePlayers()) {
                YamlConfiguration yaml = retrieveJailedPlayer(player.getUniqueId());
                if (yaml != null)
                    yamlsOnlineJailedPlayers.put(player.getUniqueId(), yaml);
            }
        }

        if (main.getConfig().getBoolean("changeGroup")) {
            if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
                if (rsp == null) {
                    main.getLogger().log(Level.SEVERE, "There was an error while hooking with Vault!");
                    main.getLogger().log(Level.SEVERE, "Group changing feature will not be used!");
                } else {
                    main.perm = rsp.getProvider();
                    main.prisonerGroup = main.getConfig().getString("prisonerGroup");
                }
            } else {
                main.getLogger().log(Level.WARNING, "Option \"changeGroup\" in config.yml is set to true, yet Vault wasn't found!");
                main.getLogger().log(Level.WARNING, "Group changing feature will not be used!");
            }
        }
    }

    public void timer() {
        for (Map.Entry<UUID, YamlConfiguration> entry : yamlsOnlineJailedPlayers.entrySet()) {
            UUID k = entry.getKey();
            YamlConfiguration v = entry.getValue();
            if (v.get("lastlocation", backupLocation).equals(backupLocation) &&
                v.getBoolean("unjailed")) {
                yamlsOnlineJailedPlayers.remove(k);
                new File(playerDataFolder, k + ".yml").delete();
            } else if (v.get("lastlocation", backupLocation).equals(backupLocation))
                continue;
            int secondsLeft = v.getInt("secondsleft");
            if (secondsLeft <= 0 || v.getBoolean("unjailed"))
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
            Bukkit.getLogger().log(Level.WARNING, "New config.yml found!");
            Bukkit.getLogger().log(Level.WARNING, "Make sure to make a backup of your settings before deleting your current config.yml!");
        }
    }
}
