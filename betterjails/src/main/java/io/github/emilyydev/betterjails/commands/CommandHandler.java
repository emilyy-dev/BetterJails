//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2024 emilyy-dev
// Copyright (c) 2024 Emilia Kond
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
import com.github.fefo.betterjails.api.model.prisoner.Prisoner;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import com.google.common.base.MoreObjects;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisoner;
import io.github.emilyydev.betterjails.config.BetterJailsConfiguration;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.exception.ExceptionHandler;
import org.incendo.cloud.annotations.parser.Parser;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static io.github.emilyydev.betterjails.util.Util.color;
import static io.github.emilyydev.betterjails.util.Util.uuidOrNil;

public final class CommandHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger("BetterJails");

  private static String durationString(Duration duration) {
    final StringBuilder timeLeftBuilder = new StringBuilder();
    duration = appendAndTruncateIfApplicable(duration, ChronoUnit.DAYS, 'd', timeLeftBuilder);
    duration = appendAndTruncateIfApplicable(duration, ChronoUnit.HOURS, 'h', timeLeftBuilder);
    duration = appendAndTruncateIfApplicable(duration, ChronoUnit.MINUTES, 'm', timeLeftBuilder);
    appendAndTruncateIfApplicable(duration, ChronoUnit.SECONDS, 's', timeLeftBuilder);
    return timeLeftBuilder.toString();
  }

  private static Duration appendAndTruncateIfApplicable(
      final Duration timeLeft,
      final ChronoUnit unit,
      final char unitChar,
      final StringBuilder timeLeftBuilder
  ) {
    if (timeLeft.compareTo(unit.getDuration()) <= -1) {
      return timeLeft;
    } else {
      final long part = timeLeft.getSeconds() / unit.getDuration().getSeconds();
      timeLeftBuilder.append(part).append(unitChar);
      return timeLeft.minus(part, unit);
    }
  }

  private final Server server;
  private final BetterJailsPlugin plugin;
  private final BetterJailsConfiguration configuration;

  public CommandHandler(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
    this.server = plugin.getServer();
    this.configuration = plugin.configuration();
  }

  @Permission("betterjails.jail")
  @Command("jail|imprison <target> <jail> <time> [reason]")
  @CommandDescription("Imprisons the target player in the indicated jail for the given amount of time")
  public void imprisonPlayer(
      final CommandContext<CommandSender> ctx,
      final CommandSender sender,
      final OfflinePlayer target,
      final Jail jail,
      final Duration time,
      @Greedy final @Nullable String reason
  ) {
    final String timeInput = ctx.parsingContext("time").consumedInput();
    final String executorName = sender.getName();
    final String prisonerName = MoreObjects.firstNonNull(target.getName(), "(unknown)");

    if (!target.hasPlayedBefore()) {
      throw new CommandError(
          ctx, CommandError.JAIL_FAILED_PLAYER_NEVER_JOINED,
          CommandError.prisonerVariable(prisonerName),
          CommandError.executorVariable(executorName),
          CommandError.jailVariable(jail.name()),
          CommandError.timeVariable(timeInput)
      );
    }

    if (target.isOnline() && target.getPlayer().hasPermission("betterjails.jail.exempt")) {
      throw new CommandError(
          ctx, CommandError.JAIL_FAILED_PLAYER_EXEMPT,
          CommandError.prisonerVariable(prisonerName),
          CommandError.executorVariable(executorName),
          CommandError.jailVariable(jail.name()),
          CommandError.timeVariable(timeInput)
      );
    }

    this.plugin.prisonerData().addJailedPlayer(target, jail, uuidOrNil(sender), executorName, time, reason, true);
    this.server.broadcast(
        this.configuration.messages().jailPlayerSuccess(prisonerName, executorName, jail.name(), timeInput, MoreObjects.firstNonNull(reason, "no reason provided")),
        "betterjails.receivebroadcast"
    );
  }

  @Permission("betterjails.jail")
  @Command("jail info <prisoner>")
  @CommandDescription("Prints information about the imprisoned player")
  public void prisonerInfo(
      final CommandContext<CommandSender> ctx,
      final CommandSender sender,
      final ApiPrisoner prisoner
  ) {
    final String executorName = sender.getName();
    if (prisoner.released()) {
      throw new CommandError(
          ctx, CommandError.INFO_FAILED_PLAYER_NOT_JAILED,
          CommandError.prisonerVariable(prisoner.nameOr("(unknown)")),
          CommandError.executorVariable(executorName)
      );
    }

    final ImmutableLocation lastLocation = prisoner.lastLocationNullable();
    final String lastLocationString = lastLocation == null
        ? color("&cunknown")
        : color(
        "x:%,d y:%,d z%,d &7in &f%s",
        (int) Math.floor(lastLocation.getX()),
        (int) Math.floor(lastLocation.getY()),
        (int) Math.floor(lastLocation.getZ()),
        lastLocation.getWorldName()
    );

    final List<String> infoLines = new ArrayList<>(9);
    infoLines.add(color("&7Info for jailed player:"));
    infoLines.add(color("  &7· Name: &f%s", prisoner.nameOr("&oundefined")));
    infoLines.add(color("  &7· UUID: &f%s", prisoner.uuid()));
    if (prisoner.imprisonmentReason() != null) {
      infoLines.add(color("  &7· Reason: &f") + prisoner.imprisonmentReason());
    }

    infoLines.add(color("  &7· Time left: &f%s", durationString(prisoner.timeLeft())));
    infoLines.add(color("  &7· Jailed in jail: &f%s", prisoner.jail().name()));
    infoLines.add(color("  &7· Jailed by: &f%s", MoreObjects.firstNonNull(prisoner.jailedBy(), "&oundefined")));
    infoLines.add(color("  &7· Location before jailed: &f%s", lastLocationString));
    infoLines.add(color(
        "  &7· Primary group: &f%s",
        MoreObjects.firstNonNull(prisoner.primaryGroup(), this.configuration.permissionHookEnabled() ? "&oundefined" : "&oFeature not enabled")
    ));

    final StringJoiner joiner = new StringJoiner(", ");
    joiner.setEmptyValue(this.configuration.permissionHookEnabled() ? "&oNone" : "&oFeature not enabled");
    prisoner.parentGroups().forEach(joiner::add);
    infoLines.add(color("  &7· All parent groups: &f%s", joiner.toString()));

    sender.sendMessage(String.join("\n", infoLines));
  }

  @Permission("betterjails.jails")
  @Command("jails")
  @CommandDescription("Prints a list of available jails")
  public void printJails(final CommandSender sender) {
    final BetterJailsConfiguration.MessageHolder messages = this.configuration.messages();
    final Map<String, Jail> jails = this.plugin.jailData().getJails();
    final List<String> buffer = new ArrayList<>();

    if (jails.isEmpty()) {
      buffer.add(messages.listJailsNoJails());
    } else {
      buffer.add(messages.listJailsFunnyMessage());
      buffer.addAll(messages.jailListEntryFormatter().formatJailList(jails.values()));
    }

    sender.sendMessage(String.join("\n", buffer));
  }

  @Permission("betterjails.unjail")
  @Command("unjail|release <prisoner>")
  @CommandDescription("Releases an imprisoned player")
  public void releasePrisoner(
      final CommandContext<CommandSender> ctx,
      final CommandSender sender,
      final ApiPrisoner prisoner
  ) {
    final String executorName = sender.getName();
    if (prisoner.released()) {
      throw new CommandError(
          ctx, CommandError.UNJAIL_FAILED_PLAYER_NOT_JAILED,
          CommandError.prisonerVariable(prisoner.nameOr("(unknown)")),
          CommandError.executorVariable(sender.getName())
      );
    }

    this.plugin.prisonerData().releasePrisoner(prisoner, this.server.getOfflinePlayer(prisoner.uuid()), uuidOrNil(sender), executorName, true);
    this.server.broadcast(
        this.configuration.messages().releasePrisonerSuccess(prisoner.nameOr("(unknown)"), executorName),
        "betterjails.receivebroadcast"
    );
  }

  @Permission("betterjails.setjail")
  @Command(value = "setjail <name>", requiredSender = Player.class)
  @CommandDescription("Creates a new jail or relocates an existing jail to where the command is executed")
  public CompletableFuture<Void> createOrRelocateJail(
      final CommandContext<Player> ctx,
      final Player sender,
      final String name
  ) {
    return this.plugin.jailData().addJail(name, ImmutableLocation.copyOf(sender.getLocation())).handleAsync((v, ex) -> {
      if (ex == null) {
        sender.sendMessage(this.configuration.messages().createJailSuccess(sender.getName(), name));
        return null;
      } else {
        LOGGER.error("An error occurred saving data for jail {}", name, ex);
        throw new CommandError(
            ctx, CommandError.SAVE_JAIL_FAILED,
            CommandError.executorVariable(sender.getName()),
            CommandError.jailVariable(name)
        );
      }
    }, this.plugin);
  }

  @Permission("betterjails.modjail")
  @Command(value = "modjail <jail> releaselocation set", requiredSender = Player.class)
  @CommandDescription("Sets the release location of a given jail")
  public CompletableFuture<Void> modifyJailSetReleaseLocation(
      final CommandContext<Player> ctx,
      final Player sender,
      final Jail jail
  ) {
    // TODO(rymiel): neither of these modjail commands run any events. Should they?
    jail.releaseLocation(ImmutableLocation.copyOf(sender.getLocation()));
    return this.plugin.jailData().save().handleAsync((v, ex) -> {
      if (ex == null) {
        sender.sendMessage(this.configuration.messages().modifyJailSuccess(sender.getName(), jail.name()));
        return null;
      } else {
        LOGGER.error("An error occurred setting release location for jail {}", jail.name(), ex);
        throw new CommandError(
            ctx, CommandError.MODIFY_JAIL_FAILED,
            CommandError.executorVariable(sender.getName()),
            CommandError.jailVariable(jail.name())
        );
      }
    }, this.plugin);
  }

  @Permission("betterjails.modjail")
  @Command(value = "modjail <jail> releaselocation clear")
  @CommandDescription("Clears the release location of a given jail")
  public CompletableFuture<Void> modifyJailClearReleaseLocation(
      final CommandContext<CommandSender> ctx,
      final CommandSender sender,
      final Jail jail
  ) {
    jail.releaseLocation(null);
    return this.plugin.jailData().save().handleAsync((v, ex) -> {
      if (ex == null) {
        sender.sendMessage(this.configuration.messages().modifyJailSuccess(sender.getName(), jail.name()));
        return null;
      } else {
        LOGGER.error("An error occurred clearing release location for jail {}", jail.name(), ex);
        throw new CommandError(
            ctx, CommandError.MODIFY_JAIL_FAILED,
            CommandError.executorVariable(sender.getName()),
            CommandError.jailVariable(jail.name())
        );
      }
    }, this.plugin);
  }

  @Permission("betterjails.deljail")
  @Command("deljail <jail>")
  @CommandDescription("Deletes a jail location from the jails list")
  public CompletableFuture<Void> deleteJail(
      final CommandContext<CommandSender> ctx,
      final CommandSender sender,
      final Jail jail
  ) {
    final String name = jail.name();
    return this.plugin.jailData().removeJail(jail).handleAsync((v, ex) -> {
      if (ex == null) {
        sender.sendMessage(this.configuration.messages().deleteJailSuccess(sender.getName(), name));
        return null;
      } else {
        LOGGER.error("An error occurred deleting data for jail {}", name, ex);
        throw new CommandError(
            ctx, CommandError.DELETE_JAIL_FAILED,
            CommandError.executorVariable(sender.getName()),
            CommandError.jailVariable(name)
        );
      }
    }, this.plugin);
  }

  @Permission("betterjails.betterjails")
  @Command("betterjails")
  @CommandDescription("Prints the version of the plugin")
  public void printInfo(final CommandSender sender) {
    sender.sendMessage(color("&bBetterJails &3by &bemilyy-dev &3- v%s", this.plugin.getDescription().getVersion()));
  }

  @Permission("betterjails.betterjails.reload")
  @Command("betterjails reload")
  @CommandDescription("Reloads the configuration file, prisoner data and jail data")
  public void reloadData(final CommandContext<CommandSender> ctx, final CommandSender sender) {
    try {
      this.plugin.reload();
      this.plugin.eventBus().post(PluginReloadEvent.class, sender);
      sender.sendMessage(this.configuration.messages().reloadData(sender.getName()));
    } catch (final IOException | InvalidConfigurationException ex) {
      LOGGER.error("An error occurred reloading plugin data", ex);
      throw new CommandError(ctx, CommandError.RELOAD_FAILED, CommandError.executorVariable(sender.getName()));
    }
  }

  @Permission("betterjails.betterjails.save")
  @Command("betterjails save")
  @CommandDescription("Saves jail and prisoner data on the spot")
  public CompletableFuture<Void> saveData(final CommandContext<CommandSender> ctx, final CommandSender sender) {
    return this.plugin.saveAll().handle((v, ex) -> {
      if (ex == null) {
        sender.sendMessage(this.configuration.messages().saveData(sender.getName()));
        return null;
      } else {
        // exception is logged in BJP#saveAll()
        throw new CommandError(ctx, CommandError.SAVE_ALL_FAILED, CommandError.executorVariable(sender.getName()));
      }
    });
  }

  @Parser(suggestions = "jail")
  public Jail resolveJail(final CommandContext<CommandSender> ctx, final CommandInput input) {
    final String name = input.readString();
    final Jail jail = this.plugin.jailData().getJail(name);
    if (jail != null) {
      return jail;
    } else {
      throw new CommandError(ctx, CommandError.RESOLVE_JAIL_FAILED, CommandError.jailVariable(name));
    }
  }

  @Suggestions("jail")
  public Stream<String> suggestJails(final String input) {
    return this.plugin.jailData().getJails().keySet().stream().filter(name -> name.startsWith(input));
  }

  @Parser(suggestions = "prisoner")
  public ApiPrisoner resolvePrisoner(final CommandContext<CommandSender> ctx, final CommandInput input) {
    final String name = input.readString();
    final ApiPrisoner prisoner = this.plugin.prisonerData().getPrisoner(this.plugin.findUniqueId(name));
    if (prisoner != null) {
      return prisoner;
    } else {
      throw new CommandError(ctx, CommandError.RESOLVE_PRISONER_FAILED, CommandError.prisonerVariable(name));
    }
  }

  @Suggestions("prisoner")
  public Stream<String> suggestPrisoners(final String input) {
    return this.plugin.prisonerData().getAllPrisoners().stream()
        .map(Prisoner::name)
        .filter(Objects::nonNull)
        .filter(name -> name.startsWith(input));
  }

  @ExceptionHandler(CommandError.class)
  public void handleCommandError(final CommandSender sender, final CommandError error) {
    sender.sendMessage(error.getMessage());
  }
}
