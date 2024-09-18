//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2024 emilyy-dev
// Copyright (c) 2024 Emilia Kond
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

package com.github.fefo.betterjails.api.model.prisoner;

import com.github.fefo.betterjails.api.model.jail.Jail;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * A prisoner class merely made to <i>reflect</i> the values in the backend prisoner map.
 * <p>
 * It contains all the information about a prisoner such as the name (if known), the UUID, the jail
 * they are in, who imprisoned them, release date and more.
 * </p>
 */
public interface Prisoner {

  /**
   * Returns the UUID of the player represented by this {@code Prisoner} object.
   *
   * @return the player UUID
   */
  @NotNull UUID uuid();

  /**
   * Returns the name of the prisoner or {@code null} if unknown.
   *
   * @return the player name
   */
  @Nullable String name();

  /**
   * Returns the player's primary group according to the permission implementation.
   * <p>
   * The nullability of this field can be due to platform settings or because it was unknown at the
   * time of jailing.
   * </p>
   *
   * @return the primary group
   */
  @Nullable String primaryGroup();

  /**
   * Returns the player's parent groups according to the permission implementation.
   * <p>
   * Unlike the {@link #primaryGroup() primary group}, this method will always return a valid set,
   * though it may be empty.
   * </p>
   *
   * @return all of the parent groups
   */
  @Unmodifiable @NotNull Set<@NotNull String> parentGroups();

  /**
   * The {@link Jail} this prisoner is currently in, whether the player is online or not.
   *
   * @return the jail this prisoner is currently in
   */
  @NotNull Jail jail();

  /**
   * Gets the name of the {@link CommandSender} that jailed the player or {@code "api"} if done
   * through the API.
   * <p>
   * Although it makes more sense for this to be {@code @NotNull}, implementation for whatever
   * reason passes {@code null} <i>sometimes.</i>
   * </p>
   *
   * @return the name of the {@link CommandSender} that jailed the player or {@code "api"}
   */
  @Nullable String jailedBy();

  /**
   * Gets the future {@link Instant} in time in which this prisoner will be released or
   * {@link Instant#MIN} if the prisoner was released while offline.
   *
   * @return the instant of release date
   */
  @NotNull Instant jailedUntil();

  /**
   * Gets the location this player was at when jailed.
   * @see #unknownLastLocation()
   *
   * @return the recorded location when this player was jailed or the "backup location" defined in
   * configuration if unknown (jailed while offline)
   */
  @NotNull ImmutableLocation lastLocation();

  /**
   * Gets the complete sentence time as a {@link Duration} for when the player was jailed.
   *
   * @return the total sentence time, or {@link Duration#ZERO} if unknown (only happens when
   * existing prisoner data was upgraded from an older version where this data did not exist)
   */
  @NotNull Duration totalSentenceTime();

  /**
   * Gets the reason under which the player was imprisoned.
   *
   * @return the imprisonment reason or {@code null} if not provided
   */
  @Nullable String imprisonmentReason();

  /**
   * Whether the player has been jailed while they were offline. This means their last location isn't known yet.
   * If true, {@link #lastLocation()} returns the "backup location".
   *
   * @return true if no last location has been saved for this prisoner yet.
   */
  boolean unknownLastLocation();
}
