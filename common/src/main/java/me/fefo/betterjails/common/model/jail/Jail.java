package me.fefo.betterjails.common.model.jail;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import me.fefo.betterjails.common.model.cell.Cell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Jail {

    private final String name;
    private final LinkedHashMap<String, Cell> cells;

    public Jail(@NotNull final String name) {
        this.name = name;
        cells = new LinkedHashMap<>();
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull LinkedHashMap<String, Cell> getCells() {
        return cells;
    }

    public void addCell(@NotNull final Cell cell) {
        cells.put(cell.getName(), cell);
    }

    public void removeCell(@NotNull final String cellName) {
        cells.remove(cellName);
    }

    /**
     * This method gets a random cell to put a jailed player into.
     *
     * @return Null if there are no cells in the jail,
     * tries to return an unoccupied cell,
     * and if none are available, it returns a random occupied cell.
     */
    public @Nullable Cell getRandomCell() {
        SecureRandom sr = new SecureRandom(SecureRandom.getSeed(10));

        if (cells.isEmpty()) {
            System.out.println("Cells list is empty. No cells to give!");
            return null;
        }

        ArrayList<String> emptyCells = new ArrayList<>();
        String[] cellsArray = (String[]) cells.keySet().toArray();
        for (String cell : cellsArray) {
            if (!cells.get(cell).isOccupied()) {
                emptyCells.add(cell);
            }
        }

        if (emptyCells.isEmpty()) {
            return cells.get(cellsArray[sr.nextInt(cells.size())]);
        }
        String[] emptyCellsArray = (String[]) emptyCells.toArray();

        return cells.get(emptyCellsArray[sr.nextInt(emptyCells.size())]);
    }
}
