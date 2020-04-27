package ar.fefo.betterjails.commands;

import ar.fefo.betterjails.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {
    private static CommandTabCompleter instance = null;
    private final Main main;

    private CommandTabCompleter(Main main) { this.main = main; }
    public static CommandTabCompleter init(Main main) {
        if (instance == null)
            instance = new CommandTabCompleter(main);
        return instance;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command cmd,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        List<String> returnedList = new ArrayList<>();

        switch (cmd.getName()) {
            case "jail": {
                switch (args.length) {
                    case 1:
                        if ("info".startsWith(args[0].toLowerCase()))
                            returnedList.add("info");
                        for (Player player : main.getServer().getOnlinePlayers())
                            if (player.getName().toLowerCase().startsWith(args[0].toLowerCase()))
                                returnedList.add(player.getName());
                        break;
                    case 2:
                        if (!args[0].equalsIgnoreCase("info")) {
                            for (String jailName : main.dataHandler.getJails().keySet())
                                if (jailName.toLowerCase().startsWith(args[1].toLowerCase()))
                                    returnedList.add(jailName);
                        } else {
                            for (Player player : main.getServer().getOnlinePlayers())
                                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                                    returnedList.add(player.getName());
                        }
                        break;
                    case 3:
                        if (args[2].length() < 2)
                            returnedList.addAll(Arrays.asList("24h",
                                                              "12h",
                                                              "6h",
                                                              "3h",
                                                              "30m"));
                        break;
                }
                break;
            }

            case "unjail": {
                if (args.length == 1) {
                    for (Player player : main.getServer().getOnlinePlayers())
                        if (player.getName().toLowerCase().startsWith(args[0].toLowerCase()))
                            returnedList.add(player.getName());
                }
                break;
            }

            case "setjail": {
                if (args.length == 1)
                    returnedList.add("<jailname>");
                break;
            }

            case "deljail": {
                if (args.length == 1)
                    for (String jailName : main.dataHandler.getJails().keySet())
                        if (jailName.toLowerCase().startsWith(args[0].toLowerCase()))
                            returnedList.add(jailName);
                break;
            }

            case "betterjails": {
                if (args.length == 1) {
                    boolean reloadPerms = sender.hasPermission("betterjails.betterjails.reload");
                    boolean savePerms = sender.hasPermission("betterjails.betterjails.save");

                    if (!reloadPerms && !savePerms)
                        break;

                    if (reloadPerms &&
                        "reload".startsWith(args[0].toLowerCase()))
                        returnedList.add("reload");

                    if (savePerms &&
                        "save".startsWith(args[0].toLowerCase()))
                        returnedList.add("save");

                }
                break;
            }
        }

        return returnedList;
    }
}
