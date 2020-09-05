package me.fefo.betterjails.config;

import java.util.Hashtable;
import java.util.Map;

import me.fefo.betterjails.BetterJails;

import org.apache.commons.lang.Validate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConfigKey<T> {
  static final Map<String, ConfigKey<?>> CONFIG_KEYS = new Hashtable<>();

  static <T> ConfigKey<T> configKey(@NotNull String key, @Nullable final T def) {
    Validate.notNull(key, "Key cannot be null!");

    key = key.trim();
    if (key.isEmpty()) {
      throw new IllegalArgumentException("Key cannot be empty!");
    }

    final ConfigKey<T> config = new ConfigKey<>(key, def);
    CONFIG_KEYS.put(key, config);

    return config;
  }

  static <T> ConfigKey<T> notReloadable(@NotNull final ConfigKey<T> configKey) {
    Validate.notNull(configKey);

    configKey.reloadable = false;
    return configKey;
  }

  private final String key;
  private final T def;

  private T value;
  private boolean reloadable;

  private ConfigKey(final String key, final T def) {
    this.key = key;
    this.def = def;
    this.reloadable = true;
  }

  /**
   * @return {@code false} if the object is marked as not reloadable, {@code true} otherwise.
   */
  public boolean isReloadable() {
    return reloadable;
  }

  /**
   * @return the path in the configuration file this object represents.
   */
  @NotNull
  public String getKey() {
    return key;
  }

  /**
   * @return The currently held value, or the provided default value if the held value is {@code null}.
   * Take into consideration that the default value may be {@code null} as well.
   */
  @Nullable
  public T getValue() {
    return value != null ? value : def;
  }

  /**
   * Reloads from the {@code config.yml} file the value denoted by the key if possible.
   *
   * @throws UnsupportedOperationException if the {@link ConfigKey} object is not reloadable,
   * @since 2.0
   */
  void reload() {
    if (!reloadable) {
      throw new UnsupportedOperationException("Cannot change value of non-reloadable key " + key);
    }

    BetterJails.getInstance().getConfig().get(key, def);
  }
}
