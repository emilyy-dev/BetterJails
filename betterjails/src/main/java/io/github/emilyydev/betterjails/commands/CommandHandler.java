//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2022 emilyy-dev
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package io.github.emilyydev.betterjails.commands;

import com.github.fefo.betterjails.api.event.plugin.PluginReloadEvent;
import com.github.fefo.betterjails.api.model.jail.Jail;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.config.BetterJailsConfiguration;
import io.github.emilyydev.betterjails.util.Util;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import static io.github.emilyydev.betterjails.util.Util.color;
import static io.github.emilyydev.betterjails.util.Util.uuidOrNil;

public class CommandHandler implements CommandExecutor, Listener {

  private static final String[] DUMMY_STRING_ARRAY = new String[0];

  private final Server server;
  private final BetterJailsPlugin plugin;
  private final BetterJailsConfiguration configuration;
  private final Map<String, UUID> namesToUuid = new HashMap<>();

  public CommandHandler(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
    this.server = plugin.getServer();
    this.configuration = plugin.configuration();

    this.server.getPluginManager().registerEvent(
        PlayerJoinEvent.class, this, EventPriority.HIGH,
        (l, e) -> playerJoin((PlayerJoinEvent) e), this.plugin
    );

    for (final OfflinePlayer offlinePlayer : this.server.getOfflinePlayers()) {
      final String name = offlinePlayer.getName();
      if (name != null) {
        this.namesToUuid.put(name.toLowerCase(Locale.ROOT), offlinePlayer.getUniqueId());
      }
    }
  }

  @Override
  public boolean onCommand(
      final @NotNull CommandSender sender,
      final @NotNull Command cmd,
      final @NotNull String alias,
      final @NotNull String @NotNull [] args
  ) {
    switch (cmd.getName()) {
      case "betterjails":
        if (args.length == 1) {
          return betterjails(sender, args[0]);
        } else {
          sender.sendMessage(color("&bBetterJails &3by &bemilyy-dev &3- v%s", this.plugin.getDescription().getVersion()));
          return true;
        }

      case "jail":
        if (args.length == 2 && "info".equalsIgnoreCase(args[0])) {
          return prisonerInfo(sender, args[1]);
        } else if (args.length == 3) {
          return jailPlayer(sender, args[0], args[1], args[2]);
        } else {
          return false;
        }

      case "jails":
        return listJails(sender);

      case "unjail":
        if (args.length != 1) {
          return false;
        } else {
          return releasePrisoner(sender, args[0]);
        }

      case "setjail":
        if (args.length != 1) {
          return false;
        } else {
          return createOrRenameJail(sender, args[0]);
        }

      case "deljail":
        if (args.length != 1) {
          return false;
        } else {
          return deleteJail(sender, args[0]);
        }

      default: // how did you get here?
        return false;
    }
  }

  private boolean betterjails(final CommandSender sender, final String argument) {
    if (argument.equalsIgnoreCase("reload") && sender.hasPermission("betterjails.betterjails.reload")) {
      try {
        this.plugin.reload();
        this.plugin.eventBus().post(PluginReloadEvent.class, sender);
        sender.sendMessage(this.configuration.messages().reloadData(sender.getName()));
      } catch (final IOException exception) {
        exception.printStackTrace();
        sender.sendMessage(color(
            "&cThere was an internal error while trying to reload the data files.\n" +
            "Please check console for more information."
        ));
      }
    } else if (argument.equalsIgnoreCase("save") && sender.hasPermission("betterjails.betterjails.save")) {
      try {
        this.plugin.dataHandler().save();
        sender.sendMessage(this.configuration.messages().saveData(sender.getName()));
      } catch (final IOException exception) {
        exception.printStackTrace();
        sender.sendMessage(color(
            "&cThere was an internal error while trying to save the data files.\n" +
            "Please check console for more information."
        ));
      }
    } else {
      sender.sendMessage(color("&bBetterJails &3by &bemilyy-dev &3- v%s", this.plugin.getDescription().getVersion()));
    }

    return true;
  }

