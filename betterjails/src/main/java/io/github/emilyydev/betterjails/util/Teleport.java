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

package io.github.emilyydev.betterjails.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.CompletableFuture;

public final class Teleport {

  private static final MethodHandle TELEPORT_ASYNC_MH;

  static {
    final MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle teleportAsyncMh;
    try {
      try {
        teleportAsyncMh = lookup.findVirtual(Entity.class, "teleportAsync", MethodType.methodType(CompletableFuture.class, Location.class));
      } catch (final NoSuchMethodException ignored) {
        try {
          lookup.findVirtual(World.class, "getChunkAtAsync", MethodType.methodType(CompletableFuture.class, Location.class));
          teleportAsyncMh = lookup.findStatic(Teleport.class, "teleportAsyncOld", MethodType.methodType(CompletableFuture.class, Entity.class, Location.class));
        } catch (final NoSuchMethodException ignored2) {
          teleportAsyncMh = lookup.findStatic(Teleport.class, "teleportAsyncOldest", MethodType.methodType(CompletableFuture.class, Entity.class, Location.class));
        }
      }

      TELEPORT_ASYNC_MH = teleportAsyncMh;
    } catch (final NoSuchMethodException | IllegalAccessException ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }

  @SuppressWarnings("unchecked")
  public static CompletableFuture<Boolean> teleportAsync(final Entity entity, final Location location) {
    try {
      return (CompletableFuture<Boolean>) TELEPORT_ASYNC_MH.invokeExact(entity, location);
    } catch (final RuntimeException | Error ex) {
      throw ex;
    } catch (final Throwable ex) {
      throw new RuntimeException(ex);
    }
  }

  private static CompletableFuture<Boolean> teleportAsyncOld(final Entity entity, final Location location) {
    return location.getWorld().getChunkAtAsync(location).thenApply(chunk -> entity.teleport(location));
  }

  private static CompletableFuture<Boolean> teleportAsyncOldest(final Entity entity, final Location location) {
    return CompletableFuture.completedFuture(entity.teleport(location));
  }

  private Teleport() {
  }
}
