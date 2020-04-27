package ar.fefo.betterjails;

import ar.fefo.betterjails.commands.CommandHandler;
import ar.fefo.betterjails.commands.CommandTabCompleter;
import ar.fefo.betterjails.listeners.Listeners;
import ar.fefo.betterjails.utils.DataHandler;
import ar.fefo.betterjails.utils.UpdateChecker;
import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.Map;

public class Main extends JavaPlugin {
    public DataHandler dataHandler = null;
    public Essentials ess = null;
    public Permission perm = null;
    public String prisonerGroup = null;
    private BukkitTask timerTask = null;

    // I really don't like commenting code..... I'm sorry (not really), I've tried (not really)... :(
    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            getLogger().info("Hooking with Essentials...");
            ess = ((Essentials)getServer().getPluginManager().getPlugin("Essentials"));
            if (ess != null)
                getLogger().info("Hooked with Essentials successfully!");
            else
                getLogger().warning("Essentials not found! Hook with Essentials cancelled!");

            if (getConfig().getBoolean("changeGroup")) {
                getLogger().info("Hooking with Vault...");
                if (getServer().getPluginManager().getPlugin("Vault") != null) {
                    RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
                    if (rsp == null) {
                        getLogger().severe("BetterJails could not hook with Vault!");
                        getLogger().severe("There was an error while hooking with Vault!");
                        getLogger().severe("Group changing feature will not be used!");
                    } else {
                        perm = rsp.getProvider();
                        prisonerGroup = getConfig().getString("prisonerGroup");
                        getLogger().info("Hooked with Vault successfully!");
                    }
                } else {
                    getLogger().warning("Hook with Vault cancelled!");
                    getLogger().warning("Option \"changeGroup\" in config.yml is set to true, yet Vault wasn't found!");
                    getLogger().warning("Group changing feature will not be used!");
                }
            }

            dataHandler = new DataHandler(this);
            getServer().getPluginManager().registerEvents(new Listeners(this), this);

            Map<String, Map<String, Object>> commands = getDescription().getCommands();
            for (String command : commands.keySet()) {
                PluginCommand cmd;
                if ((cmd = getCommand(command)) != null) {
                    cmd.setExecutor(CommandHandler.init(this));
                    cmd.setTabCompleter(CommandTabCompleter.init(this));
                }
            }

            timerTask = getServer().getScheduler().runTaskTimer(this, dataHandler::timer, 0, 20);
            if (getConfig().getInt("autoSaveTimeInMinutes") > 0)
                getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dataHandler.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 0, 20 * 60 * getConfig().getInt("autoSaveTimeInMinutes"));

            if (!getDescription().getVersion().toLowerCase().contains("b"))
                getServer().getScheduler().runTaskLater(this, () ->
                        new UpdateChecker(this, 76001).getVersion(version -> {
                            if (!getDescription().getVersion().equalsIgnoreCase(version.substring(1)))
                                getServer().getConsoleSender().sendMessage("§7[§bBetterJails§7] §3New version §b" + version + " §3for §bBetterJails §3available.");
                        }), 100);

        } catch (IOException e) {
            getLogger().severe("There was an error while trying to generate the files!");
            getLogger().severe("Disabling plugin!");
            getLogger().severe(e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            if (timerTask != null)
                timerTask.cancel();
            if (dataHandler != null)
                dataHandler.save();
        } catch (IOException e) {
            getLogger().severe("Could not save data files!");
            e.printStackTrace();
        }
    }
}
