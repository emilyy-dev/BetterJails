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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class CollectionUtils {

  @SafeVarargs
  public static @NotNull <T> Set<T> immutableSet(@NotNull final Function<? super Integer, ? extends Set<T>> setMaker,
                                                 @Nullable final T... values) {
    final Set<T> set = set(setMaker, values);
    return Collections.unmodifiableSet(set);
  }

  @SafeVarargs
  public static @NotNull <T> Set<T> set(@NotNull final Function<? super Integer, ? extends Set<T>> setMaker,
                                        @Nullable final T... values) {
    Validate.notNull(setMaker, "A valid Set maker must be provided");

    final Set<T> set = setMaker.apply(values.length);
    Collections.addAll(set, values);
    return set;
  }

  @SafeVarargs
  public static @NotNull <T, U> Map<T, U> immutableMap(@NotNull final Function<? super Integer, ? extends Map<T, U>> mapMaker,
                                                       @NotNull final Pair<T, U>... entries) {
    final Map<T, U> map = map(mapMaker, entries);
    return Collections.unmodifiableMap(map);
  }

  @SafeVarargs
  public static @NotNull <T, U> Map<T, U> map(@NotNull final Function<? super Integer, ? extends Map<T, U>> mapMaker,
                                              @NotNull final Pair<T, U>... entries) {
    Validate.notNull(mapMaker, "A valid Map maker must be provided");
    Validate.notNull(entries, "Map entries cannot be null");

    final Map<T, U> map = mapMaker.apply(entries.length);
    for (final Pair<T, U> entry : entries) {
      map.put(entry.getFirst(), entry.getSecond());
    }
    return map;
  }
}
