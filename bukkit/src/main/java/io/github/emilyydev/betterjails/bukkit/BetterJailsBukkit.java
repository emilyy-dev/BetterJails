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

package io.github.emilyydev.betterjails.bukkit;

import io.github.emilyydev.betterjails.bukkit.command.BukkitCommandAdapter;
import io.github.emilyydev.betterjails.bukkit.platform.BukkitPlatformAdapter;
import io.github.emilyydev.betterjails.bukkit.platform.BukkitTaskScheduler;
import io.github.emilyydev.betterjails.common.configuration.ConfigurationAdapter;
import io.github.emilyydev.betterjails.common.configuration.adapter.YamlConfigurationAdapter;
import io.github.emilyydev.betterjails.common.message.Subject;
import io.github.emilyydev.betterjails.common.plugin.BetterJailsBootstrap;
import io.github.emilyydev.betterjails.common.plugin.BetterJailsPlugin;
import io.github.emilyydev.betterjails.common.plugin.abstraction.PlatformAdapter;
import io.github.emilyydev.betterjails.common.plugin.abstraction.TaskScheduler;
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
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class BetterJailsBukkit extends JavaPlugin implements BetterJailsBootstrap, Listener {

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

    new BukkitCommandAdapter(this);

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
  public Collection<io.github.emilyydev.betterjails.common.plugin.abstraction.Player<Player>> getOnlinePlayers() {
    return Bukkit.getOnlinePlayers().stream().map(this.platformAdapter::adaptPlayer).collect(Collectors.toList());
  }

  public <T extends Event> void registerListener(final Class<T> eventType, final Consumer<T> handler) {
    registerListener(eventType, handler, EventPriority.NORMAL, false);
  }

  public <T extends Event> void registerListener(final Class<T> eventType, final Consumer<T> handler, final boolean callIfCancelled) {
    registerListener(eventType, handler, EventPriority.NORMAL, callIfCancelled);
  }

  public <T extends Event> void registerListener(final Class<T> eventType, final Consumer<T> handler, final EventPriority priority) {
    registerListener(eventType, handler, priority, false);
  }

  public <T extends Event> void registerListener(final Class<T> eventType, final Consumer<T> handler, final EventPriority priority, final boolean ignoreIfCancelled) {
    Bukkit.getPluginManager().registerEvent(eventType, this, priority,
                                            (l, e) -> {
                                              if (eventType.isInstance(e)) {
                                                handler.accept(eventType.cast(e));
                                              }
                                            }, this, !ignoreIfCancelled);
  }
}
