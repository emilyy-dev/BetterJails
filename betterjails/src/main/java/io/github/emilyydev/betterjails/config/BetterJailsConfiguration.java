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

package io.github.emilyydev.betterjails.config;

import com.github.fefo.betterjails.api.model.jail.Jail;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.emilyydev.betterjails.util.Util;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;

public final class BetterJailsConfiguration extends AbstractConfiguration {

  private static final String BACKUP_LOCATION = "backupLocation";
  private static final String OFFLINE_TIME = "offlineTime";
  private static final String CHANGE_GROUP = "changeGroup";
  private static final String PRISONER_GROUP = "prisonerGroup";
  private static final String AUTO_SAVE_TIME_IN_MINUTES = "autoSaveTimeInMinutes";
  private static final String MESSAGES = "messages";

  public BetterJailsConfiguration(final Path dir) {
    super(dir, "config.yml", HashMap::new);
  }

  public @Deprecated ImmutableLocation backupLocation() {
    return setting(
        BACKUP_LOCATION,
        key -> ImmutableLocation.deserialize(config().getConfigurationSection(key).getValues(false))
    );
  }

  public boolean considerOfflineTime() {
    return setting(OFFLINE_TIME, config()::getBoolean);
  }

  public boolean permissionHookEnabled() {
    return setting(CHANGE_GROUP, config()::getBoolean);
  }

  public Optional<String> prisonerPermissionGroup() {
    return setting(
        PRISONER_GROUP,
        key -> permissionHookEnabled() ?
            Optional.ofNullable(config().getString(key)) :
            Optional.empty()
    );
  }

  public Duration autoSavePeriod() {
    return setting(AUTO_SAVE_TIME_IN_MINUTES, key -> Duration.ofMinutes(config().getLong(key)));
  }

  public MessageHolder messages() {
    return setting(MESSAGES, key -> new MessageHolder(config().getConfigurationSection(key).getValues(false)));
  }

  public interface JailListFormatter {

    JailListFormatter LIST = jailList ->
        jailList.stream()
            .map(Jail::name)
            .map("&7· "::concat)
            .map(Util::color)
            .collect(Util.toImmutableList());

    JailListFormatter LINE = jailList ->
        jailList.stream()
            .map(Jail::name)
            .collect(collectingAndThen(
                joining(", ", "&7", "."),
                (s) -> ImmutableList.of(Util.color(s))
            ));

    Collection<String> formatJailList(Collection<Jail> jailList);
  }

  public static final class MessageHolder {

    private static final String JAIL_SUCCESS = "jailSuccess";
    private static final String JAIL_FAILED_PLAYER_NEVER_JOINED = "jailFailedPlayerNeverJoined";
    private static final String JAIL_FAILED_PLAYER_EXEMPT = "jailFailedPlayerExempt";
    private static final String JAIL_FAILED_JAIL_NOT_FOUND = "jailFailedJailNotFound";
    private static final String JAIL_FAILED_TIME_INCORRECT = "jailFailedTimeIncorrect";
    private static final String INFO_FAILED_PLAYER_NOT_JAILED = "infoFailedPlayerNotJailed";
    private static final String INFO_FAILED_PLAYER_NEVER_JOINED = "infoFailedPlayerNeverJoined";
    private static final String UNJAIL_SUCCESS = "unjailSuccess";
    private static final String UNJAIL_FAILED_PLAYER_NOT_JAILED = "unjailFailedPlayerNotJailed";
    private static final String UNJAIL_FAILED_PLAYER_NEVER_JOINED = "unjailFailedPlayerNeverJoined";
    private static final String SETJAIL_FROM_CONSOLE = "setjailFromConsole";
    private static final String SETJAIL_SUCCESS = "setjailSuccess";
    private static final String DELJAIL_SUCCESS = "deljailSuccess";
    private static final String DELJAIL_FAILED = "deljailFailed";
    private static final String RELOAD = "reload";
    private static final String SAVE = "save";
    private static final String LIST_NO_JAILS = "listNoJails";
    private static final String LIST_JAILS_PREMESSAGE = "listJailsPremessage";
    private static final String JAILS_FORMAT = "jailsFormat";

    private static final Pattern PLACEHOLDERS = Pattern.compile("\\{prisoner}|\\{player}|\\{jail}|\\{time}");

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Function<? super MatchResult, ? extends String> replacer(
        final Optional<String> prisoner,
        final Optional<String> executioner,
        final Optional<String> jail,
        final Optional<String> duration
    ) {
      return matchResult -> {
        final String matchedGroup = matchResult.group();
        switch (Util.removeBracesFromMatchedPlaceholderPleaseAndThankYou(matchedGroup)) {
          case "prisoner": return prisoner.orElse(matchedGroup);
          case "player": return executioner.orElse(matchedGroup);
          case "jail": return jail.orElse(matchedGroup);
          case "time": return duration.orElse(matchedGroup);
          default: return matchedGroup;
        }
      };
    }

