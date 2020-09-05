package me.fefo.betterjails.hook.permissions;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import me.fefo.facilites.TaskUtil;

import net.milkbowl.vault.permission.Permission;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public class VaultAPI extends PermissionsAPI {
  private final Permission vaultPerm;

  public static void instantiate(final Plugin plugin, final Permission vaultPerm) throws InstantiationException {
    new VaultAPI(plugin, vaultPerm);
  }

  private VaultAPI(final Plugin plugin, final Permission vaultPerm) throws InstantiationException {
    super(plugin);
    Validate.notNull(vaultPerm);

    permissionsAPI = this;
    this.vaultPerm = vaultPerm;
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final OfflinePlayer player) {
    final CompletableFuture<Collection<String>> future = new CompletableFuture<>();

    TaskUtil.async(() -> future.complete(Arrays.asList(vaultPerm.getPlayerGroups(null, player))));

    return future;
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final UUID uuid) {
    final CompletableFuture<Collection<String>> future = new CompletableFuture<>();

    TaskUtil.async(() -> {
      final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
      future.complete(Arrays.asList(vaultPerm.getPlayerGroups(null, player)));
    });

    return future;
  }

  @Override
  public CompletableFuture<Collection<String>> getParentGroups(final String playername) {
    final CompletableFuture<Collection<String>> future = new CompletableFuture<>();

    TaskUtil.async(() -> {
      final OfflinePlayer player = Bukkit.getOfflinePlayer(playername);
      future.complete(Arrays.asList(vaultPerm.getPlayerGroups(null, player)));
    });

    return future;
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final OfflinePlayer player, final String group) {
    final CompletableFuture<Void> future = new CompletableFuture<>();

    TaskUtil.async(() -> {
      for (final String g : vaultPerm.getPlayerGroups(null, player)) {
        vaultPerm.playerRemoveGroup(null, player, g);
      }
      vaultPerm.playerAddGroup(null, player, group);
      future.complete(null);
    });

    return future;
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final UUID uuid, final String group) {
    final CompletableFuture<Void> future = new CompletableFuture<>();

    TaskUtil.async(() -> {
      final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
      future.complete(setParentGroup(player, group).join());
    });

    return future;
  }

  @Override
  public CompletableFuture<Void> setParentGroup(final String playername, final String group) {
    final CompletableFuture<Void> future = new CompletableFuture<>();

    TaskUtil.async(() -> {
      final OfflinePlayer player = Bukkit.getOfflinePlayer(playername);
      future.complete(setParentGroup(player, group).join());
    });

    return future;
  }
}
