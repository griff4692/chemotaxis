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

        ChemicalType chosenChemicalType;
        switch (chemIdx) {
            case 0: chosenChemicalType= ChemicalType.RED;
                break;
            case 1: chosenChemicalType= ChemicalType.BLUE;
                break;
            case 2: chosenChemicalType= ChemicalType.GREEN;
                break;
            case 3: chosenChemicalType= ChemicalType.RED;
                break;
            default: chosenChemicalType= ChemicalType.RED;
                break;
        };
        System.out.println("chosenChemicalType " + chosenChemicalType);

        DirectionType thdLastMove;
        switch (thdLastMoveIdx) {
            case 0: thdLastMove = DirectionType.WEST;
                break;
            case 1: thdLastMove = DirectionType.EAST;
                break;
            case 2: thdLastMove = DirectionType.NORTH;
                break;
            case 3: thdLastMove = DirectionType.SOUTH;
                break;
            default: thdLastMove = DirectionType.EAST;
                break;
        };

        DirectionType secLastMove;
        switch (secLastMoveIdx) {
            case 0: secLastMove = DirectionType.WEST;
                break;
            case 1: secLastMove = DirectionType.EAST;
                break;
            case 2: secLastMove = DirectionType.NORTH;
                break;
            case 3: secLastMove = DirectionType.SOUTH;
                break;
            default: secLastMove = DirectionType.EAST;
                break;
        };

        DirectionType lastMove;
        switch (lastMoveIdx) {
            case 0: lastMove = DirectionType.WEST;
                break;
            case 1: lastMove = DirectionType.EAST;
                break;
            case 2: lastMove = DirectionType.NORTH;
                break;
            case 3: lastMove = DirectionType.SOUTH;
                break;
            default: lastMove = DirectionType.EAST;
                break;
        };
        // Move in direction of highest concentration of sought chemical
        double highestConcentration = currentCell.getConcentration(chosenChemicalType);
        System.out.println(chosenChemicalType + " " + highestConcentration);

        for (DirectionType directionType : neighborMap.keySet()) {
            // System.out.println(directionType + ", ");
            // System.out.println(highestConcentration + " " + move.directionType);
            if (highestConcentration < neighborMap.get(directionType).getConcentration(chosenChemicalType)) {
                highestConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
                move.directionType = directionType;
            }
        }
        System.out.println("highest: " + highestConcentration + " " + move.directionType);

        // Prevent backwards movement
        if ((move.directionType == DirectionType.NORTH && lastMove == DirectionType.SOUTH) ||
                (move.directionType == DirectionType.SOUTH && lastMove == DirectionType.NORTH) ||
                (move.directionType == DirectionType.EAST && lastMove == DirectionType.WEST) ||
                (move.directionType == DirectionType.WEST && lastMove == DirectionType.EAST)){
            System.out.println("Prevent backwards movement");
            if(neighborMap.get(lastMove).isOpen()){
                move.directionType = lastMove;
            }
            else {
                for(int i=0; i<4; i++){
                    DirectionType directionType;
                    switch (i) {
                        case 0: directionType = DirectionType.NORTH;
                            break;
                        case 1: directionType = DirectionType.SOUTH;
                            break;
                        case 2: directionType = DirectionType.EAST;
                            break;
                        case 3 : directionType = DirectionType.WEST;
                            break;
                        default: directionType = DirectionType.NORTH;
                            break;
                    };
                    if (directionType != lastMove && neighborMap.get(directionType).isOpen()) {
                        move.directionType = directionType;
                        break;
                    }
                }
                
                // for (DirectionType directionType : neighborMap.keySet()) {
                //     if (directionType != move.directionType && neighborMap.get(directionType).isOpen()) {
                //         move.directionType = directionType;
                //         break;
                //     }
                // }
            }

            switch (chosenChemicalType) {
                case RED: chosenChemicalType = ChemicalType.GREEN;
                    break;
                case GREEN: chosenChemicalType = ChemicalType.BLUE;
                    break;
                case BLUE: chosenChemicalType = ChemicalType.RED;
                    break;
            };
        }
        // If at maxima, change chemical type and recalculate move
        if(highestConcentration == currentCell.getConcentration(chosenChemicalType) && highestConcentration != 0.0){
            System.out.println("If at maxima, change chemical type and recalculate move");
            switch (chosenChemicalType) {
                case RED: chosenChemicalType = ChemicalType.GREEN;
                    break;
                case GREEN: chosenChemicalType = ChemicalType.BLUE;
                    break;
                case BLUE: chosenChemicalType = ChemicalType.RED;
                    break;
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
            // move.directionType = lastMove;
            if(lastMove == secLastMove){
                move.directionType = lastMove;
            }
            else if(lastMove == thdLastMove){
                move.directionType = lastMove;
            }
            else if (secLastMove == thdLastMove){
                move.directionType = lastMove;
            }
        }
        // System.out.println(move.directionType);
        // System.out.println(chosenChemicalType);
        // System.out.println(highestConcentration);
        //If the agent is at local maxima, update currState
        int currChem;
        switch (chosenChemicalType) {
            case RED: currChem = 1;
                break;
            case GREEN: currChem = 2;
                break;
            case BLUE: currChem = 3;
                break;
            default: currChem = 0;
                break;
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
            int currLastMove;
            switch (move.directionType){
                case WEST: currLastMove = 0;
                    break;
                case EAST: currLastMove = 1;
                    break;
                case NORTH: currLastMove = 2;
                    break;
                case SOUTH: currLastMove = 3;
                    break;
                case CURRENT: currLastMove = lastMoveIdx;
                    break;
                default: currLastMove = lastMoveIdx;
            };
            currState = (currChem<<6) | (currThdLastMove<<4) | (currSecLastMove<<2) | (currLastMove);
        }
        move.currentState = (byte) currState;

		return move;
	}
}