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

		/*
		* if prev == 0 look at blue;
		* Check current colour. If reached maxima, turn to other colour.
		* If both colours zero, follow green.
		* */

		/*
		* previousstate = 0 ==> first iteration. Set LSB to 1
		* LSB == 011 ==> Currently following BLUE
		* LSB == 101 ==> Currently following RED
		* 2nd bit == 1001 ==> GREEN
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
		else
			chosenChemicalType = ChemicalType.GREEN;


		double highestConcentration = currentCell.getConcentration(chosenChemicalType);
		double previousColourConcentration;

		for (DirectionType directionType : neighborMap.keySet()) {
			if (highestConcentration <= neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
				highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
				move.directionType = directionType;
			}
		}

		if (highestConcentration >= currentCell.getConcentration(chosenChemicalType) && )
			;
		else{
			// we have reached maximum. Change colour
			previousColourConcentration = highestConcentration;
			if((previousState & 3) == 3){
				chosenChemicalType = ChemicalType.RED;
				previousState = 5;

				for (DirectionType directionType : neighborMap.keySet()) {
					if (highestConcentration <= neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
						highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
						move.directionType = directionType;
					}
				}
			}
			else if ((previousState & 5) == 5){
				chosenChemicalType = ChemicalType.BLUE;
				previousState = 3;

				for (DirectionType directionType : neighborMap.keySet()) {
					if (highestConcentration <= neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
						highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
						move.directionType = directionType;
					}
				}
			}
			else
		}

		move.currentState = previousState;

		return move;
	}



}
