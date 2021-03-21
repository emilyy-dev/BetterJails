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

package com.github.fefo6644.betterjails.bukkit.hook.permissions;

import com.github.fefo6644.betterjails.bukkit.hook.BukkitServiceProvider;
import com.github.fefo6644.betterjails.common.hook.permissions.PermissionsHook;
import com.github.fefo6644.betterjails.common.plugin.BetterJailsPlugin;
import com.github.fefo6644.betterjails.common.plugin.abstraction.PlatformAdapter;
import com.github.fefo6644.betterjails.common.plugin.abstraction.TaskScheduler;
import com.google.common.collect.ImmutableList;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VaultHook extends PermissionsHook {

  public static Optional<VaultHook> hook(@NotNull final BetterJailsPlugin plugin) {
    return BukkitServiceProvider.hook(plugin, Permission.class, "Vault", VaultHook::new);
  }

  private final Permission vaultPerm;
  private final PlatformAdapter<CommandSender, Player, Location, World> adapter;
  private final TaskScheduler scheduler;

  public VaultHook(final Permission vaultPerm, final BetterJailsPlugin plugin) {
    Validate.notNull(vaultPerm);
    this.vaultPerm = vaultPerm;
    this.adapter = plugin.getPlatformAdapter();
    this.scheduler = plugin.getTaskScheduler();
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final com.github.fefo6644.betterjails.common.plugin.abstraction.Player player) {
    return CompletableFuture.supplyAsync(() -> ImmutableList.copyOf(this.vaultPerm.getPlayerGroups(this.adapter.adaptPlayer(player))),
                                         this.scheduler::async);
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final UUID uuid) {
    return CompletableFuture.supplyAsync(() -> ImmutableList.copyOf(this.vaultPerm.getPlayerGroups(Bukkit.getPlayer(uuid))),
                                         this.scheduler::async);
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final com.github.fefo6644.betterjails.common.plugin.abstraction.Player player, final String group) {
    return CompletableFuture.runAsync(() -> {

      final Player p = this.adapter.adaptPlayer(player);

      for (final String g : this.vaultPerm.getPlayerGroups(p)) {
        this.vaultPerm.playerRemoveGroup(p, g);
      }
      this.vaultPerm.playerAddGroup(p, group);

    }, this.scheduler::async);
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final UUID uuid, final String group) {
    return CompletableFuture.runAsync(() -> {

      final Player player = Bukkit.getPlayer(uuid);
      setParentGroup(this.adapter.adaptPlayer(player), group).join();

    }, this.scheduler::async);
  }
}
