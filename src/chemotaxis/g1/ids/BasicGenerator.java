package chemotaxis.g1.ids;

import chemotaxis.g1.GameCell;
import chemotaxis.g1.GameState;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.ChemicalPlacement;

import java.awt.*;
import java.util.ArrayList;

/**
 * Returns candidates for every valid cell and color combination.
 */
public class BasicGenerator implements IDSCandidateGenerator {
    public ArrayList<IDSCandidate> candidates(final GameState gs) {
        ArrayList<IDSCandidate> candidates = new ArrayList<>();
        GameCell[][] grid = gs.getGrid();
        for (int row = 0; row < grid.length; ++row) {
            for (int col = 0; col < gs.getGrid()[row].length; ++col) {
                if (grid[row][col].isBlocked()) {
                    continue;
                }
                ChemicalType[] chems = {ChemicalType.BLUE, ChemicalType.RED, ChemicalType.GREEN};
                for (ChemicalType color : chems) {
                    ChemicalPlacement p = new ChemicalPlacement();
                    p.location = new Point(row, col);
                    p.chemicals.add(color);
                    candidates.add(new IDSCandidate(p));
                }
            }
        }
        return candidates;
    }
}
