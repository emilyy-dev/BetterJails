package io.github.emilyydev.betterjails.listeners;

import io.github.emilyydev.betterjails.BetterJailsPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class UniqueIdCache implements Listener {

  private static final UUID NIL_UUID = new UUID(0L, 0L);

  private final Map<String, UUID> cache = new HashMap<>();

  public UniqueIdCache(final BetterJailsPlugin plugin) {
    plugin.getServer().getPluginManager().registerEvent(
        PlayerLoginEvent.class, this, EventPriority.MONITOR,
        (l, e) -> playerLogin((PlayerLoginEvent) e), plugin
    );

    for (final OfflinePlayer offlinePlayer : plugin.getServer().getOfflinePlayers()) {
      final String name = offlinePlayer.getName();
      if (name != null) {
        this.cache.put(name.toLowerCase(Locale.ROOT), offlinePlayer.getUniqueId());
      }
    }
  }

  public UUID findUniqueId(final String name) {
    return this.cache.getOrDefault(name.toLowerCase(Locale.ROOT), NIL_UUID);
  }

  private void playerLogin(final PlayerLoginEvent event) {
    if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
      final Player player = event.getPlayer();
      this.cache.putIfAbsent(player.getName().toLowerCase(Locale.ROOT), player.getUniqueId());
    }
  }
}
