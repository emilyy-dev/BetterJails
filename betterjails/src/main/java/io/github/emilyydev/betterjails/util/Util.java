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

import com.github.fefo.betterjails.api.model.prisoner.Prisoner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collector;

public interface Util {

  int SPIGOTMC_RESOURCE_ID = 76001;
  int BSTATS_ID = 9015;

  Collector<Object, ImmutableSet.Builder<Object>, ImmutableSet<Object>> IMMUTABLE_SET_COLLECTOR =
      Collector.of(
          ImmutableSet::builder,
          ImmutableSet.Builder::add,
          (first, second) -> first.addAll(second.build()),
          ImmutableSet.Builder::build
      );

  Collector<Object, ImmutableList.Builder<Object>, ImmutableList<Object>> IMMUTABLE_LIST_COLLECTOR =
      Collector.of(
          ImmutableList::builder,
          ImmutableList.Builder::add,
          (first, second) -> first.addAll(second.build()),
          ImmutableList.Builder::build
      );

  UUID NIL_UUID = new UUID(0L, 0L);

  static UUID uuidOrNil(final CommandSender source) {
    return source instanceof Entity ? ((Entity) source).getUniqueId() : NIL_UUID;
  }

  static String color(final String text) {
    return ChatColor.translateAlternateColorCodes('&', text);
  }

  static String color(final String text, final Object... args) {
    return ChatColor.translateAlternateColorCodes('&', String.format(text, args));
  }

  static void checkVersion(final Plugin plugin, final Consumer<? super String> consumer) {
    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
      try (
          final InputStream stream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + SPIGOTMC_RESOURCE_ID).openStream();
          final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
      ) {
        final String version = reader.readLine();
        plugin.getServer().getScheduler().runTask(plugin, () -> consumer.accept(version));
      } catch (final IOException exception) {
        plugin.getLogger().log(Level.WARNING, "Cannot look for updates", exception);
      }
    });
  }

  static Metrics prepareMetrics(final BetterJailsPlugin plugin) {
    final Metrics metrics = new Metrics(plugin, BSTATS_ID);
    metrics.addCustomChart(new SingleLineChart("jail_count", () -> plugin.dataHandler().getJails().size()));
    metrics.addCustomChart(new SingleLineChart("prisoner_count", () -> plugin.dataHandler().getPrisonerIds().size()));
    metrics.addCustomChart(new SimplePie("permission_plugin_hook", () -> plugin.permissionInterface().name()));
    metrics.addCustomChart(new AdvancedPie("sentence_time", () -> {
      final Map<String, Integer> map = new LinkedHashMap<>();
      for (final Prisoner prisoner : plugin.api().getPrisonerManager().getAllPrisoners()) {
        final Duration remainingTime = prisoner.totalSentenceTime();
        if (remainingTime.isZero()) {
          continue;
        }

        final String key;
        if (remainingTime.compareTo(Duration.ofMinutes(1L)) <= 0) {
          key = "<= 1m";
        } else if (remainingTime.compareTo(Duration.ofMinutes(10L)) <= 0) {
          key = "<= 10m";
        } else if (remainingTime.compareTo(Duration.ofHours(1L)) <= 0) {
          key = "<= 1h";
        } else if (remainingTime.compareTo(Duration.ofHours(10L)) <= 0) {
          key = "<= 10h";
        } else if (remainingTime.compareTo(Duration.ofDays(1L)) <= 0) {
          key = "<= 1d";
        } else {
          key = "> 1d";
        }

        map.merge(key, 1, Integer::sum);
      }

      return map;
    }));

    return metrics;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  static <T> Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>> toImmutableSet() {
    return (Collector) IMMUTABLE_SET_COLLECTOR;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList<T>> toImmutableList() {
    return (Collector) IMMUTABLE_LIST_COLLECTOR;
  }

  static String removeBracesFromMatchedPlaceholderPleaseAndThankYou(final String in) {
    return in.substring(1, in.length() - 1);
  }
}
