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

package io.github.emilyydev.betterjails.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class FileIO {

  private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor(task -> {
    final Thread t = new Thread(task, "BetterJails I/O Thread");
    t.setPriority(Thread.MIN_PRIORITY);
    t.setDaemon(false);
    return t;
  });

  public static CompletableFuture<Void> writeString(final Path file, final String string) {
    final CompletableFuture<Void> future = new CompletableFuture<>();
    IO_EXECUTOR.execute(() -> {
      try {
        Files.write(file, string.getBytes(StandardCharsets.UTF_8));
        future.complete(null);
      } catch (final IOException ex) {
        future.completeExceptionally(ex);
      }
    });
    return future;
  }

  public static void shutdown() throws InterruptedException {
    IO_EXECUTOR.shutdown();
    IO_EXECUTOR.awaitTermination(30L, TimeUnit.SECONDS);
  }

  private FileIO() {
  }
}
