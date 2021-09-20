package chemotaxis.g7;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
     * @param previousState    byte of previous state, bits 2-5 are a counter of the #rounds
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
        return previousRoundsCounter;
    }

    private byte setDirectionBitsInCurrentState(Byte previousState, DirectionType newDirection) {
        /**
         * update last 2 bits of the state to indicate the new direction of the agent
         */
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
        /**
         * update the counter stored in the state to keep track of the #rounds the agent hasn't sensed chemicals
         */
        int previousCounter = getRoundsCounter(previousState);
        byte newState = previousState;
        if (increase) {
            if (previousCounter < 31) {
                newState = (byte) (previousState + 4);
            }
            else {
                newState = 0;
            }
        }
        else {
            newState = (byte) (previousState + 4 - (byte) (previousCounter * 4));
        }
        return newState;
    }

    private List<DirectionType> getPossibleDirections(Map<DirectionType, ChemicalCell> neighborMap) {
        /**
         * return all possible directions (the ones that aren't blocked)
         */
        List<DirectionType> possibleDirections = new ArrayList<>();
        for (Map.Entry<DirectionType, ChemicalCell> neighborCell: neighborMap.entrySet()) {
            ChemicalCell cell = neighborCell.getValue();
            if (cell.isOpen()) {
                possibleDirections.add(neighborCell.getKey());
            }
        }
        return possibleDirections;
    }

    private DirectionType circumventBlocks(Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        /**
         * indicates which direction the agent should go in case he meets a block
         */
        DirectionType previousDirection = getPrevDirection(previousState);

        DirectionType rightDirection = getOtherDirectionList(previousDirection).get(0);
        ChemicalCell rightCell = neighborMap.get(rightDirection);
        DirectionType leftDirection = getOtherDirectionList(previousDirection).get(1);
        ChemicalCell leftCell = neighborMap.get(leftDirection);
        DirectionType oppositeDirection = getOtherDirectionList(previousDirection).get(2);
        ChemicalCell oppositeCell = neighborMap.get(oppositeDirection);

        if (!(rightCell.isBlocked())) {
            return rightDirection;
        }
        else if (!(leftCell.isBlocked())) {
            return leftDirection;
        }
        else {
            return oppositeDirection;
        }
    }

    private DirectionType randomStep(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        /**
         * randomly chooses the direction the agent should go if he needs to do a random step
         * - if the agent can move only to one direction then by default moves to that direction
         * - if there are more directions then we exclude the opposite of the previous direciton so that the agent
         *   won't make a "circle" and then we use the random number to generate a random idex for the direction
         *   the agent should move to
         */
        DirectionType previousDirection = getPrevDirection(previousState);
        List<DirectionType> possibleDirections = getPossibleDirections(neighborMap);
        if (possibleDirections.size() == 1) {
            return possibleDirections.get(0);
        }
        else {
            if (possibleDirections.contains(getOtherDirectionList(previousDirection).get(2))) {
                possibleDirections.remove(getOtherDirectionList(previousDirection).get(2));
            }
            Random rand = new Random(randomNum);
            int randomIndex = rand.nextInt(possibleDirections.size());
            return possibleDirections.get(randomIndex);
        }
    }

    @Override
    public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        /**
         * Next move functionality (order indicates priority of each scenario):
         * - if there is red chemical in the current block then turn right
         * - if there is green chemical in the current block then turn left
         * - if there is blue chemical in one of the neighboring cells then move to that direction
         * - if there is no chemical continue moving to the same direction unless
         *      1) the next cell is blocked, in that case use default behavior to circumvent blocks
         *      2) the agent hasn't sensed any chemical in 31 rounds, in that case the agent should do random steps till
         *         he senses a chemical
         */

        Move move = new Move();
        DirectionType previousDirection = getPrevDirection(previousState);

        boolean sensedChemical = false;

        if (currentCell.getConcentration(ChemicalType.RED) == 1.0) {
            DirectionType newDirection = getOtherDirectionList(previousDirection).get(0);
            move.currentState = setCounterInCurrentState(previousState, false);
            move.currentState = setDirectionBitsInCurrentState(move.currentState, newDirection);
            move.directionType = newDirection;
            sensedChemical = true;
        }
        else if (currentCell.getConcentration(ChemicalType.GREEN) == 1.0) {
            DirectionType newDirection = getOtherDirectionList(previousDirection).get(1);
            move.currentState = setCounterInCurrentState(previousState, false);
            move.currentState = setDirectionBitsInCurrentState(move.currentState, newDirection);
            move.directionType = newDirection;
            sensedChemical = true;
        }
        else if (sensedChemical == false) {
            for (DirectionType directionType : neighborMap.keySet()) {
                if (neighborMap.get(directionType).getConcentration(ChemicalType.BLUE) == 1.0) {
                    move.currentState = setCounterInCurrentState(previousState, false);
                    move.currentState = setDirectionBitsInCurrentState(move.currentState, directionType);
                    move.directionType = directionType;
                    sensedChemical = true;
                }
            }
        }

        if (sensedChemical == false) {
            int rounds = getRoundsCounter(previousState);
            if (rounds == 0) {
                move.directionType = randomStep(randomNum, previousState, currentCell, neighborMap);
                move.currentState = setDirectionBitsInCurrentState(previousState, move.directionType);
            }
            else if (rounds <= 31) {
                ChemicalCell nextChemicalCell = neighborMap.get(previousDirection);
                if (nextChemicalCell.isBlocked()) {
                    move.directionType = circumventBlocks(previousState, currentCell, neighborMap);
                }
                else {
                    move.directionType = previousDirection;
                }
                move.currentState = setCounterInCurrentState(previousState, true);
                move.currentState = setDirectionBitsInCurrentState(move.currentState, move.directionType);
            }

        }

        return move;
    }
}
