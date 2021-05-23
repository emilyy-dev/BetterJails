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

package io.github.emilyydev.betterjails.common.configuration.adapter;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.emilyydev.betterjails.common.configuration.ConfigurationAdapter;
import io.github.emilyydev.betterjails.common.plugin.BetterJailsPlugin;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class JsonConfigurationAdapter extends ConfigurationAdapter {

  private static final Type CONFIG_MAP_TYPE = new TypeToken<Map<String, Object>>() { }.getType();
  private static final Gson GSON = new Gson();

  public JsonConfigurationAdapter(final BetterJailsPlugin plugin, final Path pluginFolder) {
    super(plugin, pluginFolder, "config.json");
  }

  @Override
  protected void reload0() throws IOException {
    try (final Reader reader = Files.newBufferedReader(this.configFile)) {
      final Map<String, Object> map = GSON.fromJson(reader, CONFIG_MAP_TYPE);
      this.rootRaw.putAll(map != null ? map : ImmutableMap.of());
    }
  }
}
