package chemotaxis.g1.ids;

import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;

import java.awt.*;
import java.util.ArrayList;

/**
 * Returns candidates for every valid cell and color combination.
 */
public class BasicGenerator implements IDSCandidateGenerator {
    public ArrayList<IDSCandidate> candidates(Integer currentTurn, Integer chemicalsRemaining,
                                              ArrayList<Point> locations, ChemicalCell[][] grid) {
        ArrayList<IDSCandidate> candidates = new ArrayList<>();
        for (int row = 0; row < grid.length; ++row) {
            for (int col = 0; col < grid[row].length; ++col) {
                if (grid[row][col].isBlocked()) {
                    continue;
                }
                ChemicalType[] chems = {ChemicalType.BLUE, ChemicalType.RED, ChemicalType.GREEN};
                for (ChemicalType color : chems) {
                    candidates.add(new IDSCandidate(row, col, color));
                }
            }
        }
        return candidates;
    }
}
