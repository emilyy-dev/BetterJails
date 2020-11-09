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

package me.fefo.betterjails.common.model.cell;

import me.fefo.betterjails.common.abstraction.Location;
import org.jetbrains.annotations.NotNull;

public final class Cell {

  private final String name;
  private final Location location;

  public Cell(@NotNull final String name, @NotNull final Location location) {
    this.name = name;
    this.location = location;
  }

  public @NotNull String getName() {
    return name;
  }

  public @NotNull Location getLocation() {
    return location;
  }
}
