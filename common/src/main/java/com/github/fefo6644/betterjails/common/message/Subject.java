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

import com.github.fefo6644.betterjails.common.plugin.abstraction.DummyPlayer;
import com.github.fefo6644.betterjails.common.plugin.abstraction.Player;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.BuildableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.format.Style;
import org.apache.commons.lang.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;

public class Subject {

  public static Subject of(final @NonNull Audience audience, final @NotNull String name, final boolean isConsoleSubject) {
    Validate.notNull(audience, "audience");
    Validate.notNull(name, "name");
    return new Subject(audience, name, isConsoleSubject);
  }

  protected final String name;
  private final Audience audience;
  private final boolean isConsoleSubject;

  protected Subject(final Audience audience, final String name) {
    this(audience, name, false);
  }

  private Subject(final Audience audience, final String name, final boolean isConsoleSubject) {
    this.audience = audience;
    this.name = name;
    this.isConsoleSubject = isConsoleSubject;
  }

  public void sendMessage(final Component component) {

    if (this.isConsoleSubject && !component.children().isEmpty()) {
      // append newline so it sends the last line too
      // it sends lines on line breaks, no line break = no complete line
      sendMessageConsole(component.append(Component.newline()));
    } else {
      this.audience.sendMessage(component);
    }
  }

  private void sendMessageConsole(final Component original) {

    final class Recurrent implements Consumer<Component> {

      private final Deque<Style> styleStack = new LinkedList<>();
      private final Deque<ComponentBuilder<?, ?>> builderStack = new LinkedList<>();

      @Override
      public void accept(final Component component) {
        if (component.equals(Component.newline())) {
          ComponentBuilder<?, ?> temp = this.builderStack.pop();
          while (this.builderStack.peek() != null) {
            final ComponentBuilder<?, ?> otherTemp = this.builderStack.pop();
            otherTemp.append(temp);
            temp = otherTemp;
          }

          Subject.this.audience.sendMessage(temp.build());

          this.builderStack.clear();
          for (final Style style : this.styleStack) {
            this.builderStack.push(Component.text().style(style));
          }

          return;
        }

        // If it does not contain a Component.newline() just append it to the last builder in the stack
        // There is nothing to split
        if (!component.contains(Component.newline())) {
          ComponentBuilder<?, ?> temp = this.builderStack.peek();
          if (temp != null) {
            temp.append(component);
          } else {
            temp = Component.text();
            // apply style from non-buildable component to parent component
            // so other children also inherit from them
            temp.style(component.style());
            temp.append(component);
            this.builderStack.push(temp);
          }

          return;
        }

        final ComponentBuilder<?, ?> temp;
        if (component instanceof BuildableComponent<?, ?>) {
          temp = ((BuildableComponent<?, ?>) component.children(ImmutableList.of())).toBuilder();
        } else {
          final Component otherTempPleaseStopIt = component.children(ImmutableList.of());
          temp = Component.text();
          // apply style from non-buildable component to parent component
          // so other children also inherit from them
          temp.style(otherTempPleaseStopIt.style());
          temp.append(otherTempPleaseStopIt);
        }

        this.builderStack.push(temp);
        this.styleStack.push(component.style());
        component.children().forEach(this);
        this.styleStack.pop();
      }
    }

    new Recurrent().accept(original);
  }

  public boolean hasPermission(final String permission) {
    return this.isConsoleSubject;
  }

  public @NotNull String name() {
    return this.name;
  }

  public boolean isPlayerSubject() {
    return false;
  }

  public Player<?> asPlayerSubject() {
    return DummyPlayer.DUMMY_PLAYER;
  }
}
