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
        AgentState prevState;
        if (previousState == null) {
            prevState = new AgentState();
        } else {
            prevState = new AgentState(previousState);
        }

        Move move;
        if (prevState.isInitialized()) {
            if (prevState.getStrategy() == AgentState.Strategy.WEAK)
                move = weakFollowStrategy(prevState, neighborMap);
            else
                move = strongFollowStrategy(prevState, neighborMap);
        }
        else
            move = initialize(prevState, neighborMap, currentCell);

        prevState.setDirection(move.directionType);
        move.currentState = prevState.serialize();

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
        DirectionType nxt = nextDirection.asDirectionType();

        if (!neighborMap.containsKey(nxt) || neighborMap.get(nxt).isBlocked()) {
            // Try right first, then left, then reverse
            DirectionType right = nextDirection.rightOf().asDirectionType();
            DirectionType left = nextDirection.leftOf().asDirectionType();
            if (neighborMap.containsKey(right) && neighborMap.get(right).isOpen()) {
                return right;
            } else if (neighborMap.containsKey(left) && neighborMap.get(left).isOpen()) {
                return left;
            } else {
                return nextDirection.reverseOf().asDirectionType();
            }
        }
        // Not blocked, return direction the agent wanted to go in
        return nextDirection.asDirectionType();
    }

    public Move initialize(AgentState prevState, Map<DirectionType, ChemicalCell> neighborMap, ChemicalCell currentCell) {
        Move move = new Move();
        if (isChemicalNearby(neighborMap, currentCell, ChemicalType.GREEN)) {
            if (isChemicalNearby(neighborMap, currentCell, ChemicalType.RED)) {
                prevState.setStrategy(AgentState.Strategy.WEAK);
                prevState.setInitialized();
            }
            else {
                DirectionType nextDirection = DirectionType.CURRENT;
                for (DirectionType directionType : neighborMap.keySet()) {
                    double temp = neighborMap.get(directionType).getConcentration(ChemicalType.GREEN);
                    if (temp == 1) {
                        nextDirection = directionType;
                        prevState.setInitialized();
                        break;
                    }
                }
                move.directionType = nextDirection;
            }
        }
        else {
            prevState.setStrategy(AgentState.Strategy.WEAK);
            prevState.setInitialized();
        }

        if (prevState.getStrategy() == AgentState.Strategy.WEAK){
            DirectionType nextDirection = getHighestConcentrationDirectionWeak(ChemicalType.BLUE, neighborMap);
            if (nextDirection != null) {
                move.directionType = nextDirection;
                prevState.changeFollowColor(ChemicalType.BLUE);
            }
            move.directionType = handleWall(move, neighborMap, prevState);
        }

        return move;
    }

    public boolean isChemicalNearby(Map<DirectionType, ChemicalCell> neighborMap, ChemicalCell currentCell, ChemicalType chemicalType) {
        if (currentCell.getConcentration(chemicalType) > 0)
            return true;

        for (DirectionType directionType : neighborMap.keySet())
            if (neighborMap.get(directionType).getConcentration(chemicalType) > 0)
                return true;

        return false;
    }

    public Move weakFollowStrategy(AgentState prevState, Map<DirectionType, ChemicalCell> neighborMap) {
        Move move = new Move();
        ChemicalType followColor = prevState.getFollowColor();
        DirectionType nextDirection = getHighestConcentrationDirectionWeak(followColor, neighborMap);
        DirectionType previousDirection = prevState.getDirection().asDirectionType();
        DirectionType reverseDirection = prevState.getDirection().reverseOf().asDirectionType();

        if (nextDirection == previousDirection || nextDirection == reverseDirection || nextDirection == null)
            nextDirection = previousDirection;
        else
            prevState.changeFollowColor(followColor);

        // TODO : Can we modify handleWall to remove dependency on `move`?
        move.directionType = nextDirection;
        move.directionType = handleWall(move, neighborMap, prevState);

        return move;
    }

    public DirectionType getHighestConcentrationDirectionWeak(ChemicalType chemicalType, Map<DirectionType, ChemicalCell> neighborMap) {
        double highestConcentration = 0;
        DirectionType newDirection = null;

        // Get absolute highest value of concentration of particular chemical nearby
        for (DirectionType directionType : neighborMap.keySet()) {
            double temp = neighborMap.get(directionType).getConcentration(chemicalType);
            if (highestConcentration < temp) {
                highestConcentration = temp;
                newDirection = directionType;
            }
        }

        // Check if the same highest is present in two different directions, in which case return none
        if (newDirection != null) {
            for (DirectionType directionType : neighborMap.keySet()) {
                double temp = neighborMap.get(directionType).getConcentration(chemicalType);
                if (highestConcentration == temp && newDirection != directionType)
                    return null;
            }
            // Check the concentration of follow color in next cell compared to other chemicals
            if (!checkCellConcentrations(chemicalType, neighborMap.get(newDirection)))
                return  null;
        }

        return newDirection;
    }

    // For agent to follow a color, that color should have the highest concentration in it's cell
    public boolean checkCellConcentrations(ChemicalType followColor, ChemicalCell nextCell) {
        Map<ChemicalType, Double> concentrationMap = nextCell.getConcentrations();
        double highestConcentration = concentrationMap.get(followColor);

        for (ChemicalType color : concentrationMap.keySet()) {
            double temp = concentrationMap.get(color);
            if (temp > highestConcentration)
                return false;
        }

        return true;
    }

    public Move strongFollowStrategy(AgentState prevState, Map<DirectionType, ChemicalCell> neighborMap) {
        Move move = new Move();
        DirectionType nextDirection = prevState.getDirection().asDirectionType();

        for (DirectionType directionType : neighborMap.keySet()) {
            double temp = neighborMap.get(directionType).getConcentration(ChemicalType.BLUE);
            boolean isReverse = prevState.asCardinalDir(directionType) == prevState.getDirection().reverseOf();
            if (temp == 1 && !isReverse) {
                nextDirection = directionType;
                break;
            }
        }

        move.directionType = nextDirection;
        move.directionType = handleWall(move, neighborMap, prevState);

        return move;
    }

}