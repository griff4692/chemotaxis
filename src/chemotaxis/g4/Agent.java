package chemotaxis.g4;

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
        // Chop up the previous state into stored info:
        // 1st bit: if the agent is rolling
        // 2nd bit: unused for now
        // 3-4 bits: determine current chem - 00=RED,01=GREEN,10=BLUE
        // 5-6 bits: determine second last move - 00=WEST,01=EAST,10=NORTH,11=SOUTH
        // 7-8 bits: determine last move
        Boolean isRolling = previousState < 0;
        int chemIdx = (48 & previousState) >> 4;
        int secLastMoveIdx = (12 & previousState) >> 2;
        int lastMoveIdx = 3 & previousState;

        ChemicalType chosenChemicalType = switch (chemIdx) {
            case 0 -> ChemicalType.RED;
            case 1 -> ChemicalType.GREEN;
            case 2 -> ChemicalType.BLUE;
            default -> ChemicalType.RED;
        };

        DirectionType secLastMove = switch (secLastMoveIdx) {
            case 0 -> DirectionType.WEST;
            case 1 -> DirectionType.EAST;
            case 2 -> DirectionType.NORTH;
            case 3 -> DirectionType.SOUTH;
            default -> DirectionType.EAST;
        };

        DirectionType lastMove = switch (lastMoveIdx) {
            case 0 -> DirectionType.WEST;
            case 1 -> DirectionType.EAST;
            case 2 -> DirectionType.NORTH;
            case 3 -> DirectionType.SOUTH;
            default -> DirectionType.EAST;
        };
        // Move in direction of highest concentration of sought chemical
        double highestConcentration = currentCell.getConcentration(chosenChemicalType);
		for (DirectionType directionType : neighborMap.keySet()) {
			if (highestConcentration < neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
				highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
				move.directionType = directionType;
                isRolling = Boolean.FALSE;
			}
		}
        // If there is none of the sought chemical and the agent is rolling,
        // chose between last two moves with 50/50 prob
        if (isRolling){
            int rollDir = randomNum%2;
            if (rollDir==0){
                move.directionType = lastMove;
            }
            else{
                move.directionType = secLastMove;
            }
        }
        //If the agent is at local maxima, update currState
        int currState = 0;
        if(move.directionType == DirectionType.CURRENT){
            currState = -128;
            int currChem = switch (chosenChemicalType) {
                case RED -> 1;
                case GREEN -> 2;
                case BLUE -> 0;
            };
            currState = currState | currChem<<4 | secLastMoveIdx<<2 | lastMoveIdx;
        }
        //Construct current state byte
        else if (isRolling) {
            currState = previousState;
        }
        else{
            int currChem = switch (chosenChemicalType) {
                case RED -> 0;
                case GREEN -> 1;
                case BLUE -> 2;
            };
            int currSecLastMove = lastMoveIdx;
            int currLastMove = switch (move.directionType){
                case WEST -> 0;
                case EAST -> 1;
                case NORTH -> 2;
                case SOUTH -> 3;
                case CURRENT -> lastMoveIdx;
            };
            currState = (currChem<<4) | (currSecLastMove<<2) | (currLastMove);
        }
        move.currentState = (byte) currState;

		return move;
	}
}