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
        AgentState newState = new AgentState(prevState);
        DirectionType previousDirection = prevState.getDirection().asDirectionType();
        Move move = new Move();

        // Get new direction indicated through RED chemical
        DirectionType newDirection = getHighestConcentrationDirection(neighborMap, ChemicalType.RED, previousDirection);

        // If RED doesn't indicate a new direction, check direction indicated through blue chemical
        if (newDirection == previousDirection) {
            newDirection = getHighestConcentrationDirection(neighborMap, ChemicalType.BLUE, previousDirection);
        }

        // Check to see if the agent is trying to go backwards
        CardinalDirection newCardinalDirection = newState.asCardinalDir(newDirection);
        if (newCardinalDirection.reverseOf().asDirectionType() == previousDirection) {
            newDirection = previousDirection;
        }
        move.directionType = newDirection;

        // This is a noop if the new direction is unblocked
        move.directionType = handleWall(move, neighborMap, prevState);

        // TODO (etm): This stores the previous direction, but it doesn't really store the
        //   intended direction. So, if we want the agent to resume in the intended direction
        //   as soon as it can (no more wall blocking), we need to store the intended direction
        //   in the agent state as well.
        newState.setDirection(move.directionType);
        move.currentState = newState.serialize();
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
     * @return new direction to take
     */
    private DirectionType handleWall(Move nextMove, final Map<DirectionType, ChemicalCell> neighborMap, final AgentState prevState) {
        // Use prevState to decode `nextMode.directionType` in case it is the `CURRENT` variant
        CardinalDirection nextDirection = prevState.asCardinalDir(nextMove.directionType);

        if (neighborMap.get(nextDirection.asDirectionType()).isBlocked()) {
            // Try right first, then left, then reverse
            DirectionType right = nextDirection.rightOf().asDirectionType();
            DirectionType left = nextDirection.leftOf().asDirectionType();
            if (neighborMap.get(right).isOpen()) {
                return right;
            } else if (neighborMap.get(left).isOpen()) {
                return left;
            } else {
                return nextDirection.reverseOf().asDirectionType();
            }
        }
        // Not blocked, return direction the agent wanted to go in
        return nextMove.directionType;
    }

    /**
     * Gets the direction of highest concentration of a particular chemical in the agent's surroundings
     * <p>
     * 1. If the highest concentration is in two different directions, then the function just returns the previous direction
     * 2. If the highest concentration is 0 (i.e. no chemical of that color is present in the neighbourhood), then the
     * function will just return the previous direction
     *
     * @param neighborMap       Map of agent's immediate surroundings.
     * @param chemicalType      Color of the chemical whose highest concentration direction is to be calculated
     * @param previousDirection Agent's previous direction
     * @return new direction to take
     */
    public DirectionType getHighestConcentrationDirection(Map<DirectionType, ChemicalCell> neighborMap, ChemicalType chemicalType, DirectionType previousDirection) {
        DirectionType newDirection = previousDirection;
        double highestConcentration = 0;

        // Get absolute highest value of concentration of particular chemical nearby
        for (DirectionType directionType : neighborMap.keySet()) {
            double temp = neighborMap.get(directionType).getConcentration(chemicalType);
            if (highestConcentration < temp) {
                highestConcentration = temp;
                newDirection = directionType;
            }
        }

        // Check if the same highest is present in two different directions, in which case move same way as previous
        if (newDirection != previousDirection) {
            for (DirectionType directionType : neighborMap.keySet()) {
                double temp = neighborMap.get(directionType).getConcentration(chemicalType);
                if (highestConcentration == temp && newDirection != directionType)
                    newDirection = previousDirection;
            }
        }

        // If there is no chemical, move same way as previous
        if (highestConcentration == 0)
            newDirection = previousDirection;
        return newDirection;
    }
}