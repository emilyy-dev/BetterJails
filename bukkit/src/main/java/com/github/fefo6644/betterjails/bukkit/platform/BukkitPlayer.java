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
