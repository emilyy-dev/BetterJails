package ar.fefo.betterjails;

import com.earth2me.essentials.Essentials;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

public class Main extends JavaPlugin {
    DataHandler dataHandler;
    Essentials ess;

    // I really don't like commenting code..... I'm sorry, I've tried...
    @Override
    public void onEnable() {
        try {
            ess = ((Essentials)getServer().getPluginManager().getPlugin("Essentials"));
            dataHandler = new DataHandler(this);
            getServer().getPluginManager().registerEvents(dataHandler, this);
            CommandHandler.init(this);

            getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> dataHandler.timer(), 0, 20);
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
