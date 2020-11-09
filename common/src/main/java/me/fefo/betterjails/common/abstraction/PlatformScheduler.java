package me.fefo.betterjails.common.abstraction;

import me.fefo.betterjails.common.BetterJailsPlugin;

import java.util.function.Consumer;

public interface PlatformScheduler<T> {

  BetterJailsPlugin getPlugin();

  T sync(final Runnable task);
  void sync(final Consumer<T> task);
  T sync(final Runnable task, final long delay);
  void sync(final Consumer<T> task, final long delay);
  T sync(final Runnable task, final long delay, final long period);
  void sync(final Consumer<T> task, final long delay, final long period);

  T async(final Runnable task);
  void async(final Consumer<T> task);
  T async(final Runnable task, final long delay);
  void async(final Consumer<T> task, final long delay);
  T async(final Runnable task, final long delay, final long period);
  void async(final Consumer<T> task, final long delay, final long period);
}
