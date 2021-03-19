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
import com.github.fefo6644.betterjails.common.configuration.ConfigurationAdapter;
import com.github.fefo6644.betterjails.common.message.MessagingSubject;
import com.github.fefo6644.betterjails.common.platform.BetterJailsPlugin;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class BetterJailsCommand implements CommandSegment.Literal<MessagingSubject> {

  private final ConfigurationAdapter configurationAdapter;
  private final LiteralCommandNode<MessagingSubject> commandNode;

  {
    final LiteralArgumentBuilder<MessagingSubject> builder = literal("betterjails");
    builder
        .then(literal("reloadconfig").executes(this::reloadConfig))
        .then(literal("reloaddata").executes(this::reloadData));

    this.commandNode = builder.build();
  }

  public BetterJailsCommand(final BetterJailsPlugin plugin) {
    this.configurationAdapter = plugin.getConfigurationAdapter();
  }

  @Override
  public @NotNull LiteralCommandNode<MessagingSubject> getCommandNode() {
    return this.commandNode;
  }

  private int reloadConfig(final CommandContext<MessagingSubject> context) {
    final MessagingSubject subject = context.getSource();

    try {
      this.configurationAdapter.reload();
    } catch (final IOException exception) {
      // messages blah blah
      exception.printStackTrace();
    }

    return 1;
  }

  private int reloadData(final CommandContext<MessagingSubject> context) {

    return 1;
  }
}
