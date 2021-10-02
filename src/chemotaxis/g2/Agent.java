package chemotaxis.g2;

import java.util.ArrayList;
import java.util.Map;
import java.lang.Math;
import java.util.Random;
import java.util.List;

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
		double minDetectableConcentration = 0.001; /* Would have done a #define, but can't. CAUTION! Change if minimum detectable concentration
       changes. */

		boolean turn = false;
		int colour;

		//System.out.println("checking for blue");
		for (DirectionType directionType : neighborMap.keySet()) {
			double a = neighborMap.get(directionType).getConcentration(chosenChemicalType);
			//System.out.println(a + " directionType: " + directionType);
			if (Math.abs(a - 1.0) < minDetectableConcentration ) {
				//System.out.println("Im here");
				move.directionType = directionType;
				turn = true;
				previousState = (byte)((previousState & 248) | storeDir(directionType));
				move.currentState = previousState;
				break;
			}
		}
		//System.out.println("Out of blue loop");
		if(!turn){
			// No blue found.

			if(previousState == 0){
				//first turn and no blue . So follow red-green strategy, start with red
				//System.out.println("No blue found and first turn");
				previousState = (byte)(128 | 64) ;
			}
			else if ((previousState & 128) == 0){
				// no blue found, but strategy is follow the turns
				move.directionType = findPreviousState(previousState);
				move.currentState = previousState;
				//System.out.println("No blue found but strategy is follow the turns");

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
			if (colour == 1) {
				chosenChemicalType = ChemicalType.RED;
				System.out.println("FOLLOWING RED"); }
			else {
				chosenChemicalType = ChemicalType.GREEN;
				System.out.println("FOLLOWING GREEN"); }

			highestConcentration = currentCell.getConcentration(chosenChemicalType);
			double previousColourConcentration;

			//System.out.println("Strategy red-green : " + chosenChemicalType + " current conc : " + highestConcentration);

			for (DirectionType directionType : neighborMap.keySet()) {
				double b = neighborMap.get(directionType).getConcentration(chosenChemicalType);
				//System.out.println(1 + " " + b + "directionType: " + directionType);
				if (highestConcentration <= b) {
					highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
					//System.out.println(2 + " found higher concentration " + highestConcentration + " direction is :" + directionType);
					move.directionType = directionType;


					dirChanged = true;
				}

			}

			if(allZero(neighborMap, chosenChemicalType, currentCell) == 1 /*&& firstTurn == 1*/){
				move.directionType = DirectionType.CURRENT;

				previousState = (byte)((previousState & 248) | storeDir(move.directionType));

				move.currentState = previousState;
				dirChanged = true;
				return move;
			}

			//System.out.println(" out of the loop, concent:  " + highestConcentration + "direction type" + move);

			if (dirChanged == false) {
				if (chosenChemicalType == ChemicalType.RED) {
					chosenChemicalType = ChemicalType.GREEN;
					previousState = (byte)((previousState & (0b10011111)) | 0b00100000);
				} else {
					chosenChemicalType = ChemicalType.RED;
					previousState = (byte)((previousState & (0b10011111)) | 0b01000000);
				}

				highestConcentration = currentCell.getConcentration(chosenChemicalType);
				//System.out.println("Strategy red-green : " + chosenChemicalType + " current conc : " + highestConcentration);

				for (DirectionType directionType : neighborMap.keySet()) {
					double c = neighborMap.get(directionType).getConcentration(chosenChemicalType);
					//System.out.println(" In 2nd rg loop, concent:  " + highestConcentration + "direction type" + directionType + ", " + chosenChemicalType);
					if (highestConcentration <= c) {
						highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
						move.directionType = directionType;
						//System.out.println("Strategy red-green : " +  " found higher conc : " + highestConcentration);

						dirChanged = true;
					}
				}

				if(allZero(neighborMap, chosenChemicalType, currentCell) == 1){
					move.directionType = DirectionType.CURRENT;
					previousState = (byte)((previousState & 248) | storeDir(move.directionType));
					move.currentState = previousState;
					dirChanged = true;
					return move;
				}

			}
		}

		if (oppositeMovement(move.directionType, previousState) == 1){
			if (neighborMap.get(findPreviousState(previousState)).isOpen()) {
				move.directionType = findPreviousState(previousState);
				move.currentState = previousState;
			}
			else {
				move.directionType = randomMove(move.directionType, neighborMap);
				previousState = (byte)((previousState & 248) | storeDir(move.directionType));
				move.currentState = previousState;
			}

		}
		else{
			previousState = (byte)((previousState & 248) | storeDir(move.directionType));
			move.currentState = previousState;
		}

		System.out.println("Final Move: "+ move.directionType);
		return move;
	}

	public DirectionType randomMove(DirectionType oppisiteDir, Map<DirectionType, ChemicalCell> map){
		List<DirectionType> moves =  new ArrayList<DirectionType>();
		for (DirectionType dir: DirectionType.values()){
			if (!dir.equals(oppisiteDir)){
				moves.add(dir);
			}
		}
		Random random = new Random();
		while (true) {
			DirectionType chosenDir = moves.get(random.nextInt(moves.size()));
			if (map.get(chosenDir).isOpen()) {
				return chosenDir;
			}
		}
	}
/*
	private DirectionType randomMove (DirectionType dir, DirectionType opp, Map<DirectionType, ChemicalCell> map) {
		Random rd = new Random();
		boolean flag = rd.nextBoolean();
		for (DirectionType tempDir : map.keySet()) {
			if ((! tempDir.equals(dir)) && (! tempDir.equals(opp))) {
				if (flag) {
					return tempDir;
				}
				else {
					flag = true;
				}
			}
		}
		for (DirectionType tempDir : map.keySet()) {
			if ((! tempDir.equals(dir)) && (! tempDir.equals(opp))) {
				return tempDir;
			}
		}
		return DirectionType.CURRENT;
	}
*/

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


	public int oppositeMovement(DirectionType direction, Byte prev){
		DirectionType prevDir = findPreviousState(prev);
		if (prevDir == DirectionType.NORTH && direction == DirectionType.SOUTH)
			return 1;
		else if (prevDir == DirectionType.SOUTH && direction == DirectionType.NORTH)
			return 1;
		else if (prevDir == DirectionType.EAST && direction == DirectionType.WEST)
			return 1;
		else if (prevDir == DirectionType.WEST && direction == DirectionType.EAST)
			return 1;
		else
			return 0;
	}


}