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

package com.github.fefo6644.betterjails.common.command;

import com.github.fefo6644.betterjails.common.command.brigadier.ComponentMessage;
import com.github.fefo6644.betterjails.common.command.command.BetterJailsCommand;
import com.github.fefo6644.betterjails.common.command.segment.CommandSegment;
import com.github.fefo6644.betterjails.common.message.Message;
import com.github.fefo6644.betterjails.common.message.Subject;
import com.github.fefo6644.betterjails.common.plugin.BetterJailsPlugin;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CommandBridge implements CommandSegment.Root<Subject> {

  private final CommandDispatcher<Subject> dispatcher = new CommandDispatcher<>();
  private final RootCommandNode<Subject> rootCommandNode = this.dispatcher.getRoot();
  private final ExecutorService commandExecutor =
      Executors.newSingleThreadExecutor(
          new ThreadFactoryBuilder()
              .setNameFormat("betterjails-command-executor")
              .setDaemon(false)
              .build());

  public CommandBridge(final BetterJailsPlugin plugin) {
    ImmutableList.of(new BetterJailsCommand(plugin))
                 .forEach(command -> this.rootCommandNode.addChild(command.getCommandNode()));
  }

  @Override
  public @NotNull RootCommandNode<Subject> getCommandNode() {
    return this.rootCommandNode;
  }

  public void execute(final Subject subject, final String input) {
    this.commandExecutor.execute(() -> {
      final ParseResults<Subject> results = this.dispatcher.parse(input.trim(), subject);

      final Map<CommandNode<Subject>, CommandSyntaxException> exceptions = results.getExceptions();
      if (!exceptions.isEmpty()) {
        exceptions.values().forEach(exception -> Message.COMMAND_ERROR.send(subject, exception));
        return;
      }

      try {
        this.dispatcher.execute(results);
      } catch (final CommandSyntaxException exception) {
        final com.mojang.brigadier.Message message = exception.getRawMessage();
        if (message instanceof ComponentMessage) {
          subject.sendMessage(((ComponentMessage) message).component());
        } else {
          subject.sendMessage(Component.text(exception.getMessage()));
        }
      }
    });
  }

  public CompletableFuture<Suggestions> suggestionsFuture(final Subject subject, final String input) {
    final ParseResults<Subject> results = this.dispatcher.parse(input, subject);
    return this.dispatcher.getCompletionSuggestions(results);
  }

  public List<String> getCompletionSuggestions(final Subject subject, final String input) {
    return suggestionsFuture(subject, input).join().getList().stream()
                                            .map(Suggestion::getText)
                                            .collect(Collectors.toList());
  }
}
