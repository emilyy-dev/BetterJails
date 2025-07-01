//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2024 emilyy-dev
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package io.github.emilyydev.betterjails.listeners;

import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.PluginMetrics;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class UniqueIdCache implements Listener {

  private static final UUID NIL_UUID = new UUID(0L, 0L);

  private final Map<String, UUID> cache = new HashMap<>();

  public void register(final BetterJailsPlugin plugin) {
    final Server server = plugin.getServer();
    for (final OfflinePlayer offlinePlayer : server.getOfflinePlayers()) {
      final String name = offlinePlayer.getName();
      if (name != null) {
        this.cache.put(name.toLowerCase(Locale.ROOT), offlinePlayer.getUniqueId());
      }
    }

    server.getPluginManager().registerEvent(
        PlayerJoinEvent.class, this, EventPriority.MONITOR,
        (l, e) -> ((UniqueIdCache) l).playerLogin((PlayerJoinEvent) e), plugin
    );
  }

  public UUID findUniqueId(final String name) {
    return this.cache.getOrDefault(name.toLowerCase(Locale.ROOT), NIL_UUID);
  }

  @PluginMetrics.Metric(
      metric = PluginMetrics.ID_CACHE_SIZE,
      trackedFor = "Determining whether to replace the on-memory cache with SQLite"
  )
  public int cacheSize() {
    return this.cache.size();
  }

  private void playerLogin(final PlayerJoinEvent event) {
    final Player player = event.getPlayer();
    this.cache.putIfAbsent(player.getName().toLowerCase(Locale.ROOT), player.getUniqueId());
  }
}
