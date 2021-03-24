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

import com.github.fefo6644.betterjails.bukkit.command.CommandHandler;
import com.github.fefo6644.betterjails.bukkit.platform.BukkitPlatformAdapter;
import com.github.fefo6644.betterjails.bukkit.platform.BukkitTaskScheduler;
import com.github.fefo6644.betterjails.common.configuration.ConfigurationAdapter;
import com.github.fefo6644.betterjails.common.configuration.adapter.YamlConfigurationAdapter;
import com.github.fefo6644.betterjails.common.message.Subject;
import com.github.fefo6644.betterjails.common.plugin.BetterJailsBootstrap;
import com.github.fefo6644.betterjails.common.plugin.BetterJailsPlugin;
import com.github.fefo6644.betterjails.common.plugin.abstraction.PlatformAdapter;
import com.github.fefo6644.betterjails.common.plugin.abstraction.TaskScheduler;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public final class BetterJailsBukkit extends JavaPlugin implements BetterJailsBootstrap {

  private BukkitAudiences audiences;
  private Subject console;

  private final BukkitPlatformAdapter platformAdapter = new BukkitPlatformAdapter(this);
  private final BetterJailsPlugin betterJailsPlugin = new BetterJailsPlugin(this);
  private final ConfigurationAdapter configurationAdapter = new YamlConfigurationAdapter(this.betterJailsPlugin, getPluginFolder());
  private final TaskScheduler taskScheduler = new BukkitTaskScheduler(this.betterJailsPlugin);

  @Override
  public void onLoad() {
    this.betterJailsPlugin.load();
  }

  @Override
  public void onEnable() {
    this.audiences = BukkitAudiences.create(this);
    this.console = Subject.of(this.audiences.console(), Bukkit.getConsoleSender().getName(), true);

    this.betterJailsPlugin.enable();

    new CommandHandler(this);

//    final Metrics metrics = new Metrics(this, 9015);
//    metrics.addCustomChart(new Metrics.SingleLineChart("total-jails", null));
  }

  @Override
  public void onDisable() {
    this.betterJailsPlugin.disable();
  }

  @Override
  public BetterJailsPlugin getPlugin() {
    return this.betterJailsPlugin;
  }

  @Override
  public AudienceProvider getAudienceProvider() {
    return this.audiences;
  }

  @Override
  public Subject getConsoleSubject() {
    return this.console;
  }

  @Override
  public TaskScheduler getTaskScheduler() {
    return this.taskScheduler;
  }

  @Override
  public ConfigurationAdapter getConfigurationAdapter() {
    return this.configurationAdapter;
  }

  @Override
  public Path getPluginFolder() {
    return getDataFolder().toPath();
  }

  @Override
  @SuppressWarnings("unchecked")
  public PlatformAdapter<CommandSender, Player, Location, World> getPlatformAdapter() {
    return this.platformAdapter;
  }

  @Override
  public String getVersion() {
    return getDescription().getVersion();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<com.github.fefo6644.betterjails.common.plugin.abstraction.Player<Player>> getOnlinePlayers() {
    final ImmutableList.Builder<com.github.fefo6644.betterjails.common.plugin.abstraction.Player<Player>> builder = ImmutableList.builder();
    Bukkit.getOnlinePlayers().forEach(player -> builder.add(this.platformAdapter.adaptPlayer(player)));
    return builder.build();
  }

  public <T extends Event> void registerListener(final Class<T> eventType, final Listener listener, final Consumer<T> handler) {
    registerListener(eventType, listener, handler, EventPriority.NORMAL, false);
  }

  public <T extends Event> void registerListener(final Class<T> eventType, final Listener listener, final Consumer<T> handler, final boolean ignoreIfCancelled) {
    registerListener(eventType, listener, handler, EventPriority.NORMAL, ignoreIfCancelled);
  }

  public <T extends Event> void registerListener(final Class<T> eventType, final Listener listener, final Consumer<T> handler, final EventPriority priority) {
    registerListener(eventType, listener, handler, priority, false);
  }

  public <T extends Event> void registerListener(final Class<T> eventType, final Listener listener, final Consumer<T> handler, final EventPriority priority, final boolean ignoreIfCancelled) {
    Bukkit.getPluginManager().registerEvent(eventType, listener, priority, (l, e) -> handler.accept(eventType.cast(e)), this, ignoreIfCancelled);
  }
}
