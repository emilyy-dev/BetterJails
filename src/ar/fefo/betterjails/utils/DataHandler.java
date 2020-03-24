package ar.fefo.betterjails.utils;

import ar.fefo.betterjails.Main;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DataHandler implements Listener {
    public Location backupLocation;
    private Main main;
    private File jailsFile;
    private YamlConfiguration yamlJails;
    private HashMap<String, Jail> jails = new HashMap<>();
    private File playerDataFolder;
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
            jails.put(key, new Jail(key, yamlJails.getLocation(key)));
    }

    @Nullable
    private YamlConfiguration loadJailedPlayer(UUID uuid) {
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
                                   int secondsLeft) throws IOException {
        YamlConfiguration yaml = loadJailedPlayer(player.getUniqueId());
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
            Location lastLocation = yaml.getLocation("lastlocation");
            if (lastLocation == null) {
                yaml.set("lastlocation", backupLocation);
                lastLocation = backupLocation;
            }

            if (lastLocation.equals(backupLocation)) {
                yaml.set("lastlocation", ((Player)player).getLocation());

                if (main.getConfig().getBoolean("changeGroup")) {
                    User user = main.lp.getUserManager().getUser(player.getUniqueId());
                    if (user != null) {
                        String group = user.getPrimaryGroup();
                        yaml.set("group", group);
                        user.data().remove(Node.builder("group." + group).build());
                        user.data().add(Node.builder("group." + main.prisonerGroup).value(true).build());
                        main.lp.getUserManager().saveUser(user);
                    }
                }
            }
            ((Player)player).teleport(jail.getLocation());
            yamlsOnlineJailedPlayers.put(player.getUniqueId(), yaml);
        }

        yaml.save(new File(playerDataFolder, player.getUniqueId().toString() + ".yml"));

        if (main.ess != null)
            main.ess.getUser(player.getUniqueId()).setJailed(true);

        return true;
    }

    public boolean removeJailedPlayer(@NotNull UUID uuid) {
        FilenameFilter filter = (dir, name) -> name.equalsIgnoreCase(uuid + ".yml");
        File[] files = playerDataFolder.listFiles(filter);
        if (files == null || files.length == 0)
            return false;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(files[0]);

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.isOnline()) {
            if (main.getConfig().getBoolean("changeGroup")) {
                User user = main.lp.getUserManager().getUser(player.getUniqueId());
                if (user != null) {
                    String group = yaml.getString("group");
                    user.data().remove(Node.builder("group." + main.prisonerGroup).build());
                    user.data().add(Node.builder("group." + (group != null ? group : "default")).value(true).build());
                    main.lp.getUserManager().saveUser(user);
                }
            }

            Location lastLocation = yaml.getLocation("lastlocation");

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

        if (main.ess != null)
            main.ess.getUser(player.getUniqueId()).setJailed(false);

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
            jails.put(key, new Jail(key, yamlJails.getLocation(key)));

        yamlsOnlineJailedPlayers.clear();
        if (main.getConfig().getBoolean("offlineTime")) {
            File[] files = playerDataFolder.listFiles();
            if (files != null)
                for (File file : files)
                    yamlsOnlineJailedPlayers.put(UUID.fromString(file.getName().replace(".yml", "")), YamlConfiguration.loadConfiguration(file));
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                YamlConfiguration yaml = loadJailedPlayer(player.getUniqueId());
                if (yaml != null)
                    yamlsOnlineJailedPlayers.put(player.getUniqueId(), yaml);
            }
        }

        main.lp = main.getConfig().getBoolean("changeGroup") ? LuckPermsProvider.get() : null;
        main.prisonerGroup = main.getConfig().getBoolean("changeGroup") ? main.getConfig().getString("prisonerGroup") : null;
    }

    public void timer() {
        for (Map.Entry<UUID, YamlConfiguration> entry : yamlsOnlineJailedPlayers.entrySet()) {
            UUID k = entry.getKey();
            YamlConfiguration v = entry.getValue();
            int secondsLeft = v.getInt("secondsleft");
            if (--secondsLeft <= 0 || v.getBoolean("unjailed"))
                removeJailedPlayer(k);
            else
                v.set("secondsleft", secondsLeft);
        }
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        YamlConfiguration jailedPlayer = loadJailedPlayer(uuid);
        if (jailedPlayer != null) {
            if (!jailedPlayer.getBoolean("unjailed") && !player.hasPermission("betterjails.jail.exempt")) {
                yamlsOnlineJailedPlayers.put(uuid, jailedPlayer);
                try {
                    String jailName = jailedPlayer.getString("jail");
                    if (jailName != null)
                        addJailedPlayer(player, jailName, jailedPlayer.getInt("secondsleft"));
                    else
                        addJailedPlayer(player, jails.values().iterator().next().getName(), jailedPlayer.getInt("secondsleft"));

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                removeJailedPlayer(uuid);
            }
        }

        if (player.hasPermission("betterjails.receivebroadcast"))
            Bukkit.getScheduler().runTaskLaterAsynchronously(main, () -> {
                try {
                    URL versionURL = new URL("https://pastebin.com/raw/xUrVFGqY");
                    ReadableByteChannel rbc = Channels.newChannel(versionURL.openStream());
                    ByteBuffer buffer = ByteBuffer.allocate(128);
                    if (rbc.isOpen()) {
                        rbc.read(buffer);
                        rbc.close();

                        JsonElement json = new JsonParser().parse(new JsonReader(new StringReader(new String(buffer.array()))));
                        String version = json.getAsJsonObject().get("version").getAsString();
                        if (version.compareTo(main.getDescription().getVersion()) > 0)
                            player.sendMessage("§7[§bBetterJails§7] §3New version §bv" + version + " §3for §bBetterJails §3available.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }, 20);
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        YamlConfiguration jailedPlayer = yamlsOnlineJailedPlayers.get(uuid);
        if (jailedPlayer != null) {
            try {
                jailedPlayer.save(new File(playerDataFolder, uuid + ".yml"));
                if (!main.getConfig().getBoolean("offlineTime"))
                    yamlsOnlineJailedPlayers.remove(uuid);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(@NotNull PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskLater(main, () -> {
            YamlConfiguration jailedPlayer = yamlsOnlineJailedPlayers.get(uuid);
            if (jailedPlayer != null) {
                Jail jail = getJail(jailedPlayer.getString("jail"));
                if (jail != null)
                    player.teleport(jail.getLocation());
                else {
                    player.teleport(jails.values().iterator().next().getLocation());
                    Bukkit.getLogger().log(Level.WARNING, "Value " + jailedPlayer.getString("jail") + " for option jail on jailed played " + uuid + " is INCORRECT!");
                    Bukkit.getLogger().log(Level.WARNING, "That jail does not exist!");
                    Bukkit.getLogger().log(Level.WARNING, "Teleporting player to jail " + jails.values().iterator().next().getName() + "!");
                }
            }
        }, 1);
    }
}
