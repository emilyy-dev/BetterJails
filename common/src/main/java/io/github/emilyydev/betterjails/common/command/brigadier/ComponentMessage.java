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

package io.github.emilyydev.betterjails.common.command.brigadier;

import com.mojang.brigadier.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class ComponentMessage implements Message, ComponentLike {

  public static ComponentMessage of(final @NotNull ComponentLike like) {
    return new ComponentMessage(like);
  }

  private final Component component;

  private ComponentMessage(final ComponentLike like) {
    this.component = like.asComponent();
  }

  @Override
  public String getString() {
    return PlainComponentSerializer.plain().serialize(this.component);
  }

  public @NotNull Component component() {
    return this.component;
  }

  @Override
  public @NotNull Component asComponent() {
    return this.component;
  }

  public @NotNull String json() {
    return GsonComponentSerializer.gson().serialize(this.component);
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder("ComponentMessage{");
    builder
        .append("component=").append(this.component)
        .append('}');
    return builder.toString();
  }
}
