//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) Fefo6644 <federico.lopez.1999@outlook.com>
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

package com.github.fefo6644.betterjails.common.configuration.adapter;

import com.github.fefo6644.betterjails.common.platform.BetterJailsPlugin;
import com.github.fefo6644.betterjails.common.configuration.ConfigurationAdapter;
import com.google.common.collect.ImmutableMap;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class YamlConfigurationAdapter extends ConfigurationAdapter {

  private static final Yaml YAML;

  static {
    final LoaderOptions loaderOptions = new LoaderOptions();
    loaderOptions.setAllowDuplicateKeys(false);
    loaderOptions.setAllowRecursiveKeys(false);
    YAML = new Yaml(loaderOptions);
  }

  public YamlConfigurationAdapter(final BetterJailsPlugin plugin, final Path pluginFolder) {
    super(plugin, pluginFolder, "config.yml");
  }

  @Override
  protected void reload0() throws IOException {
    try (final Reader reader = Files.newBufferedReader(this.configFile)) {
      final Map<String, ?> map = YAML.load(reader);
      this.rootRaw.putAll(map != null ? map : ImmutableMap.of());
    }
  }
}
