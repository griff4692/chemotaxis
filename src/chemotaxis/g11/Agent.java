package chemotaxis.g11;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList ;
import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;
import chemotaxis.sim.ChemicalCell.ChemicalType;



public class Agent extends chemotaxis.sim.Agent {
    /**
     * Agent constructor
     *
     * @param simPrinter simulation printer
     */
    public Agent(SimPrinter simPrinter) {
        super(simPrinter);
    }

    public Double getHighestConcentration(Map<ChemicalType, Double> concentrations) {
        return Math.max(Math.max(concentrations.get(ChemicalType.RED), concentrations.get(ChemicalType.BLUE)), concentrations.get(ChemicalType.GREEN));
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
        /* WE suppose that for the direction we use the last 2 bits
        of the byte and we set the default mapping as stated below:
         11: NORTH
         00: SOUTH
         01: EAST
         10: WEST
        */
        HashMap<DirectionType, Integer> bitDirectionMap = new HashMap<DirectionType, Integer>();
        bitDirectionMap.put(DirectionType.NORTH, 0b11);
        bitDirectionMap.put(DirectionType.SOUTH, 0b00);
        bitDirectionMap.put(DirectionType.WEST, 0b10);
        bitDirectionMap.put(DirectionType.EAST, 0b01);

        Move move = new Move();

        Boolean hasSeenBlue= (previousState>4 || previousState<0);
        if (hasSeenBlue) {
            previousState = (byte) (previousState - 128);
        }
        move.currentState = previousState;
        Integer previousDirection = previousState & 0b11;


        ChemicalType priorityChemicalType = ChemicalType.BLUE;

        for (DirectionType directionType : neighborMap.keySet()) {
            if (neighborMap.get(directionType).getConcentration(priorityChemicalType) >= 0.99) {
                move.directionType = directionType;
                move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
                hasSeenBlue = true;
            } else {
                double highestConcentration = currentCell.getConcentration(ChemicalType.GREEN);
                //double currentConcentration = highestConcentration;
                double highest2 = 0.0;
                if (neighborMap.get(directionType).getConcentration(ChemicalType.GREEN) > highestConcentration) {
                    if (bitDirectionMap.get(directionType) + previousDirection == 3) {
                        continue;
                    } else {
                        if (!hasSeenBlue) {
                            move.directionType = directionType;
                            highestConcentration = neighborMap.get(directionType).getConcentration(ChemicalType.GREEN);
                            move.directionType = directionType;
                            move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
                        }
                    }
                }
            }
        }


        if (move.directionType == DirectionType.CURRENT && !hasSeenBlue) {
            ArrayList<DirectionType> possibledirections = new ArrayList<DirectionType>();
            for (DirectionType directionType : neighborMap.keySet()) {
                if (neighborMap.get(directionType).isOpen()) {
                    possibledirections.add(directionType);
                }
            }
            if (possibledirections.size() > 1) {
                if (previousState == 0 && possibledirections.contains(DirectionType.NORTH)) {
                    possibledirections.remove(DirectionType.NORTH);
                } else if (previousState == 1 && possibledirections.contains(DirectionType.WEST)) {
                    possibledirections.remove(DirectionType.WEST);
                } else if (previousState == 2 && possibledirections.contains(DirectionType.EAST)) {
                    possibledirections.remove(DirectionType.EAST);
                } else if (previousState == 3 && possibledirections.contains(DirectionType.SOUTH)) {
                    possibledirections.remove(DirectionType.SOUTH);
                }
            }
            int position = Math.abs(randomNum % possibledirections.size());
            move.directionType = possibledirections.get(position);
            move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
        } else if (move.directionType == DirectionType.CURRENT) {
            if (previousState == 0) {
                move.directionType = DirectionType.SOUTH;
            } else if (previousState == 1) {
                move.directionType = DirectionType.EAST;
            } else if (previousState == 2) {
                move.directionType = DirectionType.WEST;
            } else if (previousState == 3) {
                move.directionType = DirectionType.NORTH;
            }
        }

        if (hasSeenBlue) {
            move.currentState = (byte) (move.currentState + 128) ;
        }


        return move;
    }
}
