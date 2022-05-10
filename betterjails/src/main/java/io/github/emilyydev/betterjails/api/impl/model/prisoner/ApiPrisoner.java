//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2022 emilyy-dev
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
  private final Instant jailedUntil;
  private final ImmutableLocation lastLocation;

  public ApiPrisoner(
      final UUID uuid,
      final String name,
      final String primaryGroup,
      final Collection<? extends String> parentGroups,
      final Jail jail,
      final String jailedBy,
      final Instant jailedUntil,
      final ImmutableLocation lastLocation
  ) {
    this.uuid = uuid;
    this.name = name;
    this.primaryGroup = primaryGroup;
    this.parentGroups = ImmutableSet.copyOf(parentGroups);
    this.jail = jail;
    this.jailedBy = jailedBy;
    this.jailedUntil = jailedUntil;
    this.lastLocation = lastLocation;
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
    return this.jailedUntil;
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
