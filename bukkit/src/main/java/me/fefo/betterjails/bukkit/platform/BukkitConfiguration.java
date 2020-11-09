package me.fefo.betterjails.bukkit.platform;

import me.fefo.betterjails.bukkit.BetterJailsBukkit;
import me.fefo.betterjails.common.configuration.ConfigurationAdapter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public final class BukkitConfiguration implements ConfigurationAdapter {

  private final File configFile;
  private YamlConfiguration config;
  private final BetterJailsBukkit plugin;

  public BukkitConfiguration(final BetterJailsBukkit plugin, final File configFile) {
    this.plugin = plugin;
    this.configFile = configFile;
    reload();
  }

  @Override
  public void reload() {
    this.config = YamlConfiguration.loadConfiguration(configFile);
  }

  @Override
  public BetterJailsBukkit getPlugin() {
    return plugin;
  }

  @Override
  public boolean getBoolean(@NotNull final String path, final boolean fallback) {
    return config.getBoolean(path, fallback);
  }

  @Override
  public String getString(@NotNull final String path, @NotNull final String fallback) {
    return config.getString(path, fallback);
  }

  @Override
  public int getInteger(@NotNull final String path, final int fallback) {
    return config.getInt(path, fallback);
  }

  @Override
  public double getDouble(@NotNull final String path, final double fallback) {
    return config.getDouble(path, fallback);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> getList(@NotNull final String path, @NotNull final List<T> fallback) {
    return (List<T>) config.getList(path, fallback);
  }
}
