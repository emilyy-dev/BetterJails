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

package com.github.fefo6644.betterjails.common.hook.permissions.luckperms;

import com.github.fefo6644.betterjails.common.model.cell.CellManager;
import com.github.fefo6644.betterjails.common.platform.BetterJailsPlugin;
import com.github.fefo6644.betterjails.common.platform.abstraction.PlatformAdapter;
import com.github.fefo6644.betterjails.common.platform.abstraction.Player;
import com.github.fefo6644.betterjails.common.model.prisoner.Prisoner;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.jetbrains.annotations.NotNull;

public final class AvailableContexts<P> implements ContextCalculator<P> {

  private static final String KEY_JAILED_FOR = "betterjails:jailed-for";
  private static final String KEY_IS_JAILED = "betterjails:is-jailed";
  private static final String KEY_IN_JAIL = "betterjails:in-jail";

  private final CellManager cellManager;
  private final PlatformAdapter<?, P, ?, ?> adapter;

  public AvailableContexts(final BetterJailsPlugin plugin) {
    this.adapter = plugin.getPlatformAdapter();
    this.cellManager = plugin.getCellManager();
  }

  @Override
  public void calculate(final @NotNull P target, final @NotNull ContextConsumer consumer) {
    final Player player = this.adapter.adaptPlayer(target);

    consumer.accept(KEY_IS_JAILED, Boolean.toString(player.isJailed()));
    if (player.isJailed()) {
      final Prisoner prisoner = player.asPrisoner();
      consumer.accept(KEY_JAILED_FOR, prisoner.jailedFor().toString());
//      consumer.accept(KEY_IN_JAIL);
    }
  }

  @Override
  public ContextSet estimatePotentialContexts() {
    final ImmutableContextSet.Builder builder = ImmutableContextSet.builder();

    builder.add(KEY_IS_JAILED, "true");
    builder.add(KEY_IS_JAILED, "false");

    return builder.build();
  }
}
