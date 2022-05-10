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

package io.github.emilyydev.betterjails.interfaces;

import com.google.common.collect.ImmutableSet;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

final class VaultPermissionInterface extends AbstractPermissionInterface {

  private static <T> CompletionStage<? extends T> completed(final T value) {
    return CompletableFuture.completedFuture(value);
  }

  private final Permission permission;

  VaultPermissionInterface(final Server server, final String prisonerGroup) {
    super(prisonerGroup);
    this.permission = server.getServicesManager().load(Permission.class);
  }

  @Override
  public CompletionStage<? extends String> fetchPrimaryGroup(final OfflinePlayer player) {
    return completed(this.permission.getPrimaryGroup(null, player));
  }

  @Override
  public CompletionStage<? extends Set<? extends String>> fetchParentGroups(final OfflinePlayer player) {
    return completed(ImmutableSet.copyOf(this.permission.getPlayerGroups(null, player)));
  }

  @Override
  public CompletionStage<?> setPrisonerGroup(final OfflinePlayer player) {
    return fetchParentGroups(player).thenAccept(parentGroups -> {
      this.permission.playerAddGroup(null, player, prisonerGroup());
      parentGroups.forEach(group -> this.permission.playerRemoveGroup(null, player, group));
    });
  }

  @Override
  public CompletionStage<?> setParentGroups(
      final OfflinePlayer player,
      final Collection<? extends String> parentGroups
  ) {
    parentGroups.forEach(group -> this.permission.playerAddGroup(null, player, group));
    this.permission.playerRemoveGroup(null, player, prisonerGroup());
    return completed(null);
  }

  @Override
  public String name() {
    return "Vault";
  }
}
