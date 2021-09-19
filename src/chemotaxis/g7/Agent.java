package chemotaxis.g7;

import java.util.ArrayList;
import java.util.List;
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
     * @param simPrinter  simulation printer
     *
     */
    public Agent(SimPrinter simPrinter) {
        super(simPrinter);
    }

    /**
     * Move agent
     *
     * @param randomNum        random number available for agents
     * @param previousState    byte of previous state, bits 2-6 are a counter of the #rounds
     *                         the agent hasn't seen a 1.0 chemical in his cell, last 2 bits store the previous
     *                         direction (00: NORTH, 01: EAST, 10: SOUTH, 11: WEST)
     * @param currentCell      current cell
     * @param neighborMap      map of cell's neighbors
     * @return                 agent move
     *
     */

    private DirectionType getPrevDirection(Byte previousState) {
        /**
         * get previous direction from the stored byte
         */
        int previousDirectionBits = previousState % 4;
        System.out.println(previousDirectionBits);
        if (previousDirectionBits == 0) {
            return DirectionType.NORTH;
        }
        else if (previousDirectionBits == 1) {
            return DirectionType.EAST;
        }
        else if (previousDirectionBits == 2) {
            return DirectionType.SOUTH;
        }
        else {
            return DirectionType.WEST;
        }
    }

    private List<DirectionType> getOtherDirectionList(DirectionType previousDirection) {
        /**
         * get a list of the possible direction changes an agent can make.
         * first element indicates direction for turn right
         * second element indicates direction for turn left
         * third element indicates opposite direction
         */
        List<DirectionType> otherDirectionsList = new ArrayList<DirectionType>();

        if (previousDirection == DirectionType.NORTH) {
            otherDirectionsList.add(DirectionType.EAST);
            otherDirectionsList.add(DirectionType.WEST);
            otherDirectionsList.add(DirectionType.SOUTH);
        }
        else if (previousDirection == DirectionType.EAST) {
            otherDirectionsList.add(DirectionType.SOUTH);
            otherDirectionsList.add(DirectionType.NORTH);
            otherDirectionsList.add(DirectionType.WEST);
        }
        else if (previousDirection == DirectionType.SOUTH) {
            otherDirectionsList.add(DirectionType.WEST);
            otherDirectionsList.add(DirectionType.EAST);
            otherDirectionsList.add(DirectionType.NORTH);
        }
        else {
            otherDirectionsList.add(DirectionType.NORTH);
            otherDirectionsList.add(DirectionType.SOUTH);
            otherDirectionsList.add(DirectionType.EAST);
        }
        return otherDirectionsList;
    }

    private Integer getRoundsCounter(Byte previousState) {
        /**
         * get the number of rounds the agent hasn't been guided by the controller
         */
        int previousRoundsCounter = (previousState / 4) % 32;
        System.out.println(previousRoundsCounter);
        return previousRoundsCounter;
    }

    private byte setDirectionBitsInCurrentState(Byte previousState, DirectionType newDirection) {
        byte previousDirectionBits = (byte) (previousState % 4);
        byte newDirectionBits = 0;
        if (newDirection == DirectionType.NORTH) {
            newDirectionBits = 0;
        }
        else if (newDirection == DirectionType.EAST) {
            newDirectionBits = 1;
        }
        else if (newDirection == DirectionType.SOUTH) {
            newDirectionBits = 2;
        }
        else {
            newDirectionBits = 3;
        }
        byte newState = (byte) (previousState - previousDirectionBits + newDirectionBits);
        return newState;
    }

    private byte setCounterInCurrentState(Byte previousState, boolean increase) {
        int previousCounter = getRoundsCounter(previousState);
        byte newState = previousState;
        if (increase) {
            newState = (byte) (previousState + 4);
        }
        else {
            newState = (byte) (previousState - (previousCounter * 4));
        }
        return newState;
    }

    @Override
    public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        Move move = new Move();

        boolean sensedChemical = false;

        if (currentCell.getConcentration(ChemicalType.RED) == 1.0) {
            DirectionType newDirection = getOtherDirectionList(getPrevDirection(previousState)).get(0);
            move.currentState = setDirectionBitsInCurrentState(previousState, newDirection);
            move.directionType = newDirection;
            sensedChemical = true;
        }
        else if (currentCell.getConcentration(ChemicalType.GREEN) == 1.0) {
            DirectionType newDirection = getOtherDirectionList(getPrevDirection(previousState)).get(1);
            move.currentState = setDirectionBitsInCurrentState(previousState, newDirection);
            move.directionType = newDirection;
            sensedChemical = true;
        }
        else if (sensedChemical == false) {
            for (DirectionType directionType : neighborMap.keySet()) {
                if (neighborMap.get(directionType).getConcentration(ChemicalType.BLUE) == 1.0) {
                    move.currentState = setDirectionBitsInCurrentState(previousState, directionType);
                    move.directionType = directionType;
                    sensedChemical = true;
                }
            }
        }

        // TO DO: if counter = 31 then random step, else if not blocked move to the previous direction and increase counter. if blocked default behavior
        if (sensedChemical == false) {
            int rounds = getRoundsCounter(previousState);
            if (rounds < 31) {
                // TO DO: check if blocked
                // increase counter
            }
            else {
                // random walk
            }

        }

        return move;
    }
}
