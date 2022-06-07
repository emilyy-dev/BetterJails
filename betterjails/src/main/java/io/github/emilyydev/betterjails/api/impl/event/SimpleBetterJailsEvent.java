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

package io.github.emilyydev.betterjails.api.impl.event;

import com.github.fefo.betterjails.api.BetterJails;
import com.github.fefo.betterjails.api.event.BetterJailsEvent;
import org.jetbrains.annotations.NotNull;

public abstract class SimpleBetterJailsEvent implements BetterJailsEvent {

  private final BetterJails api;
  private final Class<? extends BetterJailsEvent> eventType;

  protected SimpleBetterJailsEvent(final BetterJails api, final Class<? extends BetterJailsEvent> eventType) {
    this.api = api;
    this.eventType = eventType;
  }

  @Override
  public @NotNull BetterJails getBetterJails() {
    return this.api;
  }

  @Override
  public @NotNull Class<? extends BetterJailsEvent> getEventType() {
    return this.eventType;
  }
}
