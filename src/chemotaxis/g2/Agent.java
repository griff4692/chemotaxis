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

		/*
		* Byte previousState : 3 LSBs hold previous direction
		* 001 : North
		* 010 : South
		* 011 : East
		* 100 : West
		* 101 : Current
		* 110 : Seen red. Ignore Blue?
		* */


		Move move = new Move();

		ChemicalType chosenChemicalTurn = ChemicalType.BLUE;
		ChemicalType chosenChemicalType = ChemicalType.RED;


		double minDetectableConcentration = 0.001;	/* Would have done a #define, but can't. CAUTION! Change if minimum detectable concentration
		 changes. */
		
		boolean turn = false;


		for (DirectionType directionType : neighborMap.keySet()) {
			if (Math.abs(neighborMap.get(directionType).getConcentration(chosenChemicalTurn) - 1.0) < minDetectableConcentration ) {
				move.directionType = directionType;
				turn = true;
				move.currentState = storeDir(directionType);
				break;

			}
		}

		if(!turn){
			// No blue found. Then follow previous direction
			move.directionType = findPreviousState(previousState);
			move.currentState = previousState;

		}

		/*
		* Add : if in current position for many turns, choose a different position.
		*
		 */

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
}
