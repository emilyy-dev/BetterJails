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

package com.github.fefo.betterjails.api.event;

import com.github.fefo.betterjails.api.BetterJails;
import org.jetbrains.annotations.NotNull;

/**
 * The superinterface of every event BetterJails posts on its event bus.
 * <p>
 * All events happen after the action takes place (unless noted otherwise).
 * <p>
 * No events are cancellable, they exist merely for monitoring purposes.
 */
public interface BetterJailsEvent {

  /**
   * Gets the API instance that posted this event.
   *
   * @return the API instance this event was dispatched from
   */
  @NotNull BetterJails getBetterJails();

  /**
   * Gets the specific type of this event. This is useful for listeners that are subscribed to
   * superinterfaces of some events to listen to all of the subinterfaces' dispatching.
   *
   * @return the event type
   */
  @NotNull Class<? extends BetterJailsEvent> getEventType();
}
