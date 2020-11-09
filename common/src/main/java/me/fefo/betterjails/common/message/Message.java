package me.fefo.betterjails.common.message;

import me.fefo.betterjails.common.abstraction.MessagingSubject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
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
                      .hoverEvent(text("/{0}", RED).asHoverEvent())
                      .clickEvent(suggestCommand("/{0}"))),

  PLAYERS_ONLY(prefixed(text("Only players can run this command!", RED))),

  CONFIG_RELOADED(prefixed(text("Configuration reloaded successfully!", YELLOW))),

  CONFIG_RELOAD_NOTICE(prefixed(text().color(GOLD)
                                      .decorate(ITALIC, UNDERLINED)
                                      .append(text("Please note that not all configuration settings are reloadable!"))));

  private static TextComponent.Builder prefixed(final ComponentLike component) {
    return text().append(SHORT_PREFIX.component)
                 .append(space())
                 .append(component);
  }

  private static TextComponent hyphen() {
    return text(" - ", GRAY);
  }

  private final TextComponent component;

  Message(final TextComponent.Builder builder) {
    this.component = builder.build();
  }

  public void send(@NotNull final MessagingSubject subject, @NotNull final ComponentLike... replacements) {
    Component finalMessage = component;
    for (int i = -1; i < replacements.length; ++i) {
      final ComponentLike replacement = i == -1 ? text(subject.name()) : replacements[i];
      finalMessage = finalMessage.replaceText("{" + i + "}", replacement);
    }

    subject.send(finalMessage);
  }
}
