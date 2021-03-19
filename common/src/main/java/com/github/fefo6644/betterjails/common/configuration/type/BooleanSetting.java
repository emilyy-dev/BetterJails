package com.github.fefo6644.betterjails.common.configuration.type;

import com.github.fefo6644.betterjails.common.configuration.ConfigurationAdapter;
import com.github.fefo6644.betterjails.common.configuration.Setting;
import org.jetbrains.annotations.NotNull;

public class BooleanSetting extends Setting<Boolean> {

  public BooleanSetting(final @NotNull String key, final boolean fallback, final boolean reloadable) {
    super(key, fallback, reloadable);
  }

  @Override
  public @NotNull Boolean get(final @NotNull ConfigurationAdapter configurationAdapter) {
    final Boolean value = configurationAdapter.getBoolean(key());
    return value == null ? fallback() : value;
  }
}
