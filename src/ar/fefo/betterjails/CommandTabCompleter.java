package ar.fefo.betterjails;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {
    private Main main;

    CommandTabCompleter(Main main) {
        this.main = main;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command cmd,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        List<String> returnedList = new ArrayList<>();

        switch (cmd.getName()) {
            case "jail":
                switch (args.length) {
                    case 1:
                        for (Player player : main.getServer().getOnlinePlayers())
                            if (player.getName().toLowerCase().contains(args[0].toLowerCase()))
                                returnedList.add(player.getName());
                        break;
                    case 2:
                        for (Jail jail : main.dataHandler.getJails())
                            if (jail.getName().toLowerCase().contains(args[1].toLowerCase()))
                                returnedList.add(jail.getName());
                        break;
                    case 3:
                        if (args[2].length() < 2)
                            returnedList.addAll(Arrays.asList("24h",
                                    "12h",
                                    "6h",
                                    "3h",
                                    "30m"));
                        break;
                    default:
                        returnedList.add("");
                        break;
                }
                break;

            case "unjail":
                if (args.length == 1) {
                    for (Player player : main.getServer().getOnlinePlayers())
                        if (player.getName().toLowerCase().contains(args[0].toLowerCase()))
                            returnedList.add(player.getName());
                } else {
                    returnedList.add("");
                }
                break;

            case "setjail":
                if (args.length == 1) {
                    returnedList.add("<jail name>");
                } else {
                    returnedList.add("");
                }
                break;

            case "deljail":
                if (args.length == 1) {
                    for (Jail jail : main.dataHandler.getJails())
                        if (jail.getName().toLowerCase().contains(args[0].toLowerCase()))
                            returnedList.add(jail.getName());
                } else {
                    returnedList.add("");
                }
                break;

            case "jails":
                returnedList.add("");
                break;

            case "betterjails":
                if (args.length == 1) {
                    boolean reloadPerms = sender.hasPermission("betterjails.betterjails.reload");
                    boolean savePerms = sender.hasPermission("betterjails.betterjails.save");
                    if (!reloadPerms && !savePerms) {
                        returnedList.add("");
                        break;
                    }
                    if (reloadPerms &&
                            "reload".contains(args[0].toLowerCase()))
                        returnedList.add("reload");

                    if (savePerms &&
                            "save".contains(args[0].toLowerCase()))
                        returnedList.add("save");
                } else
                    returnedList.add("");
                break;
        }

        return returnedList;
    }
}
