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

package io.github.emilyydev.betterjails.common.hook.permissions.luckperms;

import io.github.emilyydev.betterjails.common.hook.permissions.PermissionPlatformInterface;
import io.github.emilyydev.betterjails.common.plugin.BetterJailsPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.Flag;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class LuckPermsInterface implements PermissionPlatformInterface {

  public static LuckPermsInterface hook(final BetterJailsPlugin plugin, final Supplier<LuckPerms> luckPermsSupplier) {
    return new LuckPermsInterface(luckPermsSupplier.get(), plugin);
  }

  private final LuckPerms luckPerms;
  private final UserManager userManager;
  private final GroupManager groupManager;

  private LuckPermsInterface(final LuckPerms luckPerms, final BetterJailsPlugin plugin) {
    this.luckPerms = Objects.requireNonNull(luckPerms, "luckPerms");
    this.userManager = luckPerms.getUserManager();
    this.groupManager = luckPerms.getGroupManager();
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final UUID uuid) {
    return this.userManager
        .loadUser(uuid)
        .thenApply(user -> user.getInheritedGroups(user.getQueryOptions().toBuilder().flag(Flag.RESOLVE_INHERITANCE, false).build()))
        .thenApply(groups -> groups.stream().map(Group::getName).collect(Collectors.toList()));
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final UUID uuid, final String groupName) {
    return this.userManager.modifyUser(uuid, user -> {
      final Group group = this.groupManager.getGroup(groupName);
      if (group != null) {
        user.data().clear(user.getQueryOptions().context(), NodeType.INHERITANCE::matches);
        user.data().add(InheritanceNode.builder(group).build());
      }
    }).thenComposeAsync(v -> pushUserUpdate(uuid));
  }

  @Override
  public CompletableFuture<Void> addParentGroup(final UUID uuid, final String groupName) {
    return this.userManager.modifyUser(uuid, user -> {
      final Group group = this.groupManager.getGroup(groupName);
      if (group != null) { user.data().add(InheritanceNode.builder(group).build()); }
    }).thenComposeAsync(v -> pushUserUpdate(uuid));
  }

  @Override
  public CompletableFuture<Void> removeParentGroup(final UUID uuid, final String groupName) {
    return this.userManager.modifyUser(uuid, user -> {
      final Group group = this.groupManager.getGroup(groupName);
      if (group != null) { user.data().remove(InheritanceNode.builder(group).build()); }
    }).thenComposeAsync(v -> pushUserUpdate(uuid));
  }

  private CompletableFuture<Void> pushUserUpdate(final UUID uuid) {
    return this.userManager.loadUser(uuid).thenAcceptAsync(user -> this.luckPerms.getMessagingService().ifPresent(service -> service.pushUserUpdate(user)));
  }
}
