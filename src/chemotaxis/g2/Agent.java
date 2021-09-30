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
		int firstTurn = 0;


		/*
		 * Byte previousState : 3 LSBs hold previous direction
		 * 001 : North
		 * 010 : South
		 * 011 : East
		 * 100 : West
		 * 101 : Current
		 * 110 : Seen red. Ignore Blue?
		 *
		 *
		 * MSB 1 is on : red - green strategy on
		 * MSB 2 is on: currently red
		 * MSB 3 is on : currently green
		 * */


		if (previousState == 0)
			firstTurn = 1;

		chosenChemicalType = ChemicalType.BLUE;
		double highestConcentration;
		double minDetectableConcentration = 0.001;	/* Would have done a #define, but can't. CAUTION! Change if minimum detectable concentration
		 changes. */

		boolean turn = false;
		int colour;

		System.out.println("checking for blue");
		for (DirectionType directionType : neighborMap.keySet()) {
			double a = neighborMap.get(directionType).getConcentration(chosenChemicalType);
			System.out.println(a + " directionType: " + directionType);
			if (Math.abs(a - 1.0) < minDetectableConcentration ) {
				System.out.println("Im here");
				move.directionType = directionType;
				turn = true;
				previousState = (byte)((previousState & 248) | storeDir(directionType));
				move.currentState = previousState;
				break;

			}
		}
		System.out.println("Out of blue loop");
		if(!turn){
			// No blue found.

			if(previousState == 0){
				//first turn and no blue . So follow red-green strategy, start with red
				System.out.println("No blue found and first turn");
				previousState = (byte)(128 | 64) ;
			}
			else if ((previousState & 128) == 0){
				// no blue found, but strategy is follow the turns
				move.directionType = findPreviousState(previousState);
				move.currentState = previousState;
				System.out.println("No blue found but strategy is follow the turns");

				return move;
			}

		}
		else{
			// blue found but red-green on, so we turned. Return.
			// or, blue found and follow the turn strategy. Done here, Return.
			return move;
		}

		if((previousState & 128) == 128) {
			//red-green is on, follow the gradient

			colour = findRedOrGreen(previousState);
			if (colour == 1)
				chosenChemicalType = ChemicalType.RED;
			else
				chosenChemicalType = ChemicalType.GREEN;

			highestConcentration = currentCell.getConcentration(chosenChemicalType);
			double previousColourConcentration;

			System.out.println("Strategy red-green : " + chosenChemicalType + " current conc : " + highestConcentration);

			for (DirectionType directionType : neighborMap.keySet()) {
				double b = neighborMap.get(directionType).getConcentration(chosenChemicalType);
				System.out.println(1 + " " + b + "directionType: " + directionType);
				if (highestConcentration <= b) {
					highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
					System.out.println(2 + " found higher concentration " + highestConcentration + " direction is :" + directionType);
					move.directionType = directionType;
					dirChanged = true;
				}

			}

			if(allZero(neighborMap, chosenChemicalType, currentCell) == 1 /*&& firstTurn == 1*/){
				move.directionType = DirectionType.CURRENT;
				move.currentState = previousState;
				dirChanged = true;
				return move;
			}

			System.out.println(" out of the loop, concent:  " + highestConcentration + "direction type" + move);

			if (dirChanged == false) {
				if (chosenChemicalType == ChemicalType.RED) {
					chosenChemicalType = ChemicalType.GREEN;
					previousState = (byte)((previousState & (0b10011111)) | 0b00100000);
				} else {
					chosenChemicalType = ChemicalType.RED;
					previousState = (byte)((previousState & (0b10011111)) | 0b01000000);
				}

				highestConcentration = currentCell.getConcentration(chosenChemicalType);
				System.out.println("Strategy red-green : " + chosenChemicalType + " current conc : " + highestConcentration);

				for (DirectionType directionType : neighborMap.keySet()) {
					double c = neighborMap.get(directionType).getConcentration(chosenChemicalType);
					System.out.println(" In 2nd rg loop, concent:  " + highestConcentration + "direction type" + directionType + ", " + chosenChemicalType);
					if (highestConcentration <= c) {
						highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
						move.directionType = directionType;
						System.out.println("Strategy red-green : " +  " found higher conc : " + highestConcentration);
						dirChanged = true;
					}
				}

				if(allZero(neighborMap, chosenChemicalType, currentCell) == 1){
					move.directionType = DirectionType.CURRENT;
					move.currentState = previousState;
					dirChanged = true;
					return move;
				}
			}
		}

		move.currentState = previousState;
		return move;
	}


	public Byte storeDir(DirectionType directionType){
		if(directionType == DirectionType.EAST){
			return 3;
		} else if(directionType == DirectionType.WEST){
			return 4;
		} else if(directionType == DirectionType.NORTH){
			return 1;
		} else if(directionType == DirectionType.SOUTH){
			return 2;
		}else{
			return 5;
		}
	}

	public DirectionType findPreviousState(Byte previousState){
		if((previousState & 7) == 1){
			return DirectionType.NORTH;
		}
		else if ((previousState & 7) == 2){
			return DirectionType.SOUTH;
		}
		else if ((previousState & 7) == 3){
			return DirectionType.EAST;
		}
		else if ((previousState & 7) == 4){
			return DirectionType.WEST;
		}
		else{
			return DirectionType.CURRENT;
		}
	}

	public int findRedOrGreen(Byte previousState){
		if((previousState & 64) == 64) // red
			return 1;
		else
			return 0;

	}

	public int allZero(Map<DirectionType, ChemicalCell> neighborMap, ChemicalType chosenChemicalType, ChemicalCell currentCell){

		int count = 0;
		double minDetectableConcentration = 0.001, b;

		for (DirectionType directionType : neighborMap.keySet()) {
			double a = neighborMap.get(directionType).getConcentration(chosenChemicalType);
			//System.out.println(a + " directionType: " + directionType);
			if (Math.abs(a) < minDetectableConcentration ) {
				//System.out.println("Im here");
				count++;

			}
		}
		b = currentCell.getConcentration(chosenChemicalType);
		if (count == 4 && (Math.abs(b) < minDetectableConcentration))
			return 1;
		else
			return 0;

	}
}

