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

package io.github.emilyydev.betterjails.commands;

import com.google.common.collect.ImmutableList;
import io.github.emilyydev.betterjails.BetterJailsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class CommandTabCompleter implements TabCompleter {

  private final BetterJailsPlugin plugin;

  public CommandTabCompleter(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public List<String> onTabComplete(
      final @NotNull CommandSender sender,
      final @NotNull Command cmd,
      final @NotNull String alias,
      final @NotNull String @NotNull [] args
  ) {
    final ImmutableList.Builder<String> suggestionsBuilder = ImmutableList.builder();

    switch (cmd.getName()) {
      case "jail": {
        switch (args.length) {
          case 1:
            if ("info".startsWith(args[0].toLowerCase(Locale.ROOT))) {
              suggestionsBuilder.add("info");
            }
            for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
              if (player.getName().toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))) {
                suggestionsBuilder.add(player.getName());
              }
            }
            break;

          case 2:
            if (!args[0].equalsIgnoreCase("info")) {
              for (final String jailName : this.plugin.dataHandler().getJails().keySet()) {
                if (jailName.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))) {
                  suggestionsBuilder.add(jailName);
                }
              }
            } else {
              for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))) {
                  suggestionsBuilder.add(player.getName());
                }
              }
            }
            break;

          case 3:
            if (!args[0].equals("info") && args[2].length() < 2) {
              suggestionsBuilder.add("24h", "12h", "6h", "3h", "30m");
            }
            break;
        }
        break;
      }

      case "unjail": {
        if (args.length == 1) {
          for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (player.getName().toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))) {
              suggestionsBuilder.add(player.getName());
            }
          }
        }
        break;
      }

      case "setjail": {
        if (args.length == 1) {
          suggestionsBuilder.add("<jailname>");
        }
        break;
      }

      case "deljail": {
        if (args.length == 1) {
          for (final String jailName : this.plugin.dataHandler().getJails().keySet()) {
            if (jailName.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))) {
              suggestionsBuilder.add(jailName);
            }
          }
        }
        break;
      }

      case "betterjails": {
        if (args.length == 1) {
          final boolean reloadPerms = sender.hasPermission("betterjails.betterjails.reload");
          final boolean savePerms = sender.hasPermission("betterjails.betterjails.save");

          if (!reloadPerms && !savePerms) {
            break;
          }

          if (reloadPerms && "reload".startsWith(args[0].toLowerCase(Locale.ROOT))) {
            suggestionsBuilder.add("reload");
          }

          if (savePerms && "save".startsWith(args[0].toLowerCase(Locale.ROOT))) {
            suggestionsBuilder.add("save");
          }

        }
        break;
      }
    }

    return suggestionsBuilder.build();
  }
}
