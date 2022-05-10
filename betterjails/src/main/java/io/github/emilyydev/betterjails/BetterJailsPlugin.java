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

package io.github.emilyydev.betterjails;

import com.github.fefo.betterjails.api.BetterJails;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import io.github.emilyydev.betterjails.api.impl.BetterJailsApi;
import io.github.emilyydev.betterjails.api.impl.event.ApiEventBus;
import io.github.emilyydev.betterjails.api.impl.model.jail.ApiJailManager;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisonerManager;
import io.github.emilyydev.betterjails.commands.CommandHandler;
import io.github.emilyydev.betterjails.commands.CommandTabCompleter;
import io.github.emilyydev.betterjails.interfaces.PermissionInterface;
import io.github.emilyydev.betterjails.listeners.PlayerListeners;
import io.github.emilyydev.betterjails.listeners.PluginDisableListener;
import io.github.emilyydev.betterjails.util.DataHandler;
import io.github.emilyydev.betterjails.util.Util;
import net.ess3.api.IEssentials;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class BetterJailsPlugin extends JavaPlugin {

  static {
    ConfigurationSerialization.registerClass(ImmutableLocation.class);
  }

  public final DataHandler dataHandler = new DataHandler(this);
  public IEssentials essentials = null;

  private final BetterJailsApi api = new BetterJailsApi(new ApiJailManager(this), new ApiPrisonerManager(this));
  private final ApiEventBus eventBus = this.api.getEventBus();
  private PermissionInterface permissionInterface = PermissionInterface.NULL;

  public BetterJailsPlugin() {
  }

  public BetterJailsPlugin(
      final @NotNull JavaPluginLoader loader,
      final @NotNull PluginDescriptionFile description,
      final @NotNull File dataFolder,
      final @NotNull File file
  ) {
    super(loader, description, dataFolder, file);
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
    this.permissionInterface = permissionInterface;
  }

  @Override
  public void onLoad() {
    getServer().getServicesManager().register(BetterJails.class, this.api, this, ServicePriority.Normal);
  }

  @Override
  public void onEnable() {
    PluginDisableListener.create(this.eventBus).register(this);

    saveDefaultConfig();

    final Server server = getServer();
    final PluginManager pluginManager = server.getPluginManager();
    if (pluginManager.isPluginEnabled("Essentials")) {
      this.essentials = (IEssentials) pluginManager.getPlugin("Essentials");
      getLogger().info("Hooked with Essentials successfully!");
    }

    final FileConfiguration config = getConfig();
    if (config.getBoolean("changeGroup")) {
      this.permissionInterface = PermissionInterface.determinePermissionInterface(server, config.getString("prisonerGroup"));
      if (this.permissionInterface != PermissionInterface.NULL) {
        getLogger().info("Hooked with \"" + this.permissionInterface.name() + "\" successfully!");
      } else {
        getLogger().warning("Hook with a permission interface failed!");
        getLogger().warning("Option \"changeGroup\" in config.yml is set to true but no supported permission plugin (or Vault) is installed");
        getLogger().warning("Group changing feature will not be used!");
      }
    }

    try {
      this.dataHandler.init();
    } catch (final IOException | InvalidConfigurationException exception) {
      throw new RuntimeException(exception);
    }

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

    final BukkitScheduler scheduler = server.getScheduler();
    scheduler.runTaskTimer(this, this.dataHandler::timer, 0L, 20L);
    if (config.getLong("autoSaveTimeInMinutes") > 0L) {
      scheduler.runTaskTimerAsynchronously(this, () -> {
        try {
          this.dataHandler.save();
        } catch (final IOException exception) {
          exception.printStackTrace();
        }
      }, 0L, 20L * 60L * config.getLong("autoSaveTimeInMinutes"));
    }

    if (!getDescription().getVersion().endsWith("-SNAPSHOT")) {
      scheduler.runTaskLater(this, () ->
          Util.checkVersion(this, 76001, version -> {
            if (!getDescription().getVersion().equalsIgnoreCase(version.substring(1))) {
              server.getConsoleSender().sendMessage(Util.color("&7[&bBetterJails&7] &3New version &b%s &3for &bBetterJails &3available.", version));
            }
          }), 100L);
    }
  }

  @Override
  public void onDisable() {
    try {
      this.dataHandler.save();
    } catch (final IOException exception) {
      getLogger().severe("Could not save data files!");
      exception.printStackTrace();
    }

    this.eventBus.unsubscribeAll();
  }
}
