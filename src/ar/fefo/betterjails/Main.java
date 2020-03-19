package ar.fefo.betterjails;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

public class Main extends JavaPlugin {
    DataHandler dataHandler;
    Essentials ess;

    // I really don't like commenting code..... I'm sorry, I've tried... :(
    @Override
    public void onEnable() {
        try {
            ess = ((Essentials)Bukkit.getPluginManager().getPlugin("Essentials"));
            dataHandler = new DataHandler(this);
            Bukkit.getPluginManager().registerEvents(dataHandler, this);
            CommandHandler.init(this);

            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> dataHandler.timer(), 0, 20);
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> dataHandler.autoSaveTimer(), 0, 20 * 60 * 5);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "There was an error while trying to generate the files!");
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
