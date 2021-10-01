package chemotaxis.g1.ids;

import chemotaxis.g1.GameState;
import chemotaxis.sim.ChemicalPlacement;

import java.util.ArrayList;

class IDSState {
    public final long timeLimit = 900;
    public long startTime;
    // 900 leaves 100 ms to finish and return the result
    public int depthLimit;

    IDSState() {
        this.startTime = System.currentTimeMillis();
        this.depthLimit = 0;
    }

    /**
     * Copy constructor
     *
     * @param other
     */
    IDSState(IDSState other) {
        this.startTime = other.startTime;
        this.depthLimit = other.depthLimit;
    }
}

class ScoredPlacement {
    public double score;
    public ChemicalPlacement placement;

    ScoredPlacement(double score, ChemicalPlacement placement) {
        this.score = score;
        this.placement = placement;
    }

    /**
     * Score only constructor - this is used when the IDS bottoms out
     *
     * @param score
     */
    ScoredPlacement(double score) {
        this.score = score;
        this.placement = null;
    }
}

public class IDSRunner {
    // Search from the current position and return the best next move for the controller
    public static ChemicalPlacement search(GameState gameState,
                                           IDSCandidateGenerator generator, IDSHeuristic heuristic) {
        IDSState state = new IDSState();
        ScoredPlacement currentBest = new ScoredPlacement(Double.MIN_VALUE, new ChemicalPlacement());
        while (true) {
            state.depthLimit += 1;
            ScoredPlacement move = IDSRunner.findBest(state, gameState, generator, heuristic);
            if (move != null && currentBest.score < move.score) {
                currentBest = move;
            } else {
                break;
            }
        }
        return currentBest.placement;
    }

    private static ScoredPlacement findBest(IDSState state, GameState gameState,
                                            IDSCandidateGenerator generator, IDSHeuristic heuristic) {
        if (state.startTime + state.timeLimit < System.currentTimeMillis()) {
            // Timed out
            return null;
        } else if (state.depthLimit == 0) {
            // Bottomed out; score game and return score
            return new ScoredPlacement(heuristic.evaluate(gameState));
        }

        state.depthLimit -= 1;

        ArrayList<IDSCandidate> candidates = generator.candidates(gameState);
        ScoredPlacement currentBest = new ScoredPlacement(Double.MIN_VALUE, new ChemicalPlacement());
        for (IDSCandidate ic : candidates) {
            ChemicalPlacement c = ic.getPlacement();
            GameState nextGameState = gameState.placeChemicalAndStep(c);
            ScoredPlacement move = IDSRunner.findBest(state, nextGameState, generator, heuristic);
            if (move != null && currentBest.score < move.score) {
                move.placement = c;
                currentBest = move;
            } else {
                // Timed out; return null to unwind the recursion
                currentBest = null;
                break;
            }
        }
        state.depthLimit += 1;
        return currentBest;
    }
}
