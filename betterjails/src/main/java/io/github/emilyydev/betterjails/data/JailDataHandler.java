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

package io.github.emilyydev.betterjails.data;

import com.github.fefo.betterjails.api.event.jail.JailCreateEvent;
import com.github.fefo.betterjails.api.event.jail.JailDeleteEvent;
import com.github.fefo.betterjails.api.model.jail.Jail;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.api.impl.model.jail.ApiJail;
import io.github.emilyydev.betterjails.interfaces.storage.StorageAccess;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class JailDataHandler {
  private final BetterJailsPlugin plugin;
  private final StorageAccess storage;
  private final Map<String, Jail> jails = new HashMap<>();

  public JailDataHandler(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
    this.storage = plugin.storageAccess();
  }

  public void load() throws IOException {
    this.jails.clear();
    loadJails();
  }

  private void loadJails() throws IOException {
    try {
      this.jails.putAll(this.storage.loadJails().get());
    } catch (final InterruptedException ex) {
      // bleh
    } catch (final ExecutionException ex) {
      throw new IOException(ex.getCause());
    }
  }

  public CompletableFuture<Void> save() {
    return this.storage.saveJails(this.jails);
  }

  public Map<String, Jail> getJails() {
    return this.jails;
  }

  public @Nullable Jail getJail(final String name) {
    return this.jails.get(name.toLowerCase(Locale.ROOT));
  }

  public CompletableFuture<Void> addJail(final String name, final ImmutableLocation location) {
    final String lowerCaseName = name.toLowerCase(Locale.ROOT);
    this.jails.computeIfAbsent(lowerCaseName, key -> new ApiJail(key, location, null)).location(location);
    this.plugin.eventBus().post(JailCreateEvent.class, name, location);
    return save();
  }

  public CompletableFuture<Void> removeJail(final Jail jail) {
    this.jails.remove(jail.name().toLowerCase(Locale.ROOT));
    this.plugin.eventBus().post(JailDeleteEvent.class, jail);
    return save();
  }
}
