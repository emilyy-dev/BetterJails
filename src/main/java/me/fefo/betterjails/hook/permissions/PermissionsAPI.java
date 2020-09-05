package me.fefo.betterjails.hook.permissions;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import me.fefo.betterjails.BetterJails;

import net.luckperms.api.LuckPerms;

import net.milkbowl.vault.permission.Permission;

import org.apache.commons.lang.Validate;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PermissionsAPI {
  protected final Plugin plugin;
  protected static PermissionsAPI permissionsAPI = null;

  protected PermissionsAPI(final Plugin plugin) throws InstantiationException {
    if (permissionsAPI != null) {
      throw new InstantiationException("Cannot instantiate the permissions API more than once.");
    }

    this.plugin = plugin;
  }

  @Nullable
  public static PermissionsAPI getPermsApi() {
    return permissionsAPI;
  }

  public static boolean hook(@NotNull final BetterJails plugin) throws InstantiationException {
    Validate.notNull(plugin);

    if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
      plugin.getLogger().info("Hooking with LuckPerms...");
      final RegisteredServiceProvider<LuckPerms> luckPermsProvider = Bukkit.getServicesManager()
                                                                           .getRegistration(LuckPerms.class);
      if (luckPermsProvider != null) {
        LuckPermsAPI.instantiate(plugin, luckPermsProvider.getProvider());
        return true;
      }

      plugin.getLogger().warning("LuckPerms was found but could not hook with it!");
    }

    if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
      plugin.getLogger().info("Hooking with Vault...");
      final RegisteredServiceProvider<Permission> vaultPermsProvider = Bukkit.getServicesManager()
                                                                             .getRegistration(Permission.class);
      if (vaultPermsProvider != null) {
        VaultAPI.instantiate(plugin, vaultPermsProvider.getProvider());
        return true;
      }

      plugin.getLogger().warning("Vault was found but could not hook with it!");
    }

    return false;
  }

  public abstract CompletableFuture<Collection<String>> getParentGroups(final OfflinePlayer player);
  public abstract CompletableFuture<Collection<String>> getParentGroups(final UUID uuid);
  public abstract CompletableFuture<Collection<String>> getParentGroups(final String playername);
  public abstract CompletableFuture<Void> setParentGroup(final OfflinePlayer player, final String group);
  public abstract CompletableFuture<Void> setParentGroup(final UUID uuid, final String group);
  public abstract CompletableFuture<Void> setParentGroup(final String playername, final String group);

}
