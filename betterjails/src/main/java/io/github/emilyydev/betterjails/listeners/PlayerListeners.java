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

package io.github.emilyydev.betterjails.listeners;

import com.earth2me.essentials.User;
import com.github.fefo.betterjails.api.model.jail.Jail;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.util.Util;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

public class PlayerListeners implements Listener {

  public static PlayerListeners create(final BetterJailsPlugin plugin) {
    return new PlayerListeners(plugin);
  }

  private final BetterJailsPlugin plugin;

  private PlayerListeners(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
  }

  public void register() {
    final PluginManager pluginManager = this.plugin.getServer().getPluginManager();
    pluginManager.registerEvent(
        PlayerJoinEvent.class, this, EventPriority.HIGH,
        (l, e) -> playerJoin((PlayerJoinEvent) e), this.plugin
    );
    pluginManager.registerEvent(
        PlayerQuitEvent.class, this, EventPriority.NORMAL,
        (l, e) -> playerQuit((PlayerQuitEvent) e), this.plugin
    );
    pluginManager.registerEvent(
        PlayerRespawnEvent.class, this, EventPriority.HIGH,
        (l, e) -> playerRespawn((PlayerRespawnEvent) e), this.plugin
    );
  }

  private void playerJoin(final PlayerJoinEvent event) {
    final Player player = event.getPlayer();
    final UUID uuid = player.getUniqueId();

    if (this.plugin.dataHandler.isPlayerJailed(uuid)) {
      final YamlConfiguration jailedPlayer = this.plugin.dataHandler.retrieveJailedPlayer(uuid);
      if (!jailedPlayer.getBoolean("unjailed") && !player.hasPermission("betterjails.jail.exempt")) {
        this.plugin.dataHandler.loadJailedPlayer(uuid, jailedPlayer);
        try {
          final String jailName = jailedPlayer.getString("jail");
          if (jailName != null) {
            this.plugin.dataHandler.addJailedPlayer(player, jailName, null, this.plugin.dataHandler.getSecondsLeft(uuid, 0));
          } else {
            this.plugin.dataHandler.addJailedPlayer(
                player,
                this.plugin.dataHandler.getJails().values().iterator().next().name(),
                null,
                this.plugin.dataHandler.getSecondsLeft(uuid, 0)
            );
          }

        } catch (final IOException exception) {
          exception.printStackTrace();
        }
      } else {
        this.plugin.dataHandler.removeJailedPlayer(uuid);
      }
    }

    if (
        player.hasPermission("betterjails.receivebroadcast") &&
        !this.plugin.getDescription().getVersion().endsWith("-SNAPSHOT")
    ) {
      this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
          Util.checkVersion(this.plugin, 76001, version -> {
            if (!this.plugin.getDescription().getVersion().equalsIgnoreCase(version.substring(1))) {
              player.sendMessage(Util.color("&7[&bBetterJails&7] &3New version &b%s &3for &bBetterJails &3available.", version));
            }
          }), 100L);
    }
  }

  private void playerQuit(final PlayerQuitEvent event) {
    final Player player = event.getPlayer();
    final UUID uuid = player.getUniqueId();

    if (this.plugin.dataHandler.isPlayerJailed(uuid)) {
      this.plugin.dataHandler.updateSecondsLeft(uuid);
      final YamlConfiguration jailedPlayer = this.plugin.dataHandler.retrieveJailedPlayer(uuid);
      try {
        jailedPlayer.save(new File(this.plugin.dataHandler.playerDataFolder, uuid + ".yml"));
        if (!this.plugin.getConfig().getBoolean("offlineTime")) {
          this.plugin.dataHandler.unloadJailedPlayer(uuid);
          if (this.plugin.essentials != null) {
            final User user = this.plugin.essentials.getUser(uuid);
            user.setJailTimeout(0L);
            user.setJailed(true);
          }
        }
      } catch (final IOException exception) {
        exception.printStackTrace();
      }
    }
  }

  private void playerRespawn(final PlayerRespawnEvent event) {
    final Player player = event.getPlayer();
    final UUID uuid = player.getUniqueId();

    this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
      if (this.plugin.dataHandler.isPlayerJailed(uuid)) {
        final YamlConfiguration jailedPlayer = this.plugin.dataHandler.retrieveJailedPlayer(uuid);
        final Jail jail = this.plugin.dataHandler.getJail(jailedPlayer.getString("jail"));
        if (jail != null) {
          player.teleport(jail.location().mutable());
        } else {
          final Jail nextJail = this.plugin.dataHandler.getJails().values().iterator().next();
          player.teleport(nextJail.location().mutable());

          final Logger logger = this.plugin.getLogger();
          logger.warning("Value " + jailedPlayer.getString("jail") + " for option jail on jailed played " + uuid + " is INCORRECT!");
          logger.warning("That jail does not exist!");
          logger.warning("Teleporting player to jail " + nextJail.name() + "!");
        }
      }
    }, 1L);
  }
}
