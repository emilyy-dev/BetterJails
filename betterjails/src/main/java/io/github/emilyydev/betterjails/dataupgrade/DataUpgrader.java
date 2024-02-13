package io.github.emilyydev.betterjails.dataupgrade;

import io.github.emilyydev.betterjails.BetterJailsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

@FunctionalInterface
public interface DataUpgrader {

  DataUpgrader TAIL = (config, plugin) -> false;

  boolean upgrade(YamlConfiguration config, BetterJailsPlugin plugin);
}
