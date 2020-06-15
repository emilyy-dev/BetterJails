package me.fefo.betterjails.commands;

import me.fefo.betterjails.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

public final class CommandHandler implements CommandExecutor, Listener {
  private static CommandHandler instance = null;
  private final UUID defaultUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
  private final Main main;
  private final Hashtable<String, UUID> alltimePlayers = new Hashtable<>();
  private ConfigurationSection messages;

  private CommandHandler(@NotNull Main main) {
    this.main = main;
    messages = main.getConfig().getConfigurationSection("messages");
    main.getServer().getPluginManager().registerEvents(this, main);
    for (OfflinePlayer offlinePlayer : main.getServer().getOfflinePlayers()) {
      if (offlinePlayer.getName() != null) {
        alltimePlayers.put(offlinePlayer.getName(), offlinePlayer.getUniqueId());
      }
    }
  }
  public static CommandHandler init(@NotNull Main main) {
    if (instance == null) {
      instance = new CommandHandler(main);
    }
    return instance;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender,
                           @NotNull Command cmd,
                           @NotNull String alias,
                           @NotNull String[] args) {
    switch (cmd.getName()) {
      case "betterjails":
        if (args.length == 1) {
          return betterjails(sender, args[0]);
        } else {
          sender.sendMessage(ChatColor.AQUA + "BetterJails " +
                             ChatColor.DARK_AQUA + "by " +
                             ChatColor.AQUA + "Fefo6644 " +
                             ChatColor.DARK_AQUA + "- v" + main.getDescription().getVersion());
        }
        return true;

      case "jail":
        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
          return jailInfo(sender, args[1]);
        } else if (args.length == 3) {
          return jailPlayer(sender, args[0], args[1], args[2]);
        } else {
          return false;
        }

      case "jails":
        return jails(sender);

      case "unjail":
        if (args.length != 1) {
          return false;
        } else {
          return unjailPlayer(sender, args[0]);
        }

      case "setjail":
        if (args.length != 1) {
          return false;
        } else {
          return setjail(sender, args[0]);
        }

      case "deljail":
        if (args.length != 1) {
          return false;
        } else {
          return deljail(sender, args[0]);
        }
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
                                 .replace('&', ChatColor.COLOR_CHAR));
    } else if (arg.equalsIgnoreCase("save") &&
               sender.hasPermission("betterjails.betterjails.save")) {
      try {
        main.dataHandler.save();
        sender.sendMessage(messages.getString("save")
                                   .replace("{player}", sender.getName())
                                   .replace('&', ChatColor.COLOR_CHAR));
      } catch (IOException e) {
        sender.sendMessage(ChatColor.RED + "There was an internal error while trying to save the data files.\n" +
                           "Please check console for more information.");
        e.printStackTrace();
      }
    } else {
      sender.sendMessage(ChatColor.AQUA + "BetterJails " +
                         ChatColor.DARK_AQUA + "by " +
                         ChatColor.AQUA + "Fefo6644 " +
                         ChatColor.DARK_AQUA + "- v" + main.getDescription().getVersion());
    }
    return true;
  }

  private boolean jailPlayer(CommandSender sender, String prisoner, String jail, String time) {
    OfflinePlayer player = main.getServer().getOfflinePlayer(alltimePlayers.getOrDefault(prisoner, defaultUUID));
    if (!player.getUniqueId().equals(defaultUUID)) {
      if (player.isOnline() && ((Player)player).hasPermission("betterjails.jail.exempt")) {
        sender.sendMessage(messages.getString("jailFailedPlayerExempt")
                                   .replace("{prisoner}", prisoner)
                                   .replace("{player}", sender.getName())
                                   .replace("{jail}", jail)
                                   .replace("{time}", time)
                                   .replace('&', ChatColor.COLOR_CHAR));
        return true;
      }
//            if (args[2].matches("\\d+[yMwdhms]")) {
      if (time.matches("^(\\d{1,10}(\\.\\d{1,2})?)[yMwdhms]$")) {
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
                                       .replace('&', ChatColor.COLOR_CHAR));
            return true;
          }
        } catch (IOException e) {
          sender.sendMessage(ChatColor.DARK_RED + "Fatal error! Could not save player data");
          main.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "Fatal error! Could not save player data");
          e.printStackTrace();
        }

        List<Player> onlinePlayers = new ArrayList<>(main.getServer().getOnlinePlayers());
        for (Player playerToBroadcast : onlinePlayers) {
          if (playerToBroadcast.hasPermission("betterjails.receivebroadcast")) {
            playerToBroadcast.sendMessage(messages.getString("jailSuccess")
                                                  .replace("{prisoner}", prisoner)
                                                  .replace("{player}", sender.getName())
                                                  .replace("{jail}", jail)
                                                  .replace("{time}", time)
                                                  .replace('&', ChatColor.COLOR_CHAR));
          }
        }
        main.getServer().getConsoleSender().sendMessage(messages.getString("jailSuccess")
                                                                .replace("{prisoner}", prisoner)
                                                                .replace("{player}", sender.getName())
                                                                .replace("{jail}", jail)
                                                                .replace("{time}", time)
                                                                .replace('&', ChatColor.COLOR_CHAR));
      } else {
        sender.sendMessage(messages.getString("jailFailedTimeIncorrect")
                                   .replace("{prisoner}", prisoner)
                                   .replace("{player}", sender.getName())
                                   .replace("{jail}", jail)
                                   .replace("{time}", time)
                                   .replace('&', ChatColor.COLOR_CHAR));
      }
      return true;
    }
    sender.sendMessage(messages.getString("jailFailedPlayerNeverJoined")
                               .replace("{prisoner}", prisoner)
                               .replace("{player}", sender.getName())
                               .replace("{jail}", jail)
                               .replace("{time}", time)
                               .replace('&', ChatColor.COLOR_CHAR));
    return true;
  }

  private boolean jailInfo(CommandSender sender, String prisoner) {
    OfflinePlayer player = main.getServer().getOfflinePlayer(alltimePlayers.getOrDefault(prisoner, defaultUUID));
    if (!player.getUniqueId().equals(defaultUUID)) {
      UUID uuid = player.getUniqueId();
      if (main.dataHandler.isPlayerJailed(prisoner)) {
        if (main.dataHandler.getUnjailed(uuid, false) ||
            main.dataHandler.getSecondsLeft(uuid, 0) <= 0) {
          sender.sendMessage(messages.getString("infoFailedPlayerNotJailed")
                                     .replace("{prisoner}", prisoner)
                                     .replace("{player}", sender.getName())
                                     .replace('&', ChatColor.COLOR_CHAR));
          return true;
        }

        String[] infoLines = new String[8];
        DecimalFormat df = new DecimalFormat("#.##");

        double secondsleft = main.dataHandler.getSecondsLeft(uuid, 0) / 1.0;
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

        Location lastlocation = main.dataHandler.getLastLocation(uuid);
        String lastlocationString = "x:" + lastlocation.getBlockX() + " y:" +
                                    lastlocation.getBlockY() + " z:" +
                                    lastlocation.getBlockZ() + ChatColor.GRAY + " in " + ChatColor.WHITE +
                                    lastlocation.getWorld().getName();
        infoLines[0] = ChatColor.GRAY + "Info for jailed player:";
        infoLines[1] = ChatColor.GRAY + "  · Player: " + ChatColor.WHITE + main.dataHandler.getName(uuid, ChatColor.ITALIC + "undefined");
        infoLines[2] = ChatColor.GRAY + "  · UUID: " + ChatColor.WHITE + uuid;
        infoLines[3] = ChatColor.GRAY + "  · Time left: " + ChatColor.WHITE + df.format(secondsleft) + timeunit;
        infoLines[4] = ChatColor.GRAY + "  · Jailed in jail: " + ChatColor.WHITE + main.dataHandler.getJail(uuid, ChatColor.ITALIC + "undefined");
        infoLines[5] = ChatColor.GRAY + "  · Jailed by: " + ChatColor.WHITE + main.dataHandler.getJailer(uuid, ChatColor.ITALIC + "undefined");
        infoLines[6] = ChatColor.GRAY + "  · Location before jailed: " + ChatColor.WHITE + lastlocationString;
        infoLines[7] = ChatColor.GRAY + "  · Parent group: " + ChatColor.WHITE + main.dataHandler.getGroup(uuid,
                                                                                                           main.getConfig().getBoolean("changeGroup", false) ?
                                                                                                           ChatColor.ITALIC + "undefined" :
                                                                                                           ChatColor.ITALIC + "Feature not enabled");

        sender.sendMessage(infoLines);
      } else {
        sender.sendMessage(messages.getString("infoFailedPlayerNotJailed")
                                   .replace("{prisoner}", prisoner)
                                   .replace("{player}", sender.getName())
                                   .replace('&', ChatColor.COLOR_CHAR));
        return true;
      }
    } else {
      sender.sendMessage(messages.getString("infoFailedPlayerNeverJoined")
                                 .replace("{prisoner}", prisoner)
                                 .replace("{player}", sender.getName())
                                 .replace('&', ChatColor.COLOR_CHAR));
    }
    return true;
  }

  private boolean jails(CommandSender sender) {
    List<String> jailsList = new ArrayList<>();
    if (main.dataHandler.getJails().size() == 0) {
      jailsList.add(messages.getString("listNoJails")
                            .replace('&', ChatColor.COLOR_CHAR));
    } else {
      jailsList.add(messages.getString("listJailsPremessage")
                            .replace('&', ChatColor.COLOR_CHAR));

      switch (messages.getString("jailsFormat")) {
        case "list":
          main.dataHandler.getJails().forEach((k, v) -> jailsList.add(ChatColor.GRAY + "· " + k));
          break;

        case "line":
          String line = "" + ChatColor.GRAY;
          for (String key : main.dataHandler.getJails().keySet()) {
            line = line.concat(key + ", ");
          }
          jailsList.add(line.substring(0, line.lastIndexOf(',')).concat("."));
          break;
      }
    }
    sender.sendMessage(jailsList.toArray(new String[0]));
    return true;
  }

  private boolean unjailPlayer(CommandSender sender, String prisoner) {
    OfflinePlayer p = main.getServer().getOfflinePlayer(alltimePlayers.getOrDefault(prisoner, defaultUUID));
    if (!p.getUniqueId().equals(defaultUUID)) {
      // Once it's been confirmed the player exists and can be jailed, check if the jail exists.
      boolean wasUnjailed;
      wasUnjailed = main.dataHandler.removeJailedPlayer(p.getUniqueId());

      if (wasUnjailed) {
        List<Player> onlinePlayers = new ArrayList<>(main.getServer().getOnlinePlayers());
        for (Player playerToBroadcast : onlinePlayers) {
          if (playerToBroadcast.hasPermission("betterjails.receivebroadcast")) {
            playerToBroadcast.sendMessage(messages.getString("unjailSuccess")
                                                  .replace("{prisoner}", prisoner)
                                                  .replace("{player}", sender.getName())
                                                  .replace('&', ChatColor.COLOR_CHAR));
          }
        }
        main.getServer().getConsoleSender().sendMessage(messages.getString("unjailSuccess")
                                                                .replace("{prisoner}", prisoner)
                                                                .replace("{player}", sender.getName())
                                                                .replace('&', ChatColor.COLOR_CHAR));
      } else {
        sender.sendMessage(messages.getString("unjailFailedPlayerNotJailed")
                                   .replace("{prisoner}", prisoner)
                                   .replace("{player}", sender.getName())
                                   .replace('&', ChatColor.COLOR_CHAR));
      }
      return true;
    }
    sender.sendMessage(messages.getString("unjailFailedPlayerNeverJoined")
                               .replace("{prisoner}", prisoner)
                               .replace("{player}", sender.getName())
                               .replace('&', ChatColor.COLOR_CHAR));
    return true;
  }

  private boolean setjail(CommandSender sender, String jail) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(messages.getString("setjailFromConsole")
                                 .replace("{player}", sender.getName())
                                 .replace("{jail}", jail)
                                 .replace('&', ChatColor.COLOR_CHAR));
    } else {
      Player p = ((Player)sender);
      try {
        main.dataHandler.addJail(jail, p.getLocation());
        sender.sendMessage(messages.getString("setjailSuccess")
                                   .replace("{player}", sender.getName())
                                   .replace("{jail}", jail)
                                   .replace('&', ChatColor.COLOR_CHAR));
      } catch (IOException e) {
        sender.sendMessage(ChatColor.RED + "There was an error while trying to add the jail.");
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
                                   .replace('&', ChatColor.COLOR_CHAR));
      } catch (IOException e) {
        sender.sendMessage(ChatColor.RED + "There was an error while trying to remove the jail.");
        e.printStackTrace();
      }
    } else {
      sender.sendMessage(messages.getString("deljailFailed")
                                 .replace("{player}", sender.getName())
                                 .replace("{jail}", jail)
                                 .replace('&', ChatColor.COLOR_CHAR));
    }
    return true;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
    Player player = e.getPlayer();
    String name = player.getName();
    UUID uuid = player.getUniqueId();
    alltimePlayers.put(name, uuid);
  }
}
