package chemotaxis.g1.ids;

import chemotaxis.sim.ChemicalCell;

import java.awt.*;
import java.util.ArrayList;

public interface IDSCandidateGenerator {
    public ArrayList<IDSCandidate> candidates(Integer currentTurn, Integer chemicalsRemaining,
                                       ArrayList<Point> locations, ChemicalCell[][] grid);
}
