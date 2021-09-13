package chemotaxis.g5;

import java.util.Map;
import java.util.*;

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

    private List<DirectionType> getOrthogonalDirections(DirectionType previousDirection) {
        List<DirectionType> directionList = new LinkedList<DirectionType>(); 

        if (previousDirection == DirectionType.EAST) { 
            directionList.add(DirectionType.NORTH);
            directionList.add(DirectionType.SOUTH);
        }
        else if (previousDirection == DirectionType.WEST) { 
            directionList.add(DirectionType.NORTH);
            directionList.add(DirectionType.SOUTH);
        }
        else if (previousDirection == DirectionType.NORTH) { 
            directionList.add(DirectionType.EAST);
            directionList.add(DirectionType.WEST);
        }
        else { 
            directionList.add(DirectionType.EAST);
            directionList.add(DirectionType.WEST);
        }
        return directionList; 
    }

    private DirectionType getOppositeDirections(DirectionType oppositeDirection) { 
        switch (oppositeDirection) { 
            case NORTH: return DirectionType.SOUTH;
            case SOUTH: return DirectionType.NORTH; 
            case WEST: return DirectionType.EAST; 
            case EAST: return DirectionType.WEST; 
            default: return oppositeDirection;
        }
    }

    private DirectionType getPrevDirection(Byte previousState) { 
        int previous_direction_state = previousState;
        if (previous_direction_state == 0) { 
            return DirectionType.NORTH;
        }
        else if (previous_direction_state == 1) { 
            return DirectionType.SOUTH; 
        }
        else if (previous_direction_state == 2) { 
            return DirectionType.EAST; 
        }
        return DirectionType.WEST;
    }

    private DirectionType followTheWall(Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        simPrinter.println("Follow Wall");
        DirectionType previousDirection = getPrevDirection(previousState);
        simPrinter.println(previousDirection);
        ChemicalCell cellInSameDirection = neighborMap.get(previousDirection);
        List<DirectionType> orthogonalDirections = getOrthogonalDirections(previousDirection);

        DirectionType firstOrthogonalDirection = orthogonalDirections.get(0);
        DirectionType secondOrthogonalDirection = orthogonalDirections.get(1);
        ChemicalCell firstOrthogonalCell = neighborMap.get(orthogonalDirections.get(0));
        ChemicalCell secondOrthogonalCell = neighborMap.get(orthogonalDirections.get(1)); 

        if (cellInSameDirection.isBlocked())  { 
            if (firstOrthogonalCell.isBlocked() && secondOrthogonalCell.isBlocked()) { 
                return getOppositeDirections(previousDirection);
            }
            else if (firstOrthogonalCell.isBlocked()) { 
                return secondOrthogonalDirection; 
            }
            else { 
                return firstOrthogonalDirection;
            }
        }
        else if (firstOrthogonalCell.isBlocked() || firstOrthogonalCell.isBlocked()) { 
            return previousDirection;
        }
        return previousDirection;
    }

    private byte directionToByte(DirectionType direction) { 
        switch (direction) { 
            case NORTH: 
                return (byte) 0;
            case SOUTH: 
                return (byte) 1;
            case EAST:
                return (byte) 2; 
            default: 
                return (byte) 3;
        }
    }
	@Override
	public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        Move nextMove = new Move();

        // Get green chemical surroundings
        double highest = 0; 
        DirectionType maxDirection = null;
        for (DirectionType directionType: neighborMap.keySet()) { 
            if (highest < neighborMap.get(directionType).getConcentration(ChemicalType.GREEN)) {
                highest = neighborMap.get(directionType).getConcentration(ChemicalType.GREEN);
                maxDirection = directionType;
            }
        }

        if (highest != 0) {
            DirectionType nextDirection = getOppositeDirections(maxDirection);
            nextMove.currentState = directionToByte(nextDirection);
            nextMove.directionType = nextDirection;
            if (neighborMap.get(nextDirection).isBlocked()) { 
                DirectionType modifiedDirection = followTheWall(directionToByte(nextDirection), currentCell, neighborMap);
                nextMove.currentState = directionToByte(modifiedDirection);
                nextMove.directionType = modifiedDirection;
            }
            else {
                nextMove.currentState = directionToByte(nextDirection);
                nextMove.directionType = nextDirection;
            }
            return nextMove;
        }
        else {
            DirectionType nextDirection = followTheWall(previousState, currentCell, neighborMap);
            nextMove.currentState = directionToByte(nextDirection);
            nextMove.directionType = nextDirection;
            return nextMove;
        }
	}	
}