/*
 * This file is part of the BetterJails (https://github.com/Fefo6644/BetterJails).
 *
 *  Copyright (c) 2020 Fefo6644 <federico.lopez.1999@outlook.com>
 *  Copyright (c) 2020 contributors
 *
 *  BetterJails is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 3.
 *
 *  BetterJails is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.fefo.betterjails.bukkit.hook.permissions;

import me.fefo.betterjails.bukkit.hook.BukkitServiceProvider;
import me.fefo.betterjails.common.BetterJailsPlugin;
import me.fefo.betterjails.common.abstraction.PlatformAdapter;
import me.fefo.betterjails.common.abstraction.PlatformScheduler;
import me.fefo.betterjails.common.hook.permissions.PermissionsHook;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VaultHook extends PermissionsHook {

  public static Optional<VaultHook> hook(@NotNull final BetterJailsPlugin plugin) {
    return BukkitServiceProvider.hook(plugin, Permission.class, "Vault", VaultHook::new);
  }

  private final Permission vaultPerm;
  private final PlatformAdapter<Player, Location, World> adapter;
  private final PlatformScheduler<BukkitTask> scheduler;

  public VaultHook(final Permission vaultPerm, final BetterJailsPlugin plugin) {
    Validate.notNull(vaultPerm);
    this.vaultPerm = vaultPerm;
    this.adapter = plugin.getPlatformAdapter();
    this.scheduler = plugin.getPlatformScheduler(BukkitTask.class);
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final me.fefo.betterjails.common.abstraction.Player player) {
    return CompletableFuture.supplyAsync(() -> Arrays.asList(vaultPerm.getPlayerGroups(adapter.adaptPlayer(player))),
                                         scheduler::async);
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final UUID uuid) {
    return CompletableFuture.supplyAsync(() -> Arrays.asList(vaultPerm.getPlayerGroups(Bukkit.getPlayer(uuid))),
                                         scheduler::async);
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final me.fefo.betterjails.common.abstraction.Player player, final String group) {
    return CompletableFuture.runAsync(() -> {

      final Player p = adapter.adaptPlayer(player);

      for (final String g : vaultPerm.getPlayerGroups(p)) {
        vaultPerm.playerRemoveGroup(p, g);
      }
      vaultPerm.playerAddGroup(p, group);

    }, scheduler::async);
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final UUID uuid, final String group) {
    return CompletableFuture.runAsync(() -> {

      final Player player = Bukkit.getPlayer(uuid);
      setParentGroup(adapter.adaptPlayer(player), group).join();

    }, scheduler::async);
  }
}
