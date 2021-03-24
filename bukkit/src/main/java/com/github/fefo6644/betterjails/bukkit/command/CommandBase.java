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

import com.github.fefo6644.betterjails.common.message.Subject;
import com.github.fefo6644.betterjails.common.plugin.abstraction.PlatformAdapter;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.tree.CommandNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class CommandBase extends Command {

  private final TabExecutor executor;
  private final Predicate<Subject> requirement;
  private final PlatformAdapter<CommandSender, ?, ?, ?> platformAdapter;

  public CommandBase(final String name, final TabExecutor executor,
                     final PlatformAdapter<CommandSender, ?, ?, ?> platformAdapter,
                     final CommandNode<Subject> node) {
    super(name);
    this.executor = executor;
    this.requirement = node.getRequirement();
    this.platformAdapter = platformAdapter;
  }

  @Override
  public boolean testPermission(final @NotNull CommandSender target) {
    return this.requirement.test(this.platformAdapter.adaptSubject(target));
  }

  @Override
  public boolean testPermissionSilent(final @NotNull CommandSender target) {
    return this.requirement.test(this.platformAdapter.adaptSubject(target));
  }

  @Override
  public boolean execute(final @NotNull CommandSender sender,
                         final @NotNull String alias,
                         final @NotNull String[] args) {
    return this.executor.onCommand(sender, this, alias, args);
  }

  @Override
  public @NotNull List<String> tabComplete(final @NotNull CommandSender sender,
                                           final @NotNull String alias,
                                           final @NotNull String[] args) throws IllegalArgumentException {
    final List<String> suggestions = this.executor.onTabComplete(sender, this, alias, args);
    return suggestions == null ? ImmutableList.of() : suggestions;
  }
}
