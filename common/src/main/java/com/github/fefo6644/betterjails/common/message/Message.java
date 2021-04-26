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

import com.github.fefo6644.betterjails.common.command.brigadier.ComponentMessage;
import com.github.fefo6644.betterjails.common.plugin.BetterJailsPlugin;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;

import java.util.List;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.toComponent;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;

public interface Message {

  Component FULL_STOP = text('.');

  // &7[&6&nB&e&nJ&7]
  Component SHORT_PREFIX =
      text()
          .color(GRAY)
          .append(text('['),
                  text()
                      .decorate(BOLD)
                      .append(text('B', GOLD),
                              text('J', YELLOW)),
                  text(']'))
          .build();

  // &7[&6Better&eJails&7]
  Component LONG_PREFIX =
      text()
          .color(GRAY)
          .append(text('['),
                  text("Better", GOLD),
                  text("Jails", YELLOW),
                  text(']'))
          .build();

  // &7=================================================
  //
  //
  //   &6_
  //  &6|_)   _   _|_  _|_   _   ._&e    |   _.  o  |   _
  //  &6|_)  (/_   |_   |_  (/_  | &e  \_|  (_|  |  |  _>
  //
  //
  // &7=================================================

  // trust me, it does show that :^)
  Component STARTUP_BANNER =
      text()
          .color(GRAY)
          .append(text("================================================="),
                  newline(),
                  newline(),
                  text("  _", GOLD),
                  newline(),
                  text(" |_)   _   _|_  _|_   _   ._", GOLD),
                  text("    |   _.  o  |   _", YELLOW),
                  newline(),
                  text(" |_)  (/_   |_   |_  (/_  | ", GOLD),
                  text("  \\_|  (_|  |  |  _>", YELLOW),
                  newline(),
                  newline(),
                  text("================================================="))
          .build();

  // &6BetterJails &eby&6 Fefo6644 & contributors &e-&6 v{0}
  Args1<BetterJailsPlugin> PLUGIN_INFO = plugin ->
      text()
          .color(GOLD)
          .append(join(space(),
                       text("BetterJails"),
                       text("by", YELLOW),
                       text("Fefo6644 &"),
                       text()
                           .content("contributors")
                           .apply(builder -> {
                             final List<String> authors = plugin.getAuthors();
                             final List<String> contributors = authors.subList(1, authors.size());
                             builder
                                 .hoverEvent(contributors
                                                 .stream().map(Component::text)
                                                 .collect(toComponent(text(", ")))
                                                 .color(GREEN));
                           }),
                       text('-', YELLOW),
                       text('v' + plugin.getVersion())));

  // &fUsage:
  Args0 COMMAND_USAGE_TITLE = () -> prefixed(
      translatable()
          .key("betterjails.command.usage.title")
          .color(WHITE)
          .append(text(':')));

  // &7/{0}
  // Hover: &fClick to run: &7/{0}
  Args1<String> COMMAND_USAGE_ELEMENT = command -> prefixed(
      text()
          .color(GRAY)
          .content('/' + command)
          .hoverEvent(translatable("betterjails.command.usage.element.hover", WHITE, text('/' + command, GRAY)))
          .clickEvent(suggestCommand('/' + command)));

  Args1<CommandSyntaxException> COMMAND_ERROR = exception -> {
    if (exception.getRawMessage() instanceof ComponentMessage) {
      return prefixed(((ComponentMessage) exception.getRawMessage()).component().color(RED));
    } else {
      return prefixed(text(exception.getMessage(), RED));
    }
  };

  // &cOnly players can run this command.
  Args0 PLAYERS_ONLY = () -> prefixed(
      translatable()
          .key("betterjails.command.generic.players-only")
          .color(RED)
          .append(FULL_STOP));

  // &cYou do not have the permission to execute this command.
  Args0 NO_PERMISSION = () -> prefixed(
      translatable()
          .key("betterjails.command.generic.no-permission")
          .color(RED)
          .append(FULL_STOP));

  // &cThere was an error while reloading the configuration file.
  Args1<String> GENERIC_ERROR = action -> prefixed(
      translatable()
          .key("betterjails.generic.error")
          .args(translatable("betterjails.generic.error." + action))
          .color(RED)
          .append(FULL_STOP));

  // &bConfiguration file reloaded successfully
  Args0 CONFIG_RELOADED = () -> prefixed(
      translatable()
          .key("betterjails.reloadconfig.success")
          .color(AQUA)
          .append(FULL_STOP));

  // &7&o&nNote that not all configuration settings are reloadable and some will not take effect until the next server restart!
  Args0 CONFIG_RELOAD_NOTICE = () -> prefixed(
      translatable()
          .key("betterjails.reloadconfig.notice")
          .color(GRAY)
          .decorate(ITALIC, UNDERLINED)
          .append(text('!')));

  // &cIf you are a server admin, please check the server console for any errors.
  Args0 CHECK_CONSOLE = () -> prefixed(
      translatable()
          .key("betterjails.generic.check-console-for-errors")
          .color(RED)
          .append(FULL_STOP));

  static TextComponent.Builder prefixed(final ComponentLike component) {
    return TextComponent.ofChildren(SHORT_PREFIX, space(), component).toBuilder();
  }

  @FunctionalInterface
  interface Args0 {

    ComponentLike build();

    default void send(final Subject subject) {
      subject.sendMessage(build().asComponent());
    }
  }

  @FunctionalInterface
  interface Args1<T> {

    ComponentLike build(T t);

    default void send(final Subject subject, final T t) {
      subject.sendMessage(build(t).asComponent());
    }
  }

  @FunctionalInterface
  interface Args2<T, U> {

    ComponentLike build(T t, U u);

    default void send(final Subject subject, final T t, final U u) {
      subject.sendMessage(build(t, u).asComponent());
    }
  }

  @FunctionalInterface
  interface Args3<T, U, V> {

    ComponentLike build(T t, U u, V v);

    default void send(final Subject subject, final T t, final U u, final V v) {
      subject.sendMessage(build(t, u, v).asComponent());
    }
  }

  @FunctionalInterface
  interface Args4<T, U, V, W> {

    ComponentLike build(T t, U u, V v, W w);

    default void send(final Subject subject, final T t, final U u, final V v, final W w) {
      subject.sendMessage(build(t, u, v, w).asComponent());
    }
  }

  @FunctionalInterface
  interface Args5<T, U, V, W, X> {

    ComponentLike build(T t, U u, V v, W w, X x);

    default void send(final Subject subject, final T t, final U u, final V v, final W w, final X x) {
      subject.sendMessage(build(t, u, v, w, x).asComponent());
    }
  }
}
