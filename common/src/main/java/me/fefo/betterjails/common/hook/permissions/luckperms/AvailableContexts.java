/*
 * This file is part of the BetterJails (https://github.com/Fefo6644/BetterJails).
 *
 *  Copyright (c) 2020 Fefo6644 <federico.lopez.1999@outlook.com>
 *  Copyright (c) 2020 contributors
 *
 *  BetterJails is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 3.
 *
 *  BetterJails is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.fefo.betterjails.common.hook.permissions.luckperms;

import me.fefo.betterjails.common.BetterJailsPlugin;
import me.fefo.betterjails.common.abstraction.PlatformAdapter;
import me.fefo.betterjails.common.abstraction.Player;
import me.fefo.betterjails.common.model.cell.CellManager;
import me.fefo.betterjails.common.model.prisoner.Prisoner;
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
  private final PlatformAdapter<P, ?, ?> adapter;

  public AvailableContexts(final BetterJailsPlugin plugin) {
    this.adapter = plugin.getPlatformAdapter();
    this.cellManager = plugin.getCellManager();
  }

  @Override
  public void calculate(@NotNull final P target, @NotNull final ContextConsumer consumer) {
    final Player player = adapter.adaptPlayer(target);

    consumer.accept(KEY_IS_JAILED, Boolean.toString(player.isJailed()));
    if (player.isJailed()) {
      final Prisoner prisoner = player.asPrisoner();
      consumer.accept(KEY_JAILED_FOR, prisoner.getJailedFor().toString());
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
