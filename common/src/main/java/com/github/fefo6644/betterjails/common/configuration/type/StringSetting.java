package com.github.fefo6644.betterjails.common.configuration.type;

import com.github.fefo6644.betterjails.common.configuration.ConfigurationAdapter;
import com.github.fefo6644.betterjails.common.configuration.Setting;
import org.jetbrains.annotations.NotNull;

public class StringSetting extends Setting<String> {

  public StringSetting(final @NotNull String key, final @NotNull String fallback, final boolean reloadable) {
    super(key, fallback, reloadable);
  }

  @Override
  public @NotNull String get(final @NotNull ConfigurationAdapter configurationAdapter) {
    final String value = configurationAdapter.getString(key());
    return value == null ? fallback() : value;
  }
}
