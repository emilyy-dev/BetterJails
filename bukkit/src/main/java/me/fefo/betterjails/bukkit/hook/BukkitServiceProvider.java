package me.fefo.betterjails.bukkit.hook;

import me.fefo.betterjails.common.BetterJailsPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiFunction;

public abstract class BukkitServiceProvider {

  public static <T, U> Optional<U> hook(@NotNull final BetterJailsPlugin plugin,
                                        @NotNull final Class<T> serviceClass, @NotNull final String serviceName,
                                        @NotNull final BiFunction<? super T, ? super BetterJailsPlugin, ? extends U> serviceConsumerProvider) {
    Validate.notNull(plugin);
    Validate.notNull(serviceClass);
    Validate.notNull(serviceName);
    Validate.notNull(serviceConsumerProvider);

    if (Bukkit.getPluginManager().isPluginEnabled(serviceName)) {
      plugin.getPluginLogger().info("Hooking with " + serviceName + "...");
      final RegisteredServiceProvider<T> serviceProvider = Bukkit.getServicesManager().getRegistration(serviceClass);
      if (serviceProvider != null) {
        final U serviceConsumer = serviceConsumerProvider.apply(serviceProvider.getProvider(), plugin);
        return Optional.of(serviceConsumer);
      }

      plugin.getPluginLogger().warn(serviceName + " was found but could not hook with it!");
    }

    return Optional.empty();
  }

}
