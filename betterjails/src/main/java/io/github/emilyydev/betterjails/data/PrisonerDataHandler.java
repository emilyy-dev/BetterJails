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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisoner;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.SentenceExpiry;
import io.github.emilyydev.betterjails.config.BetterJailsConfiguration;
import io.github.emilyydev.betterjails.config.SubCommandsConfiguration;
import io.github.emilyydev.betterjails.data.upgrade.DataUpgrader;
import io.github.emilyydev.betterjails.data.upgrade.prisoner.V1ToV2;
import io.github.emilyydev.betterjails.data.upgrade.prisoner.V2ToV3;
import io.github.emilyydev.betterjails.data.upgrade.prisoner.V3ToV4;
import io.github.emilyydev.betterjails.interfaces.permission.PermissionInterface;
import io.github.emilyydev.betterjails.util.FileIO;
import io.github.emilyydev.betterjails.util.Teleport;
import io.github.emilyydev.betterjails.util.Util;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;

public class PrisonerDataHandler {

  public static final String LAST_LOCATION_FIELD = "last-location";
  public static final String UNKNOWN_LOCATION_FIELD = "unknown-location";
  public static final String GROUP_FIELD = "group";
  public static final String EXTRA_GROUPS_FIELD = "extra-groups";
  public static final String UUID_FIELD = "uuid";
  public static final String NAME_FIELD = "name";
  public static final String JAIL_FIELD = "jail";
  public static final String JAILED_BY_FIELD = "jailed-by";
  public static final String SECONDS_LEFT_FIELD = "seconds-left";
  public static final String TOTAL_SENTENCE_TIME = "total-sentence-time";

  private static final List<DataUpgrader> DATA_UPGRADERS =
      ImmutableList.of(
          new V1ToV2(),
          new V2ToV3(),
          new V3ToV4(),
          DataUpgrader.TAIL
      );

  public final Path playerDataFolder;
  private final BetterJailsPlugin plugin;
  private final BetterJailsConfiguration config;
  private final SubCommandsConfiguration subCommands;
  private final Server server;
  private final Map<UUID, ApiPrisoner> prisoners = new HashMap<>();

  private @Deprecated Location backupLocation;

  public PrisonerDataHandler(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
    this.config = plugin.configuration();
    this.subCommands = plugin.subCommands();
    this.server = plugin.getServer();

    final Path pluginDir = plugin.getPluginDir();
    this.playerDataFolder = pluginDir.resolve("playerdata");
  }

  public void init() throws IOException, InvalidConfigurationException {
    // TODO(v2): can't remove this yet
    this.backupLocation = this.config.backupLocation().mutable();

    Files.createDirectories(this.playerDataFolder);
    loadPrisoners();
  }

