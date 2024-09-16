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
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
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

public final class BukkitConfigurationStorage implements StorageInterface {

  private static final Logger LOGGER = LoggerFactory.getLogger("BetterJails");

  private static final String LAST_LOCATION_FIELD = "last-location";
  private static final String UNKNOWN_LOCATION_FIELD = "unknown-location";
  private static final String GROUP_FIELD = "group";
  private static final String EXTRA_GROUPS_FIELD = "extra-groups";
  private static final String UUID_FIELD = "uuid";
  private static final String NAME_FIELD = "name";
  private static final String JAIL_FIELD = "jail";
  private static final String JAILED_BY_FIELD = "jailed-by";
  private static final String SECONDS_LEFT_FIELD = "seconds-left";
  private static final String TOTAL_SENTENCE_TIME = "total-sentence-time";
  private static final String REASON_FIELD = "reason";
  private static final String LOCATION_FIELD = "location";
  private static final String JAILS_FIELD = "jails";

  private static final List<DataUpgrader> PRISONER_DATA_UPGRADERS =
      ImmutableList.of(
          new V1ToV2(),
          new V2ToV3(),
          new V3ToV4(),
          // V4 -> V5 has no structural changes besides the addition of the reason field,
          // it only exists for the version number to increase (as v5 prisoner data cannot be loaded on v4 data version)
          (config, plugin) -> { }
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
  public void savePrisoner(final ApiPrisoner prisoner) throws IOException {
    final YamlConfiguration yaml = new YamlConfiguration();
    DataUpgrader.markPrisonerVersion(yaml);

    yaml.set(UUID_FIELD, prisoner.uuid().toString());
    yaml.set(NAME_FIELD, prisoner.name());
    yaml.set(JAIL_FIELD, prisoner.jail().name().toLowerCase(Locale.ROOT));
    yaml.set(JAILED_BY_FIELD, prisoner.jailedBy());
    yaml.set(SECONDS_LEFT_FIELD, prisoner.timeLeft().getSeconds());
    yaml.set(TOTAL_SENTENCE_TIME, prisoner.totalSentenceTime().getSeconds());
    yaml.set(REASON_FIELD, prisoner.imprisonmentReason());
    yaml.set(LAST_LOCATION_FIELD, prisoner.lastLocationMutable());
    yaml.set(UNKNOWN_LOCATION_FIELD, prisoner.unknownLocation());
    yaml.set(GROUP_FIELD, prisoner.primaryGroup());
    yaml.set(EXTRA_GROUPS_FIELD, ImmutableList.copyOf(prisoner.parentGroups()));

    final byte[] bytes = yaml.saveToString().getBytes(StandardCharsets.UTF_8);
    Files.write(this.playerDataFolder.resolve(prisoner.uuid() + ".yml"), bytes);
  }

  @Override
  public void savePrisoners(final Map<UUID, ApiPrisoner> prisoners) throws IOException {
    IOException ex = null;

    for (final ApiPrisoner prisoner : prisoners.values()) {
      try {
        savePrisoner(prisoner);
      } catch (final IOException ioex) {
        if (ex == null) {
          ex = ioex;
        } else {
          ex.addSuppressed(ioex);
        }
      }
    }

    if (ex != null) {
      throw ex;
    }
  }

  @Override
  public void deletePrisoner(final ApiPrisoner prisoner) throws IOException {
    final Path playerFile = this.playerDataFolder.resolve(prisoner.uuid() + ".yml");
    Files.deleteIfExists(playerFile);
  }

  @Override
  public Map<UUID, ApiPrisoner> loadPrisoners() throws IOException {
    final Map<UUID, ApiPrisoner> out = new HashMap<>();
    final Location backupLocation = this.config.backupLocation().mutable();
    Files.createDirectories(this.playerDataFolder);

    IOException migrationException = null;

    try (final DirectoryStream<Path> ds = Files.newDirectoryStream(this.playerDataFolder)) {
      for (final Path file : ds) {
        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
        try {
          migratePrisonerData(yaml, file);
        } catch (final IOException ex) {
          if (migrationException == null) {
            migrationException = ex;
          } else {
            migrationException.addSuppressed(ex);
          }
        }

        final UUID uuid = UUID.fromString(file.getFileName().toString().replace(".yml", ""));
        final String name = yaml.getString(NAME_FIELD);

        boolean unknownLocation = yaml.getBoolean(UNKNOWN_LOCATION_FIELD, false);

        if (!unknownLocation && !yaml.contains(LAST_LOCATION_FIELD)) {
          // TODO(rymiel): issue #11
          LOGGER.warn("Failed to load last known location of prisoner {} ({}). The world they were previously in might have been removed.", uuid, name);
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
        final String reason = yaml.getString(REASON_FIELD);

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

        out.put(uuid, new ApiPrisoner(uuid, name, group, parentGroups, jail, jailedBy, expiry, totalSentenceTime, reason, lastLocation, unknownLocation));
      }
    } catch (final IOException ex) {
      if (migrationException != null) {
        ex.addSuppressed(migrationException);
      }

      throw ex;
    }

    if (migrationException != null) {
      throw migrationException;
    }

    return out;
  }

  private Map<String, Object> serializeJail(final Jail jail) {
    final Map<String, Object> map = new HashMap<>();
    map.put(NAME_FIELD, jail.name());
    map.put(LOCATION_FIELD, jail.location().mutable());
    return map;
  }

  @Override
  public void saveJail(final Jail jail) throws IOException {
    final Map<String, Jail> jails = loadJails();
    jails.put(jail.name(), jail);
    saveJails(jails);
  }

  @Override
  public void saveJails(final Map<String, Jail> jails) throws IOException {
    final YamlConfiguration yaml = new YamlConfiguration();
    DataUpgrader.markJailVersion(yaml);

    final List<Map<String, Object>> storedJails = new ArrayList<>();
    for (final Jail jail : jails.values()) {
      storedJails.add(serializeJail(jail));
    }
    yaml.set(JAILS_FIELD, storedJails);

    Files.write(this.jailsFile, yaml.saveToString().getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public void deleteJail(final Jail jail) throws IOException {
    final Map<String, Jail> jails = loadJails();
    jails.remove(jail.name());
    saveJails(jails);
  }

  @Override
  public Map<String, Jail> loadJails() throws IOException {
    final Map<String, Jail> out = new HashMap<>();
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

    return out;
  }

  private void migratePrisonerData(final YamlConfiguration config, final Path file) throws IOException {
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
      Files.write(file, config.saveToString().getBytes(StandardCharsets.UTF_8));
    }
  }


  private void migrateJailData(final YamlConfiguration config, final Path file) throws IOException {
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
      Files.write(file, config.saveToString().getBytes(StandardCharsets.UTF_8));
    }
  }
}
