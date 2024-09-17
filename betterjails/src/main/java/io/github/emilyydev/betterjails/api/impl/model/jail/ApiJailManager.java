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

package io.github.emilyydev.betterjails.api.impl.model.jail;

import com.github.fefo.betterjails.api.model.jail.Jail;
import com.github.fefo.betterjails.api.model.jail.JailManager;
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import io.github.emilyydev.betterjails.data.JailDataHandler;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public final class ApiJailManager implements JailManager {

  private final JailDataHandler jailData;

  public ApiJailManager(final JailDataHandler jailData) {
    this.jailData = jailData;
  }

  @Override
  @SuppressWarnings("ConstantConditions")
  public @NotNull Jail createAndSaveJail(final @NotNull String name, final @NotNull Location location)
      throws IllegalArgumentException {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(location, "location");

    if (this.jailData.getJail(name) != null) {
      throw new IllegalArgumentException("name");
    }

    try {
      this.jailData.addJail(name, ImmutableLocation.copyOf(location)).get();
    } catch (final InterruptedException ex) {
      // bleh
    } catch (final ExecutionException ex) {
      throw new RuntimeException(ex.getCause());
    }

    return this.jailData.getJail(name);
  }

  @Override
  public @Nullable Jail getJail(final @NotNull String name) {
    Objects.requireNonNull(name, "name");
    return this.jailData.getJail(name);
  }

  @Override
  public void deleteJail(final @NotNull Jail jail) {
    Objects.requireNonNull(jail, "jail");

    try {
      this.jailData.removeJail(jail).get();
    } catch (final InterruptedException ex) {
      // bleh
    } catch (final ExecutionException ex) {
      throw new RuntimeException(ex.getCause());
    }
  }

  @Override
  public @NotNull @UnmodifiableView Collection<@NotNull Jail> getAllJails() {
    return Collections.unmodifiableCollection(this.jailData.getJails().values());
  }
}
