//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2024 emilyy-dev
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

package io.github.emilyydev.betterjails.interfaces.storage;

import com.github.fefo.betterjails.api.model.jail.Jail;
import com.google.common.collect.ImmutableMap;
import io.github.emilyydev.betterjails.api.impl.model.prisoner.ApiPrisoner;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Gates access to the StorageInterface via a single-threaded executor service,
 * given that all operations on StorageInterface are blocking and synchronous, accessing them exclusively on
 * a single thread, every operation is queued and atomic with respect to each other.
 * It also ensures that all collections passed (maps, lists, etc.) are cloned before passing them around.
 */
public final class StorageAccess implements AutoCloseable {

  private final StorageInterface storageInterface;
  private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(task -> {
    final Thread t = new Thread(task, "BetterJails I/O Thread");
    t.setPriority(Thread.MIN_PRIORITY);
    t.setDaemon(false);
    return t;
  });

  public StorageAccess(final StorageInterface storageInterface) {
    this.storageInterface = storageInterface;
  }

  public CompletableFuture<Void> savePrisoner(final ApiPrisoner prisoner) {
    return submit(() -> this.storageInterface.savePrisoner(prisoner));
  }

  public CompletableFuture<Void> savePrisoners(final Map<UUID, ApiPrisoner> prisoners) {
    final Map<UUID, ApiPrisoner> copy = ImmutableMap.copyOf(prisoners);
    return submit(() -> this.storageInterface.savePrisoners(copy));
  }

  public CompletableFuture<Void> deletePrisoner(final ApiPrisoner prisoner) {
    return submit(() -> this.storageInterface.deletePrisoner(prisoner));
  }

  public CompletableFuture<Map<UUID, ApiPrisoner>> loadPrisoners() {
    return submit(this.storageInterface::loadPrisoners);
  }

  public CompletableFuture<Void> saveJail(final Jail jail) {
    return submit(() -> this.storageInterface.saveJail(jail));
  }

  public CompletableFuture<Void> saveJails(final Map<String, Jail> jails) {
    final Map<String, Jail> copy = ImmutableMap.copyOf(jails);
    return submit(() -> this.storageInterface.saveJails(copy));
  }

  public CompletableFuture<Void> deleteJail(final Jail jail) {
    return submit(() -> this.storageInterface.deleteJail(jail));
  }

  public CompletableFuture<Map<String, Jail>> loadJails() {
    return submit(this.storageInterface::loadJails);
  }

  @Override
  public void close() throws InterruptedException {
    this.ioExecutor.shutdown();
    if (!this.ioExecutor.awaitTermination(30L, TimeUnit.SECONDS)) {
      this.ioExecutor.shutdownNow();
    }
  }

  private CompletableFuture<Void> submit(final ThrowingRunnable task) {
    final CompletableFuture<Void> future = new CompletableFuture<>();
    this.ioExecutor.execute(() -> {
      try {
        task.run();
        future.complete(null);
      } catch (final Exception ex) {
        future.completeExceptionally(ex);
      }
    });
    return future;
  }

  private <T> CompletableFuture<T> submit(final Callable<T> task) {
    final CompletableFuture<T> future = new CompletableFuture<>();
    this.ioExecutor.execute(() -> {
      try {
        future.complete(task.call());
      } catch (final Exception ex) {
        future.completeExceptionally(ex);
      }
    });
    return future;
  }

  @FunctionalInterface
  private interface ThrowingRunnable {

    void run() throws Exception;
  }
}
