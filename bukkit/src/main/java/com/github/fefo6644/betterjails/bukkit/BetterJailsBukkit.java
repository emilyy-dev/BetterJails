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

package com.github.fefo6644.betterjails.bukkit;

import com.github.fefo6644.betterjails.bukkit.platform.BukkitPlatformAdapter;
import com.github.fefo6644.betterjails.bukkit.platform.BukkitTaskScheduler;
import com.github.fefo6644.betterjails.common.configuration.ConfigurationAdapter;
import com.github.fefo6644.betterjails.common.configuration.adapter.YamlConfigurationAdapter;
import com.github.fefo6644.betterjails.common.message.MessagingSubject;
import com.github.fefo6644.betterjails.common.platform.BetterJailsBootstrap;
import com.github.fefo6644.betterjails.common.platform.BetterJailsPlugin;
import com.github.fefo6644.betterjails.common.platform.abstraction.PlatformAdapter;
import com.github.fefo6644.betterjails.common.platform.abstraction.TaskScheduler;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public final class BetterJailsBukkit extends JavaPlugin implements BetterJailsBootstrap {

  private BukkitAudiences audiences;
  private MessagingSubject console;

  private final BukkitPlatformAdapter platformAdapter = new BukkitPlatformAdapter(this);
  private final BetterJailsPlugin betterJailsPlugin = new BetterJailsPlugin(this);
  private final ConfigurationAdapter configurationAdapter = new YamlConfigurationAdapter(this.betterJailsPlugin, getPluginFolder());
  private final TaskScheduler taskScheduler = new BukkitTaskScheduler(this.betterJailsPlugin);

  @Override
  public void onEnable() {
    this.audiences = BukkitAudiences.create(this);
    this.console = MessagingSubject.of(this.audiences.console(), Bukkit.getConsoleSender().getName());

//    final Metrics metrics = new Metrics(this, 9015);
//    metrics.addCustomChart(new Metrics.SingleLineChart("total-jails", null));
  }

  @Override
  public void onDisable() {
    this.betterJailsPlugin.disable();
  }

  @Override
  public @NotNull AudienceProvider getAudienceProvider() {
    return this.audiences;
  }

  @Override
  public @NotNull MessagingSubject getConsoleSubject() {
    return this.console;
  }

  @Override
  public @NotNull TaskScheduler getTaskScheduler() {
    return this.taskScheduler;
  }

  @Override
  public @NotNull ConfigurationAdapter getConfigurationAdapter() {
    return this.configurationAdapter;
  }

  @Override
  public @NotNull Path getPluginFolder() {
    return getDataFolder().toPath();
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull PlatformAdapter<CommandSender, Player, Location, World> getPlatformAdapter() {
    return this.platformAdapter;
  }

  @Override
  public @NotNull String getVersion() {
    return getDescription().getVersion();
  }

  @Override
  public @NotNull List<com.github.fefo6644.betterjails.common.platform.abstraction.Player> getOnlinePlayers() {
    final ImmutableList.Builder<com.github.fefo6644.betterjails.common.platform.abstraction.Player> builder = ImmutableList.builder();
    Bukkit.getOnlinePlayers().forEach(player -> builder.add(this.platformAdapter.adaptPlayer(player)));
    return builder.build();
  }
}
