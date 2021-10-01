package chemotaxis.g1;

import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalPlacement;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RallypointLayout {
    private static final int DIFFUSION_DEPTH = 25;
    private static final ChemicalCell.ChemicalType SIM_COLOR = ChemicalCell.ChemicalType.BLUE;

    ArrayList<Point> rallyPoints;
    // Map from rally point -> set of points where a single chemical placement can diffuse after 25 turns
    HashMap<Point, HashSet<Point>> rallyPointRange;

    public RallypointLayout(ArrayList<Point> points, HashMap<Point, HashSet<Point>> ranges) {
        this.rallyPoints = points;
        this.rallyPointRange = ranges;
    }

    /**
     * Calculates points on the shortest path where rallypoints should be placed for
     * the weak2 strategy.
     *
     * @param path List of points from start to target
     * @return
     */
    public static RallypointLayout calculateLayout(ArrayList<Point> path, ChemicalCell[][] grid) {
        if (path == null || path.size() == 0) {
            return null;
        }
        int targetIx = path.size() - 1;

        ArrayList<Point> rallypoints = new ArrayList<>();
        HashMap<Point, HashSet<Point>> rallypointRanges = new HashMap<>();
        int currentIx = 0;
        while (currentIx != targetIx) {
            for (int i = currentIx + 1; i < path.size(); ++i) {
                // Only params that matter are start, target, and grid (and of course, chemicals >= 1)
                GameState gs = new GameState(path.get(0), path.get(targetIx), 0, 0, 1, grid);

                ChemicalPlacement cp = new ChemicalPlacement();
                cp.location = path.get(i);
                // Color doesn't matter for calculating diffusion
                cp.chemicals.add(SIM_COLOR);

                gs = gs.placeChemicalAndStep(cp);

                for (int t = 0; t < DIFFUSION_DEPTH; ++t) {
                    // Do nothing placement, just step for diffusion
                    gs = gs.placeChemicalAndStep(new ChemicalPlacement());
                }

                HashSet<Point> range = calculateRange(gs.getGrid());

                if (range.contains(path.get(currentIx))) {
                    // Store the range here so that when we find the first point that
                    // cannot reach the current RP we have already cached the range for
                    // the previous point.
                    rallypointRanges.put(path.get(currentIx), range);
                    if (currentIx == targetIx) {
                        // Can't look any further... target becomes rally point
                        rallypoints.add(path.get(currentIx));
                    }
                } else {
                    // We cannot reach the current rallypoint from this new one
                    // So use the previous one we inspected as the next rally point
                    currentIx = i - 1;
                    rallypoints.add(path.get(currentIx));
                    // Already stored the range of this point on the previous iteration
                }
            }
        }

        return new RallypointLayout(rallypoints, rallypointRanges);
    }

    private static HashSet<Point> calculateRange(GameCell[][] grid) {
        HashSet<Point> range = new HashSet<>();
        for (int row = 0; row < grid.length; ++row) {
            for (int col = 0; col < grid[row].length; ++col) {
                ChemicalCell cell = grid[row][col].cell;
                if (cell.getConcentration(SIM_COLOR) > GameCell.MIN_DETECTABLE_CONCENTRATION) {
                    range.add(new Point(row, col));
                }
            }
        }
        return range;
    }
}
