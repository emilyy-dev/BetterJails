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

package io.github.emilyydev.betterjails.api.impl.event;

import com.github.fefo.betterjails.api.BetterJails;
import com.github.fefo.betterjails.api.event.BetterJailsEvent;
import com.github.fefo.betterjails.api.event.EventBus;
import com.github.fefo.betterjails.api.event.EventSubscription;
import com.github.fefo.betterjails.api.event.jail.JailCreateEvent;
import com.github.fefo.betterjails.api.event.jail.JailDeleteEvent;
import com.github.fefo.betterjails.api.event.plugin.PluginReloadEvent;
import com.github.fefo.betterjails.api.event.plugin.PluginSaveDataEvent;
import com.github.fefo.betterjails.api.event.prisoner.PlayerImprisonEvent;
import com.github.fefo.betterjails.api.event.prisoner.PrisonerReleaseEvent;
import com.github.fefo.betterjails.api.model.jail.Jail;
import com.github.fefo.betterjails.api.model.prisoner.Prisoner;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import io.github.emilyydev.betterjails.api.impl.event.jail.JailCreateEventImpl;
import io.github.emilyydev.betterjails.api.impl.event.jail.JailDeleteEventImpl;
import io.github.emilyydev.betterjails.api.impl.event.plugin.PluginReloadEventImpl;
import io.github.emilyydev.betterjails.api.impl.event.plugin.PluginSaveDataEventImpl;
import io.github.emilyydev.betterjails.api.impl.event.prisoner.PlayerImprisonEventImpl;
import io.github.emilyydev.betterjails.api.impl.event.prisoner.PrisonerReleaseEventImpl;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collector;

import static com.google.common.collect.Multimaps.synchronizedListMultimap;

public class ApiEventBus implements EventBus {

  private static final Map<Class<? extends BetterJailsEvent>, Constructor<? extends BetterJailsEvent>> KNOWN_EVENT_TYPES;
  private static final Collector<Object, ImmutableSet.Builder<Object>, ImmutableSet<Object>> IMMUTABLE_SET_COLLECTOR =
      Collector.of(
          ImmutableSet::builder,
          ImmutableSet.Builder::add,
          (first, second) -> first.addAll(second.build()),
          ImmutableSet.Builder::build
      );

