//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2024 emilyy-dev
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
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisoner;
import io.github.emilyydev.betterjails.util.DataHandler;
import io.github.emilyydev.betterjails.util.FileIO;
import io.github.emilyydev.betterjails.util.Util;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginManager;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerListeners implements Listener {

  public static PlayerListeners create(final BetterJailsPlugin plugin) {
    return new PlayerListeners(plugin);
  }

  private final BetterJailsPlugin plugin;
  private final Logger logger;

  private PlayerListeners(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
    this.logger = plugin.getLogger();
  }

  public void register() {
    final PluginManager pluginManager = this.plugin.getServer().getPluginManager();
    pluginManager.registerEvent(
        PlayerSpawnLocationEvent.class, this, EventPriority.HIGH,
        (l, e) -> playerSpawn((PlayerSpawnLocationEvent) e), this.plugin
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

  private void playerSpawn(final PlayerSpawnLocationEvent event) {
    final Player player = event.getPlayer();
    final UUID uuid = player.getUniqueId();

    ApiPrisoner prisoner = this.plugin.dataHandler().getPrisoner(uuid);
    if (prisoner != null) {
      final Location backupLocation = this.plugin.configuration().backupLocation().mutable();
      final Location lastLocation = prisoner.lastLocation().mutable();
      if (prisoner.released() || player.hasPermission("betterjails.jail.exempt")) {
        // The player has been released, put them back where they were
        // TODO(rymiel): backupLocation continues to be problematic
        if (!lastLocation.equals(backupLocation)) {
          event.setSpawnLocation(lastLocation);
        }

        this.plugin.dataHandler().releaseJailedPlayer(player, Util.NIL_UUID, null, false);
      } else {
        if (prisoner.incomplete()) {
          prisoner = prisoner.withLastLocation(ImmutableLocation.copyOf(player.getLocation()));
        }

        prisoner = prisoner.withTimeRunning();
        this.plugin.dataHandler().savePrisoner(prisoner);
        event.setSpawnLocation(prisoner.jail().location().mutable());
      }
    }

    if (player.hasPermission("betterjails.receivebroadcast") && !this.plugin.getDescription().getVersion().endsWith("-SNAPSHOT")) {
      this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
          Util.checkVersion(this.plugin, version -> {
            if (!this.plugin.getDescription().getVersion().equals(version)) {
              player.sendMessage(Util.color("&7[&bBetterJails&7] &3New version &b%s &3for &bBetterJails &3available.", version));
            }
          }), 100L);
    }
  }

  private void playerQuit(final PlayerQuitEvent event) {
    final Player player = event.getPlayer();
    final UUID uuid = player.getUniqueId();
    ApiPrisoner prisoner = this.plugin.dataHandler().getPrisoner(uuid);
    if (prisoner == null) {
      return;
    }

    if (!this.plugin.configuration().considerOfflineTime()) {
      prisoner = prisoner.withTimePaused();
      if (this.plugin.essentials != null) {
        final User user = this.plugin.essentials.getUser(uuid);
        user.setJailTimeout(0L);
        user.setJailed(true);
      }
    }

    this.plugin.dataHandler().savePrisoner(prisoner);
  }

  private void playerRespawn(final PlayerRespawnEvent event) {
    final Player player = event.getPlayer();
    final UUID uuid = player.getUniqueId();
    ApiPrisoner prisoner = this.plugin.dataHandler().getPrisoner(uuid);

    if (prisoner != null) {
      event.setRespawnLocation(prisoner.jail().location().mutable());
    }
  }

  private Jail getValidJail(final YamlConfiguration prisonerData, final String playerName, final UUID uuid) {
    final String jailName = prisonerData.getString(DataHandler.JAIL_FIELD);
    Jail jail = this.plugin.dataHandler().getJail(jailName);
    if (jail == null) {
      jail = this.plugin.dataHandler().getJails().values().iterator().next();
      prisonerData.set(DataHandler.JAIL_FIELD, jail.name());
      this.logger.log(Level.WARNING, "Jail {0} does not exist", jailName);
      this.logger.log(Level.WARNING, "Player {0}/{1} was attempted to relocate to {2}", new Object[]{playerName, uuid, jailName});
      this.logger.log(Level.WARNING, "Teleporting player to jail {0} instead", jail.name());
    }

    return jail;
  }
}
