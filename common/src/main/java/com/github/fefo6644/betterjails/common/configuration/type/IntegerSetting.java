package com.github.fefo6644.betterjails.common.configuration.type;

import com.github.fefo6644.betterjails.common.configuration.ConfigurationAdapter;
import com.github.fefo6644.betterjails.common.configuration.Setting;
import org.jetbrains.annotations.NotNull;

public class IntegerSetting extends Setting<Integer> {

  public IntegerSetting(final @NotNull String key, final int fallback, final boolean reloadable) {
    super(key, fallback, reloadable);
  }

  @Override
  public @NotNull Integer get(final @NotNull ConfigurationAdapter configurationAdapter) {
    final Integer value = configurationAdapter.getInt(key());
    return value == null ? fallback() : value;
  }
}
