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

package me.fefo.betterjails.common.model;

import me.fefo.betterjails.common.abstraction.Location;
import me.fefo.betterjails.common.configuration.ConfigurationAdapter;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

public class BackupLocation {

  public static BackupLocation from(@NotNull final String world,
                                    final double x, final double y, final double z,
                                    final float yaw, final float pitch) {
    Validate.notNull(world, "Invalid world name: null");

    return new BackupLocation(world, x, y, z, yaw, pitch);
  }

  public static BackupLocation create(@NotNull final ConfigurationAdapter configurationAdapter) {
    Validate.notNull(configurationAdapter, "Backup location section cannot be null!");

    final String world = configurationAdapter.getString("world", "world");
    final double x = configurationAdapter.getDouble("x", 0.0);
    final double y = configurationAdapter.getDouble("y", 0.0);
    final double z = configurationAdapter.getDouble("z", 0.0);
    final float yaw = (float) configurationAdapter.getDouble("yaw", 0.0);
    final float pitch = (float) configurationAdapter.getDouble("pitch", 0.0);

    return from(world, x, y, z, yaw, pitch);
  }

  private final Location loc;

  private BackupLocation(final String world,
                         final double x, final double y, final double z,
                         final float yaw, final float pitch) {
    this.loc = new Location(null, x, y, z, yaw, pitch);
  }

  public Location getLocation() {
    return loc;
  }
}
