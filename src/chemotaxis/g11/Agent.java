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
         11: up
         00: down
         01: right
         10: left
        */
        HashMap<DirectionType, Integer> bitDirectionMap = new HashMap<DirectionType, Integer>();
        bitDirectionMap.put(DirectionType.NORTH, 0b11);
        bitDirectionMap.put(DirectionType.SOUTH, 0b00);
        bitDirectionMap.put(DirectionType.WEST, 0b10);
        bitDirectionMap.put(DirectionType.EAST, 0b01);

        Move move = new Move();
        move.currentState = previousState;
        Integer previousDirection = previousState & 0b11;


        ChemicalType priorityChemicalType = ChemicalType.BLUE;


        for (DirectionType directionType : neighborMap.keySet()) {
            if (neighborMap.get(directionType).getConcentration(priorityChemicalType) >= 0.99) {
                move.directionType = directionType;
                move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
            }
        }
        if (move.directionType == DirectionType.CURRENT) {
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
            int position = randomNum % possibledirections.size();
            move.directionType = possibledirections.get(position);
            move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);

        }

        /*
        BLUE is SOUTH
        GREEN is EAST
        RED is NORTH
        GREEN + BLUE is WEST
         */


        return move;
    }
}
