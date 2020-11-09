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

package me.fefo.betterjails.common.configuration;

import me.fefo.betterjails.common.BetterJailsPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ConfigurationAdapter {

  void reload();
  BetterJailsPlugin getPlugin();
  boolean getBoolean(@NotNull final String path, final boolean fallback);
  String getString(@NotNull final String path, @NotNull final String fallback);
  int getInteger(@NotNull final String path, final int fallback);
  double getDouble(@NotNull final String path, final double fallback);
  <T> List<T> getList(@NotNull final String path, @NotNull final List<T> fallback);
}
