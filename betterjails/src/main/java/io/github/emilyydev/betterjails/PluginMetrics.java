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

package io.github.emilyydev.betterjails;

import com.github.fefo.betterjails.api.model.prisoner.Prisoner;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PluginMetrics {

  private static final int BSTATS_ID = 9015;

  public static Metrics prepareMetrics(final BetterJailsPlugin plugin) {
    final Metrics metrics = new Metrics(plugin, BSTATS_ID);
    metrics.addCustomChart(new SimplePie("jail_count", () -> String.valueOf(plugin.jailData().getJails().size())));
    metrics.addCustomChart(new SimplePie("prisoner_count", () -> String.valueOf(plugin.prisonerData().getAllPrisoners().size())));
    metrics.addCustomChart(new SimplePie("permission_plugin_hook", () -> plugin.permissionInterface().name()));
    metrics.addCustomChart(new AdvancedPie("sentence_time", () -> {
      final Map<String, Integer> map = new LinkedHashMap<>();
      for (final Prisoner prisoner : plugin.prisonerData().getAllPrisoners()) {
        final Duration sentenceTime = prisoner.totalSentenceTime();
        if (sentenceTime.isZero()) {
          continue;
        }

        final String key;
        if (sentenceTime.compareTo(Duration.ofMinutes(1L)) <= 0) {
          key = "<= 1m";
        } else if (sentenceTime.compareTo(Duration.ofMinutes(10L)) <= 0) {
          key = "<= 10m";
        } else if (sentenceTime.compareTo(Duration.ofHours(1L)) <= 0) {
          key = "<= 1h";
        } else if (sentenceTime.compareTo(Duration.ofHours(10L)) <= 0) {
          key = "<= 10h";
        } else if (sentenceTime.compareTo(Duration.ofDays(1L)) <= 0) {
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

  private PluginMetrics() {
  }
}
