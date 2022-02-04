//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2021 emilyy-dev
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

package com.github.fefo.betterjails.api.model.jail;

import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;

/**
 * A jail manager, for managing jails. Allows for creating, fetching and deleting {@link Jail} instances by name.
 */
@ApiStatus.NonExtendable
public interface JailManager {

  /**
   * Creates a new jail for that name at that location if it doesn't exist.
   * Jail names are case insensitive and converted to lowercase.
   * <p>
   * If it does exist this method throws an {@link IllegalArgumentException}.
   * </p>
   * <p>
   * If it does not exist, a new jail will be created, saved to storage and returned.
   * </p>
   *
   * @param name     the identifying name of the new jail
   * @param location the location of the new jail
   * @return a new jail with no prisoners
   * @throws IllegalArgumentException if there is an already existing jail with that name
   */
  @NotNull Jail createAndSaveJail(@NotNull String name, @NotNull Location location) throws IllegalArgumentException;

  /**
   * Gets the jail by that name, case insensitive.
   *
   * @param name the name of the jail to retrieve - case insensitive
   * @return the {@link Jail} instance for that name if present or {@code null} if non-existent
   */
  @Nullable Jail getJail(@NotNull String name);

  /**
   * Permanently deletes a jail from storage. Prisoners jailed in this jail will be transported
   * to another jail. If no such jail exists this method will no-op.
   *
   * @param jail the jail to remove
   */
  void deleteJail(@NotNull Jail jail);

  /**
   * Gets a set of all available {@link Jail}s.
   * <p>
   * This collection is unmodifiable and it updates over time as the backend map changes.
   * </p>
   *
   * @return an unmodifiable view of the jail collection
   */
  @NotNull @UnmodifiableView Collection<@NotNull Jail> getAllJails();
}
