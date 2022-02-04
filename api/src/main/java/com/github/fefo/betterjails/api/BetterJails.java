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

package com.github.fefo.betterjails.api;

import com.github.fefo.betterjails.api.event.EventBus;
import com.github.fefo.betterjails.api.model.jail.Jail;
import com.github.fefo.betterjails.api.model.jail.JailManager;
import com.github.fefo.betterjails.api.model.prisoner.Prisoner;
import com.github.fefo.betterjails.api.model.prisoner.PrisonerManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * The core API for the BetterJails plugin, giving access to various interfaces for other plugins to
 * work with it.
 * <p>
 * You can get a hold of an instance of this interface by calling
 * {@code Bukkit.getServicesManager().load(BetterJails.class)}.
 * </p>
 */
@ApiStatus.NonExtendable
public interface BetterJails {

  /**
   * Gets the {@link JailManager}, responsible for managing {@link Jail} instances.
   * <p>
   * This manager can be used to create, delete and retrieve instances of a {@link Jail} by name
   * or get all available jails.
   * </p>
   *
   * @return the jail manager
   */
  @NotNull JailManager getJailManager();

  /**
   * Gets the {@link PrisonerManager}, responsible for managing {@link Prisoner} instances.
   * <p>
   * This manager can be used to imprison players, retrieve and release {@link Prisoner}s or get
   * all known prisoners.
   * </p>
   *
   * @return the prisoner manager
   */
  @NotNull PrisonerManager getPrisonerManager();

  /**
   * Gets the plugin {@link EventBus}, in which other plugins can subscribe (or "listen") to certain
   * events and actions that happen throughout the functioning of the plugin.
   *
   * @return the event bus
   */
  @NotNull EventBus getEventBus();
}
