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

package me.fefo.betterjails.common.hook.permissions.luckperms;

import me.fefo.betterjails.common.BetterJailsPlugin;
import me.fefo.betterjails.common.abstraction.Player;
import me.fefo.betterjails.common.hook.permissions.PermissionsHook;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.Flag;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LuckPermsHook extends PermissionsHook {

  public static Optional<LuckPermsHook> hook(@NotNull final BetterJailsPlugin plugin) {
    final LuckPermsHook lpHook = new LuckPermsHook(LuckPermsProvider.get(), plugin);
    return Optional.of(lpHook);
  }

  private final UserManager userManager;
  private final GroupManager groupManager;

  public LuckPermsHook(final LuckPerms luckPerms, final BetterJailsPlugin plugin) {
    Validate.notNull(luckPerms);
    this.userManager = luckPerms.getUserManager();
    this.groupManager = luckPerms.getGroupManager();
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final Player player) {
    return userManager.loadUser(player.getUuid(), player.getName())
                      .thenApply(user -> user.getInheritedGroups(user.getQueryOptions().toBuilder()
                                                                     .flag(Flag.RESOLVE_INHERITANCE, false)
                                                                     .build()))
                      .thenApply(groups -> groups.stream().map(Group::getName).collect(Collectors.toList()));
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final UUID uuid) {
    return userManager.loadUser(uuid)
                      .thenApply(user -> user.getInheritedGroups(user.getQueryOptions().toBuilder()
                                                                     .flag(Flag.RESOLVE_INHERITANCE, false)
                                                                     .build()))
                      .thenApply(groups -> groups.stream().map(Group::getName).collect(Collectors.toList()));
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final Player player, final String group) {
    return setParentGroup(player.getUuid(), group);
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final UUID uuid, final String group) {
    return userManager.modifyUser(uuid, user -> {
      final Group g = groupManager.getGroup(group);
      if (g == null) {
        return;
      }

      user.data().clear(user.getQueryOptions().context(), NodeType.INHERITANCE::matches);
      user.data().add(InheritanceNode.builder(g).build());
    });
  }

}
