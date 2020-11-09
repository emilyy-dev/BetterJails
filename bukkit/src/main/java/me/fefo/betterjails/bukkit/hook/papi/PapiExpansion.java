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

package me.fefo.betterjails.bukkit.hook.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.fefo.betterjails.common.BetterJailsPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PapiExpansion extends PlaceholderExpansion {

  private static final String TOTAL_JAILED_PLAYERS = "total_jailed_players";
  private static final String ONLINE_JAILED_PLAYERS = "online_jailed_players";
  private static final String TOTAL_JAILS = "total_jails";
  private final BetterJailsPlugin plugin;

  public PapiExpansion(@NotNull final BetterJailsPlugin plugin) { this.plugin = plugin; }

  @Override
  public String getIdentifier() { return "betterjails"; }

  @Override
  public @NotNull String getAuthor() { return plugin.getAuthor(); }

  @Override
  public @NotNull String getVersion() { return plugin.getVersion(); }

  @Override
  public String onPlaceholderRequest(@Nullable Player p,
                                     @NotNull String placeholder) {
    switch (placeholder) {
      default:
        return null;
    }
  }
}
