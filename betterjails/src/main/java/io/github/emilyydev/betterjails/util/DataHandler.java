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

package io.github.emilyydev.betterjails.util;

import com.earth2me.essentials.User;
import com.github.fefo.betterjails.api.event.jail.JailCreateEvent;
import com.github.fefo.betterjails.api.event.jail.JailDeleteEvent;
import com.github.fefo.betterjails.api.event.plugin.PluginSaveDataEvent;
import com.github.fefo.betterjails.api.event.prisoner.PlayerImprisonEvent;
import com.github.fefo.betterjails.api.event.prisoner.PrisonerReleaseEvent;
import com.github.fefo.betterjails.api.model.jail.Jail;
import com.github.fefo.betterjails.api.model.prisoner.Prisoner;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.api.impl.model.jail.ApiJail;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisoner;
import io.github.emilyydev.betterjails.config.BetterJailsConfiguration;
import io.github.emilyydev.betterjails.config.SubCommandsConfiguration;
import io.github.emilyydev.betterjails.dataupgrade.DataUpgrader;
import io.github.emilyydev.betterjails.dataupgrade.V1ToV2;
import io.github.emilyydev.betterjails.interfaces.permission.PermissionInterface;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
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

public final class DataHandler {

  public static final String IS_RELEASED_FIELD = "released";
  public static final String LAST_LOCATION_FIELD = "last-location";
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
          DataUpgrader.TAIL
      );

  public final Path playerDataFolder;
  private final BetterJailsPlugin plugin;
  private final BetterJailsConfiguration config;
  private final SubCommandsConfiguration subCommands;
  private final Server server;
  private final Map<String, Jail> jails = new HashMap<>();
  private final Map<UUID, ApiPrisoner> prisoners = new HashMap<>();

  private final Path jailsFile;
  private Location backupLocation;
  private YamlConfiguration jailsYaml;

  public DataHandler(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
    this.config = plugin.configuration();
    this.subCommands = plugin.subCommands();
    this.server = plugin.getServer();

    final Path pluginDir = plugin.getPluginDir();
    this.jailsFile = pluginDir.resolve("jails.yml");
    this.playerDataFolder = pluginDir.resolve("playerdata");
  }

  public void init() throws IOException, InvalidConfigurationException {
    this.backupLocation = this.config.backupLocation().mutable();

    Files.createDirectories(this.playerDataFolder);
    loadJails();
    alertNewConfigAvailable();
    loadPrisoners();
  }

  private void loadPrisoners() throws IOException {
    try (final Stream<Path> s = Files.list(this.playerDataFolder)) {
      s.forEach(file -> {
        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
        migratePrisonerData(yaml, file);
        final UUID uuid = UUID.fromString(file.getFileName().toString().replace(".yml", ""));
        final String name = yaml.getString(NAME_FIELD);

        // TODO(rymiel): check null instead, or maybe add a separate field
        final boolean incomplete = this.backupLocation.equals(yaml.get(LAST_LOCATION_FIELD, null));

        if (!yaml.contains(LAST_LOCATION_FIELD)) {
          // TODO(rymiel): issue #11
          this.plugin.getLogger().severe("Failed to load last known location of prisoner " + uuid + " (" + name + "). The world they were previously in might have been removed.");
          yaml.set(LAST_LOCATION_FIELD, this.backupLocation);
        }

        final ImmutableLocation lastLocation = ImmutableLocation.copyOf((Location) yaml.get(LAST_LOCATION_FIELD, this.backupLocation));
        final String group = yaml.getString(GROUP_FIELD);
        final List<String> parentGroups = yaml.getStringList(EXTRA_GROUPS_FIELD);
        // TODO(rymiel): handle the case where the jail is removed
        final Jail jail = getJail(yaml.getString(JAIL_FIELD));
        final String jailedBy = yaml.getString(JAILED_BY_FIELD);
        Duration timeLeft = Duration.ofSeconds(yaml.getLong(SECONDS_LEFT_FIELD, 0L));
        final Duration totalSentenceTime = Duration.ofSeconds(yaml.getInt(TOTAL_SENTENCE_TIME, 0));
        final boolean released = yaml.getBoolean(IS_RELEASED_FIELD);

        Instant jailedUntil;
        final Player existingPlayer = this.plugin.getServer().getPlayer(uuid); // This is only relevant for reloading
        if (this.config.considerOfflineTime() || existingPlayer != null) {
          // If considering offline time, or if the player is online, the player will have a "deadline", jailedUntil,
          // whereas timeLeft would be constantly changing. Therefore, we don't store it, and timeLeft will be null.
          jailedUntil = Instant.now().plus(timeLeft);
          timeLeft = null;
        } else {
          // If not considering offline time, all players currently have a remaining time, timeLeft, but when they'd
          // be released, jailedUntil, will remain unknown until the player actually joins.
          jailedUntil = null;
        }
        prisoners.put(uuid, new ApiPrisoner(uuid, name, group, parentGroups, jail, jailedBy, jailedUntil, timeLeft, totalSentenceTime, lastLocation, released, incomplete));
      });
    }
  }

  private void loadJails() throws IOException {
    if (Files.notExists(this.jailsFile)) {
      Files.createFile(this.jailsFile);
    }

    this.jailsYaml = YamlConfiguration.loadConfiguration(this.jailsFile.toFile());
    for (final String key : this.jailsYaml.getKeys(false)) {
      final String lowerCaseKey = key.toLowerCase(Locale.ROOT);
      this.jails.put(lowerCaseKey, new ApiJail(lowerCaseKey, (Location) this.jailsYaml.get(key)));
    }
  }

  public Collection<Prisoner> getAllPrisoners() {
    return Collections.unmodifiableCollection(this.prisoners.values());
  }

  public boolean isPlayerJailed(final UUID uuid) {
    return this.prisoners.containsKey(uuid);
  }

  public ApiPrisoner getPrisoner(final UUID uuid) {
    return prisoners.get(uuid);
  }

  public Map<String, Jail> getJails() {
    return this.jails;
  }

  public @Nullable Jail getJail(final String name) {
    return this.jails.get(name.toLowerCase(Locale.ROOT));
  }

  public CompletableFuture<Void> addJail(final String name, final Location location) {
    final String lowerCaseName = name.toLowerCase(Locale.ROOT);
    this.jails.computeIfAbsent(lowerCaseName, key -> new ApiJail(key, location))
        .location(ImmutableLocation.copyOf(location));
    this.jailsYaml.set(lowerCaseName, location);
    this.plugin.eventBus().post(JailCreateEvent.class, name, ImmutableLocation.copyOf(location));
    return FileIO.writeString(this.jailsFile, this.jailsYaml.saveToString());
  }

  public CompletableFuture<Void> removeJail(final String name) {
    final String lowerCaseName = name.toLowerCase(Locale.ROOT);
    final Jail jail = this.jails.remove(lowerCaseName);
    this.jailsYaml.set(name, null); // just in case...
    this.jailsYaml.set(lowerCaseName, null);
    this.plugin.eventBus().post(JailDeleteEvent.class, jail);
    return FileIO.writeString(this.jailsFile, this.jailsYaml.saveToString());
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
    final ApiPrisoner existingPrisoner = prisoners.get(prisonerUuid);
    final Jail jail = getJail(jailName);

    if (jail == null) {
      return false;
    }

    final boolean isPlayerOnline = player.isOnline();
    final boolean isPlayerJailed = existingPrisoner != null;
    final Duration sentence = Duration.ofSeconds(secondsLeft);
    final Duration timeLeft;
    final Instant jailedUntil;
    Location knownLastLocation = null;

    if (isPlayerJailed) {
      // The player is already jailed, and being put in a new jail. Since we don't want to put their last location
      // inside the previous jail, we use their existing last location.
      knownLastLocation = existingPrisoner.lastLocation().mutable();
    }

    if (isPlayerOnline) {
      // The player is online! We can get their last location, if needed, and put them in jail immediately.
      final Player onlinePlayer = player.getPlayer();

      if (knownLastLocation == null) {
        knownLastLocation = onlinePlayer.getLocation();
      } else if (knownLastLocation.equals(this.backupLocation)) {
        // TODO(rymiel): This is fragile, if backupLocation changes. Maybe we need another field in ApiPrisoner,
        //  which is like, "locationIsCorrupt", so we know to fetch it again here. See also issue #11
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
      // If the player is online or offline time is enabled, their remaining time will stack ticking down immediately,
      // so we store the deadline of their release, and don't store timeLeft.
      jailedUntil = Instant.now().plus(sentence);
      timeLeft = null;
    } else {
      // Otherwise, the time doesn't start ticking until the player joins.
      jailedUntil = null;
      timeLeft = sentence;
    }

    if (this.plugin.essentials != null) {
      final User user = this.plugin.essentials.getUser(prisonerUuid);
      if (user != null) {
        user.setJailed(true);
        if (isPlayerOnline) {
          user.setJailTimeout(jailedUntil.toEpochMilli());
        }
      }
    }

    // If we never got a last location for this player, it means we need to get it when they log in.
    final boolean incomplete = knownLastLocation == null;
    // TODO(rymiel): backupLocation continues to be problematic
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
      final ApiPrisoner prisoner = new ApiPrisoner(prisonerUuid, player.getName(), primaryGroup, parentGroups, jail, jailerName, jailedUntil, timeLeft, sentence, lastLocation, false, incomplete);

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

  public CompletableFuture<Void> savePrisoner(ApiPrisoner prisoner) {
    this.prisoners.put(prisoner.uuid(), prisoner);

    final YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("version", DataUpgrader.VERSION);
    V1ToV2.setVersionWarning(yaml);

    yaml.set(UUID_FIELD, prisoner.uuid().toString());
    yaml.set(NAME_FIELD, prisoner.name());
    yaml.set(JAIL_FIELD, prisoner.jail().name().toLowerCase(Locale.ROOT));
    yaml.set(JAILED_BY_FIELD, prisoner.jailedBy());
    yaml.set(SECONDS_LEFT_FIELD, prisoner.timeLeft().getSeconds());
    yaml.set(TOTAL_SENTENCE_TIME, prisoner.totalSentenceTime().getSeconds());
    yaml.set(IS_RELEASED_FIELD, prisoner.released());
    yaml.set(LAST_LOCATION_FIELD, prisoner.lastLocation().mutable());
    yaml.set(GROUP_FIELD, prisoner.primaryGroup());
    yaml.set(EXTRA_GROUPS_FIELD, ImmutableList.copyOf(prisoner.parentGroups()));

    return FileIO.writeString(this.playerDataFolder.resolve(prisoner.uuid() + ".yml"), yaml.saveToString()).exceptionally(ex -> {
      this.plugin.getLogger().log(Level.SEVERE, null, ex);
      return null;
    });
  }

  public boolean releaseJailedPlayer(final OfflinePlayer player, final UUID source, final @Nullable String sourceName, final boolean teleport) {
    final UUID prisonerUuid = player.getUniqueId();
    final ApiPrisoner prisoner = prisoners.get(prisonerUuid);

    if (prisoner == null) {
      return false;
    }

    final Path playerFile = this.playerDataFolder.resolve(prisonerUuid + ".yml");

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
        final Location lastLocation = prisoner.lastLocation().mutable();
        // TODO(rymiel): backupLocation continues to be problematic
        if (!lastLocation.equals(this.backupLocation)) {
          Teleport.teleportAsync(online, lastLocation);
        }
      }

      this.prisoners.remove(prisonerUuid);
      try {
        Files.deleteIfExists(playerFile);
      } catch (final IOException ex) {
        this.plugin.getLogger().log(Level.WARNING, "Could not delete prisoner file " + playerFile, ex);
      }

      final SubCommandsConfiguration.SubCommands subCommands = this.subCommands.onRelease();
      subCommands.executeAsPrisoner(this.server, online, prisoner.jailedBy() == null ? "" : prisoner.jailedBy());
      subCommands.executeAsConsole(this.server, online, prisoner.jailedBy() == null ? "" : prisoner.jailedBy());
    } else {
      if (prisoner.released()) {
        // This player has already been released, don't need to do anything
        return true;
      }

      // TODO(rymiel): backupLocation continues to be problematic
      if (prisoner.lastLocation().mutable().equals(this.backupLocation)) {
        this.prisoners.remove(prisonerUuid);
        try {
          Files.deleteIfExists(playerFile);
        } catch (final IOException ex) {
          this.plugin.getLogger().log(Level.WARNING, "Could not delete prisoner file " + playerFile, ex);
        }
      } else {
        savePrisoner(prisoner.withReleased(true));
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
    // A Jail's location can be changed...
    this.jails.forEach((name, jail) -> this.jailsYaml.set(name.toLowerCase(Locale.ROOT), jail.location().mutable()));
    CompletableFuture<Void> cf = FileIO.writeString(this.jailsFile, this.jailsYaml.saveToString());

    for (final ApiPrisoner prisoner : this.prisoners.values()) {
      cf = cf.thenCompose(v -> savePrisoner(prisoner).whenComplete((v1, ex) -> {
        if (ex != null) {
          this.plugin.getLogger().log(Level.SEVERE, null, ex);
        }
      }));
    }

    this.plugin.eventBus().post(PluginSaveDataEvent.class);
    return cf;
  }

  public void reload() throws IOException {
    this.backupLocation = this.config.backupLocation().mutable();

    this.jails.clear();
    loadJails();

    this.prisoners.clear();
    loadPrisoners();

    if (this.config.permissionHookEnabled()) {
      this.config.prisonerPermissionGroup().ifPresent(prisonerGroup ->
          this.plugin.resetPermissionInterface(
              PermissionInterface.determinePermissionInterface(this.plugin, prisonerGroup)
          )
      );
    } else {
      this.plugin.resetPermissionInterface(PermissionInterface.NULL);
    }
  }

  public void timer() {
    final Iterator<Map.Entry<UUID, ApiPrisoner>> iterator = this.prisoners.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<UUID, ApiPrisoner> entry = iterator.next();
      final UUID key = entry.getKey();
      final ApiPrisoner prisoner = entry.getValue();
      final boolean released = prisoner.released();

      // TODO(rymiel): Not actually sure why this is here, I just copied it from the old code, probably can be removed
      if (prisoner.incomplete()) {
        if (released) {
          iterator.remove();

          final Path playerFile = this.playerDataFolder.resolve(key + ".yml");
          try {
            Files.deleteIfExists(playerFile);
          } catch (final IOException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Could not delete prisoner file " + playerFile, ex);
          }
        }
        continue;
      }

      if (released || prisoner.timeLeft().isZero() || prisoner.timeLeft().isNegative()) {
        releaseJailedPlayer(this.server.getOfflinePlayer(key), Util.NIL_UUID, "timer", true);
      }
    }
  }

  // TODO keep this or perform some kind of automatic migration?
  private void alertNewConfigAvailable() throws IOException, InvalidConfigurationException {
    final YamlConfiguration bundledConfig = new YamlConfiguration();
    try (
        final InputStream in = this.plugin.getResource("config.yml");
        final Reader reader = new InputStreamReader(Objects.requireNonNull(in, "bundled config not present"), StandardCharsets.UTF_8)
    ) {
      bundledConfig.load(reader);
    }

    final FileConfiguration existingConfig = this.plugin.getConfig();
    if (!bundledConfig.getKeys(true).equals(existingConfig.getKeys(true))) {
      this.plugin.getLogger().warning("New config.yml found!");
      this.plugin.getLogger().warning("Make sure to make a backup of your settings before deleting your current config.yml!");
    }
  }

  private void migratePrisonerData(final YamlConfiguration config, final Path file) {
    boolean changed = false;
    final int version = config.getInt("version", 1);
    if (version > DataUpgrader.VERSION) {
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
