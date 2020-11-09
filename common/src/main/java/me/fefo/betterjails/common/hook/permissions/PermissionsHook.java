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

package me.fefo.betterjails.common.hook.permissions;

import me.fefo.betterjails.common.abstraction.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class PermissionsHook {

  public abstract CompletableFuture<Collection<String>> getParentGroups(final Player player);
  public abstract CompletableFuture<Collection<String>> getParentGroups(final UUID uuid);
  public abstract CompletableFuture<Void> setParentGroup(final Player player, final String group);
  public abstract CompletableFuture<Void> setParentGroup(final UUID uuid, final String group);
}
