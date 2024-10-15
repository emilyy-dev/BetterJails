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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.UUID;
import java.util.stream.Collector;

public interface Util {

  Collector<Object, ImmutableSet.Builder<Object>, ImmutableSet<Object>> IMMUTABLE_SET_COLLECTOR =
      Collector.of(
          ImmutableSet::builder,
          ImmutableSet.Builder::add,
          (first, second) -> first.addAll(second.build()),
          ImmutableSet.Builder::build
      );

  Collector<Object, ImmutableList.Builder<Object>, ImmutableList<Object>> IMMUTABLE_LIST_COLLECTOR =
      Collector.of(
          ImmutableList::builder,
          ImmutableList.Builder::add,
          (first, second) -> first.addAll(second.build()),
          ImmutableList.Builder::build
      );

  UUID NIL_UUID = new UUID(0L, 0L);

  static UUID uuidOrNil(final CommandSender source) {
    return source instanceof Entity ? ((Entity) source).getUniqueId() : NIL_UUID;
  }

  static String color(final String text) {
    return ChatColor.translateAlternateColorCodes('&', text);
  }

  static String color(final String text, final Object... args) {
    return ChatColor.translateAlternateColorCodes('&', String.format(text, args));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  static <T> Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>> toImmutableSet() {
    return (Collector) IMMUTABLE_SET_COLLECTOR;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList<T>> toImmutableList() {
    return (Collector) IMMUTABLE_LIST_COLLECTOR;
  }

  static String removeBracesFromMatchedPlaceholderPleaseAndThankYou(final String in) {
    return in.substring(1, in.length() - 1);
  }
}
