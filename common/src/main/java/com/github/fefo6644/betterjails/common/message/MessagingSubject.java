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

package com.github.fefo6644.betterjails.common.message;

import com.github.fefo6644.betterjails.common.plugin.abstraction.Player;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.apache.commons.lang.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessagingSubject {

  public static MessagingSubject of(final @NonNull Audience audience, final @NotNull String name, final boolean isConsoleSubject) {
    Validate.notNull(audience, "audience");
    Validate.notNull(name, "name");
    return new MessagingSubject(audience, name, isConsoleSubject);
  }

  protected final String name;
  private final Audience audience;
  private final boolean isConsoleSubject;

  protected MessagingSubject(final Audience audience, final String name) {
    this(audience, name, false);
  }

  private MessagingSubject(final Audience audience, final String name, final boolean isConsoleSubject) {
    this.audience = audience;
    this.name = name;
    this.isConsoleSubject = isConsoleSubject;
  }

  public void sendMessage(final ComponentLike componentLike) {
    final Component component = componentLike.asComponent();

    if (this.isConsoleSubject && false) {
      // TODO split on Component.newline() and send each line individually...
    } else {
      this.audience.sendMessage(component);
    }
  }

  public boolean hasPermission(final String permission) {
    return this.isConsoleSubject;
  }

  public @NotNull String name() {
    return this.name;
  }

  public boolean isPlayerSubject() {
    return this instanceof Player;
  }

  public @Nullable Player<?> asPlayerSubject() {
    try {
      return (Player<?>) this;
    } catch (final ClassCastException exception) {
      return null;
    }
  }
}
