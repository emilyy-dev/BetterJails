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

import io.github.emilyydev.betterjails.util.Util;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.actionlog.Action;
import net.luckperms.api.actionlog.ActionLogger;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletionStage;

final class LuckPermsPermissionInterface extends AbstractPermissionInterface {

  private final LuckPerms luckPerms;
  private final InheritanceNode prisonerGroupNode;

  LuckPermsPermissionInterface(final Server server, final String prisonerGroup) {
    super(prisonerGroup);
    this.luckPerms = server.getServicesManager().load(LuckPerms.class);
    this.prisonerGroupNode = InheritanceNode.builder(prisonerGroup).build();
  }

  @Override
  public CompletionStage<? extends String> fetchPrimaryGroup(final OfflinePlayer player) {
    return this.luckPerms.getUserManager().loadUser(player.getUniqueId()).thenApply(User::getPrimaryGroup);
  }

  @Override
  public CompletionStage<? extends Set<? extends String>> fetchParentGroups(final OfflinePlayer player) {
    return this.luckPerms.getUserManager().loadUser(player.getUniqueId()).thenApply(user ->
        user.getNodes(NodeType.INHERITANCE)
            .stream()
            .map(InheritanceNode::getGroupName)
            .collect(Util.toImmutableSet())
    );
  }

  @Override
  public CompletionStage<?> setPrisonerGroup(final OfflinePlayer player) {
    return this.luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
          final NodeMap nodeMap = user.data();
          // TODO consider non-contextual node removal? That renders a problem for later, as currently parent groups
          //  are stored as-is, no context information, therefore it is lost when re-adding the nodes back.
          //  Remove global nodes for now...
          nodeMap.clear(ImmutableContextSet.empty(), NodeType.INHERITANCE::matches);
          nodeMap.add(this.prisonerGroupNode);
        })
        .thenCompose($ -> {
          final ActionLogger actionLogger = this.luckPerms.getActionLogger();
          final Action.Builder builder = actionLogger.actionBuilder();
          builder.sourceName("BetterJails")
              .target(player.getUniqueId())
              .targetType(Action.Target.Type.USER)
              .timestamp(Instant.now())
              .description("clear global parents & set " + prisonerGroup());
          final String name = player.getName();
          if (name != null) {
            builder.targetName(name);
          }

          return actionLogger.submitToStorage(builder.build());
        });
  }

  @Override
  public CompletionStage<?> setParentGroups(
      final OfflinePlayer player,
      final Collection<? extends String> parentGroups
  ) {
    return this.luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
          final NodeMap nodeMap = user.data();
          nodeMap.remove(this.prisonerGroupNode);
          parentGroups.stream()
              .map(InheritanceNode::builder)
              .map(InheritanceNode.Builder::build)
              .forEach(nodeMap::add);
        })
        .thenCompose($ -> {
          final ActionLogger actionLogger = this.luckPerms.getActionLogger();
          final Action.Builder builder = actionLogger.actionBuilder();
          builder.sourceName("BetterJails")
              .target(player.getUniqueId())
              .targetType(Action.Target.Type.USER)
              .timestamp(Instant.now())
              .description("remove " + prisonerGroup() + " & re-add " + String.join(", ", parentGroups));
          final String name = player.getName();
          if (name != null) {
            builder.targetName(name);
          }

          return actionLogger.submitToStorage(builder.build());
        });
  }

  @Override
  public String name() {
    return "LuckPerms";
  }
}
