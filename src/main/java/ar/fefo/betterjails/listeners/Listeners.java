package ar.fefo.betterjails.listeners;

import ar.fefo.betterjails.Main;
import ar.fefo.betterjails.utils.Jail;
import ar.fefo.betterjails.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class Listeners implements Listener {
    private Main main;

    public Listeners(Main main) { this.main = main; }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        YamlConfiguration jailedPlayer = main.dataHandler.retrieveJailedPlayer(uuid);
        if (jailedPlayer != null) {
            if (!jailedPlayer.getBoolean("unjailed") && !player.hasPermission("betterjails.jail.exempt")) {
                main.dataHandler.loadJailedPlayer(uuid, jailedPlayer);
                try {
                    String jailName = jailedPlayer.getString("jail");
                    if (jailName != null)
                        main.dataHandler.addJailedPlayer(player, jailName, null, jailedPlayer.getInt("secondsleft"));
                    else
                        main.dataHandler.addJailedPlayer(player, main.dataHandler.getJails().values().iterator().next().getName(), null, jailedPlayer.getInt("secondsleft"));

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                main.dataHandler.removeJailedPlayer(uuid);
            }
        }

        if (player.hasPermission("betterjails.receivebroadcast"))
            Bukkit.getScheduler().runTaskLater(main, () ->
                    new UpdateChecker(main, 76001).getVersion(version -> {
                        if (!main.getDescription().getVersion().equalsIgnoreCase(version))
                            player.sendMessage("§7[§bBetterJails§7] §3New version §b" + version + " §3for §bBetterJails §3available.");
                    }), 100);
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        YamlConfiguration jailedPlayer = main.dataHandler.retrieveJailedPlayer(uuid);
        if (jailedPlayer != null) {
            try {
                jailedPlayer.save(new File(main.dataHandler.playerDataFolder, uuid + ".yml"));
                if (!main.getConfig().getBoolean("offlineTime"))
                    main.dataHandler.unloadJailedPlayer(uuid);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerRespawn(@NotNull PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskLater(main, () -> {
            YamlConfiguration jailedPlayer = main.dataHandler.retrieveJailedPlayer(uuid);
            if (jailedPlayer != null) {
                Jail jail = main.dataHandler.getJail(jailedPlayer.getString("jail"));
                if (jail != null)
                    player.teleport(jail.getLocation());
                else {
                    player.teleport(main.dataHandler.getJails().values().iterator().next().getLocation());
                    Bukkit.getLogger().log(Level.WARNING, "Value " + jailedPlayer.getString("jail") + " for option jail on jailed played " + uuid + " is INCORRECT!");
                    Bukkit.getLogger().log(Level.WARNING, "That jail does not exist!");
                    Bukkit.getLogger().log(Level.WARNING, "Teleporting player to jail " + main.dataHandler.getJails().values().iterator().next().getName() + "!");
                }
            }
        }, 1);
    }
}
