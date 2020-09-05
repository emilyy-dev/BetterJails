package me.fefo.betterjails.util;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public final class BackupLocation {
  private final Location loc;

  private BackupLocation(final String world,
                         final double x,
                         final double y,
                         final double z,
                         final float yaw,
                         final float pitch) {
    this.loc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
  }

  public static BackupLocation from(@NotNull final String world,
                                    final double x,
                                    final double y,
                                    final double z,
                                    final float yaw,
                                    final float pitch) {
    Validate.notNull(world);

    return new BackupLocation(world, x, y, z, yaw, pitch);
  }

  public Location getLocation() {
    return loc;
  }
}
