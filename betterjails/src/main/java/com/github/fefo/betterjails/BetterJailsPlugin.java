//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2021 Fefo6644 <federico.lopez.1999@outlook.com>
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

package com.github.fefo.betterjails;

import com.github.fefo.betterjails.api.BetterJails;
import com.github.fefo.betterjails.api.impl.BetterJailsApi;
import com.github.fefo.betterjails.api.impl.event.ApiEventBus;
import com.github.fefo.betterjails.commands.CommandHandler;
import com.github.fefo.betterjails.commands.CommandTabCompleter;
import com.github.fefo.betterjails.listeners.PlayerListeners;
import com.github.fefo.betterjails.listeners.PluginDisableListener;
import com.github.fefo.betterjails.util.DataHandler;
import com.github.fefo.betterjails.util.UpdateChecker;
import com.github.fefo.betterjails.util.Util;
import net.ess3.api.IEssentials;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.Map;

public class BetterJailsPlugin extends JavaPlugin {

  static {
    try {
      // ensure class loading to register as ConfigurationSerializable
      BetterJailsPlugin.class.getClassLoader().loadClass("com.github.fefo.betterjails.api.util.ImmutableLocation");
    } catch (final Throwable throwable) {
      // it's bundled in so it won't throw (and if a dev messes up we'll know :D)
      throw new RuntimeException(throwable);
    }
  }

  public DataHandler dataHandler = null;
  public IEssentials essentials = null;
  public Permission permsInterface = null;
  public String prisonerGroup = null;
  private BukkitTask timerTask = null;

  // Let the plugin handle the event dispatching.
  private final ApiEventBus eventBus = new ApiEventBus();

  public ApiEventBus getEventBus() {
    return this.eventBus;
  }

  @Override
  public void onLoad() {
    Bukkit.getServicesManager().register(BetterJails.class, new BetterJailsApi(this), this, ServicePriority.Normal);
  }

  @Override
  public void onEnable() {
    PluginDisableListener.create(this.eventBus).register(this);

    saveDefaultConfig();

    try {
      if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
        this.essentials = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
        getLogger().info("Hooked with Essentials successfully!");
      }

      if (getConfig().getBoolean("changeGroup")) {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
          this.permsInterface = Bukkit.getServicesManager().load(Permission.class);
          this.prisonerGroup = getConfig().getString("prisonerGroup");
          getLogger().info("Hooked with Vault successfully!");
        } else {
          getLogger().warning("Hook with Vault failed!");
          getLogger().warning("Option \"changeGroup\" in config.yml is set to true but Vault isn't installed");
          getLogger().warning("Group changing feature will not be used!");
        }
      }

      this.dataHandler = new DataHandler(this);

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

      this.timerTask = Bukkit.getScheduler().runTaskTimer(this, this.dataHandler::timer, 0L, 20L);
      if (getConfig().getLong("autoSaveTimeInMinutes") > 0L) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
          try {
            this.dataHandler.save();
          } catch (final IOException exception) {
            exception.printStackTrace();
          }
        }, 0L, 20L * 60L * getConfig().getLong("autoSaveTimeInMinutes"));
      }

      if (!getDescription().getVersion().endsWith("-SNAPSHOT")) {
        Bukkit.getScheduler().runTaskLater(this, () ->
            new UpdateChecker(this, 76001).getVersion(version -> {
              if (!getDescription().getVersion().equalsIgnoreCase(version.substring(1))) {
                Bukkit.getConsoleSender().sendMessage(Util.color("&7[&bBetterJails&7] &3New version &b%s &3for &bBetterJails &3available.", version));
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
