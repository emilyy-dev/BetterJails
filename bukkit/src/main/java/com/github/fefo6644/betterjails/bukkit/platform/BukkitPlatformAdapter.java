//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) Fefo6644 <federico.lopez.1999@outlook.com>
// Copyright (c) contributors
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

package com.github.fefo6644.betterjails.bukkit.platform;

import com.github.fefo6644.betterjails.bukkit.BetterJailsBukkit;
import com.github.fefo6644.betterjails.common.message.MessagingSubject;
import com.github.fefo6644.betterjails.common.platform.abstraction.PlatformAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BukkitPlatformAdapter implements PlatformAdapter<CommandSender, Player, Location, World> {

  private final BetterJailsBukkit plugin;

  public BukkitPlatformAdapter(final BetterJailsBukkit plugin) {
    this.plugin = plugin;
  }

  @Override
  public CommandSender adaptSubject(final @NotNull MessagingSubject subject) {
    if (subject.isPlayerSubject()) {
      return adaptPlayer(subject.asPlayerSubject());
    }

    return Bukkit.getConsoleSender();
  }

  @Override
  public Player adaptPlayer(final @NotNull com.github.fefo6644.betterjails.common.platform.abstraction.Player player) {
    return Bukkit.getPlayer(player.uuid());
  }

  @Override
  public Location adaptLocation(final @NotNull com.github.fefo6644.betterjails.common.platform.abstraction.Location location) {
    return new Location(null, location.getX(), location.getY(), location.getZ(),
                        (float) location.getYaw(), (float) location.getPitch());
  }

  @Override
  public World adaptWorld(final @NotNull com.github.fefo6644.betterjails.common.platform.abstraction.World world) {
    return Bukkit.getWorld(world.uuid());
  }

  @Override
  public MessagingSubject adaptSubject(final @NotNull CommandSender subject) {
    if (subject instanceof Player) {
      return adaptPlayer((Player) subject);
    }

    return this.plugin.getConsoleSubject();
  }

  @Override
  public com.github.fefo6644.betterjails.common.platform.abstraction.Player adaptPlayer(final @NotNull Player player) {
    return new BukkitPlayer(player, this.plugin.getAudienceProvider().player(player.getUniqueId()));
  }

  @Override
  public com.github.fefo6644.betterjails.common.platform.abstraction.Location adaptLocation(final @NotNull Location location) {
    return new com.github.fefo6644.betterjails.common.platform.abstraction.Location(location.getX(), location.getY(), location.getZ(),
                                                                                    location.getYaw(), location.getPitch());
  }

  @Override
  public com.github.fefo6644.betterjails.common.platform.abstraction.World adaptWorld(final @NotNull World world) {
    return com.github.fefo6644.betterjails.common.platform.abstraction.World.world(world.getName(), world.getUID());
  }
}
