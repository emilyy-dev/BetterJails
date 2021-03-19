package com.github.fefo6644.betterjails.common.command.segment;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.jetbrains.annotations.NotNull;

public interface CommandSegment<S, N extends CommandNode<S>> {

  @NotNull N getCommandNode();

  default LiteralArgumentBuilder<S> literal(final String name) {
    return LiteralArgumentBuilder.literal(name);
  }

  default <T> RequiredArgumentBuilder<S, T> argument(final String name, final ArgumentType<T> type) {
    return RequiredArgumentBuilder.argument(name, type);
  }

  interface Argument<S, T> extends CommandSegment<S, ArgumentCommandNode<S, T>> { }
  interface Literal<S> extends CommandSegment<S, LiteralCommandNode<S>> { }
  interface Root<S> extends CommandSegment<S, RootCommandNode<S>> { }
}
