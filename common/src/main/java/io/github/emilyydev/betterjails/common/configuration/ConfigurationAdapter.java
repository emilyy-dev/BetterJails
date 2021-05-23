//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) emilyy-dev
// Copyright (c) contributors
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

package io.github.emilyydev.betterjails.common.configuration;

import com.google.common.base.Preconditions;
import io.github.emilyydev.betterjails.common.plugin.BetterJailsPlugin;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public abstract class ConfigurationAdapter {

  private static final String SEPARATOR = ".";
  private static final Pattern SEPARATOR_PATTERN = Pattern.compile(SEPARATOR, Pattern.LITERAL);

  protected final BetterJailsPlugin plugin;
  protected final Path configFile;
  protected final Path pluginFolder;
  protected final Map<String, Object> rootRaw = new LinkedHashMap<>();
  protected final Map<String, Object> deserialized = new LinkedHashMap<>();

  private boolean initialized = false;

  protected ConfigurationAdapter(final BetterJailsPlugin plugin, final Path pluginFolder, final String configName) {
    this.plugin = plugin;
    this.pluginFolder = pluginFolder;
    this.configFile = pluginFolder.resolve(configName);
  }

  public BetterJailsPlugin getPlugin() {
    return this.plugin;
  }

  public void load() throws IOException {
    if (Files.notExists(this.pluginFolder)) {
      Files.createDirectories(this.pluginFolder);
    }

    if (Files.notExists(this.configFile)) {
      try (final InputStream inputStream = this.plugin.getResource(this.configFile.getFileName().toString())) {
        if (inputStream != null) {
          Files.copy(inputStream, this.configFile);
        } else {
          throw new IOException("inputStream");
        }
      }
    }

    reload0();
    this.initialized = true;
    Settings.SETTINGS.forEach(configKey -> this.deserialized.put(configKey.key(), configKey.get(this)));
  }

  public void reload() throws IOException {
    this.rootRaw.clear();
    reload0();
    Settings.SETTINGS.forEach(configKey -> {
      if (configKey.reloadable()) {
        this.deserialized.put(configKey.key(), configKey.get(this));
      }
    });
  }

  /**
   * Reloads the config file from storage into {@link #rootRaw}, without attempting to do any kind
   * of object deserialization other than {@code Map}s and {@code List}s.
   *
   * @throws IOException if an {@code IO} error occurs
   */
  protected abstract void reload0() throws IOException; // don't expose

  public @Nullable Boolean getBoolean(final @NotNull String key) {
    Validate.notNull(key);
    return validate(key, get(key), Boolean.class);
  }

  public @Nullable Integer getInt(final @NotNull String key) {
    Validate.notNull(key);
    return validate(key, get(key), Integer.class);
  }

  public @Nullable Double getDouble(final @NotNull String key) {
    Validate.notNull(key);
    return validate(key, get(key), Double.class);
  }

  public @Nullable String getString(final @NotNull String key) {
    Validate.notNull(key);
    return validate(key, get(key), String.class);
  }

  @SuppressWarnings("unchecked")
  public <T> @Nullable List<T> getList(final @NotNull String key) {
    Validate.notNull(key);
    return validate(key, get(key), List.class);
  }

  @SuppressWarnings("unchecked")
  public <T> @Nullable Map<String, T> getSection(final @NotNull String key) {
    Validate.notNull(key);
    return validate(key, get(key), Map.class);
  }

  @SuppressWarnings("unchecked")
  public <T> @NotNull T get(final @NotNull Setting<T> setting) {
    Preconditions.checkState(this.initialized, "Cannot query config values at this stage; config not initialized yet");
    Validate.notNull(setting);
    T value = (T) this.deserialized.get(setting.key());
    if (value == null) {
      value = setting.get(this);
      this.deserialized.put(setting.key(), value);
    }
    return value;
  }

  private Object get(final String path) {
    return get(path, this.rootRaw);
  }

  private Object get(final String path, final Map<String, ?> map) {
    Preconditions.checkState(this.initialized, "Cannot query config values at this stage; config not initialized yet");
    final String[] pathComponents = SEPARATOR_PATTERN.split(path);

    if (pathComponents.length == 1) {
      return map.get(path);
    }

    final StringJoiner joiner = new StringJoiner(SEPARATOR);
    for (int i = 1; i < pathComponents.length; ++i) {
      joiner.add(pathComponents[i]);
    }

    final String nested = pathComponents[0];
    return get(joiner.toString(), validate(nested, map.get(nested), Map.class));
  }

  private <T> T validate(final String key, final Object value, final Class<T> type) {
    if (value == null) {
      warn("No value for config key \"%s\" (expected to be of type %s)", key, type.getSimpleName());
      return null;
    }

    if (!type.isInstance(value)) {
      warn("Config key \"%s\" expected to be of type %s but got %s instead", key, type, value.getClass());
      return null;
    }

    return type.cast(value);
  }

  private void warn(final String format, final Object... args) {
    this.plugin.getLogger().warn(String.format(format, args));
  }
}
