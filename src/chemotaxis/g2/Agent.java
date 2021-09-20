package chemotaxis.g2;

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
	@Override
	public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
		Move move = new Move();

		ChemicalType chosenChemicalType = ChemicalType.BLUE;

		double highestConcentration = currentCell.getConcentration(chosenChemicalType);
		double currentConcentration = highestConcentration;
		double result;
		double minDetectableConcentration = 0.001;	/* Would have done a #define, but can't. CAUTION! Change if minimum detectable concentration changes. */
		
		for (DirectionType directionType : neighborMap.keySet()) {
			if (highestConcentration < neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
				highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
				move.directionType = directionType;
			}
		}
		
		result = highestConcentration - currentConcentration;
		if (result < minDetectableConcentration)
			/* Then all squares have ~equal concentration. Choose a random move. */
			switch (randomNum % 5){
				case 0:
					move.directionType = DirectionType.EAST;
					break;
				case 1:
					move.directionType = DirectionType.WEST;
					break;
				case 2:
					move.directionType = DirectionType.NORTH;
					break;
				case 3:
					move.directionType = DirectionType.SOUTH;
					break;
				case 4:
					move.directionType = DirectionType.CURRENT;
					break;
			}

		return move;
	}
}
