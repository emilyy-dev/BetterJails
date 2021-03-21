//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) Fefo6644 <federico.lopez.1999@outlook.com>
// Copyright (c) contributors
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

package com.github.fefo6644.betterjails.bukkit.platform;

import com.github.fefo6644.betterjails.bukkit.BetterJailsBukkit;
import com.github.fefo6644.betterjails.common.plugin.BetterJailsPlugin;
import com.github.fefo6644.betterjails.common.plugin.abstraction.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public final class BukkitTaskScheduler extends TaskScheduler {

  private final BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
  private final BetterJailsBukkit bukkitPlugin = (BetterJailsBukkit) this.plugin.getBootstrapPlugin();

  public BukkitTaskScheduler(final BetterJailsPlugin plugin) {
    super(plugin);
  }

  @Override
  public void sync(final Runnable task) {
    this.bukkitScheduler.runTask(this.bukkitPlugin, task);
  }

  @Override
  public <T> Future<T> sync(final Callable<T> task) {
    return this.bukkitScheduler.callSyncMethod(this.bukkitPlugin, task);
  }

  @Override
  public void sync(final Runnable task, final long delay) {
    this.bukkitScheduler.runTaskLater(this.bukkitPlugin, task, delay);
  }

  @Override
  public void sync(final Runnable task, final long delay, final long period) {
    this.bukkitScheduler.runTaskTimer(this.bukkitPlugin, task, delay, period);
  }
}
