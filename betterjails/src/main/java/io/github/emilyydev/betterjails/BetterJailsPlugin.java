//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2024 emilyy-dev
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

package io.github.emilyydev.betterjails;

import com.github.fefo.betterjails.api.BetterJails;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import io.github.emilyydev.betterjails.api.impl.BetterJailsApi;
import io.github.emilyydev.betterjails.api.impl.event.ApiEventBus;
import io.github.emilyydev.betterjails.api.impl.model.jail.ApiJailManager;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisonerManager;
import io.github.emilyydev.betterjails.commands.CommandHandler;
import io.github.emilyydev.betterjails.commands.CommandTabCompleter;
import io.github.emilyydev.betterjails.config.BetterJailsConfiguration;
import io.github.emilyydev.betterjails.config.SubCommandsConfiguration;
import io.github.emilyydev.betterjails.interfaces.permission.PermissionInterface;
import io.github.emilyydev.betterjails.listeners.PlayerListeners;
import io.github.emilyydev.betterjails.listeners.PluginDisableListener;
import io.github.emilyydev.betterjails.util.DataHandler;
import io.github.emilyydev.betterjails.util.FileIO;
import io.github.emilyydev.betterjails.util.Util;
import net.ess3.api.IEssentials;
import org.bstats.bukkit.Metrics;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("DesignForExtension")
public class BetterJailsPlugin extends JavaPlugin implements Executor {

  static {
    ConfigurationSerialization.registerClass(ImmutableLocation.class);
  }

  public IEssentials essentials = null;

  private final BetterJailsApi api = new BetterJailsApi(new ApiJailManager(this), new ApiPrisonerManager(this));
  private final ApiEventBus eventBus = this.api.getEventBus();
  private final Path pluginDir = getDataFolder().toPath();
  private final BetterJailsConfiguration configuration = new BetterJailsConfiguration(this.pluginDir);
  private final SubCommandsConfiguration subCommands = new SubCommandsConfiguration(this.pluginDir);
  private final DataHandler dataHandler = new DataHandler(this);
  private PermissionInterface permissionInterface = PermissionInterface.NULL;
  private Metrics metrics = null;

  public BetterJailsPlugin() {
    this.metrics = Util.prepareMetrics(this);
  }

  public BetterJailsPlugin(final String str) { // for mockbukkit, just a dummy ctor to not enable bstats
  }

  public DataHandler dataHandler() {
    return this.dataHandler;
  }

  public BetterJailsApi api() {
    return this.api;
  }

  public ApiEventBus eventBus() {
    return this.eventBus;
  }

  public PermissionInterface permissionInterface() {
    return this.permissionInterface;
  }

  public void resetPermissionInterface(final PermissionInterface permissionInterface) {
    this.permissionInterface.close();
    this.permissionInterface = permissionInterface;
  }

  public BetterJailsConfiguration configuration() {
    return this.configuration;
  }

  public SubCommandsConfiguration subCommands() {
    return this.subCommands;
  }

  public Path getPluginDir() {
    return this.pluginDir;
  }

  @Override
  public void execute(final @NotNull Runnable command) {
    getServer().getScheduler().runTask(this, command);
  }

  @Override
  public void onLoad() {
    getServer().getServicesManager().register(BetterJails.class, this.api, this, ServicePriority.Normal);
  }

  @Override
  public void onEnable() {
    PluginDisableListener.create(this.eventBus).register(this);

    this.configuration.load();
    this.subCommands.load();

    final Server server = getServer();
    final PluginManager pluginManager = server.getPluginManager();
    if (pluginManager.isPluginEnabled("Essentials")) {
      this.essentials = (IEssentials) pluginManager.getPlugin("Essentials");
      getLogger().info("Hooked with Essentials successfully!");
    }

    if (this.configuration.permissionHookEnabled()) {
      final Optional<String> maybePrisonerGroup = this.configuration.prisonerPermissionGroup();
      final Logger logger = getLogger();
      if (maybePrisonerGroup.isPresent()) {
        this.permissionInterface = PermissionInterface.determinePermissionInterface(this, maybePrisonerGroup.get());
        if (this.permissionInterface != PermissionInterface.NULL) {
          logger.info("Hooked with \"" + this.permissionInterface.name() + "\" successfully!");
        } else {
          logger.warning("Hook with a permission interface failed!");
          logger.warning("Option \"changeGroup\" in config.yml is set to true but no supported permission plugin (or Vault) is installed");
          logger.warning("Group changing feature will not be used!");
        }
      } else {
        logger.warning("Option \"changeGroup\" in config.yml is set to true but no prisoner permission group was configured");
        logger.warning("Group changing feature will not be used!");
      }
    }

    final BukkitScheduler scheduler = server.getScheduler();

    // delay data loading to allow plugins to load additional worlds that might have jails in them
    scheduler.runTask(this, () -> {
      try {
        this.dataHandler.init();
      } catch (final IOException | InvalidConfigurationException exception) {
        getLogger().log(Level.SEVERE, "Error loading plugin data", exception);
        pluginManager.disablePlugin(this);
      }
    });

    PlayerListeners.create(this).register();

    final CommandHandler commandHandler = new CommandHandler(this);
    final CommandTabCompleter tabCompleter = new CommandTabCompleter(this);
    for (final String commandName : getDescription().getCommands().keySet()) {
      final PluginCommand command = getCommand(commandName);
      if (command != null) {
        command.setExecutor(commandHandler);
        command.setTabCompleter(tabCompleter);
      }
    }

    scheduler.runTaskTimer(this, this.dataHandler::timerNew, 0L, 20L);

    final Duration autoSavePeriod = this.configuration.autoSavePeriod();
    if (!autoSavePeriod.isZero()) {
      scheduler.runTaskTimer(this, () -> this.dataHandler.save().exceptionally(ex -> {
        getLogger().log(Level.SEVERE, "Could not save data files", ex);
        return null;
      }), autoSavePeriod.getSeconds() * 20L, autoSavePeriod.getSeconds() * 20L);
    }

    if (!getDescription().getVersion().endsWith("-SNAPSHOT")) {
      scheduler.runTaskLater(this, () -> Util.checkVersion(this, version -> {
        if (!getDescription().getVersion().equals(version)) {
          server.getConsoleSender().sendMessage(Util.color("&7[&bBetterJails&7] &3New version &b%s &3for &bBetterJails &3available.", version));
        }
      }), 100L);
    }
  }

  @Override
  public void onDisable() {
    try {
      this.dataHandler.save().get();
    } catch (final InterruptedException | ExecutionException exception) {
      getLogger().log(Level.SEVERE, "Could not save data files", exception);
    }

    try {
      FileIO.shutdown();
    } catch (final InterruptedException ignored) {
    }

    this.eventBus.unsubscribeAll();
    if (this.metrics != null) {
      this.metrics.shutdown();
    }
  }

  public void reload() throws IOException {
    this.configuration.load();
    this.subCommands.load();
    this.dataHandler.reload();
  }
}
