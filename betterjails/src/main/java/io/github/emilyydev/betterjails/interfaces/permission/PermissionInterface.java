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

package io.github.emilyydev.betterjails.interfaces.permission;

import io.github.emilyydev.betterjails.BetterJailsPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface PermissionInterface extends AutoCloseable {

  PermissionInterface NULL = new PermissionInterface() {

    @Override
    public void close() {
    }

    @Override
    public String name() {
      return "null";
    }

    @Override
    public CompletionStage<? extends String> fetchPrimaryGroup(final OfflinePlayer player) {
      return failedStage();
    }

    @Override
    public CompletionStage<? extends Set<? extends String>> fetchParentGroups(final OfflinePlayer player) {
      return failedStage();
    }

    @Override
    public CompletionStage<?> setPrisonerGroup(final OfflinePlayer player, final UUID source, final String sourceName) {
      return failedStage();
    }

    @Override
    public CompletionStage<?> setParentGroups(
        final OfflinePlayer player,
        final Collection<? extends String> parentGroups,
        final UUID source,
        final String sourceName
    ) {
      return failedStage();
    }

    private <T> CompletionStage<? extends T> failedStage() {
      final CompletableFuture<T> future = new CompletableFuture<>();
      future.completeExceptionally(new UnsupportedOperationException());
      return future;
    }
  };

  static PermissionInterface determinePermissionInterface(final BetterJailsPlugin plugin, final String prisonerGroup) {
    final PluginManager pluginManager = plugin.getServer().getPluginManager();
    if (pluginManager.isPluginEnabled("LuckPerms")) {
      return new LuckPermsPermissionInterface(plugin.getServer(), prisonerGroup);
    } else if (pluginManager.isPluginEnabled("Vault")) {
      return new VaultPermissionInterface(plugin);
    } else {
      return NULL;
    }
  }

  String name();

  void close();

  CompletionStage<? extends String> fetchPrimaryGroup(OfflinePlayer player);

  CompletionStage<? extends Set<? extends String>> fetchParentGroups(OfflinePlayer player);

  CompletionStage<?> setPrisonerGroup(OfflinePlayer player, UUID source, String sourceName);

  CompletionStage<?> setParentGroups(OfflinePlayer player, Collection<? extends String> parentGroups, UUID source, String sourceName);
}
