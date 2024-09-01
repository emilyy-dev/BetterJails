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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class ApiPrisoner implements Prisoner {

  private final UUID uuid;
  private final String name;
  private final String primaryGroup;
  private final Set<String> parentGroups;
  private final Jail jail;
  private final String jailedBy;
  private final SentenceExpiry expiry;
  private final Duration totalSentenceTime;
  private final ImmutableLocation lastLocation;
  private final ImprisonmentState imprisonmentState;

  public ApiPrisoner(
      final UUID uuid,
      final String name,
      final String primaryGroup,
      final Collection<? extends String> parentGroups,
      final Jail jail,
      final String jailedBy,
      final SentenceExpiry expiry,
      final Duration totalSentenceTime,
      final ImmutableLocation lastLocation,
      final ImprisonmentState imprisonmentState
  ) {
    this.uuid = uuid;
    this.name = name;
    this.primaryGroup = primaryGroup;
    this.parentGroups = ImmutableSet.copyOf(parentGroups);
    this.jail = jail;
    this.jailedBy = jailedBy;
    this.expiry = expiry;
    this.totalSentenceTime = totalSentenceTime;
    this.lastLocation = lastLocation;
    this.imprisonmentState = imprisonmentState;
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
    return released() ? Instant.MIN : this.expiry.expiryDate();
  }

  @Override
  public @NotNull Duration totalSentenceTime() {
    return this.totalSentenceTime;
  }

  @Override
  public @NotNull ImmutableLocation lastLocation() {
    return this.lastLocation;
  }

  public boolean released() {
    return this.imprisonmentState == ImprisonmentState.RELEASED;
  }

  public boolean incomplete() {
    return this.imprisonmentState == ImprisonmentState.UNKNOWN_LOCATION;
  }

  public @NotNull Duration timeLeft() {
    return released() ? Duration.ZERO : this.expiry.timeLeft();
  }

  public SentenceExpiry expiry() {
    return this.expiry;
  }

  public @NotNull ApiPrisoner withReleased() {
    return new ApiPrisoner(this.uuid, this.name, this.primaryGroup, this.parentGroups, this.jail, this.jailedBy, this.expiry, this.totalSentenceTime, this.lastLocation, ImprisonmentState.RELEASED);
  }

  public @NotNull ApiPrisoner withLastLocation(final ImmutableLocation location) {
    return new ApiPrisoner(this.uuid, this.name, this.primaryGroup, this.parentGroups, this.jail, this.jailedBy, this.expiry, this.totalSentenceTime, location, ImprisonmentState.KNOWN_LOCATION);
  }

  /**
   * Creates a copy of this prisoner, but with their sentence time running if it wasn't already.
   * This method swaps out {@link #timeLeft} for {@link #jailedUntil}
   */
  public @NotNull ApiPrisoner withTimeRunning() {
    return new ApiPrisoner(this.uuid, this.name, this.primaryGroup, this.parentGroups, this.jail, this.jailedBy, SentenceExpiry.of(jailedUntil()), this.totalSentenceTime, this.lastLocation, this.imprisonmentState);
  }

  /**
   * Creates a copy of this prisoner, but with their sentence time paused if it wasn't already.
   * This method swaps out {@link #jailedUntil} for {@link #timeLeft}
   */
  public @NotNull ApiPrisoner withTimePaused() {
    return new ApiPrisoner(this.uuid, this.name, this.primaryGroup, this.parentGroups, this.jail, this.jailedBy, SentenceExpiry.of(timeLeft()), this.totalSentenceTime, this.lastLocation, this.imprisonmentState);
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
           ',' + this.expiry +
           ',' + this.totalSentenceTime +
           ',' + this.lastLocation +
           ',' + this.imprisonmentState +
           ')';
  }

  public enum ImprisonmentState {
    // We don't actually know this prisoner's lastLocation, it will be filled in when they join the server.
    // Ideally lastLocation would just be optional, but that would break API and stuff
    UNKNOWN_LOCATION,
    KNOWN_LOCATION,

    // This prisoner isn't actually imprisoned any more and will be released when they join the server.
    RELEASED
  }
}
