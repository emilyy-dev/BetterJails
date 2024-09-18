//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2024 emilyy-dev
// Copyright (c) 2024 Emilia Kond
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
import com.github.fefo.betterjails.api.util.ImmutableLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ApiJail implements Jail {

  private final String name;
  private volatile ImmutableLocation location;
  private volatile ImmutableLocation releaseLocation;

  public ApiJail(final String name, final ImmutableLocation location, final ImmutableLocation releaseLocation) {
    this.name = name;
    this.location = location;
    this.releaseLocation = releaseLocation;
  }

  @Override
  public @NotNull ImmutableLocation location() {
    return this.location;
  }

  @Override
  public void location(final @NotNull ImmutableLocation location) {
    Objects.requireNonNull(location, "location");
    this.location = location;
  }

  @Override
  public @Nullable ImmutableLocation releaseLocation() {
    return this.releaseLocation;
  }

  @Override
  public void releaseLocation(final @Nullable ImmutableLocation location) {
    this.releaseLocation = location;
  }

  @Override
  public @NotNull String name() {
    return this.name;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) { return true; }
    if (other == null || other.getClass() != this.getClass()) { return false; }
    final ApiJail that = (ApiJail) other;
    return this.name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public String toString() {
    return "Jail["
        + "name=" + this.name
        + ", location=" + this.location
        + ", releaseLocation=" + this.releaseLocation
        + ']';
  }
}