    private final Map<String, String> messageMap;

    private MessageHolder(final Map<? extends String, ?> messageMap) {
      final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
      messageMap.forEach((key, value) -> builder.put(key, String.valueOf(value)));
      this.messageMap = builder.build();
    }

    public String jailPlayerSuccess(
        final String prisoner,
        final String executioner,
        final String jail,
        final String duration
    ) {
      return formatMessage(JAIL_SUCCESS, prisoner, executioner, jail, duration);
    }

    public String jailPlayerFailedNeverJoined(
        final String prisoner,
        final String executioner,
        final String jail,
        final String duration
    ) {
      return formatMessage(JAIL_FAILED_PLAYER_NEVER_JOINED, prisoner, executioner, jail, duration);
    }

    public String jailPlayerFailedExempt(
        final String prisoner,
        final String executioner,
        final String jail,
        final String duration
    ) {
      return formatMessage(JAIL_FAILED_PLAYER_EXEMPT, prisoner, executioner, jail, duration);
    }

    public String jailPlayerFailedJailNotFound(
        final String prisoner,
        final String executioner,
        final String jail,
        final String duration
    ) {
      return formatMessage(JAIL_FAILED_JAIL_NOT_FOUND, prisoner, executioner, jail, duration);
    }

    public String jailPlayerFailedInvalidTimeInput(
        final String prisoner,
        final String executioner,
        final String jail,
        final String duration
    ) {
      return formatMessage(JAIL_FAILED_TIME_INCORRECT, prisoner, executioner, jail, duration);
    }

    public String prisonerInfoFailedNotJailed(final String prisoner, final String executioner) {
      return formatMessage(INFO_FAILED_PLAYER_NOT_JAILED, prisoner, executioner, null, null);
    }

    public String prisonerInfoFailedNeverJoined(final String prisoner, final String executioner) {
      return formatMessage(INFO_FAILED_PLAYER_NEVER_JOINED, prisoner, executioner, null, null);
    }

    public String releasePrisonerSuccess(final String prisoner, final String executioner) {
      return formatMessage(UNJAIL_SUCCESS, prisoner, executioner, null, null);
    }

    public String releasePrisonerFailedNotJailed(final String prisoner, final String executioner) {
      return formatMessage(UNJAIL_FAILED_PLAYER_NOT_JAILED, prisoner, executioner, null, null);
    }

    public String releasePrisonerFailedNeverJoined(final String prisoner, final String executioner) {
      return formatMessage(UNJAIL_FAILED_PLAYER_NEVER_JOINED, prisoner, executioner, null, null);
    }

    public String createJailSuccess(final String executioner, final String jail) {
      return formatMessage(SETJAIL_SUCCESS, null, executioner, jail, null);
    }

    public String createJailFromConsole(final String executioner, final String jail) {
      return formatMessage(SETJAIL_FROM_CONSOLE, null, executioner, jail, null);
    }

    public String deleteJailSuccess(final String executioner, final String jail) {
      return formatMessage(DELJAIL_SUCCESS, null, executioner, jail, null);
    }

    public String deleteJailFailed(final String executioner, final String jail) {
      return formatMessage(DELJAIL_FAILED, null, executioner, jail, null);
    }

    public String reloadData(final String executioner) {
      return formatMessage(RELOAD, null, executioner, null, null);
    }

    public String saveData(final String executioner) {
      return formatMessage(SAVE, null, executioner, null, null);
    }

    public String listJailsNoJails() {
      return formatMessage(LIST_NO_JAILS, null, null, null, null);
    }

    public String listJailsFunnyMessage() {
      return formatMessage(LIST_JAILS_PREMESSAGE, null, null, null, null);
    }

    public JailListFormatter jailListEntryFormatter() {
      switch (this.messageMap.get(JAILS_FORMAT)) {
        case "list":
          return JailListFormatter.LIST;

        case "line":
        default:
          return JailListFormatter.LINE;
      }
    }

    private String formatMessage(
        final String key,
        final @Nullable String prisoner,
        final @Nullable String executioner,
        final @Nullable String jail,
        final @Nullable String duration
    ) {
      final Matcher matcher = PLACEHOLDERS.matcher(this.messageMap.get(key));
      final Function<? super MatchResult, ? extends String> replacer = replacer(
          Optional.ofNullable(prisoner),
          Optional.ofNullable(executioner),
          Optional.ofNullable(jail),
          Optional.ofNullable(duration)
      );

      final StringBuffer buffer = new StringBuffer();
      while (matcher.find()) {
        matcher.appendReplacement(buffer, replacer.apply(matcher.toMatchResult()));
      }

      return Util.color(matcher.appendTail(buffer).toString());
    }
  }
}
