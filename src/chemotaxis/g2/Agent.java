package chemotaxis.g2;

import java.util.Map;
import java.lang.Math;

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
		ChemicalType chosenChemicalType;
		Boolean dirChanged = false;

		/*
		* if prev == 0 look at blue;
		* Check current colour. If reached maxima, turn to other colour.
		* If both colours zero, follow green.
		* */


		/*
		* previousstate = 0 ==> first iteration. Set LSB to 1
		* LSB == 011 ==> Currently following BLUE
		* LSB == 101 ==> Currently following RED
		* LSB == 1001 ==> GREEN
		* */

		if(previousState == 0){
			previousState = 3;
		}

		if((previousState & 3) == 3){
			chosenChemicalType = ChemicalType.BLUE;
		}
		else if ((previousState & 5) == 5){
			chosenChemicalType = ChemicalType.RED;
		}
		else {
			chosenChemicalType = ChemicalType.GREEN;
		}


		double highestConcentration = currentCell.getConcentration(chosenChemicalType);
		double previousColourConcentration;

		for (DirectionType directionType : neighborMap.keySet()) {
			if (highestConcentration <= neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
				highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
				move.directionType = directionType;
				dirChanged = true;
			}
		}

		if(chosenChemicalType == ChemicalType.GREEN){
			move.currentState = previousState;
			return move;
		}

		if (dirChanged == false){
			// we have reached maximum. Change colour
			previousColourConcentration = highestConcentration;

			if((previousState & 3) == 3){
				chosenChemicalType = ChemicalType.RED;
				previousState = 5;

				highestConcentration = currentCell.getConcentration(chosenChemicalType);

				for (DirectionType directionType : neighborMap.keySet()) {
					if (highestConcentration <= neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
						highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
						move.directionType = directionType;
						dirChanged = true;
					}
				}
			}
			else if ((previousState & 5) == 5){
				chosenChemicalType = ChemicalType.BLUE;
				previousState = 3;

				highestConcentration = currentCell.getConcentration(chosenChemicalType);

				for (DirectionType directionType : neighborMap.keySet()) {
					if (highestConcentration <= neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
						highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
						move.directionType = directionType;
						dirChanged = true;
					}
				}
			}


			if (dirChanged == false){
				// then we have encountered maxima for both colours. Check if red and blue have 0 concentrations
				if (checkZeroConcentration(neighborMap) == 1){
					chosenChemicalType = ChemicalType.GREEN;
					previousState = 9;

					highestConcentration = currentCell.getConcentration(chosenChemicalType);

					for (DirectionType directionType : neighborMap.keySet()) {
						if (highestConcentration <= neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
							highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
							move.directionType = directionType;
						}
					}
				}
			}
		}


		move.currentState = previousState;

		return move;
	}

	public int checkZeroConcentration(Map<DirectionType, ChemicalCell> neighborMap){
		int blueZero = 0, redZero = 0;

		for (DirectionType directionType : neighborMap.keySet()) {
			if (neighborMap.get(directionType).getConcentration(ChemicalType.BLUE) > 0.001) {
				blueZero = 1;
			}

			if (neighborMap.get(directionType).getConcentration(ChemicalType.RED) > 0.001){
				redZero = 1;
			}
		}

		if (redZero == 0 && blueZero == 0)
			return 1;
		else
			return 0;

	}




}

