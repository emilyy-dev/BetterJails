package me.fefo.betterjails.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import org.jetbrains.annotations.NotNull;

public class ConfigReloadEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();
  private static final ConfigReloadEvent INSTANCE = new ConfigReloadEvent();

  private ConfigReloadEvent() {}

  public static void call() { Bukkit.getPluginManager().callEvent(INSTANCE); }

  @NotNull
  @Override
  public HandlerList getHandlers() { return HANDLERS; }

  @NotNull
  public static HandlerList getHandlerList() { return HANDLERS; }
}
