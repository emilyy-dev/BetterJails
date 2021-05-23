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

package io.github.emilyydev.betterjails.common.configuration;

import io.github.emilyydev.betterjails.common.util.Utils;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public abstract class Setting<T> {

  private static final Pattern VALID_KEY_PATTERN = Pattern.compile("[a-z-]{4,32}");

  private final String key;
  private final T fallback;
  private final boolean reloadable;

  protected Setting(final @NotNull String key, final @NotNull T fallback, final boolean reloadable) {
    Validate.notNull(key, "Key cannot be null!");
    Validate.notNull(fallback, "Fallback value for config cannot be null! Config key \"" + key + "\"");

    final String sanitized = Utils.sanitize(key);
    Validate.isTrue(VALID_KEY_PATTERN.matcher(sanitized).matches(), "Invalid key");

    this.key = key;
    this.fallback = fallback;
    this.reloadable = reloadable;
  }

  public abstract @NotNull T get(final @NotNull ConfigurationAdapter configurationAdapter);

  /**
   * Gets the reloadable state of this setting.
   *
   * @return {@code false} if the object is marked as not reloadable, {@code true} otherwise.
   * @since 2.0
   */
  public boolean reloadable() {
    return this.reloadable;
  }

  /**
   * Gets the path 'key' in the config file this setting is located at.
   *
   * @return the path in the configuration file
   * @since 2.0
   */
  public @NotNull String key() {
    return this.key;
  }

  /**
   * Gets the fallback/'default' value to be used if no value is found at the setting key.
   *
   * @return the fallback value
   * @since 2.0
   */
  public @NotNull T fallback() {
    return this.fallback;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder("Setting{");
    builder.append("key='").append(this.key).append('\'');
    builder.append(", fallback=").append(this.fallback);
    builder.append(", reloadable=").append(this.reloadable);
    builder.append('}');
    return builder.toString();
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Setting)) {
      return false;
    }

    final Setting<?> that = (Setting<?>) other;
    if (this.reloadable != that.reloadable) {
      return false;
    }
    return this.key.equals(that.key);
  }

  @Override
  public int hashCode() {
    int result = this.key.hashCode();
    result = 31 * result + (this.reloadable ? 1 : 0);
    return result;
  }
}
