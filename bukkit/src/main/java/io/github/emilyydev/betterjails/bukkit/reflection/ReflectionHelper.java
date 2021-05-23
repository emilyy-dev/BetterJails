//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) emilyy-dev
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

package io.github.emilyydev.betterjails.bukkit.reflection;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

public final class ReflectionHelper {

  private static final String NMS_VERSION;
  private static final MethodHandle COMMAND_MAP_METHOD_HANDLE;

  static {
    final Class<?> craftServerClass = Bukkit.getServer().getClass();
    NMS_VERSION = craftServerClass.getPackage().getName().split("\\.")[3];

    COMMAND_MAP_METHOD_HANDLE = generateCommandMapMethodHandle();
  }

  public static CommandMap getCommandMap() {
    try {
      return (CommandMap) COMMAND_MAP_METHOD_HANDLE.invoke(Bukkit.getServer());
    } catch (final Throwable throwable) {
      // fatal, throw unchecked
      throw new Error(throwable);
    }
  }

  public static String nmsClass(final @NotNull String name) {
    return String.format("net.minecraft.server.%s.%s", NMS_VERSION, Objects.requireNonNull(name, "name"));
  }

  public static String obcClass(final @NotNull String name) {
    return String.format("org.bukkit.craftbukkit.%s.%s", NMS_VERSION, Objects.requireNonNull(name, "name"));
  }

  private static MethodHandle generateCommandMapMethodHandle() {
    try {
      final MethodHandles.Lookup lookup = MethodHandles.lookup();
      final Class<?> craftServerClass = Class.forName(obcClass("CraftServer"));
      final Class<?> craftCommandMapClass = Class.forName(obcClass("command.CraftCommandMap"));
      final MethodType getCommandMapMethodType = MethodType.methodType(craftCommandMapClass);
      return lookup.findVirtual(craftServerClass, "getCommandMap", getCommandMapMethodType);
    } catch (final ReflectiveOperationException throwable) {
      // should never throw unless the platform is fundamentally broken
      throw new RuntimeException(throwable);
    }
  }

  private ReflectionHelper() { throw new UnsupportedOperationException(); }
}
