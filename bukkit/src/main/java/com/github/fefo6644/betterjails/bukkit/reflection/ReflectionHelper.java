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

package com.github.fefo6644.betterjails.bukkit.reflection;

import com.github.fefo6644.betterjails.common.command.brigadier.ComponentMessage;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public final class ReflectionHelper {

  private static final String NMS_CLASS_FORMAT;
  // use a supplier, if the server runs Paper, they expose the getCommandMap method
  private static final Supplier<CommandMap> COMMAND_MAP_SUPPLIER;
  private static final Method JSON_TO_COMPONENT_METHOD;

  static {
    NMS_CLASS_FORMAT = generateNmsClassFormat();
    COMMAND_MAP_SUPPLIER = generateCommandMapSupplier();

    try {
      final Class<?> chatSerializerClass = getNmsClass("IChatBaseComponent$ChatSerializer");
      JSON_TO_COMPONENT_METHOD = chatSerializerClass.getDeclaredMethod("a", String.class);
    } catch (final ReflectiveOperationException exception) {
      throw new RuntimeException(exception);
    }
  }

  public static CommandMap getCommandMap() {
    return COMMAND_MAP_SUPPLIER.get();
  }

  public static Message messageFromComponent(final ComponentLike like) {
    final Component component = like.asComponent();
    try {
      final String json = GsonComponentSerializer.gson().serialize(component);
      return (Message) JSON_TO_COMPONENT_METHOD.invoke(null, json);
    } catch (final ReflectiveOperationException exception) {
      return new LiteralMessage(PlainComponentSerializer.plain().serialize(component));
    }
  }

  public static Message messageFromComponent(final ComponentMessage message) {
    return messageFromComponent(message.component());
  }

  private static String generateNmsClassFormat() {
    final Class<?> craftServerClass = Bukkit.getServer().getClass();
    final String nmsVersion = craftServerClass.getPackage().getName().split("\\.")[3];
    return "net.minecraft.server." + nmsVersion + ".%s";
  }

  private static Supplier<CommandMap> generateCommandMapSupplier() {
    try {
      Bukkit.class.getDeclaredMethod("getCommandMap");
      return Bukkit::getCommandMap;
    } catch (final ReflectiveOperationException exception) {
      // ignore, the command map isn't exposed
    }

    try {
      final Class<? extends Server> craftServerClass = Bukkit.getServer().getClass();
      final Method getCommandMapMethod = craftServerClass.getDeclaredMethod("getCommandMap");
      // it's public already, no need to setAccessible(true)
      return () -> {
        try {
          return (CommandMap) getCommandMapMethod.invoke(Bukkit.getServer());
        } catch (final ReflectiveOperationException exception) {
          // shouldn't throw at this point
          throw new RuntimeException(exception);
        }
      };
    } catch (final ReflectiveOperationException exception) {
      // should never throw unless the platform is fundamentally broken
      throw new RuntimeException(exception);
    }
  }

  public static Class<?> getNmsClass(final String name) throws ReflectiveOperationException {
    return Class.forName(String.format(NMS_CLASS_FORMAT, name));
  }

  private ReflectionHelper() { throw new UnsupportedOperationException(); }
}
