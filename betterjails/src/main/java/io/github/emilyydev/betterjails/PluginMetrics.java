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
import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

public final class PluginMetrics {

  @PluginMetrics.Metric(
      metric = PluginMetrics.ID_CACHE_SIZE,
      trackedFor = "Determining whether to replace the on-memory cache with SQLite"
  )
  public static final String ID_CACHE_SIZE = "id_cache_size";

  public static final String JAIL_COUNT = "jail_count";
  public static final String PRISONER_COUNT = "prisoner_count";
  public static final String PERMISSION_PLUGIN_HOOK = "permission_plugin_hook";
  public static final String SENTENCE_TIME = "sentence_time";

  private static final int BSTATS_ID = 9015;

  public static Metrics prepareMetrics(final BetterJailsPlugin plugin) {
    final Metrics metrics = new Metrics(plugin, BSTATS_ID);
    metrics.addCustomChart(new SimplePie(ID_CACHE_SIZE, () -> String.valueOf(plugin.uniqueIdCacheSize())));
    metrics.addCustomChart(new SimplePie(JAIL_COUNT, () -> String.valueOf(plugin.jailData().getJails().size())));
    metrics.addCustomChart(new SimplePie(PRISONER_COUNT, () -> String.valueOf(plugin.prisonerData().getAllPrisoners().size())));
    metrics.addCustomChart(new SimplePie(PERMISSION_PLUGIN_HOOK, () -> plugin.permissionInterface().name()));
    metrics.addCustomChart(new AdvancedPie(SENTENCE_TIME, () -> {
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

  /**
   * Annotates a type, field, method, parameter, constructor, local variable, or package that exists solely for
   * statistic tracking and decision making, and might be removed in the future when the metric is no longer needed.
   */
  @Retention(RetentionPolicy.CLASS)
  @Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, PACKAGE})
  @Repeatable(MetricContainer.class)
  public @interface Metric {

    /**
     * {@return the metric identifier the annotated element is used to track}
     */
    @MagicConstant(valuesFromClass = PluginMetrics.class) String metric();

    /**
     * {@return the reason for this metric to exist and measure what it does}
     */
    String trackedFor();
  }

  @Retention(RetentionPolicy.CLASS)
  @Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, PACKAGE})
  public @interface MetricContainer {

    Metric[] value();
  }
}
