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

package com.github.fefo6644.betterjails.common.hook.permissions.luckperms;

import com.github.fefo6644.betterjails.common.platform.BetterJailsPlugin;
import com.github.fefo6644.betterjails.common.platform.abstraction.Player;
import com.github.fefo6644.betterjails.common.hook.permissions.PermissionsHook;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.messaging.MessagingService;
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
  private final MessagingService messagingService;

  public LuckPermsHook(final LuckPerms luckPerms, final BetterJailsPlugin plugin) {
    Validate.notNull(luckPerms);
    this.userManager = luckPerms.getUserManager();
    this.groupManager = luckPerms.getGroupManager();
    this.messagingService = luckPerms.getMessagingService().orElse(null);
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final Player player) {
    userManager.modifyUser(player.uuid(), user -> {
      // do stuff
    }).thenRun(() -> {
      if (messagingService != null) {
        messagingService.pushUpdate();
      }
    });

    return userManager.loadUser(player.uuid(), player.getName())
                      .thenApply(user -> user.getInheritedGroups(user.getQueryOptions().toBuilder()
                                                                     .flag(Flag.RESOLVE_INHERITANCE,
                                                                           false)
                                                                     .build()))
                      .thenApply(groups -> groups.stream()
                                                 .map(Group::getName)
                                                 .collect(Collectors.toList()));
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final UUID uuid) {
    return userManager.loadUser(uuid)
                      .thenApply(user -> user.getInheritedGroups(user.getQueryOptions().toBuilder()
                                                                     .flag(Flag.RESOLVE_INHERITANCE,
                                                                           false)
                                                                     .build()))
                      .thenApply(groups -> groups.stream()
                                                 .map(Group::getName)
                                                 .collect(Collectors.toList()));
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final Player player, final String group) {
    return setParentGroup(player.uuid(), group);
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
