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

package com.github.fefo.betterjails.api.impl;

import com.github.fefo.betterjails.BetterJailsPlugin;
import com.github.fefo.betterjails.api.BetterJails;
import com.github.fefo.betterjails.api.event.EventBus;
import com.github.fefo.betterjails.api.impl.model.jail.ApiJailManager;
import com.github.fefo.betterjails.api.impl.model.prisoner.ApiPrisonerManager;
import com.github.fefo.betterjails.api.model.jail.JailManager;
import com.github.fefo.betterjails.api.model.prisoner.PrisonerManager;
import org.jetbrains.annotations.NotNull;

public class BetterJailsApi implements BetterJails {

  private final JailManager jailManager;
  private final PrisonerManager prisonerManager;
  private final EventBus eventBus;

  public BetterJailsApi(final BetterJailsPlugin plugin) {
    this.jailManager = new ApiJailManager(plugin);
    this.prisonerManager = new ApiPrisonerManager(plugin);
    this.eventBus = plugin.getEventBus();
    plugin.getEventBus().setApi(this);
  }

  @Override
  public @NotNull JailManager getJailManager() {
    return this.jailManager;
  }

  @Override
  public @NotNull PrisonerManager getPrisonerManager() {
    return this.prisonerManager;
  }

  @Override
  public @NotNull EventBus getEventBus() {
    return this.eventBus;
  }
}
