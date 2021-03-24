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

package com.github.fefo6644.betterjails.common.command.command;

import com.github.fefo6644.betterjails.common.command.segment.CommandSegment;
import com.github.fefo6644.betterjails.common.message.Message;
import com.github.fefo6644.betterjails.common.message.Subject;
import com.github.fefo6644.betterjails.common.plugin.BetterJailsPlugin;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class BetterJailsCommand implements CommandSegment.Literal<Subject> {

  private final BetterJailsPlugin plugin;
  private final LiteralCommandNode<Subject> commandNode;

  {
    final LiteralArgumentBuilder<Subject> builder = literal("betterjails");
    builder
        .requires(subject -> subject.hasPermission("betterjails.betterjails"))
        .executes(this::pluginInfo)
        .then(literal("reloadconfig")
                  .requires(subject -> subject.hasPermission("betterjails.reloadconfig"))
                  .executes(this::reloadConfig))
        .then(literal("reloaddata")
                  .requires(subject -> subject.hasPermission("betterjails.reloaddata"))
                  .executes(this::reloadData))
        .then(literal("save")
                  .requires(subject -> subject.hasPermission("betterjails.save"))
                  .executes(this::save))
        .then(literal("playerinfo")
                  .requires(subject -> subject.hasPermission("betterjails.playerinfo"))
                  .then(argument("player", StringArgumentType.word()).executes(this::playerInfo)));

    this.commandNode = builder.build();
  }

  public BetterJailsCommand(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public @NotNull LiteralCommandNode<Subject> getCommandNode() {
    return this.commandNode;
  }

  private int pluginInfo(final CommandContext<Subject> context) {
    Message.PLUGIN_INFO.send(context.getSource(), this.plugin);
    return 1;
  }

  private int reloadConfig(final CommandContext<Subject> context) {
    final Subject subject = context.getSource();

    try {
      this.plugin.getConfigurationAdapter().reload();
      Message.CONFIG_RELOADED.send(subject);
      Message.CONFIG_RELOAD_NOTICE.send(subject);
    } catch (final IOException exception) {
      exception.printStackTrace();
      Message.GENERIC_ERROR.send(subject, "reloadconfig");
      Message.CHECK_CONSOLE.send(subject);
    }

    return 1;
  }

  private int reloadData(final CommandContext<Subject> context) {
    final Subject subject = context.getSource();

    try {
      // get managers & reload
      if (false) {
        throw new IOException();
      }
    } catch (final IOException exception) {
      exception.printStackTrace();
      Message.GENERIC_ERROR.send(subject, "reloaddata");
      Message.CHECK_CONSOLE.send(subject);
    }

    return 1;
  }

  private int save(final CommandContext<Subject> context) {
    final Subject subject = context.getSource();

    try {
      // get managers & save
      if (false) {
        throw new IOException();
      }
    } catch (final IOException exception) {
      exception.printStackTrace();
      Message.GENERIC_ERROR.send(subject, "save");
      Message.CHECK_CONSOLE.send(subject);
    }

    return 1;
  }

  private int playerInfo(final CommandContext<Subject> context) {

    return 1;
  }
}
