/*
 * This file is part of the BetterJails (https://github.com/Fefo6644/BetterJails).
 *
 *  Copyright (c) 2020 Fefo6644 <federico.lopez.1999@outlook.com>
 *  Copyright (c) 2020 contributors
 *
 *  BetterJails is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 3.
 *
 *  BetterJails is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.fefo.betterjails.common.util;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.ChronoUnit.YEARS;

public final class Utils {

  public static @Nullable String sanitize(@Nullable final String input) {
    if (input == null) {
      return null;
    }

    return input.toLowerCase().trim();
  }

  public static @Nullable String sanitize(@Nullable final UUID input) {
    if (input == null) {
      return null;
    }

    return input.toString();
  }

  public static @NotNull String durationString(@NotNull final Duration duration) {
    Validate.notNull(duration);

    final StringBuilder builder = new StringBuilder();
    final long total = duration.getSeconds();

    final long seconds = (total / seconds(SECONDS)) % ratio(MINUTES, SECONDS);
    final long minutes = (total / seconds(MINUTES)) % ratio(HOURS, MINUTES);
    final long hours = (total / seconds(HOURS)) % ratio(DAYS, HOURS);
    final long days = (total / seconds(DAYS)) % ratio(WEEKS, DAYS);
    final long weeks = (total / seconds(WEEKS)) % ratio(MONTHS, WEEKS);
    final long months = (total / seconds(MONTHS)) % ratio(YEARS, MONTHS);
    final long years = total / seconds(YEARS);

    if (years != 0) {
      builder.append(years).append('y');
    }
    if (months != 0) {
      builder.append(months).append("mo");
    }
    if (weeks != 0) {
      builder.append(weeks).append('w');
    }
    if (days != 0) {
      builder.append(days).append('d');
    }
    if (hours != 0) {
      builder.append(hours).append('h');
    }
    if (minutes != 0) {
      builder.append(minutes).append('m');
    }
    if (seconds != 0) {
      builder.append(seconds).append('s');
    }

    return builder.toString();
  }

  private static long seconds(@NotNull final ChronoUnit chronoUnit) {
    return chronoUnit.getDuration().getSeconds();
  }

  private static long ratio(@NotNull final ChronoUnit dividend, @NotNull final ChronoUnit divisor) {
    return dividend.getDuration().getSeconds() / divisor.getDuration().getSeconds();
  }

  public static @NotNull <T> List<String> filterArgs(@NotNull final Set<T> options, @NotNull final Function<T, String> toString,
                                                     @NotNull final String current) {
    final FilteringCriteria criteria = new FilteringCriteria(current);
    return options.stream().map(toString).filter(criteria).collect(Collectors.toList());
  }

  private static final class FilteringCriteria implements Predicate<String> {

    private final String current;

    public FilteringCriteria(final String current) {
      this.current = current;
    }

    @Override
    public boolean test(final String string) {
      return string.toLowerCase().startsWith(current.toLowerCase());
    }
  }
}
