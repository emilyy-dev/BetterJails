//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2022 emilyy-dev
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

package io.github.emilyydev.betterjails.config;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class AbstractConfiguration {

  private final Path configFile;
  private final String fileName;
  private final Map<String, Object> configurationMap;
  private volatile Configuration configuration = new MemoryConfiguration();

  public AbstractConfiguration(
      final Path dir,
      final String fileName,
      final Supplier<? extends Map<String, Object>> mapCreator
  ) {
    this.configFile = dir.resolve(fileName);
    this.fileName = fileName;
    this.configurationMap = mapCreator.get();
  }

  public final void load() {
    try {
      if (Files.notExists(this.configFile)) {
        Files.createDirectories(this.configFile.getParent());
        try (final InputStream in = AbstractConfiguration.class.getResourceAsStream('/' + this.fileName)) {
          Objects.requireNonNull(in, "Bundled " + this.fileName);
          Files.copy(in, this.configFile);
        }
      }

      final YamlConfiguration configuration = new YamlConfiguration();
      try (final BufferedReader reader = Files.newBufferedReader(this.configFile)) {
        configuration.load(reader);
      }

      this.configuration = configuration;
      this.configurationMap.clear();
    } catch (final IOException exception) {
      throw new UncheckedIOException(exception.getMessage(), exception);
    } catch (final InvalidConfigurationException exception) {
      final IOException ioEx = new IOException(exception.getMessage(), exception);
      throw new UncheckedIOException(ioEx.getMessage(), ioEx);
    }
  }

  @SuppressWarnings("unchecked")
  protected final <T> T setting(final String key, final Function<? super String, ? extends T> loader) {
    return (T) this.configurationMap.computeIfAbsent(key, loader);
  }

  protected final Configuration config() {
    return this.configuration;
  }
}
