package chemotaxis.g1;

import java.util.Map;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;

public class Agent extends chemotaxis.sim.Agent {

    /**
     * Agent constructor
     *
     * @param simPrinter simulation printer
     */
    public Agent(SimPrinter simPrinter) {
        super(simPrinter);
    }

    /**
     * Move agent
     *
     * @param randomNum     random number available for agents
     * @param previousState byte of previous state
     * @param currentCell   current cell
     * @param neighborMap   map of cell's neighbors
     * @return agent move
     */
    @Override
    public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        final AgentState prevState = new AgentState(previousState);
        Move move = new Move();

        ChemicalType chosenChemicalType = ChemicalType.BLUE;

        double highestConcentration = currentCell.getConcentration(chosenChemicalType);
        for (DirectionType directionType : neighborMap.keySet()) {
            if (highestConcentration <= neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
                highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
                move.directionType = directionType;
            }
        }
        return move;
    }

    /**
     * handleWall alters the agent's next direction to account for collisions with walls.
     * <p>
     * This routine should be called after the agent has selected its next direction.
     * If moving in the selected direction is impossible since the square is blocked,
     * this routine alters the direction according to the following rules:
     * <p>
     * 1. If possible, turn right of the currently selected direction (i.e. if the agent wants
     * to move south, try to move west instead).
     * 2. If turning right is impossible, turn left.
     * 3. If right and left are blocked, go in the opposite direction of the agent's selected
     * direction.
     * <p>
     * Importantly, the agent's state is NOT updated to reflect the newly chosen direction. This
     * means that barring external stimuli, the agent will resume moving in its previously desired
     * direction as soon as it becomes unblocked.
     * <p>
     * For example, imagine the agent wanted to move south, was blocked, and moved right instead.
     * On its next turn, if south is unblocked, the agent's default action will be to move south.
     *
     * @param nextMove    Desired next move corresponding AgentState as a byte.
     * @param neighborMap Map of agent's immediate surroundings.
     * @param prevState   Agent's previous state. Used to decode "current" to a cardinal direction
     * @return
     */
    private Move handleWall(Move nextMove, final Map<DirectionType, ChemicalCell> neighborMap, final AgentState prevState) {
        CardinalDirection nextDirection = prevState.asCardinalDir(nextMove.directionType);

        if (neighborMap.get(nextDirection.asDirectionType()).isBlocked()) {
            // Try right first, then left, then reverse
            DirectionType right = nextDirection.rightOf().asDirectionType();
            DirectionType left = nextDirection.leftOf().asDirectionType();
            if (neighborMap.get(right).isOpen()) {
                nextMove.directionType = right;
            } else if (neighborMap.get(left).isOpen()) {
                nextMove.directionType = left;
            } else {
                nextMove.directionType = nextDirection.reverseOf().asDirectionType();
            }
        }
        return nextMove;
    }
}