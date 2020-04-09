package ar.fefo.betterjails.commands;

import ar.fefo.betterjails.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
public class CommandHandler implements CommandExecutor, Listener {
    private static CommandHandler instance;
    private final UUID defaultUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private Main main;
    private ConfigurationSection messages;
    private HashMap<String, UUID> alltimePlayers = new HashMap<>();

    private CommandHandler(@NotNull Main main) {
        this.main = main;
        messages = this.main.getConfig().getConfigurationSection("messages");
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers())
            alltimePlayers.put(offlinePlayer.getName(), offlinePlayer.getUniqueId());
    }
    public static void init(@NotNull Main main) { instance = new CommandHandler(main); }
    public static CommandHandler getInstance() { return instance; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String alias,
                             @NotNull String[] args) {
        switch (cmd.getName()) {
            case "betterjails":
                if (args.length == 1)
                    return betterjails(sender, args[0]);
                else
                    sender.sendMessage("§cBetterJails §6by §cFefo6644 §6- v" + main.getDescription().getVersion());
                return true;

            case "jail":
                if (args.length == 2 && args[0].equalsIgnoreCase("info"))
                    return jailInfo(sender, args[1]);
                else if (args.length == 3)
                    return jailPlayer(sender, args[0], args[1], args[2]);
                else
                    return false;

            case "jails":
                return jails(sender);

            case "unjail":
                if (args.length != 1)
                    return false;
                else
                    return unjailPlayer(sender, args[0]);

            case "setjail":
                if (args.length != 1)
                    return false;
                else
                    return setjail(sender, args[0]);

            case "deljail":
                if (args.length != 1)
                    return false;
                else
                    return deljail(sender, args[0]);
        }
        return false;
    }

    private boolean betterjails(CommandSender sender, @NotNull String arg) {
        if (arg.equalsIgnoreCase("reload") &&
            sender.hasPermission("betterjails.betterjails.reload")) {
            main.dataHandler.reload();
            messages = main.getConfig().getConfigurationSection("messages");
            assert messages != null;
            sender.sendMessage(messages.getString("reload")
                                       .replace("{player}", sender.getName())
                                       .replace('&', '§'));
            main.getServer().getConsoleSender().sendMessage(messages.getString("reload")
                                                                    .replace("{player}", sender.getName())
                                                                    .replace('&', '§'));
        } else if (arg.equalsIgnoreCase("save") &&
                   sender.hasPermission("betterjails.betterjails.save")) {
            try {
                main.dataHandler.save();
                sender.sendMessage(messages.getString("save")
                                           .replace("{player}", sender.getName())
                                           .replace('&', '§'));
                main.getServer().getConsoleSender().sendMessage(messages.getString("save")
                                                                        .replace("{player}", sender.getName())
                                                                        .replace('&', '§'));
            } catch (IOException e) {
                sender.sendMessage("§cThere was an internal error while trying to save the data files.\n" +
                                   "Please check console for more information.");
                e.printStackTrace();
            }
        } else {
            sender.sendMessage("§cBetterJails §6by §cFefo6644 §6- v" + main.getDescription().getVersion());
        }
        return true;
    }

    private boolean jailPlayer(CommandSender sender, String prisoner, String jail, String time) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(alltimePlayers.getOrDefault(prisoner, defaultUUID));
        if (!player.getUniqueId().equals(defaultUUID)) {
            if (player.isOnline() && ((Player)player).hasPermission("betterjails.jail.exempt")) {
                sender.sendMessage(messages.getString("jailFailedPlayerExempt")
                                           .replace("{prisoner}", prisoner)
                                           .replace("{player}", sender.getName())
                                           .replace("{jail}", jail)
                                           .replace("{time}", time)
                                           .replace('&', '§'));
                return true;
            }
//            if (args[2].matches("\\d+[yMwdhms]")) {
            if (time.matches("^(\\d+(\\.\\d+)*)[yMwdhms]$")) {
                double scale;
                switch (time.charAt(time.length() - 1)) {
                    case 's':
                        scale = 1;
                        break;
                    case 'm':
                    default:
                        scale = 60;
                        break;
                    case 'h':
                        scale = 3600;
                        break;
                    case 'd':
                        scale = 3600 * 24;
                        break;
                    case 'w':
                        scale = 3600 * 24 * 7;
                        break;
                    case 'M':
                        scale = 3600 * 24 * 30.4375;
                        break;
                    case 'y':
                        scale = 3600 * 24 * 365.25;
                        break;
                }
                long seconds = (long)(scale * Double.parseDouble(time.substring(0, time.length() - 1)));
                try {
                    if (!main.dataHandler.addJailedPlayer(player, jail, sender.getName(), seconds)) {
                        sender.sendMessage(messages.getString("jailFailedJailNotFound")
                                                   .replace("{prisoner}", prisoner)
                                                   .replace("{player}", sender.getName())
                                                   .replace("{jail}", jail)
                                                   .replace("{time}", time)
                                                   .replace('&', '§'));
                        return true;
                    }
                } catch (IOException e) {
                    sender.sendMessage("§4Fatal error! Could not save player data");
                    main.getServer().getConsoleSender().sendMessage("§4Fatal error! Could not save player data");
                    e.printStackTrace();
                }

                List<Player> onlinePlayers = new ArrayList<>(main.getServer().getOnlinePlayers());
                for (Player playerToBroadcast : onlinePlayers) {
                    if (playerToBroadcast.hasPermission("betterjails.receivebroadcast"))
                        playerToBroadcast.sendMessage(messages.getString("jailSuccess")
                                                              .replace("{prisoner}", prisoner)
                                                              .replace("{player}", sender.getName())
                                                              .replace("{jail}", jail)
                                                              .replace("{time}", time)
                                                              .replace('&', '§'));
                }
                main.getServer().getConsoleSender().sendMessage(messages.getString("jailSuccess")
                                                                        .replace("{prisoner}", prisoner)
                                                                        .replace("{player}", sender.getName())
                                                                        .replace("{jail}", jail)
                                                                        .replace("{time}", time)
                                                                        .replace('&', '§'));
            } else {
                sender.sendMessage(messages.getString("jailFailedTimeIncorrect")
                                           .replace("{prisoner}", prisoner)
                                           .replace("{player}", sender.getName())
                                           .replace("{jail}", jail)
                                           .replace("{time}", time)
                                           .replace('&', '§'));
            }
            return true;
        }
        sender.sendMessage(messages.getString("jailFailedPlayerNeverJoined")
                                   .replace("{prisoner}", prisoner)
                                   .replace("{player}", sender.getName())
                                   .replace("{jail}", jail)
                                   .replace("{time}", time)
                                   .replace('&', '§'));
        return true;
    }

    private boolean jailInfo(CommandSender sender, String prisoner) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(alltimePlayers.getOrDefault(prisoner, defaultUUID));
        if (!player.getUniqueId().equals(defaultUUID)) {
            YamlConfiguration yaml = main.dataHandler.retrieveJailedPlayer(player.getUniqueId());

            if (yaml == null) {
                sender.sendMessage(messages.getString("infoFailedPlayerNotJailed")
                                           .replace("{prisoner}", prisoner)
                                           .replace("{player}", sender.getName())
                                           .replace('&', '§'));
                return true;
            }

            String[] infoLines = new String[8];
            DecimalFormat df = new DecimalFormat("#.##");
            double secondsleft = yaml.getLong("secondsleft", -1);
            char timeunit = 's';
            if (secondsleft >= 3600 * 24 * 365.25) {
                secondsleft /= (3600 * 24 * 365.25);
                timeunit = 'y';
            } else if (secondsleft >= 3600 * 24 * 30.4375) {
                secondsleft /= (3600 * 24 * 30.4375);
                timeunit = 'M';
            } else if (secondsleft >= 3600 * 24 * 7) {
                secondsleft /= (3600 * 24 * 7);
                timeunit = 'w';
            } else if (secondsleft >= 3600 * 24) {
                secondsleft /= (3600 * 24);
                timeunit = 'd';
            } else if (secondsleft >= 3600) {
                secondsleft /= 3600;
                timeunit = 'h';
            } else if (secondsleft >= 60) {
                secondsleft /= 60;
                timeunit = 'm';
            }

            Location lastlocation = (Location)yaml.get("lastlocation", new Location(main.getServer().getWorld("backupLocation.world"),
                                                                                    main.getConfig().getDouble("backupLocation.x"),
                                                                                    main.getConfig().getDouble("backupLocation.y"),
                                                                                    main.getConfig().getDouble("backupLocation.z"),
                                                                                    (float)main.getConfig().getDouble("backupLocation.yaw"),
                                                                                    (float)main.getConfig().getDouble("backupLocation.pitch")));
            String lastlocationString = "x:" + lastlocation.getBlockX() + " y:" +
                                        lastlocation.getBlockY() + " z:" +
                                        lastlocation.getBlockZ() + " §6in §f" +
                                        lastlocation.getWorld().getName();
            infoLines[0] = "§6Info for jailed player:";
            infoLines[1] = "  §6· Player: §c" + yaml.getString("name", "§oundefined");
            infoLines[2] = "  §6· UUID: §c" + yaml.getString("uuid", "§oundefined");
            infoLines[3] = "  §6· Time left: §c" + df.format(secondsleft) + timeunit;
            infoLines[4] = "  §6· Jailed in jail: §c" + yaml.getString("jail", "§oundefined");
            infoLines[5] = "  §6· Jailed by: §c" + yaml.getString("jailedby", "§oundefined");
            infoLines[6] = "  §6· Location before jailed: §f" + lastlocationString;
            infoLines[7] = "  §6· Parent group: §c" + yaml.getString("group", main.getConfig().getBoolean("changeGroup", false) ? "§oundefined" : "§oFeature not enabled");

            sender.sendMessage(infoLines);
        } else {
            sender.sendMessage(messages.getString("infoFailedPlayerNeverJoined")
                                       .replace("{prisoner}", prisoner)
                                       .replace("{player}", sender.getName())
                                       .replace('&', '§'));
        }
        return true;
    }

    private boolean jails(CommandSender sender) {
        List<String> jailsList = new ArrayList<>();
        if (main.dataHandler.getJails().size() == 0) {
            jailsList.add(messages.getString("listNoJails")
                                  .replace('&', '§'));
        } else {
            jailsList.add(messages.getString("listJailsPremessage")
                                  .replace('&', '§'));

            switch (messages.getString("jailsFormat")) {
                case "list":
                    main.dataHandler.getJails().forEach((k, v) -> jailsList.add("§6· " + k));
                    break;

                case "line":
                    String line = "§6";
                    for (String key : main.dataHandler.getJails().keySet())
                        line = line.concat(key + ", ");
                    jailsList.add(line.substring(0, line.lastIndexOf(',')).concat("."));
                    break;
            }
        }
        sender.sendMessage(jailsList.toArray(new String[0]));
        return true;
    }

    private boolean unjailPlayer(CommandSender sender, String prisoner) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(alltimePlayers.getOrDefault(prisoner, defaultUUID));
        if (!p.getUniqueId().equals(defaultUUID)) {
            // Once it's been confirmed the player exists and can be jailed, check if the jail exists.
            boolean wasUnjailed;
            wasUnjailed = main.dataHandler.removeJailedPlayer(p.getUniqueId());

            if (wasUnjailed) {
                List<Player> onlinePlayers = new ArrayList<>(main.getServer().getOnlinePlayers());
                for (Player playerToBroadcast : onlinePlayers) {
                    if (playerToBroadcast.hasPermission("betterjails.receivebroadcast"))
                        playerToBroadcast.sendMessage(messages.getString("unjailSuccess")
                                                              .replace("{prisoner}", prisoner)
                                                              .replace("{player}", sender.getName())
                                                              .replace('&', '§'));
                }
                main.getServer().getConsoleSender().sendMessage(messages.getString("unjailSuccess")
                                                                        .replace("{prisoner}", prisoner)
                                                                        .replace("{player}", sender.getName())
                                                                        .replace('&', '§'));
            } else {
                sender.sendMessage(messages.getString("unjailFailedPlayerNotJailed")
                                           .replace("{prisoner}", prisoner)
                                           .replace("{player}", sender.getName())
                                           .replace('&', '§'));
            }
            return true;
        }
        sender.sendMessage(messages.getString("unjailFailedPlayerNeverJoined")
                                   .replace("{prisoner}", prisoner)
                                   .replace("{player}", sender.getName())
                                   .replace('&', '§'));
        return true;
    }

    private boolean setjail(CommandSender sender, String jail) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.getString("setjailFromConsole")
                                       .replace("{player}", sender.getName())
                                       .replace("{jail}", jail)
                                       .replace('&', '§'));
        } else {
            Player p = ((Player)sender);
            try {
                main.dataHandler.addJail(jail, p.getLocation());
                sender.sendMessage(messages.getString("setjailSuccess")
                                           .replace("{player}", sender.getName())
                                           .replace("{jail}", jail)
                                           .replace('&', '§'));
            } catch (IOException e) {
                sender.sendMessage("§cThere was an error while trying to add the jail.");
                e.printStackTrace();
            }
        }
        return true;
    }

    private boolean deljail(CommandSender sender, String jail) {
        if (main.dataHandler.getJail(jail) != null) {
            try {
                main.dataHandler.removeJail(jail);
                sender.sendMessage(messages.getString("deljailSuccess")
                                           .replace("{player}", sender.getName())
                                           .replace("{jail}", jail)
                                           .replace('&', '§'));
            } catch (IOException e) {
                sender.sendMessage("§cThere was an error while trying to remove the jail.");
                e.printStackTrace();
            }
        } else {
            sender.sendMessage(messages.getString("deljailFailed")
                                       .replace("{player}", sender.getName())
                                       .replace("{jail}", jail)
                                       .replace('&', '§'));
        }
        return true;
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String name = player.getName();
        UUID uuid = player.getUniqueId();
        alltimePlayers.put(name, uuid);
    }
}
