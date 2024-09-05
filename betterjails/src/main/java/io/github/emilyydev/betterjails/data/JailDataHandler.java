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

import com.github.fefo.betterjails.api.event.jail.JailCreateEvent;
import com.github.fefo.betterjails.api.event.jail.JailDeleteEvent;
import com.github.fefo.betterjails.api.model.jail.Jail;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import com.google.common.collect.ImmutableList;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.api.impl.model.jail.ApiJail;
import io.github.emilyydev.betterjails.data.upgrade.DataUpgrader;
import io.github.emilyydev.betterjails.data.upgrade.jail.V1ToV2;
import io.github.emilyydev.betterjails.util.FileIO;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class JailDataHandler {
  private final BetterJailsPlugin plugin;
  private final Map<String, Jail> jails = new HashMap<>();
  private final Path jailsFile;

  private static final List<DataUpgrader> DATA_UPGRADERS =
      ImmutableList.of(
          new V1ToV2(),
          DataUpgrader.TAIL
      );

  public JailDataHandler(final BetterJailsPlugin plugin) {
    this.plugin = plugin;

    final Path pluginDir = plugin.getPluginDir();
    this.jailsFile = pluginDir.resolve("jails.yml");
  }

  public void init() throws IOException {
    loadJails();
  }

  private void loadJails() throws IOException {
    if (Files.notExists(this.jailsFile)) {
      Files.createFile(this.jailsFile);
    }

    final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.jailsFile.toFile());
    migratePrisonerData(yaml, this.jailsFile);
    @SuppressWarnings("unchecked")
    final List<Map<String, Object>> jails = (List<Map<String, Object>>) yaml.get("jails");
    for (final Map<String, Object> jail : jails) {
      final String name = ((String) jail.get("name")).toLowerCase(Locale.ROOT);
      final Location location = (Location) jail.get("location");
      this.jails.put(name, new ApiJail(name, location));
    }
  }

  private Map<String, Object> serializeJail(Jail jail) {
    final Map<String, Object> map = new HashMap<>();
    map.put("name", jail.name());
    map.put("location", jail.location().mutable());
    return map;
  }

  public CompletableFuture<Void> save() {
    final YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("version", DataUpgrader.JAIL_VERSION);
    V1ToV2.setVersionWarning(yaml);

    final List<Map<String, Object>> jails = new ArrayList<>();
    for (Jail jail : this.jails.values()) {
      jails.add(serializeJail(jail));
    }
    yaml.set("jails", jails);

    return FileIO.writeString(this.jailsFile, yaml.saveToString()).exceptionally(ex -> {
      this.plugin.getLogger().log(Level.SEVERE, null, ex);
      return null;
    });
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
    this.plugin.eventBus().post(JailCreateEvent.class, name, ImmutableLocation.copyOf(location));
    return save();
  }

  public CompletableFuture<Void> removeJail(final String name) {
    final String lowerCaseName = name.toLowerCase(Locale.ROOT);
    final Jail jail = this.jails.remove(lowerCaseName);
    this.plugin.eventBus().post(JailDeleteEvent.class, jail);
    return save();
  }

  public void reload() throws IOException {
    this.jails.clear();
    loadJails();
  }

  private void migratePrisonerData(final YamlConfiguration config, final Path file) {
    boolean changed = false;
    final int version = config.getInt("version", 1);
    if (version > DataUpgrader.JAIL_VERSION) {
      this.plugin.getLogger().warning("Jails file " + file + " is from a newer version of BetterJails");
      this.plugin.getLogger().warning("The plugin will continue to load it, but it may not function properly, errors might show up and data could be lost");
      this.plugin.getLogger().warning("!!! Consider updating BetterJails !!!");
      return;
    }

    for (final DataUpgrader upgrader : DATA_UPGRADERS.subList(version - 1, DATA_UPGRADERS.size())) {
      changed |= upgrader.upgrade(config, this.plugin);
    }

    if (changed) {
      FileIO.writeString(file, config.saveToString()).exceptionally(ex -> {
        this.plugin.getLogger().log(Level.WARNING, "Could not save jail file " + file, ex);
        return null;
      });
    }
  }
}
