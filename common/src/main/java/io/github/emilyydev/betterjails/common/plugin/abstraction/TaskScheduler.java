//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) emilyy-dev
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

package io.github.emilyydev.betterjails.common.plugin.abstraction;

import io.github.emilyydev.betterjails.common.plugin.BetterJailsPlugin;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class TaskScheduler {

  protected final BetterJailsPlugin plugin;
  protected final ScheduledExecutorService asyncScheduler =
      Executors.newScheduledThreadPool(
          16, new ThreadFactoryBuilder()
              .setNameFormat("betterjails-scheduler-thread-%d")
              .setDaemon(false)
              .setPriority(Thread.NORM_PRIORITY)
              .build());

  public TaskScheduler(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
  }

  public BetterJailsPlugin getPlugin() {
    return this.plugin;
  }

  public abstract void sync(final Runnable task);
  public abstract <T> Future<T> sync(final Callable<T> task);
  public abstract void sync(final Runnable task, final long delay);
  public abstract void sync(final Runnable task, final long delay, final long period);

  public Future<?> async(final Runnable task) {
    return this.asyncScheduler.submit(task);
  }

  public <T> Future<T> async(final Callable<T> task) {
    return this.asyncScheduler.submit(task);
  }

  public ScheduledFuture<?> async(final Runnable task, final long delay, final TimeUnit unit) {
    return this.asyncScheduler.schedule(task, delay, unit);
  }

  public <T> ScheduledFuture<T> async(final Callable<T> task, final long delay, final TimeUnit unit) {
    return this.asyncScheduler.schedule(task, delay, unit);
  }

  public ScheduledFuture<?> async(final Runnable task, final long initialDelay, final long delay, final TimeUnit unit) {
    return this.asyncScheduler.scheduleWithFixedDelay(task, initialDelay, delay, unit);
  }
}
