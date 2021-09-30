package chemotaxis.g9; // TODO modify the package name to reflect your team

import java.util.Map;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;

public class Agent extends chemotaxis.sim.Agent {

    /**
     * Agent constructor
     *
     * @param simPrinter  simulation printer
     *
     */
    public Agent(SimPrinter simPrinter) {
        super(simPrinter);
    }

    /**
     * Return number corresponding to each direction
     *
     * @param bestMove    move agent has decided to take
     * @return            number corresponding to agent's next move
     */
    public int direction(DirectionType bestMove) {
        if (bestMove == DirectionType.NORTH) {
            return 1;
        } else if (bestMove == DirectionType.EAST) {
            return 2;
        } else if (bestMove == DirectionType.SOUTH) {
            return 3;
        } else { // WEST
            return 4;
        }
    }

    /**
     * Return agent's next byte corresponding to "last move" and "don't look"
     *
     * @param move             move agent has decided to take
     * @param previousState    previous state of the agent
     * @param switchColor      should agent change color? if yes, increment next state's 10 multiplier
     * @return                 next move as an encoded byte
     */
    public byte nextMove(DirectionType move, Byte previousState, boolean switchColor) {
        int x = previousState / 10;

        if (switchColor) {
            x++;
            if (x == 3) {
                return (byte) direction(move);
            } else {
                return (byte) ((x * 10) + direction(move));
            }
        } else {
            System.out.println("Next move should be " + (byte)((x * 10) + direction(move)));
            return (byte) ((x * 10) + direction(move));
        }
    }

    /**
     * Return forbidden move
     *
     * @param previousState    previous state of the agent
     * @return                 forbidden move to be removed from neighborMap in for loop checks
     */
    public DirectionType forbiddenMove(Byte previousState) {
        int dir = previousState % 10;
        if (dir == 1) {
            return DirectionType.SOUTH;
        } else if (dir == 2) {
            return DirectionType.WEST;
        } else if (dir == 3) {
            return DirectionType.NORTH;
        } else if (dir == 4) {
            return DirectionType.EAST;
        } else {
            return null;
        }
    }

    /**
     * Finds the largest move for given color
     *
     * @param color            search color
     * @param currentCell      current cell
     * @param neighborMap      map of cell's neighbors
     * @param previousState    previous state of the agent
     * @return                 best direction to go next
     */
    public DirectionType findBestMove(ChemicalCell.ChemicalType color, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap, Byte previousState) {
        double highestCnt = currentCell.getConcentration(color);
        DirectionType highestDirection = null;

        DirectionType forbiddenDirection = forbiddenMove(previousState);
        if (forbiddenDirection != null) {
            neighborMap.remove(forbiddenDirection);
        }

        for (DirectionType directionType : neighborMap.keySet()) {
            double squareCnt = neighborMap.get(directionType).getConcentration(color);

            if (highestCnt < squareCnt) {
                highestCnt = squareCnt;
                highestDirection = directionType;
            }
        }

        return highestDirection;
    }

    /**
     * Move agent
     *
     * @param randomNum        random number available for agents
     * @param previousState    byte of previous state
     * @param currentCell      current cell
     * @param neighborMap      map of cell's neighbors
     * @return                 agent move
     *
     */
    @Override
    public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        // TODO add your code here to move the agent
        Move move = new Move();

        // if in any given state and see only larger values adjacent, stay in that state and move to larger
        // if in any given state and see only smaller values adjacent, agent reached local max so change color

        System.out.print("PREVIOUS STATE " + previousState + " ");
        int prevStateMul = previousState / 10;

        if (prevStateMul == 0) { // red
            DirectionType highestRedDirection = findBestMove(ChemicalCell.ChemicalType.RED, currentCell, neighborMap, previousState);
            System.out.println("HIGHEST RED " + highestRedDirection);

            if (highestRedDirection == null) {
                move.directionType = findBestMove(ChemicalCell.ChemicalType.GREEN, currentCell, neighborMap, previousState);
                move.currentState = nextMove(move.directionType, previousState, true);
            } else {
                move.directionType = highestRedDirection;
                move.currentState = nextMove(highestRedDirection, previousState, false);
            }
        } else if (prevStateMul == 1) { // green
            DirectionType highestGreenDirection = findBestMove(ChemicalCell.ChemicalType.GREEN, currentCell, neighborMap, previousState);
            System.out.println("HIGHEST GREEN " + highestGreenDirection);

            if (highestGreenDirection == null) {
                move.directionType = findBestMove(ChemicalCell.ChemicalType.BLUE, currentCell, neighborMap, previousState);
                move.currentState = nextMove(move.directionType, previousState, true);
            } else {
                move.directionType = highestGreenDirection;
                move.currentState = nextMove(highestGreenDirection, previousState, false);
            }
        } else if (prevStateMul == 2) { // blue
            DirectionType highestBlueDirection = findBestMove(ChemicalCell.ChemicalType.BLUE, currentCell, neighborMap, previousState);
            System.out.println("HIGHEST BLUE " + highestBlueDirection);

            if (highestBlueDirection == null) {
                move.directionType = findBestMove(ChemicalCell.ChemicalType.RED, currentCell, neighborMap, previousState);
                move.currentState = nextMove(move.directionType, previousState, true);
            } else {
                move.directionType = highestBlueDirection;
                move.currentState = nextMove(highestBlueDirection, previousState, false);
            }
        } else {}

        return move; // TODO modify the return statement to return your agent move
    }
}