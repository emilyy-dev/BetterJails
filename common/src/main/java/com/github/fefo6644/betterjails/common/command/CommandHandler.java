package com.github.fefo6644.betterjails.common.command;

import com.github.fefo6644.betterjails.common.command.segment.CommandSegment;
import com.github.fefo6644.betterjails.common.message.MessagingSubject;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.RootCommandNode;
import org.jetbrains.annotations.NotNull;

public class CommandHandler implements CommandSegment.Root<MessagingSubject> {

  private final CommandDispatcher<MessagingSubject> dispatcher = new CommandDispatcher<>();
  private final RootCommandNode<MessagingSubject> rootCommandNode = this.dispatcher.getRoot();

  {
    ImmutableList.of();
  }

  @Override
  public @NotNull RootCommandNode<MessagingSubject> getCommandNode() {
    return this.rootCommandNode;
  }
}
