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

package io.github.emilyydev.betterjails.data;

import com.earth2me.essentials.User;
import com.github.fefo.betterjails.api.event.prisoner.PlayerImprisonEvent;
import com.github.fefo.betterjails.api.event.prisoner.PrisonerReleaseEvent;
import com.github.fefo.betterjails.api.model.jail.Jail;
import com.github.fefo.betterjails.api.model.prisoner.Prisoner;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import com.google.common.collect.ImmutableSet;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisoner;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.SentenceExpiry;
import io.github.emilyydev.betterjails.config.BetterJailsConfiguration;
import io.github.emilyydev.betterjails.config.SubCommandsConfiguration;
import io.github.emilyydev.betterjails.interfaces.permission.PermissionInterface;
import io.github.emilyydev.betterjails.interfaces.storage.StorageAccess;
import io.github.emilyydev.betterjails.util.Teleport;
import io.github.emilyydev.betterjails.util.Util;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public final class PrisonerDataHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger("BetterJails");

  private final BetterJailsPlugin plugin;
  private final BetterJailsConfiguration config;
  private final SubCommandsConfiguration subCommands;
  private final StorageAccess storage;
  private final Server server;
  private final Map<UUID, ApiPrisoner> prisoners = new HashMap<>();

  private @Deprecated Location backupLocation;

  public PrisonerDataHandler(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
    this.config = plugin.configuration();
    this.subCommands = plugin.subCommands();
    this.server = plugin.getServer();
    this.storage = plugin.storageAccess();
  }

  public void load() {
    // TODO(v2): can't remove this yet
    this.backupLocation = this.config.backupLocation().mutable();
    this.prisoners.clear();
    loadPrisoners();
  }

  private void loadPrisoners() {
    try {
      this.prisoners.putAll(this.storage.loadPrisoners().get());
    } catch (final InterruptedException ex) {
      // bleh
    } catch (final ExecutionException ex) {
      throw new RuntimeException(ex.getCause());
    }
  }

  public Collection<Prisoner> getAllPrisoners() {
    return Collections.unmodifiableCollection(this.prisoners.values());
  }

  public boolean isPlayerJailed(final UUID uuid) {
    return this.prisoners.containsKey(uuid);
  }

  public ApiPrisoner getPrisoner(final UUID uuid) {
    return this.prisoners.get(uuid);
  }

  public void addJailedPlayer(
      final OfflinePlayer player,
      final Jail jail,
      final UUID jailer,
      final @Nullable String jailerName,
      final Duration sentenceDuration,
      final boolean teleport
  ) {
    final UUID prisonerUuid = player.getUniqueId();
    final ApiPrisoner existingPrisoner = this.prisoners.get(prisonerUuid);

    final boolean isPlayerOnline = player.isOnline();
    final boolean isPlayerJailed = existingPrisoner != null;
    final SentenceExpiry expiry;
    Location knownLastLocation = null;

    if (isPlayerJailed) {
      // The player is already jailed, and being put in a new jail. Since we don't want to put their last location
      // inside the previous jail, we use their existing last location.
      knownLastLocation = existingPrisoner.lastLocationMutable();
    }

    if (isPlayerOnline) {
      // The player is online! We can get their last location, if needed, and put them in jail immediately.
      final Player onlinePlayer = player.getPlayer();

      if (knownLastLocation == null) {
        knownLastLocation = onlinePlayer.getLocation();
      }

      if (teleport) {
        Teleport.teleportAsync(onlinePlayer, jail.location().mutable());
      }

      if (!isPlayerJailed) {
        // If the player is going to jail (not just moving between jails), run the onJail commands.
        final SubCommandsConfiguration.SubCommands subCommands = this.subCommands.onJail();
        subCommands.executeAsPrisoner(this.server, onlinePlayer, jailerName == null ? "" : jailerName);
        subCommands.executeAsConsole(this.server, onlinePlayer, jailerName == null ? "" : jailerName);
      }
    }

    if (isPlayerOnline || this.config.considerOfflineTime()) {
      // If the player is online or offline time is enabled, their remaining time will start ticking down immediately,
      // so we store the deadline of their release.
      expiry = SentenceExpiry.of(Instant.now().plus(sentenceDuration));
    } else {
      // Otherwise, the time doesn't start ticking until the player joins.
      expiry = SentenceExpiry.of(sentenceDuration);
    }

    if (this.plugin.essentials != null) {
      final User user = this.plugin.essentials.getUser(prisonerUuid);
      if (user != null) {
        user.setJailed(true);
        if (isPlayerOnline) {
          user.setJailTimeout(expiry.expiryDate().toEpochMilli());
        }
      }
    }

    // If we never got a last location for this player, it means we need to get it when they log in.
    final boolean unknownLocation = knownLastLocation == null;
    // TODO(v2): We have to set some location here
    final ImmutableLocation lastLocation = ImmutableLocation.copyOf(knownLastLocation == null ? this.backupLocation : knownLastLocation);
    final PermissionInterface permissionInterface = this.plugin.permissionInterface();

    final boolean groupsUnknown = existingPrisoner == null || existingPrisoner.primaryGroup() == null || existingPrisoner.released();

    final CompletionStage<? extends String> primaryGroupFuture = groupsUnknown
        ? permissionInterface.fetchPrimaryGroup(player).exceptionally(ex -> null)
        : CompletableFuture.completedFuture(existingPrisoner.primaryGroup());
    final CompletionStage<? extends Set<? extends String>> parentGroupsFuture = groupsUnknown
        ? permissionInterface.fetchParentGroups(player).thenApply(Function.<Set<? extends String>>identity()).exceptionally(ex -> ImmutableSet.of())
        : CompletableFuture.completedFuture(existingPrisoner.parentGroups());

    primaryGroupFuture.thenCombineAsync(parentGroupsFuture, (primaryGroup, parentGroups) -> {
      final ApiPrisoner prisoner = new ApiPrisoner(prisonerUuid, player.getName(), primaryGroup, parentGroups, jail, jailerName, expiry, sentenceDuration, lastLocation, unknownLocation);

      this.plugin.eventBus().post(PlayerImprisonEvent.class, prisoner);
      final CompletionStage<?> setGroupFuture = groupsUnknown
          ? permissionInterface.setPrisonerGroup(player, jailer, jailerName)
          : CompletableFuture.completedFuture(null);
      return setGroupFuture.exceptionally(ex -> {
        if (permissionInterface != PermissionInterface.NULL) {
          LOGGER.error("An error occurred changing the prisoner group for {}", prisonerUuid, ex);
        }

        return null;
      }).thenComposeAsync(v -> savePrisoner(prisoner), this.plugin).exceptionally(error -> {
        LOGGER.error("An error occurred saving prisoner data for {}", prisonerUuid, error);
        return null;
      });
    }, this.plugin);
  }

  public CompletableFuture<Void> savePrisoner(final ApiPrisoner prisoner) {
    this.prisoners.put(prisoner.uuid(), prisoner);

    return this.storage.savePrisoner(prisoner);
  }

  public void deletePrisonerFile(final ApiPrisoner prisoner) {
    try {
      this.storage.deletePrisoner(prisoner).get();
    } catch (final InterruptedException | ExecutionException ex) {
      LOGGER.error("Could not delete prisoner {}/{}", prisoner.uuid(), prisoner.name(), ex);
    }
  }

  public boolean releaseJailedPlayer(final OfflinePlayer player, final UUID source, final @Nullable String sourceName, final boolean teleport) {
    final UUID prisonerUuid = player.getUniqueId();
    final ApiPrisoner prisoner = this.prisoners.get(prisonerUuid);
    if (prisoner == null) {
      return false;
    } else {
      releasePrisoner(prisoner, player, source, sourceName, teleport);
      return true;
    }
  }

  public void releasePrisoner(
      ApiPrisoner prisoner,
      final OfflinePlayer player,
      final UUID source,
      final @Nullable String sourceName,
      final boolean teleport
  ) {
    final UUID prisonerUuid = player.getUniqueId();

    final PermissionInterface permissionInterface = this.plugin.permissionInterface();
    final Set<String> parentGroups = prisoner.parentGroups();
    permissionInterface.setParentGroups(player, parentGroups, source, sourceName)
        .whenComplete((ignored, ex) -> {
          if (ex != null && permissionInterface != PermissionInterface.NULL) {
            LOGGER.error("An error occurred setting back prisoner's parent groups for {} {}", prisonerUuid, parentGroups, ex);
          }
        });

    if (player.isOnline()) {
      // Player is online, we can teleport them out of jail right away and clear up all their data
      final Player online = Objects.requireNonNull(player.getPlayer());
      if (teleport) {
        final Location lastLocation = prisoner.lastLocationMutable();
        if (lastLocation != null) {
          Teleport.teleportAsync(online, lastLocation);
        }
      }

      this.prisoners.remove(prisonerUuid);
      deletePrisonerFile(prisoner);

      final SubCommandsConfiguration.SubCommands subCommands = this.subCommands.onRelease();
      subCommands.executeAsPrisoner(this.server, online, prisoner.jailedBy() == null ? "" : prisoner.jailedBy());
      subCommands.executeAsConsole(this.server, online, prisoner.jailedBy() == null ? "" : prisoner.jailedBy());
    } else {
      if (prisoner.released()) {
        // This player has already been released, don't need to do anything
        return;
      }

      if (prisoner.unknownLocation()) {
        // This prisoner has never joined during the entire duration of their sentence, meaning they are already where
        // they need to be, so we can immediately forget they exist.
        this.prisoners.remove(prisonerUuid);
        deletePrisonerFile(prisoner);
      } else {
        prisoner = prisoner.withReleased();
        savePrisoner(prisoner).exceptionally(error -> {
          LOGGER.error("An error occurred saving data for prisoner {}", prisonerUuid, error);
          return null;
        });
      }
    }

    if (this.plugin.essentials != null) {
      final User user = this.plugin.essentials.getUser(prisonerUuid);
      if (user != null && user.isJailed()) {
        user.setJailTimeout(0L);
        user.setJailed(false);
      }
    }

    this.plugin.eventBus().post(PrisonerReleaseEvent.class, prisoner);
  }

  public CompletableFuture<Void> save() {
    return this.storage.savePrisoners(this.prisoners);
  }

  public void timer() {
    final Iterator<Map.Entry<UUID, ApiPrisoner>> iterator = this.prisoners.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<UUID, ApiPrisoner> entry = iterator.next();
      final UUID key = entry.getKey();
      final ApiPrisoner prisoner = entry.getValue();
      final boolean released = prisoner.released();

      // This prisoner has no known location, but they're also released. This means they're exactly where they need to
      // be once they join, and so we can forget they exist.
      if (prisoner.unknownLocation()) {
        if (released) {
          iterator.remove();
          deletePrisonerFile(prisoner);
        }
        continue;
      }

      if (released) {
        releaseJailedPlayer(this.server.getOfflinePlayer(key), Util.NIL_UUID, "timer", true);
      }
    }
  }
}
