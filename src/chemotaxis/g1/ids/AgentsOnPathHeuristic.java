package chemotaxis.g1.ids;

import chemotaxis.g1.AgentLoc;
import chemotaxis.g1.GameState;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Very simple heuristic that scores a game state by taking the number
 * of chemicals remaining and subtracting one for each agent off of the
 * path agents are supposed to be following.
 */
public class AgentsOnPathHeuristic implements IDSHeuristic {
    private HashSet<Point> pathPoints;

    public AgentsOnPathHeuristic(ArrayList<Point> path) {
        this.pathPoints = new HashSet<>(path);
    }

    @Override
    public double evaluate(GameState gameState) {
        double score = gameState.getChemicalsRemaining();
        for (AgentLoc loc : gameState.getAgents()) {
            if (this.pathPoints.contains(loc.loc)) {
                continue;
            }
            score -= 5;
        }
        return score;
    }
}