  private void loadPrisoners() throws IOException {
    try (final Stream<Path> s = Files.list(this.playerDataFolder)) {
      s.forEach(file -> {
        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
        migratePrisonerData(yaml, file);
        final UUID uuid = UUID.fromString(file.getFileName().toString().replace(".yml", ""));
        final String name = yaml.getString(NAME_FIELD);

        boolean unknownLocation = yaml.getBoolean(UNKNOWN_LOCATION_FIELD, false);

        if (!unknownLocation && !yaml.contains(LAST_LOCATION_FIELD)) {
          // TODO(rymiel): issue #11
          this.plugin.getLogger().severe("Failed to load last known location of prisoner " + uuid + " (" + name + "). The world they were previously in might have been removed.");
          unknownLocation = true;
        }

        final String jailName = yaml.getString(JAIL_FIELD);
        Jail jail = this.plugin.jailData().getJail(jailName);
        if (jail == null) {
          // If the jail has been removed, just fall back to the first jail in the config. If there are no jails, this
          // will throw an exception, but why would you have no jails?
          jail = this.plugin.jailData().getJails().values().iterator().next();
          this.plugin.getLogger().log(Level.WARNING, "Jail {0} does not exist", jailName);
          this.plugin.getLogger().log(Level.WARNING, "Player {0}/{1} was attempted to relocate to {2}", new Object[]{name, uuid, jail.name()});
        }

        // TODO(v2): We have to set some location here, due to @NotNull API contract in Prisoner. It should be made
        //  nullable eventually, since backupLocation no longer carries any significance.
        final ImmutableLocation lastLocation = ImmutableLocation.copyOf((Location) yaml.get(LAST_LOCATION_FIELD, this.backupLocation));
        final String group = yaml.getString(GROUP_FIELD);
        final List<String> parentGroups = yaml.getStringList(EXTRA_GROUPS_FIELD);
        final String jailedBy = yaml.getString(JAILED_BY_FIELD);
        final Duration timeLeft = Duration.ofSeconds(yaml.getLong(SECONDS_LEFT_FIELD, 0L));
        final Duration totalSentenceTime = Duration.ofSeconds(yaml.getInt(TOTAL_SENTENCE_TIME, 0));

        final Player existingPlayer = this.server.getPlayer(uuid); // This is only relevant for reloading

        final SentenceExpiry expiry;
        if (this.config.considerOfflineTime() || existingPlayer != null) {
          // If considering offline time, or if the player is online, the player will have a "deadline", jailedUntil,
          // whereas timeLeft would be constantly changing.
          expiry = SentenceExpiry.of(Instant.now().plus(timeLeft));
        } else {
          // If not considering offline time, all players currently have a remaining time, timeLeft, but when they'd
          // be released, jailedUntil, will remain unknown until the player actually joins.
          expiry = SentenceExpiry.of(timeLeft);
        }
        
        this.prisoners.put(uuid, new ApiPrisoner(uuid, name, group, parentGroups, jail, jailedBy, expiry, totalSentenceTime, lastLocation, unknownLocation));
      });
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

  public boolean addJailedPlayer(
      final OfflinePlayer player,
      final String jailName,
      final UUID jailer,
      final @Nullable String jailerName,
      final long secondsLeft,
      final boolean teleport
  ) {
    final UUID prisonerUuid = player.getUniqueId();
    final ApiPrisoner existingPrisoner = this.prisoners.get(prisonerUuid);
    final Jail jail = this.plugin.jailData().getJail(jailName);

    if (jail == null) {
      return false;
    }

    final boolean isPlayerOnline = player.isOnline();
    final boolean isPlayerJailed = existingPrisoner != null;
    final Duration sentence = Duration.ofSeconds(secondsLeft);
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
      expiry = SentenceExpiry.of(Instant.now().plus(sentence));
    } else {
      // Otherwise, the time doesn't start ticking until the player joins.
      expiry = SentenceExpiry.of(sentence);
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
      final ApiPrisoner prisoner = new ApiPrisoner(prisonerUuid, player.getName(), primaryGroup, parentGroups, jail, jailerName, expiry, sentence, lastLocation, unknownLocation);

      this.plugin.eventBus().post(PlayerImprisonEvent.class, prisoner);
      final CompletionStage<?> setGroupFuture = groupsUnknown
          ? permissionInterface.setPrisonerGroup(player, jailer, jailerName)
          : CompletableFuture.completedFuture(null);
      return setGroupFuture.exceptionally(exception -> {
        if (permissionInterface != PermissionInterface.NULL) {
          this.plugin.getLogger().log(Level.SEVERE, null, exception);
        }

        return null;
      }).thenComposeAsync(v -> savePrisoner(prisoner), this.plugin);
    }, this.plugin);

    return true;
  }

