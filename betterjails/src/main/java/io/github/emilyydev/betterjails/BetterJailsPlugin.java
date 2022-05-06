//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2021 emilyy-dev
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
import io.github.emilyydev.betterjails.listeners.PlayerListeners;
import io.github.emilyydev.betterjails.listeners.PluginDisableListener;
import io.github.emilyydev.betterjails.util.DataHandler;
import io.github.emilyydev.betterjails.util.Util;
import net.ess3.api.IEssentials;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class BetterJailsPlugin extends JavaPlugin {

  static {
    ConfigurationSerialization.registerClass(ImmutableLocation.class);
  }

  public final DataHandler dataHandler = new DataHandler(this);
  public IEssentials essentials = null;
  public Permission permsInterface = null;
  public String prisonerGroup = null;
  private BukkitTask timerTask = null;

  private final BetterJailsApi api = new BetterJailsApi(new ApiJailManager(this), new ApiPrisonerManager(this));
  private final ApiEventBus eventBus = this.api.getEventBus();

  public BetterJailsApi getApi() {
    return this.api;
  }

  public ApiEventBus getEventBus() {
    return this.eventBus;
  }

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

  @Override
  public void onLoad() {
    getServer().getServicesManager().register(BetterJails.class, this.api, this, ServicePriority.Normal);
  }

  @Override
  public void onEnable() {
    PluginDisableListener.create(this.eventBus).register(this);

    saveDefaultConfig();

    try {
      final PluginManager pluginManager = getServer().getPluginManager();
      if (pluginManager.isPluginEnabled("Essentials")) {
        this.essentials = (IEssentials) pluginManager.getPlugin("Essentials");
        getLogger().info("Hooked with Essentials successfully!");
      }

      if (getConfig().getBoolean("changeGroup")) {
        if (pluginManager.isPluginEnabled("Vault")) {
          this.permsInterface = getServer().getServicesManager().load(Permission.class);
          this.prisonerGroup = getConfig().getString("prisonerGroup");
          getLogger().info("Hooked with Vault successfully!");
        } else {
          getLogger().warning("Hook with Vault failed!");
          getLogger().warning("Option \"changeGroup\" in config.yml is set to true but Vault isn't installed");
          getLogger().warning("Group changing feature will not be used!");
        }
      }

      this.dataHandler.init();

      PlayerListeners.create(this).register();

      final CommandHandler commandHandler = new CommandHandler(this);
      final CommandTabCompleter tabCompleter = new CommandTabCompleter(this);

      final Map<String, ?> commands = getDescription().getCommands();
      for (final String commandName : commands.keySet()) {
        final PluginCommand command = getCommand(commandName);
        if (command != null) {
          command.setExecutor(commandHandler);
          command.setTabCompleter(tabCompleter);
        }
      }

      final BukkitScheduler scheduler = getServer().getScheduler();
      this.timerTask = scheduler.runTaskTimer(this, this.dataHandler::timer, 0L, 20L);
      if (getConfig().getLong("autoSaveTimeInMinutes") > 0L) {
        scheduler.runTaskTimerAsynchronously(this, () -> {
          try {
            this.dataHandler.save();
          } catch (final IOException exception) {
            exception.printStackTrace();
          }
        }, 0L, 20L * 60L * getConfig().getLong("autoSaveTimeInMinutes"));
      }

      if (!getDescription().getVersion().endsWith("-SNAPSHOT")) {
        scheduler.runTaskLater(this, () ->
            Util.checkVersion(this, 76001, version -> {
              if (!getDescription().getVersion().equalsIgnoreCase(version.substring(1))) {
                getServer().getConsoleSender().sendMessage(Util.color("&7[&bBetterJails&7] &3New version &b%s &3for &bBetterJails &3available.", version));
              }
            }), 100L);
      }
    } catch (final IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  @Override
  public void onDisable() {
    try {
      if (this.timerTask != null) {
        this.timerTask.cancel();
      }
      if (this.dataHandler != null) {
        this.dataHandler.save();
      }
    } catch (final IOException exception) {
      getLogger().severe("Could not save data files!");
      exception.printStackTrace();
    }

    this.eventBus.unsubscribeAll();
  }
}
