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

import com.google.common.collect.ImmutableList;
import io.github.emilyydev.betterjails.util.Util;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SubCommandsConfiguration extends AbstractConfiguration {

  private static final String ON_JAIL = "on-jail";
  private static final String ON_RELEASE = "on-release";

  public SubCommandsConfiguration(final Path dir) {
    super(dir, "subcommands.yml", HashMap::new);
  }

  public SubCommands onJail() {
    return setting(ON_JAIL, key -> new SubCommands(config().getConfigurationSection(key)));
  }

  public SubCommands onRelease() {
    return setting(ON_RELEASE, key -> new SubCommands(config().getConfigurationSection(key)));
  }

  public static final class SubCommands {

    private static final String AS_PRISONER = "as-prisoner";
    private static final String AS_CONSOLE = "as-console";

    private static final Pattern PLACEHOLDERS = Pattern.compile("\\{prisoner}|\\{player}");

    private static Function<? super MatchResult, ? extends String> replacer(
        final String prisoner,
        final String executioner
    ) {
      return matchResult -> {
        final String matchedGroup = matchResult.group();
        switch (Util.removeBracesFromMatchedPlaceholderPleaseAndThankYou(matchedGroup)) {
          case "prisoner": return prisoner;
          case "player": return executioner;
          default: return matchedGroup;
        }
      };
    }

    private final Collection<String> asPrisoner;
    private final Collection<String> asConsole;

    private SubCommands(final ConfigurationSection section) {
      final List<String> asPrisoner = section.getStringList(AS_PRISONER);
      asPrisoner.removeIf(String::isEmpty);
      this.asPrisoner = ImmutableList.copyOf(asPrisoner);

      final List<String> asConsole = section.getStringList(AS_CONSOLE);
      asConsole.removeIf(String::isEmpty);
      this.asConsole = ImmutableList.copyOf(asConsole);
    }

    public void executeAsPrisoner(final Server server, final CommandSender prisoner, final String executioner) {
      final String prisonerName = prisoner.getName();
      this.asPrisoner.stream()
          .map(s -> replacePlaceholders(s, prisonerName, executioner))
          .forEach(s -> server.dispatchCommand(prisoner, s));
    }

    public void executeAsConsole(final Server server, final CommandSender prisoner, final String executioner) {
      final String prisonerName = prisoner.getName();
      final CommandSender consoleSender = server.getConsoleSender();
      this.asConsole.stream()
          .map(s -> replacePlaceholders(s, prisonerName, executioner))
          .forEach(s -> server.dispatchCommand(consoleSender, s));
    }

    private String replacePlaceholders(final String command, final String prisoner, final String executioner) {
      final Function<? super MatchResult, ? extends String> replacer = replacer(prisoner, executioner);
      final Matcher matcher = PLACEHOLDERS.matcher(command);
      final StringBuffer buffer = new StringBuffer();
      while (matcher.find()) {
        matcher.appendReplacement(buffer, replacer.apply(matcher.toMatchResult()));
      }

      return matcher.appendTail(buffer).toString();
    }
  }
}
