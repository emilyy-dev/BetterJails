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
