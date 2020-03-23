package ar.fefo.betterjails.commands;

import ar.fefo.betterjails.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ConstantConditions")
public class CommandHandler implements CommandExecutor {
    private static CommandHandler instance;
    private Main main;
    private ConfigurationSection messages;

    private CommandHandler(@NotNull Main main) {
        this.main = main;
        messages = this.main.getConfig().getConfigurationSection("messages");
    }
    public static void init(@NotNull Main main) { instance = new CommandHandler(main); }
    public static CommandHandler getInstance() { return instance; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String alias,
                             @NotNull String[] args) {
        switch (cmd.getName()) {
            case "betterjails": {
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("reload") &&
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
                    } else if (args[0].equalsIgnoreCase("save") &&
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
                            sender.sendMessage("§cThere was an internal error while trying to save the data files.\nPlease check console for more information.");
                            e.printStackTrace();
                        }
                    } else {
                        sender.sendMessage("§cBetterJails §6by §cFefo6644 §6- v" + main.getDescription().getVersion());
                    }
                } else {
                    sender.sendMessage("§cBetterJails §6by §cFefo6644 §6- v" + main.getDescription().getVersion());
                }
                break;
            }

            case "jail": {
                if (args.length != 3) {
                    return false;
                } else {
                    OfflinePlayer player = null;
                    for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers())
                        if (offlinePlayer.getName().equalsIgnoreCase(args[0])) {
                            player = offlinePlayer;
                            break;
                        }

                    if (player != null) {
                        if (player.isOnline() && ((Player)player).hasPermission("betterjails.jail.exempt")) {
                            sender.sendMessage(messages.getString("jailFailedPlayerExempt")
                                                       .replace("{prisoner}", args[0])
                                                       .replace("{player}", sender.getName())
                                                       .replace("{jail}", args[1])
                                                       .replace("{time}", args[2])
                                                       .replace('&', '§'));
                            return true;
                        }
                        if (args[2].matches("\\d+[yMwdhms]")) {
                            double scale;
                            switch (args[2].charAt(args[2].length() - 1)) {
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
                            int seconds = (int)(scale * Integer.parseInt(args[2].substring(0, args[2].length() - 1)));
                            try {
                                if (!main.dataHandler.addJailedPlayer(player, args[1], seconds)) {
                                    sender.sendMessage(messages.getString("jailFailedJailNotFound")
                                                               .replace("{prisoner}", args[0])
                                                               .replace("{player}", sender.getName())
                                                               .replace("{jail}", args[1])
                                                               .replace("{time}", args[2])
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
                                                                          .replace("{prisoner}", args[0])
                                                                          .replace("{player}", sender.getName())
                                                                          .replace("{jail}", args[1])
                                                                          .replace("{time}", args[2])
                                                                          .replace('&', '§'));
                            }
                            main.getServer().getConsoleSender().sendMessage(messages.getString("jailSuccess")
                                                                                    .replace("{prisoner}", args[0])
                                                                                    .replace("{player}", sender.getName())
                                                                                    .replace("{jail}", args[1])
                                                                                    .replace("{time}", args[2])
                                                                                    .replace('&', '§'));
                        } else {
                            sender.sendMessage(messages.getString("jailFailedTimeIncorrect")
                                                       .replace("{prisoner}", args[0])
                                                       .replace("{player}", sender.getName())
                                                       .replace("{jail}", args[1])
                                                       .replace("{time}", args[2])
                                                       .replace('&', '§'));
                        }
                        return true;
                    }
                    sender.sendMessage(messages.getString("jailFailedPlayerNeverJoined")
                                               .replace("{prisoner}", args[0])
                                               .replace("{player}", sender.getName())
                                               .replace("{jail}", args[1])
                                               .replace("{time}", args[2])
                                               .replace('&', '§'));
                }
                break;
            }

            case "jails": {
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
                break;
            }

            case "unjail": {
                if (args.length != 1) {
                    return false;
                } else {
                    OfflinePlayer[] alltimePlayers = main.getServer().getOfflinePlayers();
                    for (OfflinePlayer p : alltimePlayers) {
                        // Check if the player even exists in the server.
                        if (Objects.requireNonNull(p.getName()).equalsIgnoreCase(args[0])) {
                            // Once it's been confirmed the player exists and can be jailed, check if the jail exists.
                            boolean wasUnjailed;
                            wasUnjailed = main.dataHandler.removeJailedPlayer(p.getUniqueId());

                            if (wasUnjailed) {
                                List<Player> onlinePlayers = new ArrayList<>(main.getServer().getOnlinePlayers());
                                for (Player playerToBroadcast : onlinePlayers) {
                                    if (playerToBroadcast.hasPermission("betterjails.receivebroadcast"))
                                        playerToBroadcast.sendMessage(messages.getString("unjailSuccess")
                                                                              .replace("{prisoner}", args[0])
                                                                              .replace("{player}", sender.getName())
                                                                              .replace('&', '§'));
                                }
                                main.getServer().getConsoleSender().sendMessage(messages.getString("unjailSuccess")
                                                                                        .replace("{prisoner}", args[0])
                                                                                        .replace("{player}", sender.getName())
                                                                                        .replace('&', '§'));
                            } else {
                                sender.sendMessage(messages.getString("unjailFailedPlayerNotJailed")
                                                           .replace("{prisoner}", args[0])
                                                           .replace("{player}", sender.getName())
                                                           .replace('&', '§'));
                            }
                            return true;
                        }
                    }
                    sender.sendMessage(messages.getString("unjailFailedPlayerNeverJoined")
                                               .replace("{prisoner}", args[0])
                                               .replace("{player}", sender.getName())
                                               .replace('&', '§'));
                }
                break;
            }

            case "setjail": {
                if (args.length != 1) {
                    return false;
                } else if (!(sender instanceof Player)) {
                    sender.sendMessage(messages.getString("setjailFromConsole")
                                               .replace("{player}", sender.getName())
                                               .replace("{jail}", args[0])
                                               .replace('&', '§'));
                } else {
                    Player p = ((Player)sender);
                    try {
                        main.dataHandler.addJail(args[0], p.getLocation());
                        sender.sendMessage(messages.getString("setjailSuccess")
                                                   .replace("{player}", sender.getName())
                                                   .replace("{jail}", args[0])
                                                   .replace('&', '§'));
                    } catch (IOException e) {
                        sender.sendMessage("§cThere was an error while trying to add the jail.");
                        e.printStackTrace();
                    }
                }
                break;
            }

            case "deljail": {
                if (args.length != 1) {
                    return false;
                } else {
                    if (main.dataHandler.getJail(args[0]) != null) {
                        try {
                            main.dataHandler.removeJail(args[0]);
                            sender.sendMessage(messages.getString("deljailSuccess")
                                                       .replace("{player}", sender.getName())
                                                       .replace("{jail}", args[0])
                                                       .replace('&', '§'));
                        } catch (IOException e) {
                            sender.sendMessage("§cThere was an error while trying to remove the jail.");
                            e.printStackTrace();
                        }
                    } else {
                        sender.sendMessage(messages.getString("deljailFailed")
                                                   .replace("{player}", sender.getName())
                                                   .replace("{jail}", args[0])
                                                   .replace('&', '§'));
                    }
                }
                break;
            }
        }
        return true;
    }
}
