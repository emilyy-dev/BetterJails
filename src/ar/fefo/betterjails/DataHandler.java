package ar.fefo.betterjails;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class DataHandler implements Listener {
    private Main main;

    private File jailsFile;
    private YamlConfiguration yamlJails;
    private ConfigurationSection jailsSection;

    private File jailedPlayersFile;
    private YamlConfiguration yamlJailedPlayers;
    private ConfigurationSection jailedPlayersSection;

    DataHandler(Main main) throws IOException {
        this.main = main;

        jailsFile = new File(this.main.getDataFolder() + File.separator + "jails.yml");
        if (!jailsFile.getParentFile().exists())
            if (!jailsFile.toPath().getParent().toFile().mkdirs())
                throw new IOException("Could not create jails folder.");
        loadJails();

        jailedPlayersFile = new File(this.main.getDataFolder() + File.separator + "jailed_players.yml");
        if (!jailedPlayersFile.getParentFile().exists())
            if (!jailedPlayersFile.toPath().getParent().toFile().mkdirs())
                throw new IOException("Could not create data folder.");
        loadJailedPlayers();
    }

    private void loadJails() throws IOException {
        if (jailsFile.createNewFile()) {
            InputStream istream = main.getResource("jails.yml");
            if (istream == null)
                throw new IOException("\"jails.yml\" could not be found in the plugin's resources.");
            OutputStream ostream = new FileOutputStream(jailsFile);
            byte[] buffer = new byte[istream.available()];
            istream.read(buffer);
            ostream.write(buffer);

            istream.close();
            ostream.close();
        }
        yamlJails = YamlConfiguration.loadConfiguration(jailsFile);

        jailsSection = yamlJails.getConfigurationSection("jails");
    }

    private void loadJailedPlayers() throws IOException {
        if (jailedPlayersFile.createNewFile()) {
            InputStream istream = main.getResource("jailed_players.yml");
            if (istream == null)
                throw new IOException("\"jailed_players.yml\" could not be found in the plugin's resources.");
            OutputStream ostream = new FileOutputStream(jailedPlayersFile);
            byte[] buffer = new byte[istream.available()];
            istream.read(buffer);
            ostream.write(buffer);

            istream.close();
            ostream.close();
        }
        yamlJailedPlayers = YamlConfiguration.loadConfiguration(jailedPlayersFile);

        jailedPlayersSection = yamlJailedPlayers.getConfigurationSection("players");
    }

    List<Jail> getJails() {
        List<Jail> jails = new ArrayList<>();

        if (jailsSection == null)
            jailsSection = yamlJails.createSection("jails");
        for (String key : jailsSection.getKeys(false)) {
            Location location = jailsSection.getLocation(key);
            jails.add(new Jail(key, location));
        }
        return jails;
    }

    Jail getJail(String name) {
        for (Jail jail : getJails())
            if (jail.getName().equalsIgnoreCase(name))
                return jail;
        return null;
    }

    void addJail(String name, Location location) throws IOException {
        if (jailsSection == null)
            jailsSection = yamlJails.createSection("jails");
        jailsSection.set(name, location);
        yamlJails.save(jailsFile);
    }

    void removeJail(String name) throws IOException {
        jailsSection.set(name, null);
        yamlJails.save(jailsFile);
    }

    void addJailedPlayer(@NotNull OfflinePlayer player,
                         @NotNull String jail,
                         int seconds,
                         boolean saveFile) throws IOException {
        Location lastLocation = new Location(main.getServer().getWorlds().get(0),
                0.0,
                0.0,
                0.0,
                0.0f,
                0.0f);

        if (main.ess != null)
            main.ess.getUser(player.getUniqueId()).setJailed(true);

        if (jailedPlayersSection == null)
            jailedPlayersSection = yamlJailedPlayers.createSection("players");
        jailedPlayersSection.set(player.getUniqueId() + ".name", player.getName());
        jailedPlayersSection.set(player.getUniqueId() + ".jail", jail);
        jailedPlayersSection.set(player.getUniqueId() + ".secondsLeft", seconds);
        jailedPlayersSection.set(player.getUniqueId() + ".unjailed", false);
        if (jailedPlayersSection.contains(player.getUniqueId() + ".lastlocation")) {
            if (jailedPlayersSection.getLocation(player.getUniqueId() + ".lastlocation").equals(lastLocation)) {
                jailedPlayersSection.set(player.getUniqueId() + ".lastlocation", ((Player) player).getLocation());
            }
        } else {
            lastLocation = player.isOnline() ? ((Player) player).getLocation() : lastLocation;
            jailedPlayersSection.set(player.getUniqueId() + ".lastlocation", lastLocation);
        }

        if (player.isOnline())
            ((Player) player).teleport(getJail(jail).getLocation());

        if (saveFile)
            yamlJailedPlayers.save(jailedPlayersFile);
    }

    boolean removeJailedPlayer(@NotNull UUID uuid, boolean saveFile) throws IOException {
        if (!jailedPlayersSection.contains(uuid.toString()))
            return false;

        Location lastLocation = jailedPlayersSection.getLocation(uuid + ".lastlocation");

        OfflinePlayer player = main.getServer().getOfflinePlayer(uuid);
        if (player.isOnline()) {
            ((Player) player).teleport(lastLocation);
            if (main.ess != null)
                main.ess.getUser(uuid).setJailed(false);

            jailedPlayersSection.set(uuid.toString(), null);
        } else {
            Location zeroZero = new Location(main.getServer().getWorlds().get(0),
                    0.0,
                    0.0,
                    0.0,
                    0.0f,
                    0.0f);

            if (lastLocation.equals(zeroZero))
                jailedPlayersSection.set(uuid.toString(), null);
            else
                jailedPlayersSection.set(uuid + ".unjailed", true);
        }
        if (saveFile)
            yamlJailedPlayers.save(jailedPlayersFile);
        return true;
    }

    void save() throws IOException {
        yamlJails.save(jailsFile);
        yamlJailedPlayers.save(jailedPlayersFile);
    }

    void reload() {
        yamlJails = YamlConfiguration.loadConfiguration(jailsFile);
        jailsSection = yamlJails.getConfigurationSection("jails");
        yamlJailedPlayers = YamlConfiguration.loadConfiguration(jailedPlayersFile);
        jailedPlayersSection = yamlJailedPlayers.getConfigurationSection("players");

        for (Player onlinePlayer : main.getServer().getOnlinePlayers()) {
            UUID uuid = onlinePlayer.getUniqueId();
            if (jailedPlayersSection.contains(uuid.toString()) &&
                    onlinePlayer.hasPermission("betterjails.jail.exempt")) {
                jailedPlayersSection.set(uuid.toString(), null);
                if (main.ess != null)
                    main.ess.getUser(uuid).setJailed(false);
                continue;
            }

            if (jailedPlayersSection.contains(uuid.toString()) &&
                    jailedPlayersSection.getBoolean(uuid + ".unjailed")) {
                try {
                    removeJailedPlayer(uuid, false);
                } catch (IOException ex) {
                    main.getServer().getConsoleSender().sendMessage("§4Fatal error! Could not saved updated jailed_players.yml");
                    ex.printStackTrace();
                }
            } else if (jailedPlayersSection.contains(uuid.toString()) &&
                    !jailedPlayersSection.getBoolean(uuid + ".unjailed")) {
                try {
                    addJailedPlayer(main.getServer().getOfflinePlayer(uuid),
                            Objects.requireNonNull(jailedPlayersSection.getString(uuid + ".jail")),
                            jailedPlayersSection.getInt(uuid + ".secondsLeft"),
                            false);
                } catch (IOException ex) {
                    main.getServer().getConsoleSender().sendMessage("§4Fatal error! Could not saved updated jailed_players.yml");
                    ex.printStackTrace();
                }
            }
        }
    }

    void timer() {
        if (jailedPlayersSection == null)
            jailedPlayersSection = yamlJailedPlayers.createSection("players");
        for (String key : jailedPlayersSection.getKeys(false)) {
            if (main.getServer().getOfflinePlayer(UUID.fromString(key)).isOnline()) {
                int secondsLeft = jailedPlayersSection.getInt(key + ".secondsLeft");
                jailedPlayersSection.set(key + ".secondsLeft", --secondsLeft);
                if (secondsLeft <= 0) {
                    try {
                        removeJailedPlayer(UUID.fromString(key), true);
                    } catch (IOException e) {
                        main.getLogger().log(Level.SEVERE, "Could not updated jailed_players.yml!");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @EventHandler
    void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (jailedPlayersSection.contains(uuid.toString()) &&
                player.hasPermission("betterjails.jail.exempt")) {
            jailedPlayersSection.set(uuid.toString(), null);
            if (main.ess != null)
                main.ess.getUser(uuid).setJailed(false);
            return;
        }

        if (jailedPlayersSection.contains(uuid.toString()) &&
                jailedPlayersSection.getBoolean(uuid + ".unjailed")) {
            try {
                removeJailedPlayer(uuid, true);
            } catch (IOException ex) {
                main.getServer().getConsoleSender().sendMessage("§4Fatal error! Could not saved updated jailed_players.yml");
                ex.printStackTrace();
            }
        } else if (jailedPlayersSection.contains(uuid.toString()) &&
                !jailedPlayersSection.getBoolean(uuid + ".unjailed")) {
            try {
                addJailedPlayer(main.getServer().getOfflinePlayer(uuid),
                        Objects.requireNonNull(jailedPlayersSection.getString(uuid + ".jail")),
                        jailedPlayersSection.getInt(uuid + ".secondsLeft"),
                        true);
            } catch (IOException ex) {
                main.getServer().getConsoleSender().sendMessage("§4Fatal error! Could not saved updated jailed_players.yml");
                ex.printStackTrace();
            }
        }
    }

    @EventHandler
    void onPlayerRespawn(@NotNull PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (jailedPlayersSection.contains(uuid.toString())) {
            Location jailLocation = getJail(jailedPlayersSection.getString(uuid + ".jail")).getLocation();
            main.getServer().getScheduler().runTaskLater(main,
                    () -> player.teleport(jailLocation),
                    1);
        }
    }
}
