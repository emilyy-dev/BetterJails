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

import com.github.fefo.betterjails.api.event.prisoner.PrisonerReleaseEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;
import java.util.function.Consumer;

/**
 * The interface used for handling subscriptions to BetterJails events.
 */
public interface EventBus {

  /**
   * Registers an event handler for events of that type and subtypes under the provided plugin.
   * <p>
   * The plugin instance is used for the purpose of unsubscribing events registered by that plugin
   * when disabled.
   * <p>
   * Subscribing to an event type accounts for subinterfaces (subscribing to
   * {@link BetterJailsEvent} allows for handling every event of a subtype)
   *
   * @param plugin    the plugin subscribing to this event
   * @param eventType the interface or subinterface of the events to listen to
   * @param handler   the consumer that handles the event
   * @param <T>       the type of the events to listen to
   * @return an {@link EventSubscription} with information such as the subscribing plugin,
   * the event type, the subscription activation state and more
   */
  <T extends BetterJailsEvent> @NotNull EventSubscription<T> subscribe(@NotNull Plugin plugin, @NotNull Class<T> eventType, @NotNull Consumer<? super T> handler);

  /**
   * Removes the subscription from the subscriptions map and calls
   * {@link EventSubscription#unsubscribe()}.
   *
   * @param subscription the subscription to unregister
   * @param <T>          tThe type of event this subscription handles
   */
  <T extends BetterJailsEvent> void unsubscribe(@NotNull EventSubscription<T> subscription);

  /**
   * Removes all event subscriptions the provided plugin registered from the subscriptions map
   * and calls {@link EventSubscription#unsubscribe()} for all of them.
   *
   * @param plugin the plugin to unsubscribe all listeners from
   */
  void unsubscribe(@NotNull Plugin plugin);

  /**
   * Removes all subscriptions the provided plugin registered from the subscriptions map and calls
   * {@link EventSubscription#unsubscribe()} for all of them.
   * <p>
   * This does <b>not</b> account for subinterfaces (unsubscribing from {@link BetterJailsEvent}
   * will unsubscribe those handlers that listen to {@code BetterJailsEvent} exclusively, not those
   * that listen to specific subtypes like {@link PrisonerReleaseEvent}).
   *
   * @param plugin    The plugin to unsubscribe all listeners from.
   * @param eventType The class of the event to unsubscribe from.
   * @param <T>       The type of events to unsubscribe from.
   */
  <T extends BetterJailsEvent> void unsubscribe(@NotNull Plugin plugin, @NotNull Class<T> eventType);

  /**
   * Gets an unmodifiable set of all the subscriptions registered by the provided plugin.
   *
   * @param plugin the plugin to get the subscriptions for
   * @return a set with all the plugin's subscriptions
   */
  @NotNull @Unmodifiable Set<@NotNull EventSubscription<? extends BetterJailsEvent>> getSubscriptions(@NotNull Plugin plugin);

  /**
   * Returns an unmodifiable set containing all {@link EventSubscription}s that correspond to that
   * plugin that listen to the specific type of event, it does <b>not</b> account for subinterfaces.
   *
   * @param plugin    the plugin to get the subscriptions from
   * @param eventType the class of the event to get subscriptions from
   * @param <T>       the type of events to get subscriptions from
   * @return a set with all the subscriptions for that plugin for that event type
   */
  <T extends BetterJailsEvent> @NotNull @Unmodifiable Set<@NotNull EventSubscription<T>> getSubscriptions(@NotNull Plugin plugin, @NotNull Class<T> eventType);

  /**
   * Does the same as {@link #getSubscriptions(Plugin, Class)} but unlike it, it accounts for
   * subinterfaces (passing {@code BetterJailsEvent} as event type brings up all subscriptions that
   * listen to subtypes).
   *
   * @param plugin    the plugin to get the subscriptions from
   * @param eventType the class or superclass of the events to get subscriptions from
   * @param <T>       the type of events to get subscriptions from
   * @return a set with all the subscriptions for that plugin for events of that type
   */
  <T extends BetterJailsEvent> @NotNull @Unmodifiable Set<@NotNull EventSubscription<? extends T>> getAllSubscriptions(@NotNull Plugin plugin, @NotNull Class<T> eventType);
}
