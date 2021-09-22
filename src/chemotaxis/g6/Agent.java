package chemotaxis.g6;

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

        ChemicalType chosenChemicalType = ChemicalType.RED;
        if(previousState/2 == 1)
            chosenChemicalType = ChemicalType.GREEN;
        else if(previousState/2 == 2)
            chosenChemicalType = ChemicalType.BLUE;

		double highestConcentration = currentCell.getConcentration(chosenChemicalType);
		for (DirectionType directionType : neighborMap.keySet()) {
			if (highestConcentration < neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
				highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
				move.directionType = directionType;
			}
		}

        if(move.directionType == DirectionType.CURRENT && previousState%2 == 1){
            previousState++;
            if(previousState==6) previousState = 0;
        }
        else if(move.directionType != DirectionType.CURRENT && previousState%2 == 0){
            previousState++;
        }
        move.currentState = previousState;

		return move;
	}
}