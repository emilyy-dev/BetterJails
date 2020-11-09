package me.fefo.betterjails.common.model.cell;

import me.fefo.betterjails.common.abstraction.Location;
import me.fefo.betterjails.common.storage.StorageProvider;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;
import java.util.Map;

import static me.fefo.betterjails.common.util.Utils.sanitize;

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

    final String sanitized = sanitize(name);
    if (!sanitized.matches(VALID_CELL_NAME)) {
      throw new IllegalArgumentException("Cell name \"" + name + "\" is not valid");
    }

    final Cell cell = new Cell(sanitized, location);

    if (overwrite) {
      loadedCells.put(sanitized, cell);
    } else {
      loadedCells.putIfAbsent(sanitized, cell);
    }

    return loadedCells.get(sanitized);
  }

  public @Nullable Cell getCell(@NotNull final String name) {
    Validate.notNull(name, "Cell name cannot be null");
    return loadedCells.get(sanitize(name));
  }
}
