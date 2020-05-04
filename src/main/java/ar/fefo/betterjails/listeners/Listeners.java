package ar.fefo.betterjails.listeners;

import ar.fefo.betterjails.Main;
import ar.fefo.betterjails.utils.Jail;
import ar.fefo.betterjails.utils.UpdateChecker;
import com.earth2me.essentials.User;
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

public class Listeners implements Listener {
    private final Main main;

    public Listeners(Main main) { this.main = main; }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (main.dataHandler.isPlayerJailed(uuid)) {
            YamlConfiguration jailedPlayer = main.dataHandler.retrieveJailedPlayer(uuid);
            if (!jailedPlayer.getBoolean("unjailed") && !player.hasPermission("betterjails.jail.exempt")) {
                main.dataHandler.loadJailedPlayer(uuid, jailedPlayer);
                try {
                    String jailName = jailedPlayer.getString("jail");
                    if (jailName != null)
                        main.dataHandler.addJailedPlayer(player, jailName, null, main.dataHandler.getSecondsLeft(uuid, 0));
                    else
                        main.dataHandler.addJailedPlayer(player, main.dataHandler.getJails().values().iterator().next().getName(), null, main.dataHandler.getSecondsLeft(uuid, 0));

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                main.dataHandler.removeJailedPlayer(uuid);
            }
        }

        if (player.hasPermission("betterjails.receivebroadcast"))
            main.getServer().getScheduler().runTaskLater(main, () ->
                    new UpdateChecker(main, 76001).getVersion(version -> {
                        if (!main.getDescription().getVersion().equalsIgnoreCase(version.substring(1)))
                            player.sendMessage("§7[§bBetterJails§7] §3New version §b" + version + " §3for §bBetterJails §3available.");
                    }), 100);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (main.dataHandler.isPlayerJailed(uuid)) {
            main.dataHandler.updateSecondsLeft(uuid);
            YamlConfiguration jailedPlayer = main.dataHandler.retrieveJailedPlayer(uuid);
            try {
                jailedPlayer.save(new File(main.dataHandler.playerDataFolder, uuid + ".yml"));
                if (!main.getConfig().getBoolean("offlineTime")) {
                    main.dataHandler.unloadJailedPlayer(uuid);
                    if (main.ess != null) {
                        User user = main.ess.getUser(uuid);
                        user.setJailTimeout(0);
                        user.setJailed(true);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerRespawn(@NotNull PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        main.getServer().getScheduler().runTaskLater(main, () -> {
            if (main.dataHandler.isPlayerJailed(uuid)) {
                YamlConfiguration jailedPlayer = main.dataHandler.retrieveJailedPlayer(uuid);
                Jail jail = main.dataHandler.getJail(jailedPlayer.getString("jail"));
                if (jail != null)
                    player.teleport(jail.getLocation());
                else {
                    player.teleport(main.dataHandler.getJails().values().iterator().next().getLocation());
                    main.getLogger().warning("Value " + jailedPlayer.getString("jail") + " for option jail on jailed played " + uuid + " is INCORRECT!");
                    main.getLogger().warning("That jail does not exist!");
                    main.getLogger().warning("Teleporting player to jail " + main.dataHandler.getJails().values().iterator().next().getName() + "!");
                }
            }
        }, 1);
    }
}
