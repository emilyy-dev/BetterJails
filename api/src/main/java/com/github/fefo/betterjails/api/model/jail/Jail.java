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

package com.github.fefo.betterjails.api.model.jail;

import com.github.fefo.betterjails.api.model.prisoner.PrisonerManager;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

/**
 * Represents a virtual jail that can be identified by name and located in a world on the server
 */
public interface Jail {

  /**
   * Gets the current location players will be teleported to when jailed.
   *
   * @return the jail location
   */
  @NotNull ImmutableLocation location();

  /**
   * Sets the new location players will be teleported to when jailed.
   * <p>
   * This method <b>does not</b> teleport the current prisoners to the new location, use
   * {@link PrisonerManager#jailPlayer(UUID, Jail, Duration)} to relocate them.
   *
   * @param location the new location
   */
  void location(@NotNull ImmutableLocation location);

  /**
   * The identifying name of this jail. This is a unique identifier.
   *
   * @return the jail name
   */
  @NotNull String name();
}
