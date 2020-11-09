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

package me.fefo.betterjails.common;

import me.fefo.betterjails.common.abstraction.MessagingSubject;
import me.fefo.betterjails.common.abstraction.PlatformAdapter;
import me.fefo.betterjails.common.abstraction.PlatformScheduler;
import me.fefo.betterjails.common.configuration.ConfigurationAdapter;
import me.fefo.betterjails.common.model.cell.CellManager;
import me.fefo.betterjails.common.model.prisoner.PrisonerManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public interface BetterJailsPlugin {

  @NotNull CellManager getCellManager();
  @NotNull PrisonerManager getPrisonerManager();
  @NotNull ConfigurationAdapter getConfigurationAdapter();
  @NotNull <T> PlatformScheduler<T> getPlatformScheduler(@NotNull Class<T> taskClass);
  @NotNull MessagingSubject getConsole();
  @NotNull <P, L, W> PlatformAdapter<P, L, W> getPlatformAdapter();

  @NotNull Logger getPluginLogger();
  @NotNull String getVersion();

  default @NotNull String getAuthor() {
    return "Fefo6644";
  }
}
