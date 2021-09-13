package chemotaxis.g1; // TODO modify the package name to reflect your team

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;
import chemotaxis.sim.ChemicalCell.ChemicalType;

public class Agent extends chemotaxis.sim.Agent {
    private SimPrinter sp;

    /**
     * Agent constructor
     *
     * @param simPrinter simulation printer
     *
     */
    public Agent(SimPrinter simPrinter) {
        super(simPrinter);
        sp = simPrinter;
    }

    /**
     * Move agent
     *
     * @param randomNum     random number available for agents
     * @param previousState byte of previous state
     * @param currentCell   current cell
     * @param neighborMap   map of cell's neighbors
     * @return agent move
     *
     */
    @Override
    public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell,
            Map<DirectionType, ChemicalCell> neighborMap) {
        Move agentMove = new Move();
        DirectionType dir = getBlueDirection(neighborMap, 0.99);

        if (dir != DirectionType.CURRENT) {
            agentMove.currentState = getDirectionByte(dir);
            agentMove.directionType = dir;
        } else if (previousState.equals((byte) 0)) {
            if (dir == DirectionType.CURRENT) {
                Byte stateVal = (byte) ((int) Math.abs(randomNum % 15) + 1);
                agentMove.currentState = stateVal;
                agentMove.directionType = getDirectionFromByte(stateVal);
                agentMove.currentState = (byte) 99;
                return agentMove;
            }
        } else if (previousState.equals((byte) 99)) {
            if (dir == DirectionType.CURRENT) {
                Byte stateVal = (byte) ((int) Math.abs(randomNum % 15) + 1);
                agentMove.currentState = stateVal;
                agentMove.directionType = getDirectionFromByte(stateVal);
                agentMove.currentState = (byte) 99;
                return agentMove;
            }
        } else if(neighborMap.get(getDirectionFromByte(previousState)).isBlocked()) {
            agentMove.directionType = getRandomDirection(getDirectionFromByte(previousState), randomNum);
        } else {
            agentMove.directionType = getDirectionFromByte(previousState);
        }
        agentMove.currentState = getDirectionByte(agentMove.directionType);

        return agentMove;
    }

    private DirectionType getRandomDirection(DirectionType prev, Integer random) {
        if(getDirectionByte(prev).intValue()%2 == 0) {
            if(random%2==0) return DirectionType.WEST;
            else return DirectionType.EAST;
        }
        else {
            if(random%2==0) return DirectionType.SOUTH;
            else return DirectionType.NORTH;
        }
    }

    private DirectionType avoidBluePath(Map<DirectionType, ChemicalCell> neighborMap, Double threshold) {
        DirectionType res = DirectionType.CURRENT;
        Map<DirectionType, Map<ChemicalCell.ChemicalType, Double>> concentrationMap = getConcentrations(neighborMap);
        for(DirectionType dir : concentrationMap.keySet()) {
            if(concentrationMap.get(dir).get(ChemicalCell.ChemicalType.BLUE)<threshold) return dir;
        }
        return DirectionType.CURRENT;
    }

    private DirectionType getBlueDirection(Map<DirectionType, ChemicalCell> neighborMap, Double blueThreshold) {
        Map<DirectionType, Map<ChemicalCell.ChemicalType, Double>> concentrationMap = getConcentrations(neighborMap);
        DirectionType absoluteBlue = DirectionType.CURRENT;

        for (DirectionType dir : concentrationMap.keySet()) {
            if (concentrationMap.get(dir).get(ChemicalCell.ChemicalType.BLUE) >= blueThreshold)
                absoluteBlue = dir;
        }

        return absoluteBlue;
    }

    private DirectionType getGreenDirection(Map<DirectionType, ChemicalCell> neighborMap, Double greenThreshold) {
        Map<DirectionType, Map<ChemicalCell.ChemicalType, Double>> concentrationMap = getConcentrations(neighborMap);
        DirectionType absoluteGreen = DirectionType.CURRENT;

        for(DirectionType dir : concentrationMap.keySet()) {
            if(concentrationMap.get(dir).get(ChemicalCell.ChemicalType.BLUE) >= greenThreshold) absoluteGreen = dir;
        }

        return absoluteGreen;
    }

    private DirectionType[] getRedDirection(Map<DirectionType, ChemicalCell> neighborMap, Double redThreshold) {
        Map<DirectionType, Map<ChemicalCell.ChemicalType, Double>> concentrationMap = getConcentrations(neighborMap);
        DirectionType[] absoluteRed = { DirectionType.CURRENT, DirectionType.CURRENT };

        for (DirectionType dir : concentrationMap.keySet()) {
            if (concentrationMap.get(dir).get(ChemicalCell.ChemicalType.RED) >= redThreshold && concentrationMap.get(dir).get(ChemicalCell.ChemicalType.RED) <= redThreshold+0.1 ) {
                if (absoluteRed[0].equals(DirectionType.CURRENT)) {
                    absoluteRed[0] = dir;
                } else if (absoluteRed[1].equals(DirectionType.CURRENT)) {
                    absoluteRed[1] = dir;
                }
            }
            ;
        }
        System.out.println(Arrays.toString(absoluteRed));
        return absoluteRed;
    }

    private DirectionType getInitiationDiagonalDirection(Byte b){
        if (b.equals((byte) 5) || b.equals((byte) 7)){
            return DirectionType.WEST;
        } else if (b.equals((byte) 6) || b.equals((byte) 8)){
            return DirectionType.EAST;
        } else if (b.equals((byte) 9) || b.equals((byte) 10)){
            return DirectionType.NORTH;
        } else {
            return DirectionType.SOUTH;
        }
    }

    private Byte getNextUncertainDiagonalByte(Byte b, Map<DirectionType, ChemicalCell> neighborMap){
        switch(b){
            case (byte) 13:
                if(neighborMap.get(DirectionType.WEST).getConcentration(ChemicalType.RED) > neighborMap.get(DirectionType.EAST).getConcentration(ChemicalType.RED)){
                    return (byte) 7;
                } else {
                    return (byte) 6;
                }
            case (byte) 14:
                if(neighborMap.get(DirectionType.WEST).getConcentration(ChemicalType.RED) > neighborMap.get(DirectionType.EAST).getConcentration(ChemicalType.RED)){
                    return (byte) 5;
                } else {
                    return (byte) 8;
                }
            case (byte) 15:
                if(neighborMap.get(DirectionType.NORTH).getConcentration(ChemicalType.RED) > neighborMap.get(DirectionType.SOUTH).getConcentration(ChemicalType.RED)){
                    return (byte) 9;
                } else {
                    return (byte) 11;
                }
            case (byte) 16:
                if(neighborMap.get(DirectionType.NORTH).getConcentration(ChemicalType.RED) > neighborMap.get(DirectionType.SOUTH).getConcentration(ChemicalType.RED)){
                    return (byte) 10;
                } else {
                    return (byte) 12;
                }
            default:
                return (byte) 0;
        }
    }

    private Byte getInitialDiagonalDirectionByte(DirectionType d1, DirectionType d2) {
            // 16 E -> ?
            // 15 W -> ?
            // 14 N -> ?
            // 13 S -> ?
            // 12 S -> E
            // 11 S -> W
            // 10 N -> E
            // 9 N -> W
            // 8 E -> N
            // 7 W -> S
            // 6 E -> S
            // 5 W -> N
        if(d2.equals(DirectionType.CURRENT) || d1.equals(DirectionType.CURRENT)){
            
            switch (d1) {
                case NORTH:
                    return (byte) 14;
                case SOUTH:
                    return (byte) 13;
                case EAST:
                    return (byte) 16;
                case WEST:
                    return (byte) 15;
                default:
                    return (byte) 14;
            }
        }
        switch (d1) {
            case NORTH:
                switch (d2) {
                    case EAST:
                        return (byte) 10;
                    case WEST:
                        return (byte) 9;
                }
            case SOUTH:
                switch (d2) {
                    case EAST:
                        return (byte) 12;
                    case WEST:
                        return (byte) 11;
                }
            case EAST:
                switch (d2) {
                    case NORTH:
                        return (byte) 8;
                    case SOUTH:
                        return (byte) 6;
                }
            case WEST:
                switch (d2) {
                    case NORTH:
                        return (byte) 5;
                    case SOUTH:
                        return (byte) 7;
                }
            default:
              return (byte) 0;
        }
    }

    private Byte getNextDiagonalState(Byte b, DirectionType d) {
        switch (d) {
            case NORTH:
                switch (b) {
                    case (byte) 5:
                        return (byte) 9;
                    case (byte) 8:
                        return (byte) 10;
                    default:
                        return 10;
                }
            case SOUTH:
                switch (b) {
                    case (byte) 6:
                        return (byte) 12;
                    case (byte) 7:
                        return (byte) 11;
                    default:
                        return 11;
                }
            case EAST:
                switch (b) {
                    case (byte) 10:
                        return (byte) 8;
                    case (byte) 12:
                        return (byte) 6;
                    default:
                        return 12;
                }
            case WEST:
                switch (b) {
                    case (byte) 9:
                        return (byte) 5;
                    case (byte) 11:
                        return (byte) 7;
                }
            default:
                return (byte) 0;
        }
    }

    private DirectionType getNextDiagonalDirection(Byte b) {
        switch (b) {
            case (byte) 12:
                return DirectionType.EAST;
            case (byte) 11:
                return DirectionType.WEST;
            case (byte) 10:
                return DirectionType.EAST;
            case (byte) 9:
                return DirectionType.WEST;
            case (byte) 8:
                return DirectionType.NORTH;
            case (byte) 7:
                return DirectionType.SOUTH;
            case (byte) 6:
                return DirectionType.SOUTH;
            case (byte) 5:
                return DirectionType.NORTH;
            default:
                return DirectionType.CURRENT;
        }
    }

    private Byte getDirectionByte(DirectionType dir) {
        switch (dir) {
            case SOUTH:
                return (byte) 4;
            case WEST:
                return (byte) 3;
            case NORTH:
                return (byte) 2;
            case EAST:
                return (byte) 1;
            default:
                return (byte) 0;
        }
    }

    private DirectionType getDirectionFromByte(Byte b) {
        switch (b) {
            // 16 E -> ?
            // 15 W -> ?
            // 14 N -> ?
            // 13 S -> ?
            // 12 S -> E
            // 11 S -> W
            // 10 N -> E
            // 9 N -> W
            // 8 E -> N
            // 7 W -> S
            // 6 E -> S
            // 5 W -> N
            case (byte) 16:
                return DirectionType.EAST;
            case (byte) 15:
                return DirectionType.WEST;
            case (byte) 14:
                return DirectionType.NORTH;
            case (byte) 13:
                return DirectionType.SOUTH;
            case (byte) 12:
                return DirectionType.SOUTH;
            case (byte) 11:
                return DirectionType.SOUTH;
            case (byte) 10:
                return DirectionType.NORTH;
            case (byte) 9:
                return DirectionType.NORTH;
            case (byte) 8:
                return DirectionType.EAST;
            case (byte) 7:
                return DirectionType.WEST;
            case (byte) 6:
                return DirectionType.EAST;
            case (byte) 5:
                return DirectionType.WEST;
            case (byte) 4:
                return DirectionType.SOUTH;
            case (byte) 3:
                return DirectionType.WEST;
            case (byte) 2:
                return DirectionType.NORTH;
            case (byte) 1:
                return DirectionType.EAST;
            default:
                return DirectionType.CURRENT;
        }
    }

    private Map<DirectionType, Map<ChemicalCell.ChemicalType, Double>> getConcentrations(
            Map<DirectionType, ChemicalCell> neighborMap) {
        Map<DirectionType, Map<ChemicalCell.ChemicalType, Double>> concentrationMap = new HashMap<>();
        //sp.println(neighborMap.keySet().toString());
        for(DirectionType dir : neighborMap.keySet()) {
            concentrationMap.put(dir, neighborMap.get(dir).getConcentrations());
        }
        //System.out.println(concentrationMap.toString());
        return concentrationMap;
    }
}