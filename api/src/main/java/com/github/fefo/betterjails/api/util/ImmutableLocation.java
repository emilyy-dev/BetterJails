//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2021 emilyy-dev
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

package com.github.fefo.betterjails.api.util;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * An immutable representation of a Bukkit {@link Location}. All operations that modify <i>any</i>
 * field ({@code world}, {@code x}, {@code y}, {@code z}, {@code pitch} or {@code yaw})
 * of this location object will result in a new {@code ImmutableLocation} being returned or a no-op
 * if the method returns {@code void} (e.g. {@link #add(double, double, double)});
 * if the operation is such that ends up, effectively, not modifying any of the fields, {@code this}
 * will be returned instead.
 * <p>
 * This extension also requires the {@link World} to not be {@code null}.
 * <p>
 * To create a Bukkit {@code Location} out of an {@code ImmutableLocation}
 * use the {@link #mutable()} method
 */
@Unmodifiable
public final class ImmutableLocation implements ConfigurationSerializable {

  @Contract("_ -> new")
  public static @NotNull ImmutableLocation copyOf(final @NotNull Location location) {
    return new ImmutableLocation(requireNonNull(location));
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull ImmutableLocation at(final @NotNull World world,
                                              final double x, final double y, final double z) {
    return new ImmutableLocation(requireNonNull(world), x, y, z, 0.0f, 0.0f);
  }

  @Contract("_, _, _, _, _, _ -> new")
  public static @NotNull ImmutableLocation at(final @NotNull World world,
                                              final double x, final double y, final double z,
                                              final float yaw, final float pitch) {
    return new ImmutableLocation(requireNonNull(world), x, y, z, yaw, pitch);
  }

  @Contract("_ -> new")
  public static ImmutableLocation deserialize(final @NotNull Map<String, Object> serialized) {
    return copyOf(Location.deserialize(serialized));
  }

  @Contract("_ -> new")
  public static ImmutableLocation valueOf(final @NotNull Map<String, Object> serialized) {
    return copyOf(Location.deserialize(serialized));
  }

  private final World world;
  private final double x, y, z;
  private final float pitch, yaw;

  private ImmutableLocation(final World world, final double x, final double y, final double z,
                            final float yaw, final float pitch) {
    this.world = world;
    this.x = x;
    this.y = y;
    this.z = z;
    this.pitch = pitch;
    this.yaw = yaw;
  }

  private ImmutableLocation(final Location location) {
    this(requireNonNull(location.getWorld()), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
  }

  @Contract(value = " -> new", pure = true)
  public Location mutable() {
    return new Location(this.world, this.x, this.y, this.z, this.yaw, this.pitch);
  }

  public @NotNull World getWorld() {
    return this.world;
  }

  public double getX() {
    return this.x;
  }

  public double getY() {
    return this.y;
  }

  public double getZ() {
    return this.z;
  }

  public float getPitch() {
    return this.pitch;
  }

  public float getYaw() {
    return this.yaw;
  }

  @Contract(pure = true)
  public ImmutableLocation add(final @NotNull ImmutableLocation that) {
    return add(that.x, that.y, that.z);
  }

  @Contract(pure = true)
  public ImmutableLocation add(final @NotNull Location location) {
    return add(location.getX(), location.getY(), location.getZ());
  }

  @Contract(pure = true)
  public ImmutableLocation add(final @NotNull Vector vector) {
    return add(vector.getX(), vector.getY(), vector.getZ());
  }

  @Contract(pure = true)
  public ImmutableLocation add(final double x, final double y, final double z) {
    if (x == 0.0 && y == 0.0 && z == 0.0) {
      return this;
    }
    final double newX = this.x + x;
    final double newY = this.y + y;
    final double newZ = this.z + z;
    return new ImmutableLocation(this.world, newX, newY, newZ, this.yaw, this.pitch);
  }

  @Contract(pure = true)
  public ImmutableLocation subtract(final @NotNull ImmutableLocation that) {
    return subtract(that.x, that.y, that.z);
  }

  @Contract(pure = true)
  public ImmutableLocation subtract(final @NotNull Location location) {
    return subtract(location.getX(), location.getY(), location.getZ());
  }

  @Contract(pure = true)
  public ImmutableLocation subtract(final @NotNull Vector vector) {
    return subtract(vector.getX(), vector.getY(), vector.getZ());
  }

  @Contract(pure = true)
  public ImmutableLocation subtract(final double x, final double y, final double z) {
    if (x == 0.0 && y == 0.0 && z == 0.0) {
      return this;
    }
    final double newX = this.x - x;
    final double newY = this.y - y;
    final double newZ = this.z - z;
    return new ImmutableLocation(this.world, newX, newY, newZ, this.yaw, this.pitch);
  }

  @Contract(pure = true)
  public ImmutableLocation multiply(final double m) {
    if (m == 1.0) {
      return this;
    }
    final double x = this.x * m;
    final double y = this.y * m;
    final double z = this.z * m;
    return new ImmutableLocation(this.world, x, y, z, this.yaw, this.pitch);
  }

  @Contract(pure = true)
  public ImmutableLocation zero() {
    return multiply(0.0);
  }

  @Override
  public @NotNull Map<String, Object> serialize() {
    final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

    builder.put("world", this.world.getName());

    builder.put("x", this.x);
    builder.put("y", this.y);
    builder.put("z", this.z);

    builder.put("yaw", this.yaw);
    builder.put("pitch", this.pitch);

    return builder.build();
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("ImmutableLocation{");
    builder.append("world=");
    builder.append(this.world);
    builder.append(",x=");
    builder.append(this.x);
    builder.append(",y=");
    builder.append(this.y);
    builder.append(",z=");
    builder.append(this.z);
    builder.append(",pitch=");
    builder.append(this.pitch);
    builder.append(",yaw=");
    builder.append(this.yaw);
    builder.append('}');
    return builder.toString();
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ImmutableLocation)) {
      return false;
    }

    final ImmutableLocation that = (ImmutableLocation) other;

    if (Double.compare(that.x, this.x) != 0) {
      return false;
    }
    if (Double.compare(that.y, this.y) != 0) {
      return false;
    }
    if (Double.compare(that.z, this.z) != 0) {
      return false;
    }
    if (Float.compare(that.pitch, this.pitch) != 0) {
      return false;
    }
    if (Float.compare(that.yaw, this.yaw) != 0) {
      return false;
    }
    return this.world.equals(that.world);
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = this.world.hashCode();
    temp = Double.doubleToLongBits(this.x);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.y);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.z);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (this.pitch != +0.0f ? Float.floatToIntBits(this.pitch) : 0);
    result = 31 * result + (this.yaw != +0.0f ? Float.floatToIntBits(this.yaw) : 0);
    return result;
  }
}
