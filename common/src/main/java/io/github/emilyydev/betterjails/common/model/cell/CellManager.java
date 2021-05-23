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

package io.github.emilyydev.betterjails.common.model.cell;

import io.github.emilyydev.betterjails.common.plugin.abstraction.Location;
import io.github.emilyydev.betterjails.common.storage.StorageProvider;
import io.github.emilyydev.betterjails.common.util.Utils;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;
import java.util.Map;

public final class CellManager {

  private static final String VALID_CELL_NAME = "^[a-zA-Z0-9-_]{1,32}$";

  private final StorageProvider storage;
  private final Map<String, Cell> loadedCells = new Hashtable<>();

  public CellManager(final StorageProvider storage) {
    this.storage = storage;
  }

  /**
   * Creates and saves into memory mapping and storage a new cell location with the given name.
   *
   * @param name      The name of the cell location.
   * @param location  The location of said cell. This is where players will be teleported when jailed.
   * @param overwrite Should you overwrite an old location if a cell with that name already exists?
   * @return The newly created cell or the old cell <i>if and only if</i>
   * {@code overwrite} is {@code false} <i>and</i> a cell with that name already exists.
   * @throws IllegalArgumentException if either {@code name} or {@code location} are {@code null}.
   * @throws IllegalArgumentException if {@code name} doesn't match {@link #VALID_CELL_NAME}.
   */
  public @NotNull Cell createCell(@NotNull final String name,
                                  @NotNull final Location location,
                                  final boolean overwrite) throws IllegalArgumentException {
    Validate.notNull(name, "Cell name cannot be null");
    Validate.notNull(location, "Cell location cannot be null");

    final String sanitized = Utils.sanitize(name);
    if (!sanitized.matches(VALID_CELL_NAME)) {
      throw new IllegalArgumentException("Cell name \"" + name + "\" is not valid");
    }

    final Cell cell = new Cell(sanitized, location);

    if (overwrite) {
      this.loadedCells.put(sanitized, cell);
    } else {
      this.loadedCells.putIfAbsent(sanitized, cell);
    }

    return this.loadedCells.get(sanitized);
  }

  public @Nullable Cell getCell(@NotNull final String name) {
    Validate.notNull(name, "Cell name cannot be null");
    return this.loadedCells.get(Utils.sanitize(name));
  }
}
