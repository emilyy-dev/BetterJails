//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2021 emilyy-dev
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

package com.github.fefo.betterjails.api.event;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * An event subscription containing information about the type of events it handles, the handler
 * itself, the registering plugin and its activation state.
 *
 * @param <T> the type of event this subscription listens to
 */
public interface EventSubscription<T extends BetterJailsEvent> {

  /**
   * Gets the subscription underlying event handler.
   *
   * @return the event handler
   */
  @NotNull Consumer<? super T> handler();

  /**
   * Gets the interface or superinterface of event types this subscription handles.
   *
   * @return the event type this subscription registered to
   */
  @NotNull Class<T> eventType();

  /**
   * Gets the plugin that owns this event subscription.
   *
   * @return the plugin registering this subscription
   */
  @NotNull Plugin plugin();

  /**
   * Deactivates this subscription and replaces the handler with one that always throws.
   */
  void unsubscribe();

  /**
   * Gets the activation state of this subscription, {@code true} if it is still handling events,
   * {@code false} otherwise.
   *
   * @return the subscription activation state
   */
  boolean isActive();

  /**
   * Gets the inverse of the activation state of this subscription.
   * <p>
   * Useful for using as method reference as predicate ({@code EventSubscription::isNotActive}).
   * </p>
   *
   * @return {@code true} if the subscription is <b>not</b> active
   */
  default boolean isNotActive() {
    return !isActive();
  }
}
