package ar.fefo.betterjails;

import ar.fefo.betterjails.commands.CommandHandler;
import ar.fefo.betterjails.commands.CommandTabCompleter;
import ar.fefo.betterjails.listeners.Listeners;
import ar.fefo.betterjails.utils.DataHandler;
import ar.fefo.betterjails.utils.UpdateChecker;
import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

public class Main extends JavaPlugin {
    public DataHandler dataHandler = null;
    public Essentials ess = null;
    public Permission perm = null;
    public String prisonerGroup = null;

    // I really don't like commenting code..... I'm sorry (not really), I've tried (not really)... :(
    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            getLogger().log(Level.INFO, "Hooking with Essentials...");
            ess = ((Essentials)getServer().getPluginManager().getPlugin("Essentials"));
            if (ess != null)
                getLogger().log(Level.INFO, "Hooked with Essentials successfully!");
            else
                getLogger().log(Level.WARNING, "Essentials not found! Hook with Essentials cancelled!");

            if (getConfig().getBoolean("changeGroup")) {
                getLogger().log(Level.INFO, "Hooking with Vault...");
                if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                    RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
                    if (rsp == null) {
                        getLogger().log(Level.SEVERE, "BetterJails could not hook with Vault!");
                        getLogger().log(Level.SEVERE, "There was an error while hooking with Vault!");
                        getLogger().log(Level.SEVERE, "Group changing feature will not be used!");
                    } else {
                        perm = rsp.getProvider();
                        prisonerGroup = getConfig().getString("prisonerGroup");
                        getLogger().log(Level.INFO, "Hooked with Vault successfully!");
                    }
                } else {
                    getLogger().log(Level.WARNING, "Hook with Vault cancelled!");
                    getLogger().log(Level.WARNING, "Option \"changeGroup\" in config.yml is set to true, yet Vault wasn't found!");
                    getLogger().log(Level.WARNING, "Group changing feature will not be used!");
                }
            }

            dataHandler = new DataHandler(this);
            Bukkit.getPluginManager().registerEvents(new Listeners(this), this);
            CommandHandler.init(this);
            CommandTabCompleter.init(this);

            Map<String, Map<String, Object>> commands = getDescription().getCommands();
            for (String command : commands.keySet()) {
                PluginCommand cmd;
                if ((cmd = getCommand(command)) != null) {
                    cmd.setExecutor(CommandHandler.getInstance());
                    cmd.setTabCompleter(CommandTabCompleter.getInstance());
                }
            }

            Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    dataHandler.timer();
                }
            }, 0, 20);
            if (getConfig().getInt("autoSaveTimeInMinutes") > 0)
                Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dataHandler.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 0, 20 * 60 * getConfig().getInt("autoSaveTimeInMinutes"));

            Bukkit.getScheduler().runTaskLater(this, () ->
                    new UpdateChecker(this, 76001).getVersion(version -> {
                        if (!getDescription().getVersion().equalsIgnoreCase(version))
                            getServer().getConsoleSender().sendMessage("§7[§bBetterJails§7] §3New version §b" + version + " §3for §bBetterJails §3available.");
                    }), 100);

        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "There was an error while trying to generate the files!");
            getLogger().log(Level.SEVERE, "Disabling plugin!");
            getLogger().log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            dataHandler.save();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save data files!");
            e.printStackTrace();
        }
    }
}
