package io.github.emilyydev.betterjails.interfaces.storage;

import com.github.fefo.betterjails.api.model.jail.Jail;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import com.google.common.collect.ImmutableList;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.api.impl.model.jail.ApiJail;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisoner;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.SentenceExpiry;
import io.github.emilyydev.betterjails.config.BetterJailsConfiguration;
import io.github.emilyydev.betterjails.data.upgrade.DataUpgrader;
import io.github.emilyydev.betterjails.data.upgrade.prisoner.V1ToV2;
import io.github.emilyydev.betterjails.data.upgrade.prisoner.V2ToV3;
import io.github.emilyydev.betterjails.data.upgrade.prisoner.V3ToV4;
import io.github.emilyydev.betterjails.util.FileIO;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class BukkitConfigurationStorage implements StorageInterface {
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
  public static final String LOCATION_FIELD = "location";
  public static final String JAILS_FIELD = "jails";

  private static final Logger LOGGER = LoggerFactory.getLogger("BetterJails");

  private static final List<DataUpgrader> PRISONER_DATA_UPGRADERS =
      ImmutableList.of(
          new V1ToV2(),
          new V2ToV3(),
          new V3ToV4()
      );

  private static final List<DataUpgrader> JAIL_DATA_UPGRADERS =
      ImmutableList.of(
          new io.github.emilyydev.betterjails.data.upgrade.jail.V1ToV2()
      );

  private final BetterJailsPlugin plugin;
  private final Server server;
  private final BetterJailsConfiguration config;
  private final Path playerDataFolder;
  private final Path jailsFile;

  public BukkitConfigurationStorage(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
    this.config = plugin.configuration();
    this.server = plugin.getServer();
    final Path pluginDir = plugin.getPluginDir();
    this.playerDataFolder = pluginDir.resolve("playerdata");
    this.jailsFile = pluginDir.resolve("jails.yml");
  }

  @Override
  public CompletableFuture<Void> savePrisoner(ApiPrisoner prisoner) {
    final YamlConfiguration yaml = new YamlConfiguration();
    DataUpgrader.markPrisonerVersion(yaml);

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

    return FileIO.writeString(this.playerDataFolder.resolve(prisoner.uuid() + ".yml"), yaml.saveToString());
  }

  @Override
  public CompletableFuture<Void> savePrisoners(Map<UUID, ApiPrisoner> prisoners) {
    CompletableFuture<Void> cf = CompletableFuture.completedFuture(null);

    for (final ApiPrisoner prisoner : prisoners.values()) {
      final CompletableFuture<Void> savePrisonerFuture = savePrisoner(prisoner);
      cf = cf.thenCompose(v -> savePrisonerFuture);
    }

    return cf;
  }

  @Override
  public CompletableFuture<Void> deletePrisoner(ApiPrisoner prisoner) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    final Path playerFile = this.playerDataFolder.resolve(prisoner.uuid() + ".yml");
    try {
      Files.deleteIfExists(playerFile);
      future.complete(null);
    } catch (final IOException ex) {
      LOGGER.warn("Could not delete prisoner file {}", playerFile, ex);
      future.completeExceptionally(ex);
    }

    return future;
  }

  @Override
  public CompletableFuture<Map<UUID, ApiPrisoner>> loadPrisoners() {
    final Map<UUID, ApiPrisoner> out = new HashMap<>();
    final CompletableFuture<Map<UUID, ApiPrisoner>> future = new CompletableFuture<>();
    final Location backupLocation = this.config.backupLocation().mutable();
    try {
      Files.createDirectories(this.playerDataFolder);
      try (final Stream<Path> s = Files.list(this.playerDataFolder)) {
        s.forEach(file -> {
          final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
          migratePrisonerData(yaml, file);
          final UUID uuid = UUID.fromString(file.getFileName().toString().replace(".yml", ""));
          final String name = yaml.getString(NAME_FIELD);

          boolean unknownLocation = yaml.getBoolean(UNKNOWN_LOCATION_FIELD, false);

          if (!unknownLocation && !yaml.contains(LAST_LOCATION_FIELD)) {
            // TODO(rymiel): issue #11
            LOGGER.error("Failed to load last known location of prisoner {} ({}). The world they were previously in might have been removed.", uuid, name);
            unknownLocation = true;
          }

          final String jailName = yaml.getString(JAIL_FIELD);
          Jail jail = this.plugin.jailData().getJail(jailName);
          if (jail == null) {
            // If the jail has been removed, just fall back to the first jail in the config. If there are no jails, this
            // will throw an exception, but why would you have no jails?
            jail = this.plugin.jailData().getJails().values().iterator().next();
            LOGGER.warn("Jail {} does not exist", jailName);
            LOGGER.warn("Player {}/{} was attempted to relocate to {}", name, uuid, jail.name());
          }

          // TODO(v2): We have to set some location here, due to @NotNull API contract in Prisoner. It should be made
          //  nullable eventually, since backupLocation no longer carries any significance.
          final ImmutableLocation lastLocation = ImmutableLocation.copyOf((Location) yaml.get(LAST_LOCATION_FIELD, backupLocation));
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

          out.put(uuid, new ApiPrisoner(uuid, name, group, parentGroups, jail, jailedBy, expiry, totalSentenceTime, lastLocation, unknownLocation));
        });
      }

      future.complete(out);
    } catch (IOException e) {
      future.completeExceptionally(e);
    }
    return future;
  }

  private Map<String, Object> serializeJail(final Jail jail) {
    final Map<String, Object> map = new HashMap<>();
    map.put(NAME_FIELD, jail.name());
    map.put(LOCATION_FIELD, jail.location().mutable());
    return map;
  }

  @Override
  public CompletableFuture<Void> saveJail(Jail jail) {
    return this.loadJails().thenCompose(map -> {
      map.put(jail.name(), jail);
      return this.saveJails(map);
    });
  }

  @Override
  public CompletableFuture<Void> saveJails(Map<String, Jail> jails) {
    final YamlConfiguration yaml = new YamlConfiguration();
    DataUpgrader.markJailVersion(yaml);

    final List<Map<String, Object>> storedJails = new ArrayList<>();
    for (final Jail jail : jails.values()) {
      storedJails.add(serializeJail(jail));
    }
    yaml.set(JAILS_FIELD, storedJails);

    return FileIO.writeString(this.jailsFile, yaml.saveToString());
  }

  @Override
  public CompletableFuture<Void> deleteJail(Jail jail) {
    return this.loadJails().thenCompose(map -> {
      map.remove(jail.name());
      return this.saveJails(map);
    });
  }

  @Override
  public CompletableFuture<Map<String, Jail>> loadJails() {
    final Map<String, Jail> out = new HashMap<>();
    final CompletableFuture<Map<String, Jail>> future = new CompletableFuture<>();
    try {
      if (Files.notExists(this.jailsFile)) {
        Files.createFile(this.jailsFile);
      }

      final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.jailsFile.toFile());
      migrateJailData(yaml, this.jailsFile);
      final List<Map<?, ?>> jails = yaml.getMapList(JAILS_FIELD);
      for (final Map<?, ?> jail : jails) {
        final String name = ((String) jail.get(NAME_FIELD)).toLowerCase(Locale.ROOT);
        final Location location = (Location) jail.get(LOCATION_FIELD);
        out.put(name, new ApiJail(name, location));
      }

      future.complete(out);
    } catch (IOException e) {
      future.completeExceptionally(e);
    }
    return future;
  }

  private void migratePrisonerData(final YamlConfiguration config, final Path file) {
    boolean changed = false;
    final int version = config.getInt("version", 1);
    if (version > DataUpgrader.PRISONER_VERSION) {
      LOGGER.warn("Prisoner file {} is from a newer version of BetterJails", file);
      LOGGER.warn("The plugin will continue to load it, but it may not function properly, errors might show up and data could be lost");
      LOGGER.warn("!!! Consider updating BetterJails !!!");
      return;
    }

    for (final DataUpgrader upgrader : PRISONER_DATA_UPGRADERS.subList(version - 1, PRISONER_DATA_UPGRADERS.size())) {
      upgrader.upgrade(config, this.plugin);
      changed = true;
    }

    if (changed) {
      DataUpgrader.markPrisonerVersion(config);
      FileIO.writeString(file, config.saveToString()).exceptionally(ex -> {
        LOGGER.warn("Could not save player data file {}", file, ex);
        return null;
      });
    }
  }


  private void migrateJailData(final YamlConfiguration config, final Path file) {
    boolean changed = false;
    final int version = config.getInt("version", 1);
    if (version > DataUpgrader.JAIL_VERSION) {
      LOGGER.warn("Jails file {} is from a newer version of BetterJails", file);
      LOGGER.warn("The plugin will continue to load it, but it may not function properly, errors might show up and data could be lost");
      LOGGER.warn("!!! Consider updating BetterJails !!!");
      return;
    }

    for (final DataUpgrader upgrader : JAIL_DATA_UPGRADERS.subList(version - 1, JAIL_DATA_UPGRADERS.size())) {
      upgrader.upgrade(config, this.plugin);
      changed = true;
    }

    if (changed) {
      DataUpgrader.markJailVersion(config);
      FileIO.writeString(file, config.saveToString()).exceptionally(ex -> {
        LOGGER.warn("Could not save jail file {}", file, ex);
        return null;
      });
    }
  }
}
