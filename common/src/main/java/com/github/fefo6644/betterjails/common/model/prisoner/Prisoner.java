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

package com.github.fefo6644.betterjails.common.model.prisoner;

import com.github.fefo6644.betterjails.common.plugin.abstraction.Location;
import com.github.fefo6644.betterjails.common.plugin.abstraction.Player;
import net.kyori.adventure.audience.Audience;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public abstract class Prisoner<P> extends Player<P> {

  private final Set<String> groups;
  private final String jailedBy;
  private final Duration jailedFor;
  private final Instant jailedUntil;
  private final Location lastLocation;

  public Prisoner(final @NotNull UUID uuid, final @Nullable String name,
                  final @NotNull String jailedBy, final @NotNull Location lastLocation,
                  final @NotNull Duration duration, final @NotNull Set<String> groups,
                  final @NotNull Audience audience, final @NotNull P player) {
    super(uuid, name, audience, player);
    Validate.notNull(jailedBy);
    Validate.notNull(lastLocation);

    this.groups = groups;
    this.jailedBy = jailedBy;
    this.jailedFor = duration;
    this.jailedUntil = Instant.now().plus(duration);
    this.lastLocation = lastLocation;
  }

  @Override
  public boolean isJailed() {
    return true;
  }

  @Override
  public Prisoner<P> asPrisoner() {
    return this;
  }

  public Set<String> groups() {
    return this.groups;
  }

  public String jailedBy() {
    return this.jailedBy;
  }

  public Duration jailedFor() {
    return this.jailedFor;
  }

  public Instant jailedUntil() {
    return this.jailedUntil;
  }

  public Location lastLocation() {
    return this.lastLocation;
  }
}
