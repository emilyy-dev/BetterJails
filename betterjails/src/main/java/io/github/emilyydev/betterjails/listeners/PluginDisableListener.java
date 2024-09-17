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

package io.github.emilyydev.betterjails.listeners;

import io.github.emilyydev.betterjails.api.impl.event.ApiEventBus;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

public final class PluginDisableListener implements Listener {

  public static PluginDisableListener create(final ApiEventBus eventBus) {
    return new PluginDisableListener(eventBus);
  }

  private final ApiEventBus eventBus;

  private PluginDisableListener(final ApiEventBus eventBus) {
    this.eventBus = eventBus;
  }

  public void register(final Plugin plugin) {
    plugin.getServer().getPluginManager().registerEvent(
        PluginDisableEvent.class, this, EventPriority.NORMAL,
        (l, e) -> pluginDisable((PluginDisableEvent) e), plugin
    );
  }

  private void pluginDisable(final PluginDisableEvent event) {
    this.eventBus.unsubscribe(event.getPlugin());
  }
}
