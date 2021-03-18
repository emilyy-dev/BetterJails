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

package me.fefo.betterjails.bukkit;

import me.fefo.betterjails.bukkit.platform.BukkitConfiguration;
import me.fefo.betterjails.bukkit.platform.BukkitScheduler;
import me.fefo.betterjails.common.BetterJailsPlugin;
import me.fefo.betterjails.common.abstraction.MessagingSubject;
import me.fefo.betterjails.common.abstraction.PlatformAdapter;
import me.fefo.betterjails.common.abstraction.PlatformScheduler;
import me.fefo.betterjails.common.logging.SQLite;
import me.fefo.betterjails.common.model.cell.CellManager;
import me.fefo.betterjails.common.model.jail.Jail;
import me.fefo.betterjails.common.model.jail.JailManager;
import me.fefo.betterjails.common.model.prisoner.PrisonerManager;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public final class BetterJailsBukkit extends JavaPlugin implements BetterJailsPlugin {

  private BukkitConfiguration configuration;
  private final BukkitAudiences audiences = BukkitAudiences.create(this);
  private final MessagingSubject console = new MessagingSubject(audiences.console(), Identity.nil()) {
    final ConsoleCommandSender bukkitConsole = Bukkit.getConsoleSender();

    @Override
    public String name() {
      return bukkitConsole.getName();
    }
  };

  private final JailManager jailManager = new JailManager(null, this);
  private final CellManager cellManager = new CellManager(null);
  private final PrisonerManager prisonerManager = new PrisonerManager();
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final PlatformScheduler<BukkitTask> schedulerUtil = new BukkitScheduler(this);
  private final Utils utils = new Utils();
  private final SQLite sqLite = new SQLite("bj-log");

  @Override
  public void onEnable() {

    saveDefaultConfig();

    configuration = new BukkitConfiguration(this, new File(getDataFolder(), "config.yml"));

    final Metrics metrics = new Metrics(this, 9015);
    metrics.addCustomChart(new Metrics.SingleLineChart("total-jails", null));
  }

  @Override
  public void onDisable() {
    // maybe something, probs ye
  }

  @SuppressWarnings("unchecked")
  @Override
  public @NotNull <T> PlatformScheduler<T> getPlatformScheduler(@NotNull final Class<T> taskClass) {
    if (taskClass.equals(BukkitTask.class)) {
      return (PlatformScheduler<T>) schedulerUtil;
    }

    throw new RuntimeException();
  }

  @Override
  public @NotNull MessagingSubject getConsole() {
    return console;
  }

  @Override
  public @NotNull BukkitConfiguration getConfigurationAdapter() {
    return configuration;
  }

  @Override
  public @NotNull CellManager getCellManager() {
    return cellManager;
  }

  @Override
  public @NotNull PrisonerManager getPrisonerManager() {
    return prisonerManager;
  }

  @Override
  public @NotNull <P, L, W> PlatformAdapter<P, L, W> getPlatformAdapter() {
    return null;
  }

  @Override
  public @NotNull Logger getPluginLogger() {
    return logger;
  }

  @Override
  public @NotNull String getAuthor() {
    return getDescription().getAuthors().get(0);
  }

  @Override
  public @NotNull String getVersion() {
    return getDescription().getVersion();
  }

  public @NotNull Utils getUtils() { return utils; }

  public @NotNull SQLite getSqLite() { return sqLite; }
}
