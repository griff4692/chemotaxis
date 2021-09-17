package chemotaxis.g11;
package chemotaxis.dummy;

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
     * @param previousState    byte of previous state
     * @param currentCell      current cell
     * @param neighborMap      map of cell's neighbors
     * @return                 agent move
     *
     */
    HashMap<DirectionType, Integer> bitDirectionMap = new HashMap<DirectionType, Integer>();
    bitDirectionMap.put(DirectionType.NORTH, 0b11);
    bitDirectionMap.put(DirectionType.SOUTH, 0b00);
    bitDirectionMap.put(DirectionType.WEST, 0b10);
    bitDirectionMap.put(DirectionType.EAST, 0b11);
	@Override
	public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
		
        HashMap<DirectionType, Integer> bitDirectionMap = new HashMap<DirectionType, Integer>();
        bitDirectionMap.put(DirectionType.NORTH, 0b11);
        bitDirectionMap.put(DirectionType.SOUTH, 0b00);
        bitDirectionMap.put(DirectionType.WEST, 0b10);
        bitDirectionMap.put(DirectionType.EAST, 0b11);
        
        Move move = new Move();


		ChemicalType chosenChemicalType = ChemicalType.BLUE;

		// double highestConcentration = currentCell.getConcentration(chosenChemicalType);
        
        //
		for (DirectionType directionType : neighborMap.keySet()) {
			if (neighborMap.get(directionType).getConcentration(chosenChemicalType)==1.0) 
            {
				// highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
				move.directionType = directionType;
                move.currentState = bitDirectionMap.get(move.directionType) | previousState; 
              
			}
		}

        /* WE suppose that for the direction we use the last 2 bits of the byte and we set the default mapping as stated below
        * 11: up
        * 00: down
        * 01: right
        * 10: left
        */
        Integer previousDirection = previousState & 0b11;
        if ( move.directionType == 'CURRENT' ) { 

            if ( previousDirection == 0)
            { move.directionType = DirectionType.SOUTH } 
            else if (previousDirection == 1)
             {move.directionType = DirectionType. EAST} 
            else if (previousDirection == 2)
             {move.directionType = DirectionType.WEST } 
            else { move.directionType = DirectionType.NORTH} 
        }
        

		return move;
	}
}