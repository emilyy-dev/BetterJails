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

package io.github.emilyydev.betterjails.data.upgrade.jail;

import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.data.upgrade.DataUpgrader;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

public final class V1ToV2 implements DataUpgrader {
  // TODO(rymiel): this is duplicated, i know it's my fault
  private static final MethodHandle SET_INLINE_COMMENTS_MH;

  static {
    final Lookup lookup = lookup();
    MethodHandle setInlineCommentsMh;
    try {
      setInlineCommentsMh = lookup.findVirtual(ConfigurationSection.class, "setInlineComments", methodType(void.class, String.class, List.class));
    } catch (final NoSuchMethodException | IllegalAccessException ex) {
      // no warning for you
      try {
        setInlineCommentsMh = lookup.findStatic(V1ToV2.class, "setInlineCommentsNoop", methodType(void.class, ConfigurationSection.class, String.class, List.class));
      } catch (final NoSuchMethodException | IllegalAccessException ex2) {
        throw new ExceptionInInitializerError(ex2);
      }
    }

    SET_INLINE_COMMENTS_MH = setInlineCommentsMh;
  }

  private static void setInlineCommentsNoop(final ConfigurationSection section, final String path, final List<String> comments) {
  }

  public static void setVersionWarning(final ConfigurationSection section) {
    setInlineComments(section, "version", Collections.singletonList("DO NOT CHANGE OR REMOVE THIS VALUE UNDER ANY CIRCUMSTANCES"));
  }

  // shouldn't really be here if it's public but bleh
  private static void setInlineComments(final ConfigurationSection section, final String path, final List<String> comments) {
    try {
      SET_INLINE_COMMENTS_MH.invokeExact(section, path, comments);
    } catch (final RuntimeException | Error ex) {
      throw ex;
    } catch (final Throwable ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public boolean upgrade(final ConfigurationSection config, final BetterJailsPlugin plugin) {
    // NOTE: this accounts for if a jail named "version" exists.
    if (config.getInt("version", 1) >= 2) {
      return false;
    }

    final Set<String> keys = config.getKeys(false);
    final List<Map<String, Object>> jails = new ArrayList<>();
    for (String key : keys) {
      final Map<String, Object> jail = new HashMap<>();
      jail.put("name", key);
      jail.put("location", config.get(key));
      config.set(key, null);
      jails.add(jail);
    }

    config.set("version", 2);
    setVersionWarning(config);
    config.set("jails", jails);

    return true;
  }
}
