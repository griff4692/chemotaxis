package chemotaxis.g11;

import java.util.Map;
import java.util.HashMap;

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
     * @param simPrinter  simulation printer
     *
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
     * @param randomNum        random number available for agents
     * @param previousState    byte of previous state
     * @param currentCell      current cell
     * @param neighborMap      map of cell's neighbors
     * @return                 agent move
     *
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

        /*
        ChemicalType chosenChemicalType = ChemicalType.BLUE;


        for (DirectionType directionType : neighborMap.keySet()) {
            if (neighborMap.get(directionType).getConcentration(chosenChemicalType) >= 0.99) {
                move.directionType = directionType;
                move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
            }
        }
        */

        Map<ChemicalType, Double> concentrations = currentCell.getConcentrations();
        double highestConcentration = getHighestConcentration(concentrations);
        double currentConcentration = highestConcentration;
        for (DirectionType directionType : neighborMap.keySet()) {
            Map<ChemicalType, Double> neighborConcentrations = neighborMap.get(directionType).getConcentrations();
            if (highestConcentration < getHighestConcentration(neighborConcentrations)) {
                highestConcentration = getHighestConcentration(neighborConcentrations);
                if (bitDirectionMap.get(directionType) + previousDirection == 3) {
                    continue;
                } else {
                    move.directionType = directionType;
                    move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
                }
            }
        }

        /*
        BLUE is SOUTH
        GREEN is EAST
        RED is NORTH
        GREEN + BLUE is WEST
         */

        if (highestConcentration > 0 && currentConcentration == highestConcentration) {
            if (concentrations.get(ChemicalType.BLUE) == highestConcentration && concentrations.get(ChemicalType.GREEN) == highestConcentration) {
                move.directionType = DirectionType.WEST;
                move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
            } else if (concentrations.get(ChemicalType.RED) == highestConcentration) {
                move.directionType = DirectionType.NORTH;
                move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
            } else if (concentrations.get(ChemicalType.GREEN) == highestConcentration) {
                move.directionType = DirectionType.EAST;
                move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
            } else if (concentrations.get(ChemicalType.BLUE) == highestConcentration) {
                move.directionType = DirectionType.SOUTH;
                move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
            }
        }

        if ( move.directionType == DirectionType.CURRENT ) {
            if ( previousDirection == 0)
            { move.directionType = DirectionType.SOUTH; }
            else if (previousDirection == 1)
            {move.directionType = DirectionType.EAST; }
            else if (previousDirection == 2)
            {move.directionType = DirectionType.WEST; }
            else { move.directionType = DirectionType.NORTH; }
        }

        return move;
    }
}