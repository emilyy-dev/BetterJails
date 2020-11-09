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

package me.fefo.betterjails.common.model.prisoner;

import me.fefo.betterjails.common.abstraction.Location;
import me.fefo.betterjails.common.abstraction.Player;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public abstract class Prisoner extends Player {

  private final String group;
  private final String jailedBy;
  private final Duration jailedFor;
  private final Instant jailedUntil;
  private final Location lastLocation;

  public Prisoner(@NotNull final UUID uuid, @NotNull final String name,
                  @NotNull final String jailedBy, @NotNull final Location lastLocation,
                  @NotNull final Duration duration, @Nullable final String group) {
    super(uuid, name);
    Validate.notNull(jailedBy);
    Validate.notNull(lastLocation);

    this.group = group;
    this.jailedBy = jailedBy;
    this.jailedFor = duration;
    this.jailedUntil = Instant.now().plus(duration);
    this.lastLocation = lastLocation;
  }

  @Override
  public boolean isJailed() {
    return true;
  }

  public String getGroup() {
    return group;
  }

  public String getJailedBy() {
    return jailedBy;
  }

  public Duration getJailedFor() {
    return jailedFor;
  }

  public Instant getJailedUntil() {
    return jailedUntil;
  }

  public Location getLastLocation() {
    return lastLocation;
  }
}
