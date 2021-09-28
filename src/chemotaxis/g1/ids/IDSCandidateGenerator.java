package chemotaxis.g1.ids;

import chemotaxis.g1.GameState;
import chemotaxis.sim.ChemicalPlacement;

import java.util.ArrayList;

public interface IDSCandidateGenerator {
    ArrayList<ChemicalPlacement> candidates(final GameState gameState);
}
