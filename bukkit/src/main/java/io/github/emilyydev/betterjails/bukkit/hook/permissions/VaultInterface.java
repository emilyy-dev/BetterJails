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

package io.github.emilyydev.betterjails.bukkit.hook.permissions;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import io.github.emilyydev.betterjails.bukkit.hook.BukkitServiceProvider;
import io.github.emilyydev.betterjails.common.hook.permissions.PermissionPlatformInterface;
import io.github.emilyydev.betterjails.common.plugin.BetterJailsPlugin;
import io.github.emilyydev.betterjails.common.plugin.abstraction.TaskScheduler;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getOfflinePlayer;

public final class VaultInterface implements PermissionPlatformInterface {

  public static Optional<VaultInterface> hook(final BetterJailsPlugin plugin) {
    return BukkitServiceProvider.hook(plugin, Permission.class, VaultInterface::new);
  }

  private final Permission vault;
  private final TaskScheduler scheduler;

  private VaultInterface(final Permission vault, final BetterJailsPlugin plugin) {
    this.vault = Objects.requireNonNull(vault, "vault");
    this.scheduler = plugin.getTaskScheduler();
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final UUID uuid) {
    return CompletableFuture.supplyAsync(() -> ImmutableList.copyOf(this.vault.getPlayerGroups(null, getOfflinePlayer(uuid))), this.scheduler::async);
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final UUID uuid, final String groupName) {
    return getParentGroups(uuid).thenAcceptAsync(groups -> {
      final OfflinePlayer player = getOfflinePlayer(uuid);
      this.vault.playerAddGroup(null, player, groupName);
      groups.stream().filter(Predicates.not(groupName::equals)).collect(Collectors.toList())
            .forEach(group -> this.vault.playerRemoveGroup(null, player, group));
    }, this.scheduler::async);
  }

  @Override
  public CompletableFuture<Void> addParentGroup(final UUID uuid, final String groupName) {
    return CompletableFuture.runAsync(() -> this.vault.playerAddGroup(null, getOfflinePlayer(uuid), groupName), this.scheduler::async);
  }

  @Override
  public CompletableFuture<Void> removeParentGroup(final UUID uuid, final String groupName) {
    return CompletableFuture.runAsync(() -> this.vault.playerRemoveGroup(null, getOfflinePlayer(uuid), groupName), this.scheduler::async);
  }
}
