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

package io.github.emilyydev.betterjails.api.impl.event.jail;

import com.github.fefo.betterjails.api.BetterJails;
import com.github.fefo.betterjails.api.event.BetterJailsEvent;
import com.github.fefo.betterjails.api.event.jail.JailDeleteEvent;
import com.github.fefo.betterjails.api.model.jail.Jail;
import io.github.emilyydev.betterjails.api.impl.event.SimpleBetterJailsEvent;
import org.jetbrains.annotations.NotNull;

public class JailDeleteEventImpl extends SimpleBetterJailsEvent implements JailDeleteEvent {

  private final Jail jail;

  public JailDeleteEventImpl(final BetterJails api, final Class<? extends BetterJailsEvent> eventType,
      final Jail jail) {
    super(api, eventType);
    this.jail = jail;
  }

  @Override
  public @NotNull Jail jail() {
    return this.jail;
  }
}