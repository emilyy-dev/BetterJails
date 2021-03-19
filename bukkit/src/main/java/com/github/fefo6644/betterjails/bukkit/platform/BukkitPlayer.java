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

import com.github.fefo6644.betterjails.common.platform.abstraction.Location;
import com.github.fefo6644.betterjails.common.platform.abstraction.Player;
import com.github.fefo6644.betterjails.common.platform.abstraction.World;
import net.kyori.adventure.audience.Audience;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BukkitPlayer extends Player {

  public BukkitPlayer(final @NotNull org.bukkit.entity.Player player, final @NotNull Audience audience) {
    super(player.getUniqueId(), player.getName(), audience);
  }

  @Override
  public void teleport(final @NotNull Location location, final @NotNull World world) {
    Validate.notNull(location, "location");
    Validate.notNull(world, "world");
    final org.bukkit.World bukkitWorld = Bukkit.getWorld(world.uuid());
    final org.bukkit.Location bukkitLocation = new org.bukkit.Location(bukkitWorld, location.getX(), location.getY(), location.getZ(),
                                                                       (float) location.getYaw(), (float) location.getPitch());
    Optional.ofNullable(Bukkit.getPlayer(uuid()))
            .ifPresent(player -> player.teleport(bukkitLocation));
  }
}
