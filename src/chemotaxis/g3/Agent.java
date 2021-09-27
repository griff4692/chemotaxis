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



		/*
		Behavior 1: Doesn't detect anything -- go in a spiral
		 */

		/*
		Behavior 2: Detect that agent is "stuck" somewhere


		1. Predict concentration of all 3 colors (note that if a given cell as (0,0,0), it is considered a block)

		2a. If last 3 digits of prediction < 256 (i.e. 0.3123)
			- Store last three digits (i.e. 123)

		2b If last 3 digits of prediction > 256 (i.e. 0.4567)
			- Store last two digits (i.e. 67)

		 */

		/*
		0. Compare prediction (stored in previousState) to current cell.
			- If it's a match we're stuck -- move on to some random behavior.
			- If not do new prediction with steps below.
			- Initial value of previousState = 0
		 */

		/*
		double currentConcentration = currentCell.getConcentration(ChemicalType.RED) +
									currentCell.getConcentration(ChemicalType.GREEN) +
									currentCell.getConcentration(ChemicalType.BLUE);

		if (currentConcentration == previousState) {

			System.out.println("prediction matches");

		} else {

			System.out.println("prediction doesn't match");
		}*/

		System.out.println("Random Num: ");
		System.out.println(Math.abs(randomNum%4));

		//note: if all the concentrations are zero it will move in the direction
		//of the last direction iterated through
		double highestConcentration = currentCell.getConcentration(chosenChemicalType);
		int zero_count = 0;
		for (DirectionType directionType : neighborMap.keySet()) {
			if (neighborMap.get(directionType).getConcentration(chosenChemicalType) == 0) {
				zero_count++;
				System.out.println(zero_count);
			}
			if (highestConcentration <= neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
				highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
				move.directionType = directionType;
			}
		}

		/* all surrounding cells have no chemical
		Direction based on randNum%4
			- 0: right
			- 1: down
			- 2: left
			- 3: up
		*/
		if (zero_count == 4) {
			if (Math.abs(randomNum)%4 == 0) {
				move.directionType = DirectionType.EAST;
			} else if (Math.abs(randomNum)%4 == 1) {
				move.directionType = DirectionType.SOUTH;
			} else if (Math.abs(randomNum)%4 == 2) {
				move.directionType = DirectionType.WEST;
			} else if (Math.abs(randomNum)%4 == 3) {
				move.directionType = DirectionType.NORTH;
			}
		}
		return move;
	}
}