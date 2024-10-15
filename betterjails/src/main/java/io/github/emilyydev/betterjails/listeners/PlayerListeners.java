//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2024 emilyy-dev
// Copyright (c) 2024 Emilia Kond
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
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.UpdateChecker;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisoner;
import io.github.emilyydev.betterjails.config.SubCommandsConfiguration;
import io.github.emilyydev.betterjails.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.UUID;

public final class PlayerListeners implements Listener {

  private static final Logger LOGGER = LoggerFactory.getLogger("BetterJails");

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

    ApiPrisoner prisoner = this.plugin.prisonerData().getPrisoner(uuid);
    if (prisoner != null) {
      if (prisoner.released() || player.hasPermission("betterjails.jail.exempt")) {
        // The player has been released...
        // put them back where they were if there is no release location, and at the release location otherwise
        final ImmutableLocation lastLocation = prisoner.lastLocationNullable();
        final ImmutableLocation releaseLocation = prisoner.jail().releaseLocation();
        if (releaseLocation != null) {
          event.setSpawnLocation(releaseLocation.mutable());
        } else if (lastLocation != null) {
          event.setSpawnLocation(lastLocation.mutable());
        }

        this.plugin.prisonerData().releaseJailedPlayer(player, Util.NIL_UUID, null, false);
      } else {
        if (prisoner.unknownLastLocation()) {
          prisoner = prisoner.withLastLocation(ImmutableLocation.copyOf(player.getLocation()));

          // Must be delayed by 1 tick, otherwise player.isOnline() is false and stuff explodes
          final String jailedBy = prisoner.jailedBy() == null ? "" : prisoner.jailedBy();
          this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            final SubCommandsConfiguration.SubCommands subCommands = this.plugin.subCommands().onJail();
            subCommands.executeAsPrisoner(this.plugin.getServer(), player, jailedBy);
            subCommands.executeAsConsole(this.plugin.getServer(), player, jailedBy);
          }, 1);
        }

        prisoner = prisoner.withTimeRunning();
        this.plugin.prisonerData().savePrisoner(prisoner).exceptionally(error -> {
          LOGGER.error("An error occurred saving data for prisoner {}", uuid, error);
          return null;
        });
        event.setSpawnLocation(prisoner.jail().location().mutable());
      }
    }

    if (player.hasPermission("betterjails.receivebroadcast")) {
      this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
          UpdateChecker.fetchRemoteVersion(this.plugin).thenAccept(version -> {
            if (!this.plugin.getDescription().getVersion().equals(version)) {
              player.sendMessage(Util.color("&7[&bBetterJails&7] &3New version &b%s &3for &bBetterJails &3available.", version));
            }
          }), 100L);
    }
  }

  private void playerQuit(final PlayerQuitEvent event) {
    final Player player = event.getPlayer();
    final UUID uuid = player.getUniqueId();
    ApiPrisoner prisoner = this.plugin.prisonerData().getPrisoner(uuid);
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

    this.plugin.prisonerData().savePrisoner(prisoner).exceptionally(error -> {
      LOGGER.error("An error occurred saving data for prisoner {}", uuid, error);
      return null;
    });
  }

  private void playerRespawn(final PlayerRespawnEvent event) {
    final Player player = event.getPlayer();
    final UUID uuid = player.getUniqueId();
    final ApiPrisoner prisoner = this.plugin.prisonerData().getPrisoner(uuid);

    if (prisoner != null) {
      event.setRespawnLocation(prisoner.jail().location().mutable());
    }
  }
}
