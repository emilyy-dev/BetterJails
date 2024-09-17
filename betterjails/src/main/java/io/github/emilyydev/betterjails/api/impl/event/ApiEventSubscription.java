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

package io.github.emilyydev.betterjails.api.impl.event;

import com.github.fefo.betterjails.api.event.BetterJailsEvent;
import com.github.fefo.betterjails.api.event.EventSubscription;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class ApiEventSubscription<T extends BetterJailsEvent> implements EventSubscription<T> {

  private static final Consumer<?> ILLEGAL_HANDLER = t -> {
    throw new IllegalStateException("Inactive subscription");
  };

  private boolean active = true;
  private Consumer<? super T> handler;
  private final Plugin plugin;
  private final Class<T> eventType;

  public ApiEventSubscription(final Plugin plugin, final Class<T> eventType, final Consumer<? super T> handler) {
    this.plugin = plugin;
    this.eventType = eventType;
    this.handler = handler;
  }

  @Override
  public @NotNull Consumer<? super T> handler() {
    return this.handler;
  }

  @Override
  public @NotNull Class<T> eventType() {
    return this.eventType;
  }

  @Override
  public @NotNull Plugin plugin() {
    return this.plugin;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void unsubscribe() {
    this.active = false;
    this.handler = (Consumer<? super T>) ILLEGAL_HANDLER;
  }

  @Override
  public boolean isActive() {
    return this.active;
  }
}
