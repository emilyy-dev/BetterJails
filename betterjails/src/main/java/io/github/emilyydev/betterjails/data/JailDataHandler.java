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
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.api.impl.model.jail.ApiJail;
import io.github.emilyydev.betterjails.util.FileIO;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class JailDataHandler {
  private final BetterJailsPlugin plugin;
  private final Map<String, Jail> jails = new HashMap<>();

  private final Path jailsFile;
  private YamlConfiguration jailsYaml;

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

    this.jailsYaml = YamlConfiguration.loadConfiguration(this.jailsFile.toFile());
    for (final String key : this.jailsYaml.getKeys(false)) {
      final String lowerCaseKey = key.toLowerCase(Locale.ROOT);
      this.jails.put(lowerCaseKey, new ApiJail(lowerCaseKey, (Location) this.jailsYaml.get(key)));
    }
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

  public CompletableFuture<Void> save() {
    // A Jail's location can be changed...
    this.jails.forEach((name, jail) -> this.jailsYaml.set(name.toLowerCase(Locale.ROOT), jail.location().mutable()));

    return FileIO.writeString(this.jailsFile, this.jailsYaml.saveToString());
  }

  public void reload() throws IOException {
    this.jails.clear();
    loadJails();
  }
}
