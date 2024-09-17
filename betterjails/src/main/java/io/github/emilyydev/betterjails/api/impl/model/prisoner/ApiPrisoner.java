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
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public final class ApiPrisoner implements Prisoner {

  private final UUID uuid;
  private final String name;
  private final String primaryGroup;
  private final Set<String> parentGroups;
  private final Jail jail;
  private final String jailedBy;
  private final SentenceExpiry expiry;
  private final Duration totalSentenceTime;
  private final String imprisonmentReason;
  private final ImmutableLocation lastLocation;
  private final boolean unknownLocation; // TODO(v2): lastLocation should just be nullable

  public ApiPrisoner(
      final UUID uuid,
      final String name,
      final String primaryGroup,
      final Collection<? extends String> parentGroups,
      final Jail jail,
      final String jailedBy,
      final SentenceExpiry expiry,
      final Duration totalSentenceTime,
      final String imprisonmentReason,
      final ImmutableLocation lastLocation,
      final boolean unknownLocation
  ) {
    this.uuid = uuid;
    this.name = name;
    this.primaryGroup = primaryGroup;
    this.parentGroups = ImmutableSet.copyOf(parentGroups);
    this.jail = jail;
    this.jailedBy = jailedBy;
    this.expiry = expiry;
    this.totalSentenceTime = totalSentenceTime;
    this.imprisonmentReason = imprisonmentReason;
    this.lastLocation = lastLocation;
    this.unknownLocation = unknownLocation;
  }

  @Override
  public @NotNull UUID uuid() {
    return this.uuid;
  }

  @Override
  public @Nullable String name() {
    return this.name;
  }

  public @NotNull String nameOr(final String fallback) {
    return MoreObjects.firstNonNull(this.name, fallback);
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
  public @Nullable String imprisonmentReason() {
    return this.imprisonmentReason;
  }

  // TODO(v2): make nullable, remove deprecation
  @Override
  public @NotNull ImmutableLocation lastLocation() {
    return this.lastLocation;
  }

  public @Nullable ImmutableLocation lastLocationNullable() {
    return this.unknownLocation ? null : this.lastLocation;
  }

  /**
   * True if this prisoner isn't actually imprisoned any more and will be released when they join the server.
   */
  public boolean released() {
    return timeLeft().isZero() || timeLeft().isNegative();
  }

  public boolean unknownLocation() {
    return this.unknownLocation;
  }

  public @NotNull Duration timeLeft() {
    return this.expiry.timeLeft();
  }

  public SentenceExpiry expiry() {
    return this.expiry;
  }

  public @NotNull ApiPrisoner withReleased() {
    return new ApiPrisoner(this.uuid, this.name, this.primaryGroup, this.parentGroups, this.jail, this.jailedBy, SentenceExpiry.of(Duration.ZERO), this.totalSentenceTime, this.imprisonmentReason, this.lastLocation, this.unknownLocation);
  }

  public @NotNull ApiPrisoner withLastLocation(final ImmutableLocation location) {
    return new ApiPrisoner(this.uuid, this.name, this.primaryGroup, this.parentGroups, this.jail, this.jailedBy, this.expiry, this.totalSentenceTime, this.imprisonmentReason, location, false);
  }

  /**
   * Creates a copy of this prisoner, but with their sentence time running if it wasn't already.
   * This method swaps out {@link #timeLeft} for {@link #jailedUntil}
   */
  public @NotNull ApiPrisoner withTimeRunning() {
    return new ApiPrisoner(this.uuid, this.name, this.primaryGroup, this.parentGroups, this.jail, this.jailedBy, SentenceExpiry.of(jailedUntil()), this.totalSentenceTime, this.imprisonmentReason, this.lastLocation, this.unknownLocation);
  }

  /**
   * Creates a copy of this prisoner, but with their sentence time paused if it wasn't already.
   * This method swaps out {@link #jailedUntil} for {@link #timeLeft}
   */
  public @NotNull ApiPrisoner withTimePaused() {
    return new ApiPrisoner(this.uuid, this.name, this.primaryGroup, this.parentGroups, this.jail, this.jailedBy, SentenceExpiry.of(timeLeft()), this.totalSentenceTime, this.imprisonmentReason, this.lastLocation, this.unknownLocation);
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
           ',' + this.unknownLocation +
           ')';
  }
}
