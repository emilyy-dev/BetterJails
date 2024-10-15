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

package io.github.emilyydev.betterjails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class UpdateChecker {

  private static final Logger LOGGER = LoggerFactory.getLogger("BetterJails");

  private static final int SPIGOTMC_RESOURCE_ID = 76001;
  private static final URL API_URL;

  static {
    URL apiUrl;
    try {
      apiUrl = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + SPIGOTMC_RESOURCE_ID);
    } catch (final MalformedURLException ex) {
      LOGGER.error(null, ex);
      apiUrl = null;
    }

    API_URL = apiUrl;
  }

  public static CompletableFuture<String> fetchRemoteVersion(final BetterJailsPlugin plugin) {
    if (API_URL == null) {
      // do not complete
      return new CompletableFuture<>();
    }

    final CompletableFuture<String> future = new CompletableFuture<>();
    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
      try {
        final String version;
        try (
            final InputStream stream = API_URL.openStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
        ) {
          version = reader.readLine();
        }

        future.complete(version);
      } catch (final IOException ex) {
        LOGGER.warn("An error occurred looking for plugin updates", ex);
        future.completeExceptionally(ex);
      }
    });

    return future.thenApplyAsync(Function.identity(), plugin);
  }

  private UpdateChecker() {
  }
}
