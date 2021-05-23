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

package io.github.emilyydev.betterjails.common.util;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Utils {

  public static @NotNull String sanitize(final @Nullable String input) {
    if (input == null) {
      return "null";
    }

    return input.trim().toLowerCase(Locale.ROOT);
  }

  public static @NotNull String shortDuration(final @NotNull Duration duration) {
    Validate.notNull(duration);

    final StringJoiner joiner = new StringJoiner("");

    long years = duration.getSeconds();
    long months = years % seconds(ChronoUnit.YEARS);
    long weeks = months % seconds(ChronoUnit.MONTHS);
    long days = weeks % seconds(ChronoUnit.WEEKS);
    long hours = days % seconds(ChronoUnit.DAYS);
    long minutes = hours % seconds(ChronoUnit.HOURS);
    final long seconds = minutes % seconds(ChronoUnit.MINUTES);

    years /= seconds(ChronoUnit.YEARS);
    months /= seconds(ChronoUnit.MONTHS);
    weeks /= seconds(ChronoUnit.WEEKS);
    days /= seconds(ChronoUnit.DAYS);
    hours /= seconds(ChronoUnit.HOURS);
    minutes /= seconds(ChronoUnit.MINUTES);

    if (years != 0) {
      joiner.add(years + "y");
    }
    if (months != 0) {
      joiner.add(months + "mo");
    }
    if (weeks != 0) {
      joiner.add(weeks + "w");
    }
    if (days != 0) {
      joiner.add(days + "d");
    }
    if (hours != 0) {
      joiner.add(hours + "h");
    }
    if (minutes != 0) {
      joiner.add(minutes + "m");
    }
    if (seconds != 0) {
      joiner.add(seconds + "s");
    }

    return joiner.toString();
  }

  public static @NotNull String longDuration(final @NotNull Duration duration) {
    Validate.notNull(duration);

    final StringJoiner joiner = new StringJoiner(", ");

    long years = duration.getSeconds();
    long months = years % seconds(ChronoUnit.YEARS);
    long weeks = months % seconds(ChronoUnit.MONTHS);
    long days = weeks % seconds(ChronoUnit.WEEKS);
    long hours = days % seconds(ChronoUnit.DAYS);
    long minutes = hours % seconds(ChronoUnit.HOURS);
    final long seconds = minutes % seconds(ChronoUnit.MINUTES);

    years /= seconds(ChronoUnit.YEARS);
    months /= seconds(ChronoUnit.MONTHS);
    weeks /= seconds(ChronoUnit.WEEKS);
    days /= seconds(ChronoUnit.DAYS);
    hours /= seconds(ChronoUnit.HOURS);
    minutes /= seconds(ChronoUnit.MINUTES);

    if (years != 0) {
      joiner.add(years + " year" + (years > 1 ? "s" : ""));
    }
    if (months != 0) {
      joiner.add(months + " month" + (months > 1 ? "s" : ""));
    }
    if (weeks != 0) {
      joiner.add(weeks + " week" + (weeks > 1 ? "s" : ""));
    }
    if (days != 0) {
      joiner.add(days + " day" + (days > 1 ? "s" : ""));
    }
    if (hours != 0) {
      joiner.add(hours + " hour" + (hours > 1 ? "s" : ""));
    }
    if (minutes != 0) {
      joiner.add(minutes + " minute" + (minutes > 1 ? "s" : ""));
    }
    if (seconds != 0) {
      joiner.add(seconds + " second" + (seconds > 1 ? "s" : ""));
    }

    return joiner.toString();
  }

  public static <T> @NotNull List<String> filterArgs(final @NotNull Iterable<T> options,
                                                     final @NotNull Function<T, String> toString,
                                                     final @NotNull String current) {
    final ImmutableList.Builder<String> suggestionsBuilder = ImmutableList.builder();
    filterArgs(options, toString, current, suggestionsBuilder::add);
    return suggestionsBuilder.build();
  }

  public static <T> void filterArgs(final @NotNull Iterable<T> options,
                                    final @NotNull Function<T, String> toString,
                                    final @NotNull String current,
                                    final @NotNull Consumer<String> action) {
    final String sanitized = sanitize(current);
    for (final T option : options) {
      final String stringified = toString.apply(option);
      if (stringified.toLowerCase(Locale.ROOT).startsWith(sanitized)) {
        action.accept(stringified);
      }
    }
  }

  private static long seconds(final ChronoUnit chronoUnit) {
    return chronoUnit.getDuration().getSeconds();
  }

  private Utils() {
    throw new UnsupportedOperationException();
  }
}
