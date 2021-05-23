//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) emilyy-dev
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

package io.github.emilyydev.betterjails.bukkit.command;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import io.github.emilyydev.betterjails.bukkit.BetterJailsBukkit;
import io.github.emilyydev.betterjails.bukkit.reflection.ReflectionHelper;
import io.github.emilyydev.betterjails.common.command.CommandBridge;
import io.github.emilyydev.betterjails.common.message.Subject;
import io.github.emilyydev.betterjails.common.plugin.abstraction.PlatformAdapter;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler implements TabExecutor, Listener {

  private final List<Pattern> commandPatterns;
  private final PlatformAdapter<CommandSender, ?, ?, ?> platformAdapter;
  private final CommandBridge commandBridge;

  private final Map<UUID, Suggestions> suggestionMap = new HashMap<>();
  private final BiFunction<Subject, String, List<String>> suggestionsRecorder;

  public CommandHandler(final BetterJailsBukkit bukkitPlugin) {
    this.platformAdapter = bukkitPlugin.getPlatformAdapter();
    this.commandBridge = bukkitPlugin.getPlugin().getCommandBridge();

    final String prefix = bukkitPlugin.getName().toLowerCase(Locale.ROOT);
    final CommandMap commandMap = ReflectionHelper.getCommandMap();
    final ImmutableList.Builder<Pattern> builder = ImmutableList.builder();
    for (final CommandNode<Subject> node : this.commandBridge.getCommandNode().getChildren()) {
      builder.add(Pattern.compile("^(?:" + prefix + ":)?(" + node.getName() + ") "));

      final BukkitCommand bukkitCommand = new BukkitCommand(node.getName(), this, this.platformAdapter, node, bukkitPlugin);
      commandMap.register(prefix, bukkitCommand);
    }
    this.commandPatterns = builder.build();


    try {
      Class.forName("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent");
      bukkitPlugin.registerListener(AsyncTabCompleteEvent.class, this::asyncTabComplete);
    } catch (final ClassNotFoundException exception) {
      // ignore; we just won't calculate suggestion completions async
    }


    BiFunction<Subject, String, List<String>> suggestionsRecorder;
    try {
      throw new ClassNotFoundException();
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

    String commandName = null;
    for (final Pattern pattern : this.commandPatterns) {
      final Matcher matcher = pattern.matcher(buffer);
      if (matcher.find()) {
        commandName = matcher.group(1);
        break;
      }
    }

    if (commandName == null) {
      return;
    }

    // get rid of typed alias, use registered command name
    // this is temporary until I figure out a way to support command aliases
    // https://github.com/Mojang/brigadier/issues/46
    // if that ever gets fixed I'll just probably shade brig in
    // (something I'll have to do anyway for pre-1.13/legacy builds)
    // no aliases 'till then
    // (or I'll just register multiple literal nodes with the same command & children nodes)
    buffer = commandName + buffer.substring(buffer.indexOf(' '));
    event.setHandled(true);
    event.setCompletions(this.suggestionsRecorder.apply(this.platformAdapter.adaptSubject(event.getSender()), buffer));
  }
}
