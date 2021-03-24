//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) Fefo6644 <federico.lopez.1999@outlook.com>
// Copyright (c) contributors
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

package com.github.fefo6644.betterjails.bukkit.command;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.github.fefo6644.betterjails.bukkit.BetterJailsBukkit;
import com.github.fefo6644.betterjails.bukkit.reflection.ReflectionHelper;
import com.github.fefo6644.betterjails.common.command.CommandBridge;
import com.github.fefo6644.betterjails.common.message.Subject;
import com.github.fefo6644.betterjails.common.plugin.abstraction.PlatformAdapter;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandHandler implements TabExecutor, Listener {

  private final List<Predicate<String>> commandPatterns;
  private final PlatformAdapter<CommandSender, ?, ?, ?> platformAdapter;
  private final CommandBridge commandBridge;

  private final Map<UUID, Suggestions> suggestionMap = new HashMap<>();
  private final BiFunction<Subject, String, List<String>> suggestionsRecorder;

  public CommandHandler(final BetterJailsBukkit bukkitPlugin) {
    this.platformAdapter = bukkitPlugin.getPlatformAdapter();
    this.commandBridge = bukkitPlugin.getPlugin().getCommandBridge();

    final String prefix = bukkitPlugin.getName().toLowerCase(Locale.ROOT);
    final CommandMap commandMap = ReflectionHelper.getCommandMap();
    final ImmutableList.Builder<Predicate<String>> builder = ImmutableList.builder();
    for (final CommandNode<Subject> node : this.commandBridge.getCommandNode().getChildren()) {
      builder.add(
          Pattern.compile("^/?(?:" + prefix + ":)?(?:" + node.getName() + ") ")
                 .asPredicate());

      final CommandBase commandBase = new CommandBase(node.getName(), this, this.platformAdapter, node);
      commandMap.register(prefix, commandBase);
    }
    this.commandPatterns = builder.build();


    try {
      Class.forName("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent");
      bukkitPlugin.registerListener(AsyncTabCompleteEvent.class, this, this::asyncTabComplete, true);
    } catch (final ClassNotFoundException exception) {
      // ignore; we just won't calculate suggestion completions async
    }


    BiFunction<Subject, String, List<String>> suggestionsRecorder;
    try {
      Class.forName("com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent");
      bukkitPlugin.registerListener(AsyncPlayerSendSuggestionsEvent.class, this, this::asyncSendSuggestions, true);

      suggestionsRecorder = (subject, input) -> {
        final Suggestions suggestions = this.commandBridge.completionSuggestions(subject, input).join();
        this.suggestionMap.put(subject.asPlayerSubject().uuid(), suggestions);
        return suggestions.getList().stream().map(Suggestion::getText).collect(Collectors.toList());
      };
    } catch (final ClassNotFoundException exception) {
      suggestionsRecorder = this.commandBridge::getCompletionSuggestions;
    }
    this.suggestionsRecorder = suggestionsRecorder;
  }

  @Override
  public boolean onCommand(final @NotNull CommandSender sender,
                           final @NotNull Command command,
                           final @NotNull String alias,
                           final @NotNull String[] args) {
    final String cmd = command.getName() + ' ' + String.join(" ", args);
    this.commandBridge.execute(this.platformAdapter.adaptSubject(sender), cmd);
    return true;
  }

  @Override
  public List<String> onTabComplete(final @NotNull CommandSender sender,
                                    final @NotNull Command command,
                                    final @NotNull String alias,
                                    final @NotNull String[] args) {
    final String cmd = command.getName() + ' ' + String.join(" ", args);
    return this.suggestionsRecorder.apply(this.platformAdapter.adaptSubject(sender), cmd);
  }

  private void asyncTabComplete(final AsyncTabCompleteEvent event) {
    if (!event.isCommand() || event.isHandled()) {
      return;
    }

    String buffer = event.getBuffer();
    if (buffer.charAt(0) == '/') {
      buffer = buffer.substring(1);
    }

    boolean matched = false;
    for (final Predicate<String> predicate : this.commandPatterns) {
      if (predicate.test(buffer)) {
        matched = true;
        break;
      }
    }

    if (!matched) {
      return;
    }

    buffer = buffer.substring(buffer.indexOf(':') + 1); // get rid of fallback prefix if any
    event.setHandled(true);
    event.setCompletions(this.suggestionsRecorder.apply(this.platformAdapter.adaptSubject(event.getSender()), buffer));
  }

  private void asyncSendSuggestions(final AsyncPlayerSendSuggestionsEvent event) {
    // TODO: intercept and put tooltips in
    // keep the map clean for now
    this.suggestionMap.remove(event.getPlayer().getUniqueId());
  }
}
