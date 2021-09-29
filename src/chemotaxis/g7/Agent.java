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
     *                         direction (00: NORTH, 01: EAST, 10: SOUTH, 11: WEST), bit 7 will indicate whether the
     *                         agent has started moving in the right direction or not
     * @param currentCell      current cell
     * @param neighborMap      map of cell's neighbors
     * @return                 agent move
     *
     */
    private int getDirectionBits(Byte previousState) {
        int previousDirectionBits;
        if (previousState < 0) {
            previousDirectionBits = (previousState + 128) % 4;
        } else {
            previousDirectionBits = previousState % 4;
        }
        return previousDirectionBits;
    }


    private DirectionType getPrevDirection(Byte previousState) {
        /**
         * get previous direction from the stored byte
         */
        int previousDirectionBits = getDirectionBits(previousState);
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
        int previousDirectionBits = getDirectionBits(previousState);
        int previousRoundsCounter;
        if (previousState < 0) {
            previousRoundsCounter = ((previousState + 128) - previousDirectionBits) / 4;
        } else {
            previousRoundsCounter = previousState / 4;
        }
        return previousRoundsCounter;
    }

    private Integer getRandomWalkBit(Byte previousState) {
        /**
         * get the first bit of the state
         */
        if (previousState < 0) {
            return 1;
        }
        else {
            return 0;
        }
    }

    private byte setDirectionBitsInCurrentState(Byte previousState, DirectionType newDirection) {
        /**
         * update last 2 bits of the state to indicate the new direction of the agent
         */
        int previousDirectionBits = getDirectionBits(previousState);
        byte newState;
        if (previousState < 0)
            newState = (byte) (previousState & ((byte) (127)));
//        previousState = (byte) (previousState & ((byte) (127)));
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
        newState = (byte) (previousState - previousDirectionBits + newDirectionBits);
        if (previousState < 0)
            newState = (byte) (newState | (-128));
        return newState;
    }

    private byte setCounterInCurrentState(Byte previousState, boolean increase) {
        /**
         * update the counter stored in the state to keep track of the #rounds the agent hasn't sensed chemicals
         */
        int previousCounter = getRoundsCounter(previousState);
        byte newState = previousState;
        if (previousState < 0)
            newState = (byte) (previousState & ((byte) (127)));
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
        if (previousState < 0)
            newState = (byte) (newState | (-128));
        return newState;
    }

    private byte setRandomWalkBitInCurrentState(Byte previousState) {
        /**
         * update the first bit stored in the state so that the agent knows whether he has started moving to the right
         * direction after leaving starting point
         */
        byte newState = (byte) (previousState | -128);
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
        ChemicalCell previousDirectionCell = neighborMap.get(previousDirection);
        DirectionType rightDirection = getOtherDirectionList(previousDirection).get(0);
        ChemicalCell rightCell = neighborMap.get(rightDirection);
        DirectionType leftDirection = getOtherDirectionList(previousDirection).get(1);
        ChemicalCell leftCell = neighborMap.get(leftDirection);
        DirectionType oppositeDirection = getOtherDirectionList(previousDirection).get(2);
        ChemicalCell oppositeCell = neighborMap.get(oppositeDirection);

        if  (!(previousDirectionCell.isBlocked())) {
            return previousDirection;
        }
        else if (!(rightCell.isBlocked())) {
            return rightDirection;
        }
        else if (!(leftCell.isBlocked())) {
            return leftDirection;
        }
        else {
            return oppositeDirection;
        }
    }

    private DirectionType circumventBlocksForBlue(Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        /**
         * indicates which direction the agent should go in case he meets a block
         */
        DirectionType previousDirection = getPrevDirection(previousState);
        ChemicalCell previousDirectionCell = neighborMap.get(previousDirection);
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
        else if (!oppositeCell.isBlocked()){
            return oppositeDirection;
        } else {
            return previousDirection;
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

        Move move = new Move();
        DirectionType previousDirection = getPrevDirection(previousState);

        boolean sensedChemical = false;

        if (getRandomWalkBit(previousState) == 0) {
            double maxConcentration = 0.0;
            DirectionType maxConcentrationDirection = previousDirection;
            for (DirectionType directionType : neighborMap.keySet()) {
                if (neighborMap.get(directionType).getConcentration(ChemicalType.BLUE) > maxConcentration) {
                    maxConcentration = neighborMap.get(directionType).getConcentration(ChemicalType.BLUE);
                    maxConcentrationDirection = directionType;
                }
            }
            if (maxConcentration > 0.001) {
                System.out.println("Agent is in the start cell --> found blue!!!");
                move.currentState = setRandomWalkBitInCurrentState(previousState);
                move.currentState = setDirectionBitsInCurrentState(move.currentState, maxConcentrationDirection);
                move.currentState = setCounterInCurrentState(move.currentState, false);
                move.directionType = maxConcentrationDirection;
                sensedChemical = true;
            }
            if (sensedChemical == false) {
                // if in the start position, agent only has one way to go, set random bit to 1
                List<DirectionType> possibleDirections = getPossibleDirections(neighborMap);
                if (possibleDirections.size() == 1) {
                    move.directionType = possibleDirections.get(0);
                    move.currentState = setRandomWalkBitInCurrentState(previousState);
                    move.currentState = setCounterInCurrentState(move.currentState, false);
                    move.currentState = setDirectionBitsInCurrentState(move.currentState, move.directionType);
                } else {
                    System.out.println("Agent is in the start cell --> random walk!!!");
                    move.directionType = randomStep(randomNum, previousState, currentCell, neighborMap);
                    move.currentState = setDirectionBitsInCurrentState(previousState, move.directionType);
                }
            }
        }
        else {
            double redConcentration = 0.0;
            double greenConcentration = 0.0;
            double blueConcentration = 0.0;

            if (currentCell.getConcentration(ChemicalType.BLUE) > 0.001 &&
                    currentCell.getConcentration(ChemicalType.BLUE) > neighborMap.get(previousDirection).getConcentration(ChemicalType.BLUE) &&
                    currentCell.getConcentration(ChemicalType.BLUE) > neighborMap.get(getOtherDirectionList(previousDirection).get(2)).getConcentration(ChemicalType.BLUE)) {
                blueConcentration = currentCell.getConcentration(ChemicalType.BLUE);
            }

            if (currentCell.getConcentration(ChemicalType.RED) > 0.001 &&
                    currentCell.getConcentration(ChemicalType.RED) > neighborMap.get(previousDirection).getConcentration(ChemicalType.RED) &&
                    currentCell.getConcentration(ChemicalType.RED) > neighborMap.get(getOtherDirectionList(previousDirection).get(2)).getConcentration(ChemicalType.RED)) {
                redConcentration = currentCell.getConcentration(ChemicalType.RED);
            }

            if (currentCell.getConcentration(ChemicalType.GREEN) > 0.001 &&
                    currentCell.getConcentration(ChemicalType.GREEN) > neighborMap.get(previousDirection).getConcentration(ChemicalType.GREEN) &&
                    currentCell.getConcentration(ChemicalType.GREEN) > neighborMap.get(getOtherDirectionList(previousDirection).get(2)).getConcentration(ChemicalType.GREEN)) {
                greenConcentration = currentCell.getConcentration(ChemicalType.GREEN);
            }

            if (redConcentration > greenConcentration && redConcentration > blueConcentration) {
                System.out.println("red local maximum");
                System.out.println(redConcentration);
                System.out.println(greenConcentration);
                System.out.println(blueConcentration);
                System.out.println("previous Direction:" + previousDirection.toString());
                DirectionType newDirection = getOtherDirectionList(previousDirection).get(0);
                System.out.println("newDirection:" + newDirection.toString());
                ChemicalCell nextChemicalCell = neighborMap.get(newDirection);
                if (nextChemicalCell.isBlocked()) {
                    System.out.println("next cell is blocked");
                    newDirection = circumventBlocks(previousState, currentCell, neighborMap);
                    System.out.println(previousState + "newDirection:" + newDirection.toString());
                }
                move.currentState = setCounterInCurrentState(previousState, false);
                move.currentState = setDirectionBitsInCurrentState(move.currentState, newDirection);
                move.directionType = newDirection;
                sensedChemical = true;
            } else if (greenConcentration > redConcentration && greenConcentration > blueConcentration) {
                System.out.println("green local maximum");
                System.out.println(redConcentration);
                System.out.println(greenConcentration);
                System.out.println(blueConcentration);
                System.out.println("previous Direction:" + previousDirection.toString());
                DirectionType newDirection = getOtherDirectionList(previousDirection).get(1);
                System.out.println("newDirection:" + newDirection.toString());
                ChemicalCell nextChemicalCell = neighborMap.get(newDirection);
                if (nextChemicalCell.isBlocked()) {
                    System.out.println("next cell is blocked");
                    newDirection = circumventBlocks(previousState, currentCell, neighborMap);
                }
                move.currentState = setCounterInCurrentState(previousState, false);
                move.currentState = setDirectionBitsInCurrentState(move.currentState, newDirection);
                move.directionType = newDirection;
                sensedChemical = true;
            }
            else if (blueConcentration > redConcentration && blueConcentration > greenConcentration) {
                System.out.println("blue local maximum");
                System.out.println(redConcentration);
                System.out.println(greenConcentration);
                System.out.println(blueConcentration);
                System.out.println("previous Direction:" + previousDirection.toString());
                DirectionType newDirection = getOtherDirectionList(previousDirection).get(2);
                System.out.println("newDirection:" + newDirection.toString());
                ChemicalCell nextChemicalCell = neighborMap.get(newDirection);
                if (nextChemicalCell.isBlocked()) {
                    System.out.println("next cell is blocked");
                    newDirection = circumventBlocksForBlue(previousState, currentCell, neighborMap);
                    System.out.println("newDirection:" + newDirection.toString());
                }
                move.currentState = setCounterInCurrentState(previousState, false);
                move.currentState = setDirectionBitsInCurrentState(move.currentState, newDirection);
                move.directionType = newDirection;
                sensedChemical = true;
            } else if ((blueConcentration == redConcentration && blueConcentration > 0) ||
                    (blueConcentration == greenConcentration && blueConcentration > 0) ||
                    (redConcentration == greenConcentration && redConcentration > 0)) {
                move.currentState = setCounterInCurrentState(previousState, false);
                move.currentState = setDirectionBitsInCurrentState(move.currentState, previousDirection);
                move.directionType = previousDirection;
                sensedChemical = true;
            }

            if (sensedChemical == false) {
                int rounds = getRoundsCounter(previousState);
                if (rounds == 0) {
                    List<DirectionType> possibleDirections = getPossibleDirections(neighborMap);
                    if (possibleDirections.size() == 1) {
                        move.directionType = possibleDirections.get(0);
                        move.currentState = setCounterInCurrentState(move.currentState, false);
                        move.currentState = setDirectionBitsInCurrentState(move.currentState, move.directionType);
                    } else {
                        System.out.println("random step");
                        move.directionType = randomStep(randomNum, previousState, currentCell, neighborMap);
                        move.currentState = setDirectionBitsInCurrentState(previousState, move.directionType);
                    }
                } else if (rounds <= 31) {
                    ChemicalCell nextChemicalCell = neighborMap.get(previousDirection);
                    if (nextChemicalCell.isBlocked()) {
                        System.out.println("next cell is blocked");
                        move.directionType = circumventBlocks(previousState, currentCell, neighborMap);
                        move.currentState = setDirectionBitsInCurrentState(previousState, move.directionType);
                        boolean shouldIncreaseCounter = true;
                        if (move.directionType == getOtherDirectionList(previousDirection).get(2)) {
                            shouldIncreaseCounter = false;
                        }
                        else if (move.directionType != getOtherDirectionList(previousDirection).get(2) && (getPossibleDirections(neighborMap).size() == 2)) {
                            shouldIncreaseCounter = false;
                        }
                        if (shouldIncreaseCounter) {
                            move.currentState = setCounterInCurrentState(move.currentState, true);
                        }
                    } else {
                        System.out.println("continue moving in the previous direction:" + previousDirection);
                        move.directionType = previousDirection;
                        move.currentState = setCounterInCurrentState(previousState, true);
                        move.currentState = setDirectionBitsInCurrentState(move.currentState, move.directionType);
                    }
                }
            } else {
                move.currentState = setRandomWalkBitInCurrentState(move.currentState);
            }
            System.out.println("sense chemical:" + sensedChemical);
            System.out.println("blue" + blueConcentration + "green:" + greenConcentration + "red:" + redConcentration);
            System.out.println("previous:" + previousDirection + "direction:"+ move.directionType);
        }

        return move;
    }
}
