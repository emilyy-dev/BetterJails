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

package io.github.emilyydev.betterjails.data.upgrade;

import io.github.emilyydev.betterjails.BetterJailsPlugin;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

@FunctionalInterface
public interface DataUpgrader {

  int PRISONER_VERSION = 4;
  int JAIL_VERSION = 2;

  static void markPrisonerVersion(final ConfigurationSection config) {
    config.set("version", PRISONER_VERSION);
    SetInlineCommentsHelper.setVersionWarning(config);
  }

  static void markJailVersion(final ConfigurationSection config) {
    config.set("version", JAIL_VERSION);
    SetInlineCommentsHelper.setVersionWarning(config);
  }

  void upgrade(ConfigurationSection config, BetterJailsPlugin plugin);
}

final class SetInlineCommentsHelper {

  private static final MethodHandle SET_INLINE_COMMENTS_MH;

  static {
    final MethodHandles.Lookup lookup = lookup();
    MethodHandle setInlineCommentsMh;
    try {
      setInlineCommentsMh = lookup.findVirtual(ConfigurationSection.class, "setInlineComments", methodType(void.class, String.class, List.class));
    } catch (final NoSuchMethodException | IllegalAccessException ex) {
      // no warning for you
      try {
        setInlineCommentsMh = lookup.findStatic(SetInlineCommentsHelper.class, "setInlineCommentsNoop", methodType(void.class, ConfigurationSection.class, String.class, List.class));
      } catch (final NoSuchMethodException | IllegalAccessException ex2) {
        throw new ExceptionInInitializerError(ex2);
      }
    }

    SET_INLINE_COMMENTS_MH = setInlineCommentsMh;
  }

  static void setVersionWarning(final ConfigurationSection config) {
    setInlineComments(config, "version", Collections.singletonList("DO NOT CHANGE OR REMOVE THIS VALUE UNDER ANY CIRCUMSTANCES"));
  }

  private static void setInlineCommentsNoop(final ConfigurationSection config, final String path, final List<String> comments) {
  }

  private static void setInlineComments(final ConfigurationSection config, final String path, final List<String> comments) {
    try {
      SET_INLINE_COMMENTS_MH.invokeExact(config, path, comments);
    } catch (final RuntimeException | Error ex) {
      throw ex;
    } catch (final Throwable ex) {
      throw new RuntimeException(ex);
    }
  }
}
