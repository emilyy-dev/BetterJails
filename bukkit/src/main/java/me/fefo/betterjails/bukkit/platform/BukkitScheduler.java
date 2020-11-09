package me.fefo.betterjails.bukkit.platform;

import me.fefo.betterjails.bukkit.BetterJailsBukkit;
import me.fefo.betterjails.common.BetterJailsPlugin;
import me.fefo.betterjails.common.abstraction.PlatformScheduler;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class BukkitScheduler implements PlatformScheduler<BukkitTask> {

  private final BetterJailsBukkit plugin;
  private final org.bukkit.scheduler.BukkitScheduler scheduler = Bukkit.getScheduler();

  public BukkitScheduler(@NotNull final BetterJailsBukkit plugin) {
    this.plugin = plugin;
  }

  @Override
  public BetterJailsPlugin getPlugin() {
    return plugin;
  }

  @Override
  public BukkitTask sync(final Runnable task) {
    return scheduler.runTask(plugin, task);
  }

  @Override
  public void sync(final Consumer<BukkitTask> task) {
    scheduler.runTask(plugin, task);
  }

  @Override
  public BukkitTask sync(final Runnable task, final long delay) {
    return scheduler.runTaskLater(plugin, task, delay);
  }

  @Override
  public void sync(final Consumer<BukkitTask> task, final long delay) {
    scheduler.runTaskLater(plugin, task, delay);
  }

  @Override
  public BukkitTask sync(final Runnable task, final long delay, final long period) {
    return scheduler.runTaskTimer(plugin, task, delay, period);
  }

  @Override
  public void sync(final Consumer<BukkitTask> task, final long delay, final long period) {
    scheduler.runTaskTimer(plugin, task, delay, period);
  }

  @Override
  public BukkitTask async(final Runnable task) {
    return scheduler.runTaskAsynchronously(plugin, task);
  }

  @Override
  public void async(final Consumer<BukkitTask> task) {
    scheduler.runTaskAsynchronously(plugin, task);
  }

  @Override
  public BukkitTask async(final Runnable task, final long delay) {
    return scheduler.runTaskLaterAsynchronously(plugin, task, delay);
  }

  @Override
  public void async(final Consumer<BukkitTask> task, final long delay) {
    scheduler.runTaskLaterAsynchronously(plugin, task, delay);
  }

  @Override
  public BukkitTask async(final Runnable task, final long delay, final long period) {
    return scheduler.runTaskTimerAsynchronously(plugin, task, delay, period);
  }

  @Override
  public void async(final Consumer<BukkitTask> task, final long delay, final long period) {
    scheduler.runTaskTimerAsynchronously(plugin, task, delay, period);
  }
}
