//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2021 emilyy-dev
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

import net.md_5.bungee.api.ChatColor;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public interface Util {

  static String color(final String text, final Object... args) {
    return ChatColor.translateAlternateColorCodes('&', String.format(text, args));
  }

  static void checkVersion(final Plugin plugin, final int id, final Consumer<? super String> consumer) {
    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
      try (
          final InputStream stream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + id).openStream();
          final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
      ) {
        consumer.accept(reader.readLine());
      } catch (final IOException exception) {
        plugin.getLogger().warning("Cannot look for updates: " + exception.getMessage());
      }
    });
  }
}
