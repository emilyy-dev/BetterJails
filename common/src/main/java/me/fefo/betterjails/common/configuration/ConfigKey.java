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

package me.fefo.betterjails.common.configuration;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

import static me.fefo.betterjails.common.util.Utils.sanitize;

public class ConfigKey<T> {

  private static final String VALID_KEY = "^[a-z-]{4,32}$";

  static <T> ConfigKey<T> configKey(@NotNull final String key, @NotNull final T fallback,
                                    @NotNull final BiFunction<? super ConfigurationAdapter, ? super ConfigKey<T>, ? extends T> adapter) {
    Validate.notNull(key, "Key cannot be null!");
    Validate.notNull(fallback, "Fallback value for config cannot be null! Config key \"" + key + "\"");
    Validate.notNull(key, "Adapter function cannot be null!");

    final String sanitized = sanitize(key);
    if (!sanitized.matches(VALID_KEY)) {
      throw new IllegalArgumentException("Invalid key");
    }

    return new ConfigKey<>(sanitized, fallback, adapter);
  }

  static <T> ConfigKey<T> notReloadable(@NotNull final String key, @NotNull final T fallback,
                                        @NotNull final BiFunction<? super ConfigurationAdapter, ? super ConfigKey<T>, ? extends T> adapter) {
    final ConfigKey<T> configKey = configKey(key, fallback, adapter);
    configKey.reloadable = false;
    return configKey;
  }

  private final String key;
  private final T fallback;
  private boolean reloadable = true;
  private final BiFunction<? super ConfigurationAdapter, ? super ConfigKey<T>, ? extends T> adapter;


  private ConfigKey(final String key, final T fallback, final BiFunction<? super ConfigurationAdapter, ? super ConfigKey<T>, ? extends T> adapter) {
    this.key = key;
    this.fallback = fallback;
    this.adapter = adapter;
  }

  /**
   * @return {@code false} if the object is marked as not reloadable, {@code true} otherwise.
   * @since 2.0
   */
  public boolean isReloadable() {
    return reloadable;
  }

  /**
   * @return the path in the configuration file this object represents.
   * @since 2.0
   */
  @NotNull
  public String getKey() {
    return key;
  }

  /**
   * @return the fallback (default) value that should be used when no value is found for that key.
   * @since 2.0
   */
  @NotNull
  public T getFallback() {
    return fallback;
  }

  /**
   * @param configurationAdapter the platform's configuration adapter
   *                             to abstract it from library/file format used.
   * @return the transformed "processed" output value.
   * @throws NullPointerException is thrown if no transformation should be applied;
   *                              i.e. if {@code function} is {@code null}.
   * @since 2.0
   */
  @Nullable
  public T adapt(final ConfigurationAdapter configurationAdapter) {
    return adapter.apply(configurationAdapter, this);
  }
}
