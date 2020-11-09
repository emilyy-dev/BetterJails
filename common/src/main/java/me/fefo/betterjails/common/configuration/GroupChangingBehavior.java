package me.fefo.betterjails.common.configuration;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum GroupChangingBehavior {
  ADD,
  SET,
  CLEAR,
  WHITELIST,
  BLACKLIST;

  public static GroupChangingBehavior find(@NotNull final ConfigurationAdapter configurationAdapter,
                                           @NotNull final ConfigKey<GroupChangingBehavior> configKey) {
    final String value = configurationAdapter.getString(configKey.getKey(), "");
    Validate.notNull(value);

    final String sanitized = value.toUpperCase(Locale.ROOT).replaceAll("[\\s-_]", "");
    try {
      return valueOf(sanitized);
    } catch (IllegalArgumentException exception) {
      return configKey.getFallback();
    }
  }
}
