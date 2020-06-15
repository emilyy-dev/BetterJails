package me.fefo.betterjails;

import com.earth2me.essentials.Essentials;
import me.fefo.betterjails.commands.CommandHandler;
import me.fefo.betterjails.commands.CommandTabCompleter;
import me.fefo.betterjails.listeners.EventListeners;
import me.fefo.betterjails.papi.PAPIExpansion;
import me.fefo.betterjails.utils.DataHandler;
import me.fefo.betterjails.utils.UpdateChecker;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public final class Main extends JavaPlugin {
  public static final String CONFIG_FIELD_BACKUP_LOCATION = "backupLocation";
  public static final String CONFIG_FIELD_OFFLINE_TIME = "offlineTime";
  public static final String CONFIG_FIELD_CHANGE_GROUP = "changeGroup";
  public static final String CONFIG_FIELD_PRISONER_GROUP = "prisonerGroup";
  public static final String CONFIG_FIELD_AUTOSAVE_TIME = "autoSaveTimeInMinutes";
  public static final String CONFIG_FIELD_USE_MYSQL = "useMySQL";

  public DataHandler dataHandler = null;
  public Essentials essentials = null;
  public Permission permManager = null;
  public String prisonerGroup = null;
  private BukkitTask timerTask = null;

  // I really don't like commenting code..... I'm sorry (not really), I've tried (not really)... :(
  @Override
  public void onEnable() {
    saveDefaultConfig();

    try {
      getLogger().info("Hooking with Essentials...");
      essentials = ((Essentials)getServer().getPluginManager().getPlugin("Essentials"));
      if (essentials != null) {
        getLogger().info("Hooked with Essentials successfully!");
      } else {
        getLogger().warning("Essentials not found! Hook with Essentials cancelled!");
      }

      getLogger().info("Registering PAPI placeholders...");
      if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
        if (new PAPIExpansion(this).register()) {
          getLogger().info("PAPI placeholders registered successfully!");
        } else {
          getLogger().warning("Could not register PAPI placeholders.");
        }
      } else {
        getLogger().info("PlaceholderAPI not found! Placeholders registered cancelled!");
      }


      if (getConfig().getBoolean(CONFIG_FIELD_CHANGE_GROUP)) {
        getLogger().info("Hooking with Vault...");
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
          RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
          if (rsp == null) {
            getLogger().severe("BetterJails could not hook with Vault!");
            getLogger().severe("There was an error while hooking with Vault!");
            getLogger().severe("Group changing feature will not be used!");
          } else {
            permManager = rsp.getProvider();
            prisonerGroup = getConfig().getString("prisonerGroup");
            getLogger().info("Hooked with Vault successfully!");
          }
        } else {
          getLogger().warning("Hook with Vault cancelled!");
          getLogger().warning("Option \"" + CONFIG_FIELD_CHANGE_GROUP + "\" in config.yml is set to true, yet Vault wasn't found!");
          getLogger().warning("Group changing feature will not be used!");
        }
      }

      dataHandler = new DataHandler(this);
      getServer().getPluginManager().registerEvents(new EventListeners(this), this);

      Map<String, Map<String, Object>> commands = getDescription().getCommands();
      for (String command : commands.keySet()) {
        PluginCommand cmd;
        if ((cmd = getCommand(command)) != null) {
          cmd.setExecutor(CommandHandler.init(this));

          // TODO: use Brigadier instead, probably Commodore.
          cmd.setTabCompleter(CommandTabCompleter.init(this));
        }
      }

      timerTask = getServer().getScheduler().runTaskTimer(this, dataHandler::timer, 0, 20);
      if (getConfig().getInt(CONFIG_FIELD_AUTOSAVE_TIME) > 0) {
        getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
          @Override
          public void run() {
            try {
              dataHandler.save();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }, 0, 20 * 60 * getConfig().getInt(CONFIG_FIELD_AUTOSAVE_TIME));
      }

      if (!getDescription().getVersion().contains("-BETA")) {
        getServer().getScheduler().runTaskLater(this, () ->
                new UpdateChecker(this, 76001).getVersion(version -> {
                  if (!getDescription().getVersion().equalsIgnoreCase(version.substring(1))) {
                    getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "[" +
                                                               ChatColor.AQUA + "BetterJails" +
                                                               ChatColor.GRAY + "] " +
                                                               ChatColor.DARK_AQUA + "New version " +
                                                               ChatColor.AQUA + version + " " +
                                                               ChatColor.DARK_AQUA + "for " +
                                                               ChatColor.AQUA + "BetterJails " +
                                                               ChatColor.DARK_AQUA + "available.");
                  }
                }), 100);
      }

    } catch (IOException | SQLException e) {
      if (e instanceof IOException) {
        getLogger().severe("There was an error while trying to generate the files!");
        getLogger().severe("Disabling plugin!");
        getLogger().severe(e.getMessage());
      } else {
        getLogger().severe("There was an error while trying connect to the database!");
        getLogger().severe("Disabling plugin!");
      }
      e.printStackTrace();
      getServer().getPluginManager().disablePlugin(this);
    }
  }

  @Override
  public void onDisable() {
    try {
      if (timerTask != null) {
        timerTask.cancel();
      }
      if (dataHandler != null) {
        dataHandler.save();
      }
    } catch (IOException e) {
      getLogger().severe("Could not save data files!");
      e.printStackTrace();
    }
  }
}
