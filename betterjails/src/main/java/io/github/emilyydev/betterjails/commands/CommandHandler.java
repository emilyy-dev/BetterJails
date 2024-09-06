//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2024 emilyy-dev
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
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisoner;
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
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.regex.Pattern;

import static io.github.emilyydev.betterjails.util.Util.color;
import static io.github.emilyydev.betterjails.util.Util.uuidOrNil;

public final class CommandHandler implements CommandExecutor, Listener {

  private static final Logger LOGGER = LoggerFactory.getLogger("BetterJails");

  private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d{1,10}(\\.\\d{1,2})?)[yMwdhms]$");
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
        PlayerLoginEvent.class, this, EventPriority.HIGH,
        (l, e) -> playerLogin((PlayerLoginEvent) e), this.plugin
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
          betterjails(sender, args[0]);
        } else {
          sender.sendMessage(color("&bBetterJails &3by &bemilyy-dev &3- v%s", this.plugin.getDescription().getVersion()));
        }
        return true;

      case "jail":
        if (args.length == 2 && "info".equalsIgnoreCase(args[0])) {
          prisonerInfo(sender, args[1]);
          return true;
        } else if (args.length == 3) {
          jailPlayer(sender, args[0], args[1], args[2]);
          return true;
        } else {
          return false;
        }

      case "jails":
        listJails(sender);
        return true;

      case "unjail":
        if (args.length != 1) {
          return false;
        } else {
          releasePrisoner(sender, args[0]);
          return true;
        }

      case "setjail":
        if (args.length != 1) {
          return false;
        } else {
          createOrRenameJail(sender, args[0]);
          return true;
        }

      case "deljail":
        if (args.length != 1) {
          return false;
        } else {
          deleteJail(sender, args[0]);
          return true;
        }

      default: // how did you get here?
        return false;
    }
  }

  private void betterjails(final CommandSender sender, final String argument) {
    if (argument.equalsIgnoreCase("reload") && sender.hasPermission("betterjails.betterjails.reload")) {
      try {
        this.plugin.reload();
        this.plugin.eventBus().post(PluginReloadEvent.class, sender);
        sender.sendMessage(this.configuration.messages().reloadData(sender.getName()));
      } catch (final IOException ex) {
        LOGGER.error("An error occurred reloading plugin data", ex);
        sender.sendMessage(color(
            "&cThere was an internal error while trying to reload the data files.\n" +
                "Please check console for more information."
        ));
      }
    } else if (argument.equalsIgnoreCase("save") && sender.hasPermission("betterjails.betterjails.save")) {
      this.plugin.saveAll().whenComplete((v, ex) -> {
        if (ex == null) {
          sender.sendMessage(this.configuration.messages().saveData(sender.getName()));
        } else {
          sender.sendMessage(color(
              "&cThere was an internal error while trying to save the data files.\n" +
                  "Please check console for more information."
          ));
        }
      });
    } else {
      sender.sendMessage(color("&bBetterJails &3by &bemilyy-dev &3- v%s", this.plugin.getDescription().getVersion()));
    }
  }

  private void jailPlayer(final CommandSender sender, final String prisoner, final String jail, final String time) {
    final String executioner = sender.getName();
    final OfflinePlayer player = this.server.getOfflinePlayer(
        this.namesToUuid.getOrDefault(prisoner.toLowerCase(Locale.ROOT), Util.NIL_UUID)
    );

    if (player.getUniqueId().equals(Util.NIL_UUID)) {
      sender.sendMessage(this.configuration.messages().jailPlayerFailedNeverJoined(
          prisoner, executioner, jail, time
      ));
      return;
    }

    if (player.isOnline() && player.getPlayer().hasPermission("betterjails.jail.exempt")) {
      sender.sendMessage(this.configuration.messages().jailPlayerFailedExempt(
          prisoner, executioner, jail, time
      ));
      return;
    }

    if (!DURATION_PATTERN.matcher(time).matches()) {
      sender.sendMessage(this.configuration.messages().jailPlayerFailedInvalidTimeInput(
          prisoner, executioner, jail, time
      ));
      return;
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
    if (!this.plugin.prisonerData().addJailedPlayer(player, jail, uuidOrNil(sender), executioner, seconds, true)) {
      sender.sendMessage(this.configuration.messages().jailPlayerFailedJailNotFound(
          prisoner, executioner, jail, time
      ));
      return;
    }

    this.server.broadcast(
        this.configuration.messages().jailPlayerSuccess(prisoner, executioner, jail, time),
        "betterjails.receivebroadcast"
    );
  }

  private void prisonerInfo(final CommandSender sender, final String prisonerName) {
    final String executioner = sender.getName();
    final OfflinePlayer player = this.server.getOfflinePlayer(
        this.namesToUuid.getOrDefault(prisonerName.toLowerCase(Locale.ROOT), Util.NIL_UUID)
    );

    if (player.getUniqueId().equals(Util.NIL_UUID)) {
      sender.sendMessage(this.configuration.messages().prisonerInfoFailedNeverJoined(prisonerName, executioner));
      return;
    }

    final UUID uuid = player.getUniqueId();
    final ApiPrisoner prisoner = this.plugin.prisonerData().getPrisoner(uuid);
    if (prisoner == null) {
      sender.sendMessage(this.configuration.messages().prisonerInfoFailedNotJailed(prisonerName, executioner));
      return;
    }

    if (prisoner.released()) {
      sender.sendMessage(this.configuration.messages().prisonerInfoFailedNotJailed(prisonerName, executioner));
      return;
    }

    double secondsLeft = prisoner.timeLeft().getSeconds();
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

    final Location lastLocation = prisoner.lastLocationMutable();
    final String lastLocationString = lastLocation == null
        ? color("&cunknown")
        : color(
          "x:%,d y:%,d z%,d &7in &f%s",
          lastLocation.getBlockX(),
          lastLocation.getBlockY(),
          lastLocation.getBlockZ(),
          lastLocation.getWorld().getName()
        );

    final List<String> infoLines = new ArrayList<>(9);
    infoLines.add(color("&7Info for jailed player:"));
    infoLines.add(color("  &7· Name: &f%s", prisoner.name() == null ? "&oundefined" : prisoner.name()));
    infoLines.add(color("  &7· UUID: &f%s", uuid));
    infoLines.add(color("  &7· Time left: &f%,.2f%s", secondsLeft, timeUnit));
    infoLines.add(color("  &7· Jailed in jail: &f%s", prisoner.jail().name()));
    infoLines.add(color("  &7· Jailed by: &f%s", prisoner.jailedBy() == null ? "&oundefined" : prisoner.jailedBy()));
    infoLines.add(color("  &7· Location before jailed: &f%s", lastLocationString));
    infoLines.add(color("  &7· Primary group: &f%s", prisoner.primaryGroup() == null
        ? (this.configuration.permissionHookEnabled() ? "&oundefined" : "&oFeature not enabled")
        : prisoner.primaryGroup()
    ));

    final StringJoiner joiner = new StringJoiner(", ");
    joiner.setEmptyValue(this.configuration.permissionHookEnabled() ? "&oNone" : "&oFeature not enabled");
    prisoner.parentGroups().forEach(joiner::add);
    infoLines.add(color("  &7· All parent groups: &f%s", joiner.toString()));

    sender.sendMessage(infoLines.toArray(DUMMY_STRING_ARRAY));
  }

  private void listJails(final CommandSender sender) {
    final BetterJailsConfiguration.MessageHolder messages = this.configuration.messages();
    final Map<String, Jail> jails = this.plugin.jailData().getJails();
    final List<String> jailListMessageThingu = new ArrayList<>();

    if (jails.isEmpty()) {
      jailListMessageThingu.add(messages.listJailsNoJails());
    } else {
      jailListMessageThingu.add(messages.listJailsFunnyMessage());
      jailListMessageThingu.addAll(messages.jailListEntryFormatter().formatJailList(jails.values()));
    }

    sender.sendMessage(jailListMessageThingu.toArray(DUMMY_STRING_ARRAY));
  }

  private void releasePrisoner(final CommandSender sender, final String prisoner) {
    final String executioner = sender.getName();
    final OfflinePlayer player = this.server.getOfflinePlayer(
        this.namesToUuid.getOrDefault(prisoner.toLowerCase(Locale.ROOT), Util.NIL_UUID)
    );

    if (player.getUniqueId().equals(Util.NIL_UUID)) {
      sender.sendMessage(this.configuration.messages().releasePrisonerFailedNeverJoined(prisoner, executioner));
      return;
    }

    final boolean wasReleased = this.plugin.prisonerData().releaseJailedPlayer(player, uuidOrNil(sender), executioner, true);
    if (wasReleased) {
      this.server.broadcast(
          this.configuration.messages().releasePrisonerSuccess(prisoner, executioner),
          "betterjails.receivebroadcast"
      );
    } else {
      sender.sendMessage(this.configuration.messages().releasePrisonerFailedNotJailed(prisoner, executioner));
    }
  }

  private void createOrRenameJail(final CommandSender sender, final String jail) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(this.configuration.messages().createJailFromConsole(sender.getName(), jail));
      return;
    }

    final Player player = (Player) sender;
    this.plugin.jailData().addJail(jail, player.getLocation()).whenCompleteAsync((v, ex) -> {
      if (ex == null) {
        sender.sendMessage(this.configuration.messages().createJailSuccess(sender.getName(), jail));
      } else {
        LOGGER.error("An error occurred saving data for jail {}", jail, ex);
        sender.sendMessage(color("&cThere was an error while trying to add the jail."));
      }
    }, this.plugin);
  }

  private void deleteJail(final CommandSender sender, final String jail) {
    if (this.plugin.jailData().getJail(jail) == null) {
      sender.sendMessage(this.configuration.messages().deleteJailFailed(sender.getName(), jail));
      return;
    }

    this.plugin.jailData().removeJail(jail).whenCompleteAsync((v, ex) -> {
      if (ex == null) {
        sender.sendMessage(this.configuration.messages().deleteJailSuccess(sender.getName(), jail));
      } else {
        LOGGER.error("An error occurred deleting data for jail {}", jail, ex);
        sender.sendMessage(color("&cThere was an error while trying to remove the jail."));
      }
    }, this.plugin);
  }

  private void playerLogin(final PlayerLoginEvent event) {
    final Player player = event.getPlayer();
    this.namesToUuid.put(player.getName().toLowerCase(Locale.ROOT), player.getUniqueId());
  }
}
