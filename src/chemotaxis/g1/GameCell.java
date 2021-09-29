package chemotaxis.g1;

import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.DirectionType;

import java.awt.*;
import java.util.Objects;

public class GameCell {
    public boolean occupied;
    public ChemicalCell cell;

    /**
     * Copy constructor
     *
     * @param priorCell
     */
    GameCell(GameCell priorCell) {
        this.occupied = priorCell.occupied;
        this.cell = GameCell.cloneChemicalCell(priorCell.cell);
    }

    /**
     * Default constructor from ChemicalCell
     *
     * @param priorCell
     */
    GameCell(ChemicalCell priorCell) {
        this.occupied = false;
        this.cell = GameCell.cloneChemicalCell(priorCell);
    }

    public boolean isBlocked() {
        return this.cell.isBlocked();
    }

    // ChemicalCell doesn't have a proper clone method
    public static ChemicalCell cloneChemicalCell(final ChemicalCell priorCell) {
        ChemicalCell newCell = new ChemicalCell(priorCell.isOpen());
        ChemicalCell.ChemicalType[] chems = {ChemicalCell.ChemicalType.BLUE, ChemicalCell.ChemicalType.RED, ChemicalCell.ChemicalType.GREEN};
        for (ChemicalCell.ChemicalType c : chems) {
            newCell.setConcentration(c, priorCell.getConcentration(c));
        }
        return newCell;
    }

    /**
     * Attenuates concentrations < 0.001 to 0
     *
     * @param priorCell
     * @return
     */
    public static ChemicalCell cloneAttenuatedChemicalCell(final ChemicalCell priorCell) {
        double MIN_DETECTABLE_CONCENTRATION = 0.001;
        ChemicalCell newCell = new ChemicalCell(priorCell.isOpen());
        ChemicalCell.ChemicalType[] chems = {ChemicalCell.ChemicalType.BLUE, ChemicalCell.ChemicalType.RED, ChemicalCell.ChemicalType.GREEN};
        for (ChemicalCell.ChemicalType c : chems) {
            double conc = priorCell.getConcentration(c);
            if (conc < MIN_DETECTABLE_CONCENTRATION) {
                conc = 0;
            }
            newCell.setConcentration(c, conc);
        }
        return newCell;
    }

    // Comparison method for chemical cells
    public static boolean chemCellEquals(ChemicalCell a, ChemicalCell b) {
        ChemicalCell.ChemicalType[] chems = {ChemicalCell.ChemicalType.BLUE, ChemicalCell.ChemicalType.RED, ChemicalCell.ChemicalType.GREEN};
        for (ChemicalCell.ChemicalType c : chems) {
            if (!Objects.equals(a.getConcentration(c), b.getConcentration(c))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a point representing a 1-step move in the direction `dir`.
     * <p>
     * NOTE: Points are not validate and may be out of bounds.
     * Separate bounds checking is required.
     *
     * @param current
     * @param dir
     * @return
     */
    public static Point pointInDirection(final Point current, final DirectionType dir) {
        switch (dir) {
            case CURRENT:
                return new Point(current);
            case NORTH:
                return new Point(current.x - 1, current.y);
            case SOUTH:
                return new Point(current.x + 1, current.y);
            case WEST:
                return new Point(current.x, current.y - 1);
            case EAST:
                return new Point(current.x, current.y + 1);
        }
        throw new RuntimeException("unexpected DirectionType enum");
    }
}