  private boolean jailPlayer(final CommandSender sender, final String prisoner, final String jail, final String time) {
    final String executioner = sender.getName();
    final OfflinePlayer player = this.server.getOfflinePlayer(
        this.namesToUuid.getOrDefault(prisoner.toLowerCase(Locale.ROOT), Util.NIL_UUID)
    );

    if (player.getUniqueId().equals(Util.NIL_UUID)) {
      sender.sendMessage(this.configuration.messages().jailPlayerFailedNeverJoined(
          prisoner, executioner, jail, time
      ));
      return true;
    }

    if (player.isOnline() && player.getPlayer().hasPermission("betterjails.jail.exempt")) {
      sender.sendMessage(this.configuration.messages().jailPlayerFailedExempt(
          prisoner, executioner, jail, time
      ));
      return true;
    }

    if (!time.matches("^(\\d{1,10}(\\.\\d{1,2})?)[yMwdhms]$")) {
      sender.sendMessage(this.configuration.messages().jailPlayerFailedInvalidTimeInput(
          prisoner, executioner, jail, time
      ));
      return true;
    }

    final double scale;
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

    final long seconds = (long) (scale * Double.parseDouble(time.substring(0, time.length() - 1)));
    try {
      if (!this.plugin.dataHandler().addJailedPlayer(player, jail, uuidOrNil(sender), executioner, seconds)) {
        sender.sendMessage(this.configuration.messages().jailPlayerFailedJailNotFound(
            prisoner, executioner, jail, time
        ));
        return true;
      }
    } catch (final IOException exception) {
      exception.printStackTrace();
      sender.sendMessage(color("&4Fatal error! Could not save player data"));
    }

    this.server.broadcast(
        this.configuration.messages().jailPlayerSuccess(prisoner, executioner, jail, time),
        "betterjails.receivebroadcast"
    );

    return true;
  }

  private boolean prisonerInfo(final CommandSender sender, final String prisoner) {
    final String executioner = sender.getName();
    final OfflinePlayer player = this.server.getOfflinePlayer(
        this.namesToUuid.getOrDefault(prisoner.toLowerCase(Locale.ROOT), Util.NIL_UUID)
    );

    if (player.getUniqueId().equals(Util.NIL_UUID)) {
      sender.sendMessage(this.configuration.messages().prisonerInfoFailedNeverJoined(prisoner, executioner));
      return true;
    }

    final UUID uuid = player.getUniqueId();
    if (!this.plugin.dataHandler().isPlayerJailed(prisoner)) {
      sender.sendMessage(this.configuration.messages().prisonerInfoFailedNotJailed(prisoner, executioner));
      return true;
    }

    if (
        this.plugin.dataHandler().isReleased(uuid, false) ||
        this.plugin.dataHandler().getSecondsLeft(uuid, 0) <= 0
    ) {
      sender.sendMessage(this.configuration.messages().prisonerInfoFailedNotJailed(prisoner, executioner));
      return true;
    }

    double secondsLeft = this.plugin.dataHandler().getSecondsLeft(uuid, 0);
    char timeUnit = 's';
    if (secondsLeft >= 3600 * 24 * 365.25) {
      secondsLeft /= 3600 * 24 * 365.25;
      timeUnit = 'y';

    } else if (secondsLeft >= 3600 * 24 * 30.4375) {
      secondsLeft /= 3600 * 24 * 30.4375;
      timeUnit = 'M';

    } else if (secondsLeft >= 3600 * 24 * 7) {
      secondsLeft /= 3600 * 24 * 7;
      timeUnit = 'w';

    } else if (secondsLeft >= 3600 * 24) {
      secondsLeft /= 3600 * 24;
      timeUnit = 'd';

    } else if (secondsLeft >= 3600) {
      secondsLeft /= 3600;
      timeUnit = 'h';

    } else if (secondsLeft >= 60) {
      secondsLeft /= 60;
      timeUnit = 'm';
    }

    final Location lastLocation = this.plugin.dataHandler().getLastLocation(uuid);
    final String lastLocationString = color(
        "x:%,d y:%,d z%,d &7in &f%s",
        lastLocation.getBlockX(),
        lastLocation.getBlockY(),
        lastLocation.getBlockZ(),
        lastLocation.getWorld().getName()
    );

    final List<String> infoLines = new ArrayList<>(9);
    infoLines.add(color("&7Info for jailed player:"));
    infoLines.add(color("  &7· Name: &f%s", this.plugin.dataHandler().getName(uuid, "&oundefined")));
    infoLines.add(color("  &7· UUID: &f%s", uuid));
    infoLines.add(color("  &7· Time left: &f%,.2f%s", secondsLeft, timeUnit));
    infoLines.add(color("  &7· Jailed in jail: &f%s", this.plugin.dataHandler().getJail(uuid, "&oundefined")));
    infoLines.add(color("  &7· Jailed by: &f%s", this.plugin.dataHandler().getJailer(uuid, "&oundefined")));
    infoLines.add(color("  &7· Location before jailed: &f%s", lastLocationString));
    infoLines.add(color("  &7· Primary group: &f%s", this.plugin.dataHandler().getPrimaryGroup(
        uuid, this.configuration.permissionHookEnabled() ? "&oundefined" : "&oFeature not enabled"
    )));

    final StringJoiner joiner = new StringJoiner(", ");
    joiner.setEmptyValue(this.configuration.permissionHookEnabled() ? "&oNone" : "&oFeature not enabled");
    this.plugin.dataHandler().getAllParentGroups(uuid).forEach(joiner::add);
    infoLines.add(color("  &7· All parent groups: &f%s", joiner.toString()));

    sender.sendMessage(infoLines.toArray(DUMMY_STRING_ARRAY));
    return true;
  }

