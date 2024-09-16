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

package com.github.fefo.betterjails.api.model.prisoner;

import com.github.fefo.betterjails.api.model.jail.Jail;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.util.Collection;
import java.util.UUID;

/**
 * <b>Extreme care should be taken when handling prisoner data.</b> The backend implementation was
 * <b>not</b> originally designed with this purpose in mind, {@link #getPrisoner(UUID)} creates a
 * new object on each call and {@link #getAllPrisoners()} does not fully reflect the backend map.
 */
@ApiStatus.NonExtendable
public interface PrisonerManager {

  /**
   * Gets a {@link Prisoner} by the player's unique ID.
   *
   * @param uuid the UUID of the player
   * @return the prisoner object or {@code null} if the player isn't jailed
   */
  @Nullable Prisoner getPrisoner(@NotNull UUID uuid);

  /**
   * Imprisons a player and teleports them to the corresponding jail if they are online or on join
   * if offline.
   * <p>
   * The provided duration may or may not be constant in time, that is depending on the
   * {@code offlineTime} setting in the configuration.
   * <p>
   * If the player is already jailed the prisoner will be re-jailed and the new prisoner object will
   * be returned.
   *
   * @param uuid     the UUID of the player to imprison
   * @param jail     the jail the player will be teleported to
   * @param duration duration of the jailing time
   * @return the prisoner representing the player
   */
  default @NotNull Prisoner jailPlayer(final @NotNull UUID uuid, final @NotNull Jail jail, final @NotNull Duration duration) {
    return jailPlayer(uuid, jail, duration, null);
  }

  /**
   * Imprisons a player and teleports them to the corresponding jail if they are online or on join
   * if offline.
   * <p>
   * The provided duration may or may not be constant in time, that is depending on the
   * {@code offlineTime} setting in the configuration.
   * <p>
   * If the player is already jailed the prisoner will be re-jailed and the new prisoner object will
   * be returned.
   *
   * @param uuid     the UUID of the player to imprison
   * @param jail     the jail the player will be teleported to
   * @param duration duration of the jailing time
   * @param reason   imprisonment reason
   * @return the prisoner representing the player
   */
  @NotNull Prisoner jailPlayer(@NotNull UUID uuid, @NotNull Jail jail, @NotNull Duration duration, @Nullable String reason);

  /**
   * Releases a prisoner immediately if online or schedules for releasing if offline.
   *
   * @param prisoner the prisoner to release
   * @return {@code true} if unjailed successfully (both if online or offline)
   */
  boolean releasePrisoner(@NotNull Prisoner prisoner);

  /**
   * Checks if a player by the provided unique ID is currently jailed (offline or not).
   *
   * @param uuid the UUID for the player to check imprisonment status
   * @return {@code true} if the player is currently jailed
   */
  boolean isPlayerJailed(@NotNull UUID uuid);

  /**
   * Gets a set of currently jailed prisoners. The collection includes prisoners that are currently
   * offline and are scheduled for releasing.
   *
   * @return an unmodifiable collection of all prisoners
   */
  @NotNull @Unmodifiable Collection<@NotNull Prisoner> getAllPrisoners();
}
