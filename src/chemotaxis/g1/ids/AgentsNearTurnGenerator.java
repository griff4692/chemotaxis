package chemotaxis.g1.ids;

import chemotaxis.g1.AgentLoc;
import chemotaxis.g1.GameState;
import chemotaxis.sim.ChemicalPlacement;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Generator that returns chemical placements at turning points if agents will reach them
 * within a certain number of turns without any interactions. The chemical type of the
 * generated placement will match the follow color of the agent when it reaches the turn point.
 *
 * Also returns the "no placement" candidate since that's always a good option to consider.
 */
public class AgentsNearTurnGenerator implements IDSCandidateGenerator {
    private HashMap<Point, Point> turnJunctions;
    private int lookahead;

    /**
     * @param turnJunctions Hash map of `junction` -> `placement` where `junction` is the
     *                      point the agent reaches when a chemical needs to be placed
     *                      at `placement`.
     *                      <p>
     *                      For example, imagine that when an agent reaches the `junction` (1,1)
     *                      a chemical must be placed at `placement` (2,1).
     * @param lookaheadTurns Number of turns to look ahead
     */
    AgentsNearTurnGenerator(HashMap<Point, Point> turnJunctions, int lookaheadTurns) {
        this.turnJunctions = turnJunctions;
        this.lookahead = lookaheadTurns;
    }


    @Override
    public ArrayList<IDSCandidate> candidates(final GameState gameState) {
        HashSet<IDSCandidate> candidates = new HashSet<>();
        // The "do nothing" placement
        candidates.add(new IDSCandidate(new ChemicalPlacement()));

        // Step forward without placing chemicals to see where the agents end up
        ArrayList<GameState> states = stepForward(this.lookahead, gameState);
        for (GameState state : states) {
            for (AgentLoc agent : state.getAgents()) {
                if (this.turnJunctions.containsKey(agent.loc)) {
                    ChemicalPlacement cp = new ChemicalPlacement();
                    cp.location = this.turnJunctions.get(agent.loc);
                    cp.chemicals.add(agent.state.getFollowColor());
                    candidates.add(new IDSCandidate(cp));
                }
            }
        }
        return new ArrayList<>(candidates);
    }

    private ArrayList<GameState> stepForward(int steps, final GameState gs) {
        ArrayList<GameState> states = new ArrayList<>();
        for (int i = 0; i < steps; ++i) {
            // Step forward without placing any chemicals
            states.add(gs.placeChemicalAndStep(new ChemicalPlacement()));
        }
        return states;
    }
}
