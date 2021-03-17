package com.github.fefo.betterjails.listeners;

import com.github.fefo.betterjails.api.impl.event.ApiEventBus;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

public class PluginDisableListener implements Listener {

  public static PluginDisableListener create(final ApiEventBus eventBus) {
    return new PluginDisableListener(eventBus);
  }

  private final ApiEventBus eventBus;

  private PluginDisableListener(final ApiEventBus eventBus) {
    this.eventBus = eventBus;
  }

  public void register(final Plugin plugin) {
    Bukkit.getPluginManager().registerEvent(PluginDisableEvent.class, this, EventPriority.NORMAL,
                                            (l, e) -> pluginDisable((PluginDisableEvent) e), plugin);
  }

  private void pluginDisable(final PluginDisableEvent event) {
    this.eventBus.unsubscribe(event.getPlugin());
  }
}
