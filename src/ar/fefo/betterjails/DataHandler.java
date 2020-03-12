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
            Location location = new Location(
                    main.getServer().getWorld(Objects.requireNonNull(jailsSection.getString(key + ".world"))),
                    jailsSection.getDouble(key + ".x"),
                    jailsSection.getDouble(key + ".y"),
                    jailsSection.getDouble(key + ".z"),
                    (float)jailsSection.getDouble(key + ".yaw"),
                    (float)jailsSection.getDouble(key + ".pitch"));
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
        jailsSection.set(name + ".x", location.getX());
        jailsSection.set(name + ".y", location.getY());
        jailsSection.set(name + ".z", location.getZ());
        jailsSection.set(name + ".yaw", location.getYaw());
        jailsSection.set(name + ".pitch", location.getPitch());
        jailsSection.set(name + ".world", Objects.requireNonNull(location.getWorld()).getName());
        yamlJails.save(jailsFile);
    }

    void removeJail(String name) throws IOException {
        jailsSection.set(name, null);
        yamlJails.save(jailsFile);
    }

    void addJailedPlayer(@NotNull OfflinePlayer player,
                         @NotNull String jail,
                         int seconds) throws IOException {
        Location lastLocation = new Location(main.getServer().getWorlds().get(0),
                                             0.0,
                                             0.0,
                                             0.0,
                                             0.0f,
                                             0.0f);
        if (main.ess != null)
            main.ess.getUser(player.getUniqueId()).setJailed(true);
        if (player.isOnline()) {
            lastLocation = ((Player)player).getLocation();
            ((Player)player).teleport(getJail(jail).getLocation());
        }

        if (jailedPlayersSection == null)
            jailedPlayersSection = yamlJailedPlayers.createSection("players");
        jailedPlayersSection.set(player.getUniqueId() + ".name", player.getName());
        jailedPlayersSection.set(player.getUniqueId() + ".jail", jail);
        jailedPlayersSection.set(player.getUniqueId() + ".secondsLeft", seconds);
        jailedPlayersSection.set(player.getUniqueId() + ".unjailed", false);
        jailedPlayersSection.set(player.getUniqueId() + ".x", lastLocation.getX());
        jailedPlayersSection.set(player.getUniqueId() + ".y", lastLocation.getY());
        jailedPlayersSection.set(player.getUniqueId() + ".z", lastLocation.getZ());
        jailedPlayersSection.set(player.getUniqueId() + ".yaw", lastLocation.getYaw());
        jailedPlayersSection.set(player.getUniqueId() + ".pitch", lastLocation.getPitch());
        jailedPlayersSection.set(player.getUniqueId() + ".world", Objects.requireNonNull(lastLocation.getWorld()).getName());

        yamlJailedPlayers.save(jailedPlayersFile);
    }

    void removeJailedPlayer(UUID uuid) throws IOException {
        OfflinePlayer player = main.getServer().getOfflinePlayer(uuid);
        if (player.isOnline()) {
            Location lastLocation = new Location(
                    main.getServer().getWorld(Objects.requireNonNull(jailedPlayersSection.getString(uuid + ".world"))),
                    jailedPlayersSection.getDouble(uuid + ".x"),
                    jailedPlayersSection.getDouble(uuid + ".y"),
                    jailedPlayersSection.getDouble(uuid + ".z"),
                    (float)jailedPlayersSection.getDouble(uuid + ".yaw"),
                    (float)jailedPlayersSection.getDouble(uuid + ".pitch"));
            ((Player)player).teleport(lastLocation);
            if (main.ess != null)
                main.ess.getUser(uuid).setJailed(false);

            jailedPlayersSection.set(uuid.toString(), null);
        } else {
            jailedPlayersSection.set(uuid + ".unjailed", true);
        }
        yamlJailedPlayers.save(jailedPlayersFile);
    }

    void save() throws IOException {
        yamlJails.save(jailsFile);
        yamlJailedPlayers.save(jailedPlayersFile);
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
                        removeJailedPlayer(UUID.fromString(key));
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
                removeJailedPlayer(uuid);
            } catch (IOException ex) {
                main.getServer().getConsoleSender().sendMessage("§4Fatal error! Could not saved updated jailed_players.yml");
                ex.printStackTrace();
            }
        } else if (jailedPlayersSection.contains(uuid.toString()) &&
                   !jailedPlayersSection.getBoolean(uuid + ".unjailed")) {
            try {
                addJailedPlayer(main.getServer().getOfflinePlayer(uuid),
                                Objects.requireNonNull(jailedPlayersSection.getString(uuid + ".jail")),
                                jailedPlayersSection.getInt(uuid + ".secondsLeft"));
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
