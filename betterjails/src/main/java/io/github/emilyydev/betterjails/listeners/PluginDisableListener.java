package io.github.emilyydev.betterjails.listeners;

import io.github.emilyydev.betterjails.api.impl.event.ApiEventBus;
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
    plugin.getServer().getPluginManager().registerEvent(
        PluginDisableEvent.class, this, EventPriority.NORMAL,
        (l, e) -> pluginDisable((PluginDisableEvent) e), plugin
    );
  }

  private void pluginDisable(final PluginDisableEvent event) {
    this.eventBus.unsubscribe(event.getPlugin());
  }
}
