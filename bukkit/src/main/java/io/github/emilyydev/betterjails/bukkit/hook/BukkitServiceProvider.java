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

package io.github.emilyydev.betterjails.bukkit.hook;

import io.github.emilyydev.betterjails.common.plugin.BetterJailsPlugin;
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
      plugin.getLogger().info("Hooking with " + serviceName + "...");
      final RegisteredServiceProvider<T> serviceProvider = Bukkit.getServicesManager().getRegistration(serviceClass);
      if (serviceProvider != null) {
        final U serviceConsumer = serviceConsumerProvider.apply(serviceProvider.getProvider(), plugin);
        return Optional.of(serviceConsumer);
      }

      plugin.getLogger().warn(serviceName + " was found but could not hook with it!");
    }

    return Optional.empty();
  }

}