  public CompletableFuture<Void> savePrisoner(final ApiPrisoner prisoner) {
    this.prisoners.put(prisoner.uuid(), prisoner);

    final YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("version", DataUpgrader.PRISONER_VERSION);
    V1ToV2.setVersionWarning(yaml);

    yaml.set(UUID_FIELD, prisoner.uuid().toString());
    yaml.set(NAME_FIELD, prisoner.name());
    yaml.set(JAIL_FIELD, prisoner.jail().name().toLowerCase(Locale.ROOT));
    yaml.set(JAILED_BY_FIELD, prisoner.jailedBy());
    yaml.set(SECONDS_LEFT_FIELD, prisoner.timeLeft().getSeconds());
    yaml.set(TOTAL_SENTENCE_TIME, prisoner.totalSentenceTime().getSeconds());
    yaml.set(LAST_LOCATION_FIELD, prisoner.lastLocationMutable());
    yaml.set(UNKNOWN_LOCATION_FIELD, prisoner.unknownLocation());
    yaml.set(GROUP_FIELD, prisoner.primaryGroup());
    yaml.set(EXTRA_GROUPS_FIELD, ImmutableList.copyOf(prisoner.parentGroups()));

    return FileIO.writeString(this.playerDataFolder.resolve(prisoner.uuid() + ".yml"), yaml.saveToString()).exceptionally(ex -> {
      this.plugin.getLogger().log(Level.SEVERE, null, ex);
      return null;
    });
  }

  public void deletePrisonerFile(final Prisoner prisoner) {
    final Path playerFile = this.playerDataFolder.resolve(prisoner.uuid() + ".yml");
    try {
      Files.deleteIfExists(playerFile);
    } catch (final IOException ex) {
      this.plugin.getLogger().log(Level.WARNING, "Could not delete prisoner file " + playerFile, ex);
    }
  }

  public boolean releaseJailedPlayer(final OfflinePlayer player, final UUID source, final @Nullable String sourceName, final boolean teleport) {
    final UUID prisonerUuid = player.getUniqueId();
    ApiPrisoner prisoner = this.prisoners.get(prisonerUuid);

    if (prisoner == null) {
      return false;
    }

    final PermissionInterface permissionInterface = this.plugin.permissionInterface();
    permissionInterface.setParentGroups(player, prisoner.parentGroups(), source, sourceName)
        .whenComplete((ignored, exception) -> {
          if (exception != null && permissionInterface != PermissionInterface.NULL) {
            this.plugin.getLogger().log(Level.SEVERE, null, exception);
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
        return true;
      }

      if (prisoner.unknownLocation()) {
        // This prisoner has never joined during the entire duration of their sentence, meaning they are already where
        // they need to be, so we can immediately forget they exist.
        this.prisoners.remove(prisonerUuid);
        deletePrisonerFile(prisoner);
      } else {
        prisoner = prisoner.withReleased();
        savePrisoner(prisoner);
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
    return true;
  }

  public CompletableFuture<Void> save() {
    CompletableFuture<Void> cf = CompletableFuture.completedFuture(null);

    for (final ApiPrisoner prisoner : this.prisoners.values()) {
      cf = cf.thenCompose(v -> savePrisoner(prisoner).whenComplete((v1, ex) -> {
        if (ex != null) {
          this.plugin.getLogger().log(Level.SEVERE, null, ex);
        }
      }));
    }

    return cf;
  }

  public void reload() throws IOException {
    this.backupLocation = this.config.backupLocation().mutable();

    this.prisoners.clear();
    loadPrisoners();
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

  private void migratePrisonerData(final YamlConfiguration config, final Path file) {
    boolean changed = false;
    final int version = config.getInt("version", 1);
    if (version > DataUpgrader.PRISONER_VERSION) {
      this.plugin.getLogger().warning("Prisoner file " + file + " is from a newer version of BetterJails");
      this.plugin.getLogger().warning("The plugin will continue to load it, but it may not function properly, errors might show up and data could be lost");
      this.plugin.getLogger().warning("!!! Consider updating BetterJails !!!");
      return;
    }

    for (final DataUpgrader upgrader : DATA_UPGRADERS.subList(version - 1, DATA_UPGRADERS.size())) {
      changed |= upgrader.upgrade(config, this.plugin);
    }

    if (changed) {
      FileIO.writeString(file, config.saveToString()).exceptionally(ex -> {
        this.plugin.getLogger().log(Level.WARNING, "Could not save player data file " + file, ex);
        return null;
      });
    }
  }
}
