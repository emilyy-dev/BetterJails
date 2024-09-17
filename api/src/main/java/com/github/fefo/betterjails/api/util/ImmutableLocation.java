//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2022 emilyy-dev
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.Objects;

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
 * </p>
 * <p>
 * To create a Bukkit {@code Location} out of an {@code ImmutableLocation}
 * use the {@link #mutable()} method
 * </p>
 */
@Unmodifiable
public final class ImmutableLocation implements ConfigurationSerializable {

  @Contract("_ -> new")
  public static @NotNull ImmutableLocation copyOf(final @NotNull Location location) {
    return new ImmutableLocation(requireNonNull(location));
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull ImmutableLocation at(
      final @NotNull World world,
      final double x,
      final double y,
      final double z
  ) {
    return new ImmutableLocation(requireNonNull(world).getName(), x, y, z, 0.0f, 0.0f);
  }

  @Contract("_, _, _, _, _, _ -> new")
  public static @NotNull ImmutableLocation at(
      final @NotNull World world,
      final double x,
      final double y,
      final double z,
      final float yaw,
      final float pitch
  ) {
    return new ImmutableLocation(requireNonNull(world).getName(), x, y, z, yaw, pitch);
  }

  @Contract("_ -> new")
  public static ImmutableLocation deserialize(final @NotNull Map<String, Object> serialized) {
    return new ImmutableLocation(
        (String) serialized.get("world"),
        ((Number) serialized.get("x")).doubleValue(),
        ((Number) serialized.get("y")).doubleValue(),
        ((Number) serialized.get("z")).doubleValue(),
        ((Number) serialized.get("yaw")).floatValue(),
        ((Number) serialized.get("pitch")).floatValue()
    );
  }

  @Contract("_ -> new")
  public static ImmutableLocation valueOf(final @NotNull Map<String, Object> serialized) {
    return deserialize(serialized);
  }

  private final String worldName;
  private final double x, y, z;
  private final float pitch, yaw;

  private ImmutableLocation(
      final String worldName,
      final double x,
      final double y,
      final double z,
      final float yaw,
      final float pitch
  ) {
    this.worldName = worldName;
    this.x = x;
    this.y = y;
    this.z = z;
    this.pitch = pitch;
    this.yaw = yaw;
  }

  private ImmutableLocation(final Location location) {
    this(
        requireNonNull(location.getWorld()).getName(),
        location.getX(),
        location.getY(),
        location.getZ(),
        location.getYaw(),
        location.getPitch()
    );
  }

  @Contract(value = " -> new", pure = true)
  public Location mutable() {
    return new Location(resolveWorld(), this.x, this.y, this.z, this.yaw, this.pitch);
  }

  public @NotNull World getWorld() {
    return resolveWorld();
  }

  public @NotNull String getWorldName() {
    return this.worldName;
  }

  private World resolveWorld() {
    return requireNonNull(Bukkit.getWorld(this.worldName), "World " + this.worldName + " is not loaded or doesn't exist");
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
    return new ImmutableLocation(this.worldName, newX, newY, newZ, this.yaw, this.pitch);
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
    return new ImmutableLocation(this.worldName, newX, newY, newZ, this.yaw, this.pitch);
  }

  @Contract(pure = true)
  public ImmutableLocation multiply(final double m) {
    if (m == 1.0) {
      return this;
    }

    final double x = this.x * m;
    final double y = this.y * m;
    final double z = this.z * m;
    return new ImmutableLocation(this.worldName, x, y, z, this.yaw, this.pitch);
  }

  @Contract(pure = true)
  public ImmutableLocation zero() {
    return multiply(0.0);
  }

  @Override
  public @NotNull Map<String, Object> serialize() {
    return ImmutableMap.<String, Object>builder()
        .put("world", this.worldName)
        .put("x", this.x)
        .put("y", this.y)
        .put("z", this.z)
        .put("yaw", this.yaw)
        .put("pitch", this.pitch)
        .build();
  }

  @Override
  public String toString() {
    return "ImmutableLocation["
        + "worldName=" + this.worldName
        + ",x=" + this.x
        + ",y=" + this.y
        + ",z=" + this.z
        + ",yaw=" + this.yaw
        + ",pitch=" + this.pitch
        + ']';
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) { return true; }
    if (!(other instanceof ImmutableLocation)) { return false; }

    final ImmutableLocation that = (ImmutableLocation) other;
    return Double.compare(that.x, this.x) == 0 &&
        Double.compare(that.y, this.y) == 0 &&
        Double.compare(that.z, this.z) == 0 &&
        Float.compare(that.pitch, this.pitch) == 0 &&
        Float.compare(that.yaw, this.yaw) == 0 &&
        this.worldName.equals(that.worldName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.worldName, this.x, this.y, this.z, this.pitch, this.yaw);
  }
}
