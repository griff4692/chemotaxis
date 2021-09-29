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
        double threshold = 0.05;
		Move move = new Move();
        // Chop up the previous state into stored info:
        // 1-2nd bits: determine current chem - 00=INITIAL STATE (RED,NO ROLLING),01=RED,10=GREEN, 11=BLUE
        // 3-4 bits: determine third last move - 00=WEST,01=EAST,10=NORTH,11=SOUTH
        // 5-6 bits: determine second last move
        // 7-8 bits: determine last move
        int chemIdx = (192 & previousState) >> 6;
        System.out.println("\n chemIdx" + chemIdx);
        int thdLastMoveIdx = (48 & previousState) >> 4;
        int secLastMoveIdx = (12 & previousState) >> 2;
        int lastMoveIdx = 3 & previousState;

        ChemicalType chosenChemicalType = switch (chemIdx) {
            case 0 -> ChemicalType.RED;
            case 1 -> ChemicalType.RED;
            case 2 -> ChemicalType.GREEN;
            case 3 -> ChemicalType.BLUE;
            default -> ChemicalType.RED;
        };
        System.out.println("chosenChemicalType " + chosenChemicalType);

        DirectionType thdLastMove = switch (thdLastMoveIdx) {
            case 0 -> DirectionType.WEST;
            case 1 -> DirectionType.EAST;
            case 2 -> DirectionType.NORTH;
            case 3 -> DirectionType.SOUTH;
            default -> DirectionType.EAST;
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
        System.out.println(chosenChemicalType + " " + highestConcentration);
        for (DirectionType directionType : neighborMap.keySet()) {
            if (highestConcentration < neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
                highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
                move.directionType = directionType;
            }
        }
        System.out.println(highestConcentration + " " + move.directionType);

        // Prevent backwards movement
        if ((move.directionType == DirectionType.NORTH && lastMove == DirectionType.SOUTH) ||
                (move.directionType == DirectionType.SOUTH && lastMove == DirectionType.NORTH) ||
                (move.directionType == DirectionType.EAST && lastMove == DirectionType.WEST) ||
                (move.directionType == DirectionType.WEST && lastMove == DirectionType.EAST)){
            if(neighborMap.get(lastMove).isOpen()){
                move.directionType = lastMove;
            }
            else {
                for (DirectionType directionType : neighborMap.keySet()) {
                    if (directionType != move.directionType && neighborMap.get(directionType).isOpen()) {
                        move.directionType = directionType;
                        break;
                    }
                }
            }

            chosenChemicalType = switch (chosenChemicalType) {
                case RED -> ChemicalType.GREEN;
                case GREEN -> ChemicalType.BLUE;
                case BLUE -> ChemicalType.RED;
            };
        }
        // If at maxima, change chemical type and recalculate move
        if(highestConcentration == currentCell.getConcentration(chosenChemicalType) && highestConcentration != 0.0){
            chosenChemicalType = switch (chosenChemicalType) {
                case RED -> ChemicalType.GREEN;
                case GREEN -> ChemicalType.BLUE;
                case BLUE -> ChemicalType.RED;
            };
            // Move in direction of highest concentration of sought chemical
            highestConcentration = currentCell.getConcentration(chosenChemicalType);
            for (DirectionType directionType : neighborMap.keySet()) {
                if (highestConcentration < neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
                    highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
                    move.directionType = directionType;
                }
            }
            // Prevent backwards movement, except for when it is the only open cell
            if ((move.directionType == DirectionType.NORTH && lastMove == DirectionType.SOUTH) ||
                    (move.directionType == DirectionType.SOUTH && lastMove == DirectionType.NORTH) ||
                    (move.directionType == DirectionType.EAST && lastMove == DirectionType.WEST) ||
                    (move.directionType == DirectionType.WEST && lastMove == DirectionType.EAST)){
                if(neighborMap.get(lastMove).isOpen()){
                    move.directionType = lastMove;
                }
                else {
                    for (DirectionType directionType : neighborMap.keySet()) {
                        if (directionType != move.directionType && neighborMap.get(directionType).isOpen()) {
                            move.directionType = directionType;
                            break;
                        }
                    }
                }
            }
        }
        // If there is none of the sought chemical and the agent is not in the initial state,
        // chose the majority direction of the previous three moves
        if (highestConcentration == 0.0 && chemIdx != 0){
            move.directionType = lastMove;
            // if(lastMove == secLastMove){
            //     move.directionType = lastMove;
            // }
            // else if(lastMove == thdLastMove){
            //     move.directionType = lastMove;
            // }
            // else if (secLastMove == thdLastMove){
            //     move.directionType = secLastMove;
            // }
        }
        System.out.println(move.directionType);
        System.out.println(chosenChemicalType);
        System.out.println(highestConcentration);
        //If the agent is at local maxima, update currState
        int currChem = switch (chosenChemicalType) {
            case RED -> 1;
            case GREEN -> 2;
            case BLUE -> 3;
        };
        int currState = 0;
        if(move.directionType == DirectionType.CURRENT && previousState == 0) {
            currState = 0;
        }
        else if (highestConcentration == 0){
            currState = (currChem<<6) | (thdLastMoveIdx<<4) | (secLastMoveIdx<<2) | (lastMoveIdx);
        }
        else{
            int currSecLastMove = lastMoveIdx;
            int currThdLastMove = secLastMoveIdx;
            int currLastMove = switch (move.directionType){
                case WEST -> 0;
                case EAST -> 1;
                case NORTH -> 2;
                case SOUTH -> 3;
                case CURRENT -> lastMoveIdx;
            };
            currState = (currChem<<6) | (currThdLastMove<<4) | (currSecLastMove<<2) | (currLastMove);
        }
        move.currentState = (byte) currState;

		return move;
	}
}