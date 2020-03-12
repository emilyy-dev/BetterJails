package ar.fefo.betterjails;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandHandler implements CommandExecutor {
    private Main main;

    private CommandHandler(Main main) {
        this.main = main;

        CommandTabCompleter tabCompleter = new CommandTabCompleter(this.main);

        this.main.getCommand("jail").setExecutor(this);
        this.main.getCommand("jail").setTabCompleter(tabCompleter);
        this.main.getCommand("jails").setExecutor(this);
        this.main.getCommand("jails").setTabCompleter(tabCompleter);
        this.main.getCommand("unjail").setExecutor(this);
        this.main.getCommand("unjail").setTabCompleter(tabCompleter);
        this.main.getCommand("setjail").setExecutor(this);
        this.main.getCommand("setjail").setTabCompleter(tabCompleter);
        this.main.getCommand("deljail").setExecutor(this);
        this.main.getCommand("deljail").setTabCompleter(tabCompleter);
        this.main.getCommand("betterjails").setExecutor(this);
        this.main.getCommand("betterjails").setTabCompleter(tabCompleter);
    }

    static void init(Main main) {
        new CommandHandler(main);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String alias,
                             @NotNull String[] args) {
        switch (cmd.getName()) {
            case "betterjails":
                sender.sendMessage("§cBetterJails §6by §cFefo6644 §6- v" + main.getDescription().getVersion());
                break;

            case "jail":
                if (args.length != 3) {
                    return false;
                } else {
                    OfflinePlayer[] alltimePlayers = main.getServer().getOfflinePlayers();
                    for (OfflinePlayer p : alltimePlayers) {
                        // Check if the player even exists in the server.
                        if (Objects.requireNonNull(p.getName()).equalsIgnoreCase(args[0])) {
                            // Once it's been confirmed the player exists and can be jailed, check if the jail exists.
                            Player player = p.getPlayer();
                            if (player != null && player.hasPermission("betterjails.jail.exempt")) {
                                sender.sendMessage("§c" + args[0] + " cannot be jailed.");
                                return true;
                            }
                            if (main.dataHandler.getJail(args[1]) != null) {
                                // Once it's been confirmed the jail exists, check if the time provided is valid.
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
                                        main.dataHandler.addJailedPlayer(p, args[1], seconds);
                                    } catch (IOException e) {
                                        sender.sendMessage("§4Fatal error! Could not saved updated jailed_players.yml");
                                        main.getServer().getConsoleSender().sendMessage("§4Fatal error! Could not saved updated jailed_players.yml");
                                        e.printStackTrace();
                                    }

                                    List<Player> onlinePlayers = new ArrayList<>(main.getServer().getOnlinePlayers());
                                    for (Player playerToBroadcast : onlinePlayers) {
                                        if (playerToBroadcast.hasPermission("betterjails.receivebroadcast"))
                                            playerToBroadcast.sendMessage("§c" + args[0] + " §6was jailed by §c" + sender.getName() + " §6for §c" + args[2]);
                                    }
                                    main.getServer().getConsoleSender().sendMessage("§c" + args[0] + " §6was jailed by §c" + sender.getName() + " §6for §c" + args[2]);
                                } else {
                                    sender.sendMessage("§cThe time provided is not valid.");
                                }
                                return true;
                            }
                            sender.sendMessage("§cJail §4" + args[1] + " §cnot found.");
                            return true;
                        }
                    }
                    sender.sendMessage("§cPlayer §4" + args[0] + " §cnever joined this server.");
                }
                break;

            case "jails":
                String[] messages = new String[main.dataHandler.getJails().size() + 1];
                if (main.dataHandler.getJails().size() == 0) {
                    messages[0] = "§6There are no jails available!";
                } else {
                    messages[0] = "§6Available jails:";
                    for (int i = 0; i < messages.length - 1; ++i)
                        messages[i + 1] = " §6§l· §r§6" + main.dataHandler.getJails().get(i).getName();
                }
                sender.sendMessage(messages);
                break;

            case "unjail":
                if (args.length != 1) {
                    return false;
                } else {
                    OfflinePlayer[] alltimePlayers = main.getServer().getOfflinePlayers();
                    for (OfflinePlayer p : alltimePlayers) {
                        // Check if the player even exists in the server.
                        if (Objects.requireNonNull(p.getName()).equalsIgnoreCase(args[0])) {
                            // Once it's been confirmed the player exists and can be jailed, check if the jail exists.
                            try {
                                main.dataHandler.removeJailedPlayer(p.getUniqueId());
                            } catch (IOException e) {
                                sender.sendMessage("§4Fatal error! Could not saved updated jailed_players.yml");
                                main.getServer().getConsoleSender().sendMessage("§4Fatal error! Could not saved updated jailed_players.yml");
                                e.printStackTrace();
                            }

                            List<Player> onlinePlayers = new ArrayList<>(main.getServer().getOnlinePlayers());
                            for (Player playerToBroadcast : onlinePlayers) {
                                if (playerToBroadcast.hasPermission("betterjails.recievebroadcast"))
                                    playerToBroadcast.sendMessage("§c" + args[0] + " §6was unjailed by §c" + sender.getName());
                            }
                            main.getServer().getConsoleSender().sendMessage("§c" + args[0] + " §6was unjailed by §c" + sender.getName());
                            return true;
                        }
                    }
                    sender.sendMessage("§cPlayer §4" + args[0] + " §cnever joined this server.");
                }
                break;

            case "setjail":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cOnly players can set jails!");
                } else if (args.length != 1) {
                    return false;
                } else {
                    Player p = ((Player)sender);
                    try {
                        main.dataHandler.addJail(args[0], p.getLocation());
                        sender.sendMessage("§6Jail added successfully!");
                    } catch (IOException e) {
                        sender.sendMessage("§cThere was an error while trying to add the jail.");
                        e.printStackTrace();
                    }
                }
                break;

            case "deljail":
                if (args.length != 1) {
                    return false;
                } else {
                    if (main.dataHandler.getJail(args[0]) != null) {
                        try {
                            main.dataHandler.removeJail(args[0]);
                            sender.sendMessage("§6Jail removed successfully!");
                        } catch (IOException e) {
                            sender.sendMessage("§cThere was an error while trying to remove the jail.");
                            e.printStackTrace();
                        }
                    } else {
                        sender.sendMessage("§cThat jail does not exist!");
                    }
                }
                break;
        }
        return true;
    }
}