  static {
    final ImmutableMap.Builder<Class<? extends BetterJailsEvent>, Constructor<? extends BetterJailsEvent>> builder =
        ImmutableMap.builder();

    try {
      builder
          .put(JailCreateEvent.class, JailCreateEventImpl.class.getConstructor(BetterJails.class, Class.class, String.class, ImmutableLocation.class))
          .put(JailDeleteEvent.class, JailDeleteEventImpl.class.getConstructor(BetterJails.class, Class.class, Jail.class))
          .put(PlayerImprisonEvent.class, PlayerImprisonEventImpl.class.getConstructor(BetterJails.class, Class.class, Prisoner.class))
          .put(PrisonerReleaseEvent.class, PrisonerReleaseEventImpl.class.getConstructor(BetterJails.class, Class.class, Prisoner.class))
          .put(PluginReloadEvent.class, PluginReloadEventImpl.class.getConstructor(BetterJails.class, Class.class, CommandSender.class))
          .put(PluginSaveDataEvent.class, PluginSaveDataEventImpl.class.getConstructor(BetterJails.class, Class.class));
    } catch (final ReflectiveOperationException exception) {
      // ignore, ***shouldn't*** throw
    }

    KNOWN_EVENT_TYPES = builder.build();
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>> toImmutableSet() {
    return (Collector) IMMUTABLE_SET_COLLECTOR;
  }

  private BetterJails api = null;
  private final ListMultimap<Class<? extends BetterJailsEvent>, EventSubscription<? extends BetterJailsEvent>> subscriptions =
      synchronizedListMultimap(ArrayListMultimap.create());

  public void setApi(final BetterJails api) {
    this.api = api;
  }

  @Override
  public <T extends BetterJailsEvent> @NotNull EventSubscription<T> subscribe(
      final @NotNull Plugin plugin, final @NotNull Class<T> eventType,
      final @NotNull Consumer<? super T> handler) {
    final EventSubscription<T> subscription = new ApiEventSubscription<>(plugin, eventType, handler);
    this.subscriptions.get(eventType).add(subscription);
    return subscription;
  }

  @Override
  public <T extends BetterJailsEvent> void unsubscribe(final @NotNull EventSubscription<T> subscription) {
    this.subscriptions.get(subscription.eventType()).remove(subscription);
    subscription.unsubscribe();
  }

  @Override
  public void unsubscribe(final @NotNull Plugin plugin) {
    this.subscriptions.values().removeIf(subscription -> {
      if (plugin.equals(subscription.plugin())) {
        subscription.unsubscribe();
        return true;
      } else {
        return false;
      }
    });
  }

  @Override
  public <T extends BetterJailsEvent> void unsubscribe(final @NotNull Plugin plugin,
      final @NotNull Class<T> eventType) {
    this.subscriptions.get(eventType).removeIf(subscription -> {
      if (plugin.equals(subscription.plugin())) {
        subscription.unsubscribe();
        return true;
      } else {
        return false;
      }
    });
  }

  public void unsubscribeAll() {
    this.subscriptions.values().forEach(EventSubscription::unsubscribe);
    this.subscriptions.clear();
  }

  @Override
  public @NotNull @Unmodifiable Set<@NotNull EventSubscription<? extends BetterJailsEvent>> getSubscriptions(
      final @NotNull Plugin plugin) {
    return this.subscriptions.values().stream()
        .filter(subscription -> plugin.equals(subscription.plugin()))
        .collect(toImmutableSet());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends BetterJailsEvent> @NotNull @Unmodifiable Set<@NotNull EventSubscription<T>> getSubscriptions(
      final @NotNull Plugin plugin, final @NotNull Class<T> eventType) {
    return this.subscriptions.get(eventType).stream()
        .filter(subscription -> plugin.equals(subscription.plugin()))
        .map(subscription -> (EventSubscription<T>) subscription)
        .collect(toImmutableSet());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends BetterJailsEvent> @NotNull @Unmodifiable Set<@NotNull EventSubscription<? extends T>> getAllSubscriptions(
      final @NotNull Plugin plugin, final @NotNull Class<T> eventType) {
    return this.subscriptions.values().stream()
        .filter(subscription -> plugin.equals(subscription.plugin()))
        .filter(subscription -> subscription.eventType().isAssignableFrom(eventType))
        .map(subscription -> (EventSubscription<? extends T>) subscription)
        .collect(toImmutableSet());
  }

  @SuppressWarnings("unchecked")
  public <T extends BetterJailsEvent> T post(final Class<T> type, final Object... args) {
    Objects.requireNonNull(this.api, "api");

    final T event;
    try {
      final Object[] allArgs = new Object[args.length + 2];
      allArgs[0] = this.api;
      allArgs[1] = type;
      System.arraycopy(args, 0, allArgs, 2, args.length);
      event = type.cast(KNOWN_EVENT_TYPES.get(type).newInstance(allArgs));
    } catch (final Throwable throwable) {
      throw new Error("Unknown event type " + type, throwable);
    }

    final Iterator<Map.Entry<Class<? extends BetterJailsEvent>, EventSubscription<? extends BetterJailsEvent>>> iterator =
        this.subscriptions.entries().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<Class<? extends BetterJailsEvent>, EventSubscription<? extends BetterJailsEvent>> entry = iterator.next();
      final Class<? extends BetterJailsEvent> eventType = entry.getKey();
      final EventSubscription<? extends BetterJailsEvent> subscription = entry.getValue();

      if (subscription.isNotActive()) {
        iterator.remove();
        continue;
      }

      if (eventType.isInstance(event)) {
        ((EventSubscription<T>) subscription).handler().accept(event);
      }
    }

    return event;
  }
}
