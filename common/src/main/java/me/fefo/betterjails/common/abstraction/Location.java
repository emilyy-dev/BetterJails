package me.fefo.betterjails.common.abstraction;

import org.jetbrains.annotations.NotNull;

public class Location {

  private World world;
  private double x;
  private double y;
  private double z;
  private double yaw;
  private double pitch;

  public Location(@NotNull final World world, final double x, final double y, final double z, final double yaw, final double pitch) {
    this.world = world;
    this.x = x;
    this.y = y;
    this.z = z;
    this.yaw = yaw;
    this.pitch = pitch;
  }
}