  private boolean listJails(final CommandSender sender) {
    final BetterJailsConfiguration.MessageHolder messages = this.configuration.messages();
    final Map<String, Jail> jails = this.plugin.dataHandler().getJails();
    final List<String> jailListMessageThingu = new ArrayList<>();

    if (jails.isEmpty()) {
      jailListMessageThingu.add(messages.listJailsNoJails());
    } else {
      jailListMessageThingu.add(messages.listJailsFunnyMessage());
      jailListMessageThingu.addAll(messages.jailListEntryFormatter().formatJailList(jails.values()));
    }

    sender.sendMessage(jailListMessageThingu.toArray(DUMMY_STRING_ARRAY));
    return true;
  }

  private boolean releasePrisoner(final CommandSender sender, final String prisoner) {
    final String executioner = sender.getName();
    final OfflinePlayer player = this.server.getOfflinePlayer(
        this.namesToUuid.getOrDefault(prisoner.toLowerCase(Locale.ROOT), Util.NIL_UUID)
    );

    if (player.getUniqueId().equals(Util.NIL_UUID)) {
      sender.sendMessage(this.configuration.messages().releasePrisonerFailedNeverJoined(prisoner, executioner));
      return true;
    }

    final boolean wasReleased = this.plugin.dataHandler().releaseJailedPlayer(
        player.getUniqueId(),
        uuidOrNil(sender),
        executioner
    );

    if (wasReleased) {
      this.server.broadcast(
          this.configuration.messages().releasePrisonerSuccess(prisoner, executioner),
          "betterjails.receivebroadcast"
      );
    } else {
      sender.sendMessage(this.configuration.messages().releasePrisonerFailedNotJailed(prisoner, executioner));
    }

    return true;

  }

  private boolean createOrRenameJail(final CommandSender sender, final String jail) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(this.configuration.messages().createJailFromConsole(sender.getName(), jail));
      return true;
    }

    final Player player = (Player) sender;
    try {
      this.plugin.dataHandler().addJail(jail, player.getLocation());
      sender.sendMessage(this.configuration.messages().createJailSuccess(sender.getName(), jail));

    } catch (final IOException exception) {
      sender.sendMessage(color("&cThere was an error while trying to add the jail."));
      exception.printStackTrace();
    }

    return true;
  }

  private boolean deleteJail(final CommandSender sender, final String jail) {
    if (this.plugin.dataHandler().getJail(jail) == null) {
      sender.sendMessage(this.configuration.messages().deleteJailFailed(sender.getName(), jail));
      return true;
    }

    try {
      this.plugin.dataHandler().removeJail(jail);
      sender.sendMessage(this.configuration.messages().deleteJailSuccess(sender.getName(), jail));
    } catch (final IOException exception) {
      sender.sendMessage(color("&cThere was an error while trying to remove the jail."));
      exception.printStackTrace();
    }

    return true;
  }

  private void playerJoin(final PlayerJoinEvent event) {
    final Player player = event.getPlayer();
    this.namesToUuid.put(player.getName().toLowerCase(Locale.ROOT), player.getUniqueId());
  }
}
