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

package com.github.fefo6644.betterjails.common.message;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;

public enum Message {
  // &7[&6&nB&e&nJ&7]
  SHORT_PREFIX(text().color(GRAY)
                     .append(text('['))
                     .append(text().decorate(BOLD)
                                   .append(text('B', GOLD))
                                   .append(text('J', YELLOW)))
                     .append(text(']'))),

  // &7=================================================
  //
  //
  //   &6_
  //  &6|_)   _   _|_  _|_   _   ._&e    |   _.  o  |   _
  //  &6|_)  (/_   |_   |_  (/_  | &e  \_|  (_|  |  |  _>
  //
  //
  // &7=================================================
  STARTUP_BANNER(text().color(GRAY)
                       .append(text("================================================="))
                       .append(newline()).append(newline())
                       .append(text().append(space()).append(space())
                                     .append(text('_', GOLD)))
                       .append(newline())
                       .append(text().append(space())
                                     .append(text("|_)   _   _|_  _|_   _   ._", GOLD))
                                     .append(text("    |   _.  o  |   _", YELLOW)))
                       .append(newline())
                       .append(text().append(space())
                                     .append(text().append(text("|_)  (/_   |_   |_  (/_  | ", GOLD))
                                                   .append(text("  \\_|  (_|  |  |  _>", YELLOW))))
                       .append(newline()).append(newline())
                       .append(text("================================================="))),

  // &7[&6Better&eJails&7]
  LONG_PREFIX(text().color(GRAY)
                    .append(text('['))
                    .append(text("Better", GOLD))
                    .append(text("Jails", YELLOW))
                    .append(text(']'))),

  // &6BetterJails&e by &6Fefo6644&e - &6v{0}
  VERSION(text().color(GOLD)
                .append(text("BetterJails"))
                .append(text(" by ", YELLOW))
                .append(text("Fefo6644"))
                .append(hyphen())
                .append(text("v{0}"))),

  USAGE_TITLE(prefixed(text("Usages:", RED))),

  USAGE_COMMAND(text().append(text("/{0}", RED))
                      .hoverEvent(text().append(text("Click to run ", WHITE))
                                        .append(text("/{0}", GRAY))
                                        .build().asHoverEvent())
                      .clickEvent(suggestCommand("/{0}"))),

  PLAYERS_ONLY(prefixed(text("Only players can run this command!", RED))),

  CONFIG_RELOADED(prefixed(text("Configuration reloaded successfully!", YELLOW))),

  CONFIG_RELOAD_NOTICE(prefixed(text().color(GOLD)
                                      .decorate(ITALIC, UNDERLINED)
                                      .append(text("Please note that not all configuration settings are reloadable!"))));

  private static final Pattern INDEX_PATTERN = Pattern.compile("\\{(-1|\\d+)}");

  private static TextComponent.Builder prefixed(final ComponentLike component) {
    return text().append(SHORT_PREFIX.component)
                 .append(space())
                 .append(component);
  }

  private static TextComponent hyphen() {
    return text(" - ", GRAY);
  }

  private final Component component;

  Message(final ComponentLike fromBuilder) {
    this.component = fromBuilder.asComponent();
  }

  public void send(@NonNull final MessagingSubject subject, @NonNull final String... replacements) {
    Component replaced = component.replaceText(INDEX_PATTERN, builder -> {
      final Matcher matcher = INDEX_PATTERN.matcher(builder.content());
      matcher.find();
      final int index = Integer.parseInt(matcher.group(1));
      return index == -1 ? text(String.valueOf(subject.name())) : text(replacements[index]);
    });

    final ClickEvent click = replaced.clickEvent();
    if (click != null) {

    }

    subject.sendMessage(Identity.nil(), replaced);
  }
}
