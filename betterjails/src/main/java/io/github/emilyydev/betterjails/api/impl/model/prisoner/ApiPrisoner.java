//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2024 emilyy-dev
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

package io.github.emilyydev.betterjails.api.impl.model.prisoner;

import com.github.fefo.betterjails.api.model.jail.Jail;
import com.github.fefo.betterjails.api.model.prisoner.Prisoner;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ApiPrisoner implements Prisoner {

  private final UUID uuid;
  private final String name;
  private final String primaryGroup;
  private final Set<String> parentGroups;
  private final Jail jail;
  private final String jailedBy;
  private final @Nullable Instant jailedUntil;
  private final @Nullable Duration timeLeft;
  private final Duration totalSentenceTime;
  private final ImmutableLocation lastLocation;
  // If true, this prisoner isn't actually imprisoned any more and will be released when they join the server.
  private final boolean released;
  // If true, then we don't actually know this prisoner's lastLocation, it will be filled in when they join the server.
  // Ideally lastLocation would just be optional, but that would break API and stuff.
  private final boolean incomplete;
  // TODO: maybe replace these booleans with a three-state enum, because I'm pretty sure both can't be true at once.

  public @Deprecated ApiPrisoner(
      final UUID uuid,
      final String name,
      final String primaryGroup,
      final Collection<? extends String> parentGroups,
      final Jail jail,
      final String jailedBy,
      final @NotNull Instant jailedUntil,
      final Duration totalSentenceTime,
      final ImmutableLocation lastLocation
  ) {
    this.uuid = uuid;
    this.name = name;
    this.primaryGroup = primaryGroup;
    this.parentGroups = ImmutableSet.copyOf(parentGroups);
    this.jail = jail;
    this.jailedBy = jailedBy;
    this.jailedUntil = jailedUntil;
    this.timeLeft = null;
    this.totalSentenceTime = totalSentenceTime;
    this.lastLocation = lastLocation;
    this.released = false;
    this.incomplete = false;
  }

  @Contract("_, _, _, _, _, _, null, null, _, _, _, _ -> fail")
  public ApiPrisoner(
      final UUID uuid,
      final String name,
      final String primaryGroup,
      final Collection<? extends String> parentGroups,
      final Jail jail,
      final String jailedBy,
      final @Nullable Instant jailedUntil,
      final @Nullable Duration timeLeft,
      final Duration totalSentenceTime,
      final ImmutableLocation lastLocation,
      final boolean released,
      final boolean incomplete
  ) {
    this.uuid = uuid;
    this.name = name;
    this.primaryGroup = primaryGroup;
    this.parentGroups = ImmutableSet.copyOf(parentGroups);
    this.jail = jail;
    this.jailedBy = jailedBy;
    this.jailedUntil = jailedUntil;
    this.timeLeft = timeLeft;
    this.totalSentenceTime = totalSentenceTime;
    this.lastLocation = lastLocation;
    this.released = released;
    this.incomplete = incomplete;
  }

  @Override
  public @NotNull UUID uuid() {
    return this.uuid;
  }

  @Override
  public @Nullable String name() {
    return this.name;
  }

  @Override
  public @Nullable String primaryGroup() {
    return this.primaryGroup;
  }

  @Override
  public @Unmodifiable @NotNull Set<@NotNull String> parentGroups() {
    return this.parentGroups;
  }

  @Override
  public @NotNull Jail jail() {
    return this.jail;
  }

  @Override
  public @Nullable String jailedBy() {
    return this.jailedBy;
  }

  @Override
  public @NotNull Instant jailedUntil() {
    if (this.jailedUntil == null) {
      return Instant.now().plus(Objects.requireNonNull(timeLeft));
    } else {
      return jailedUntil;
    }
  }

  @Override
  public @NotNull Duration totalSentenceTime() {
    return this.totalSentenceTime;
  }

  @Override
  public @NotNull ImmutableLocation lastLocation() {
    return this.lastLocation;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) { return true; }
    if (!(other instanceof Prisoner)) { return false; }
    return this.uuid.equals(((Prisoner) other).uuid());
  }

  @Override
  public int hashCode() {
    return this.uuid.hashCode();
  }

  @Override
  public String toString() {
    return "Prisoner(" +
           this.uuid +
           ',' + '"' + this.name + '"' +
           ',' + '"' + this.primaryGroup + '"' +
           ',' + this.parentGroups +
           ',' + this.jail +
           ',' + '"' + this.jailedBy + '"' +
           ',' + this.jailedUntil +
           ',' + this.lastLocation +
           ')';
  }
}
