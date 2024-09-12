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
    for (final OfflinePlayer offlinePlayer : plugin.getServer().getOfflinePlayers()) {
      final String name = offlinePlayer.getName();
      if (name != null) {
        this.cache.put(name.toLowerCase(Locale.ROOT), offlinePlayer.getUniqueId());
      }
    }
  }

  public void register(final BetterJailsPlugin plugin) {
    plugin.getServer().getPluginManager().registerEvent(
        PlayerLoginEvent.class, this, EventPriority.MONITOR,
        (l, e) -> playerLogin((PlayerLoginEvent) e), plugin
    );
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
