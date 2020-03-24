package ar.fefo.betterjails;

import ar.fefo.betterjails.commands.CommandHandler;
import ar.fefo.betterjails.commands.CommandTabCompleter;
import ar.fefo.betterjails.utils.DataHandler;
import com.earth2me.essentials.Essentials;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;
import java.util.logging.Level;

public class Main extends JavaPlugin {
    public DataHandler dataHandler;
    public Essentials ess = null;
    public LuckPerms lp;
    public String prisonerGroup;

    // I really don't like commenting code..... I'm sorry, I've tried... :(
    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            ess = ((Essentials)getServer().getPluginManager().getPlugin("Essentials"));
            lp = getConfig().getBoolean("changeGroup") ? LuckPermsProvider.get() : null;
            prisonerGroup = getConfig().getBoolean("changeGroup") ? getConfig().getString("prisonerGroup") : null;

            dataHandler = new DataHandler(this);
            Bukkit.getPluginManager().registerEvents(dataHandler, this);
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

            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> dataHandler.timer(), 0, 20);
            if (getConfig().getInt("autoSaveTimeInMinutes") > 0)
                Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                    try {
                        dataHandler.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, 0, 20 * 60 * getConfig().getInt("autoSaveTimeInMinutes"));

            Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
                try {
                    URL versionURL = new URL("https://pastebin.com/raw/gz16Wmt7");
                    ReadableByteChannel rbc = Channels.newChannel(versionURL.openStream());
                    ByteBuffer buffer = ByteBuffer.allocate(128);
                    if (rbc.isOpen()) {
                        rbc.read(buffer);
                        rbc.close();

                        String version = new String(buffer.array());
                        if (version.compareTo(getDescription().getVersion()) > 0)
                            getServer().getConsoleSender().sendMessage("§7[§bBetterJails§7] §3New version §bv" + version + " §3for §bBetterJails §3available.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 20);

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
