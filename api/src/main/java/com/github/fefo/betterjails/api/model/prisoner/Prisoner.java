//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2021 Fefo6644 <federico.lopez.1999@outlook.com>
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

import java.time.Instant;
import java.util.UUID;

/**
 * A prisoner class merely made to <i>reflect</i> the values in the backend prisoner map.
 * <p>
 * It contains all the information about a prisoner such as the name (if known), the UUID, the jail
 * they are in, who imprisoned them, release date and more.
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
   *
   * @return the primary group
   */
  @Nullable String primaryGroup();

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
   *
   * @return the name of the {@link CommandSender} that jailed the player or {@code "api"}
   */
  @Nullable String jailedBy();

  /**
   * Gets the future {@link Instant} in time in which this prisoner will be released or
   * {@link Instant#MIN} if the prisoner was unjailed while offline.
   *
   * @return the instant of release date
   */
  @NotNull Instant jailedUntil();

  /**
   * Gets the location this player was at when jailed.
   *
   * @return the recorded location when this player was jailed or the "backup location" defined in
   * configuration if unknown (jailed while offline).
   */
  @NotNull ImmutableLocation lastLocation();
}
