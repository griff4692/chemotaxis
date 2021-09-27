package chemotaxis.g3;

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

		//see highest in hiarchy color is sees in its space or one immediately adjacent:
		//(highest) blue, green, red (lowest)
		//set that chemical to chosen chemical type
		ChemicalType highest_priority = ChemicalType.RED;

		if(currentCell.getConcentration(ChemicalType.BLUE) != 0)
		{
			highest_priority = ChemicalType.BLUE;
		}
		else if(currentCell.getConcentration(ChemicalType.GREEN) != 0)
		{
			highest_priority = ChemicalType.GREEN;
		}

		for(DirectionType directionType : neighborMap.keySet())
		{
			if(neighborMap.get(directionType).getConcentration(ChemicalType.BLUE) != 0)
			{
				highest_priority = ChemicalType.BLUE;
			}
			else if(highest_priority != ChemicalType.BLUE &&
					(neighborMap.get(directionType).getConcentration(ChemicalType.GREEN) != 0))
			{
				highest_priority = ChemicalType.GREEN;
			}
		}

		ChemicalType chosenChemicalType = highest_priority;

		//note: if all the concentrations are zero it will move in the direction
		//of the last direction iterated through
		double highestConcentration = currentCell.getConcentration(chosenChemicalType);
		for (DirectionType directionType : neighborMap.keySet()) {
			if (highestConcentration <= neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
				highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
				move.directionType = directionType;
			}
		}
		return move;
	}
}