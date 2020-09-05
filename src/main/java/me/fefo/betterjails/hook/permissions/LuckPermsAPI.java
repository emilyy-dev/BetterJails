package me.fefo.betterjails.hook.permissions;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import me.fefo.facilites.TaskUtil;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.Flag;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;

import org.apache.commons.lang.Validate;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public class LuckPermsAPI extends PermissionsAPI {
  private final UserManager userManager;
  private final GroupManager groupManager;

  public static void instantiate(final Plugin plugin, final LuckPerms luckPerms) throws InstantiationException {
    new LuckPermsAPI(plugin, luckPerms);
  }

  private LuckPermsAPI(final Plugin plugin, final LuckPerms luckPerms) throws InstantiationException {
    super(plugin);
    Validate.notNull(luckPerms);

    permissionsAPI = this;
    this.userManager = luckPerms.getUserManager();
    this.groupManager = luckPerms.getGroupManager();
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final OfflinePlayer player) {
    return userManager.loadUser(player.getUniqueId(), player.getName())
                      .thenApplyAsync(user -> user.getInheritedGroups(QueryOptions.builder(QueryMode.NON_CONTEXTUAL)
                                                                                  .flag(Flag.RESOLVE_INHERITANCE, false)
                                                                                  .build()))
                      .thenApplyAsync(groups -> groups.stream()
                                                      .map(Group::getName)
                                                      .collect(Collectors.toList()));
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final UUID uuid) {
    return userManager.loadUser(uuid)
                      .thenApplyAsync(user -> user.getNodes(NodeType.INHERITANCE))
                      .thenApplyAsync(nodes -> nodes.stream()
                                                    .map(InheritanceNode::getGroupName)
                                                    .collect(Collectors.toList()));
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final String playername) {
    final CompletableFuture<Collection<String>> future = new CompletableFuture<>();

    TaskUtil.async(() -> {
      final OfflinePlayer player = Bukkit.getOfflinePlayer(playername);
      future.complete(getParentGroups(player).join());
    });

    return future;
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final OfflinePlayer player, final String group) {
    return setParentGroup(player.getUniqueId(), group);
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final UUID uuid, final String group) {
    return userManager.modifyUser(uuid, user -> {
      final Group g = groupManager.getGroup(group);
      if (g == null) {
        return;
      }

      user.data().clear(NodeType.INHERITANCE::matches);
      user.data().add(InheritanceNode.builder(g).build());
    });
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final String playername, final String group) {
    final CompletableFuture<Void> future = new CompletableFuture<>();

    TaskUtil.async(() -> {
      final OfflinePlayer player = Bukkit.getOfflinePlayer(playername);
      future.complete(setParentGroup(player.getUniqueId(), group).join());
    });

    return future;
  }
}
