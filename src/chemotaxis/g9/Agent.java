package chemotaxis.g9; // TODO modify the package name to reflect your team

import java.util.Map;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
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
     * Finds largest red cell direction
     *
     * @param currentCell      current cell
     * @param neighborMap      map of cell's neighbors
     */
    public DirectionType findHighestRed(ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        double highestRed   = currentCell.getConcentration(ChemicalCell.ChemicalType.RED);
        DirectionType highestRedDirection = null;

        for (DirectionType directionType : neighborMap.keySet()) {
            double redCnt = neighborMap.get(directionType).getConcentration(ChemicalCell.ChemicalType.RED);

            if (highestRed < redCnt) {
                highestRed = redCnt;
                highestRedDirection = directionType;
            }
        }

        return highestRedDirection;
    }

    /**
     * Finds largest green cell direction
     *
     * @param currentCell      current cell
     * @param neighborMap      map of cell's neighbors
     */
    public DirectionType findHighestGreen(ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        double highestGreen   = currentCell.getConcentration(ChemicalCell.ChemicalType.GREEN);
        DirectionType highestGreenDirection = null;

        for (DirectionType directionType : neighborMap.keySet()) {
            double greenCnt = neighborMap.get(directionType).getConcentration(ChemicalCell.ChemicalType.GREEN);

            if (highestGreen < greenCnt) {
                highestGreen = greenCnt;
                highestGreenDirection = directionType;
            }
        }

        return highestGreenDirection;
    }

    /**
     * Finds largest blue cell direction
     *
     * @param currentCell      current cell
     * @param neighborMap      map of cell's neighbors
     */
    public DirectionType findHighestBlue(ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        double highestBlue   = currentCell.getConcentration(ChemicalCell.ChemicalType.BLUE);
        DirectionType highestBlueDirection = null;

        for (DirectionType directionType : neighborMap.keySet()) {
            double blueCnt = neighborMap.get(directionType).getConcentration(ChemicalCell.ChemicalType.BLUE);

            if (highestBlue < blueCnt) {
                highestBlue = blueCnt;
                highestBlueDirection = directionType;
            }
        }

        return highestBlueDirection;
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
        // TODO add your code here to move the agent
        Move move = new Move();

        System.out.println("LOOKING FOR " + (previousState == 0 ? "RED" : (previousState == 1 ? "GREEN" : "BLUE")));

        // if in any given state and see only larger values adjacent, stay in that state and move to larger
        // if in any given state and see only smaller values adjacent, agent reached local max so change color

        if (previousState == 0) { // red
            DirectionType highestRedDirection = findHighestRed(currentCell, neighborMap);
            if (highestRedDirection == null) {
                move.currentState = 1;
                move.directionType = findHighestGreen(currentCell, neighborMap);
            } else {
                move.currentState = 0;
                move.directionType = highestRedDirection;
            }
        } else if (previousState == 1) { // green
            DirectionType highestGreenDirection = findHighestGreen(currentCell, neighborMap);

            if (highestGreenDirection == null) {
                move.currentState = 2;
                move.directionType = findHighestBlue(currentCell, neighborMap);
            } else {
                move.currentState = 1;
                move.directionType = highestGreenDirection;
            }
        } else if (previousState == 2) { // blue
            DirectionType highestBlueDirection = findHighestBlue(currentCell, neighborMap);

            if (highestBlueDirection == null) {
                move.currentState = 0;
                move.directionType = findHighestRed(currentCell, neighborMap);
            } else {
                move.currentState = 2;
                move.directionType = highestBlueDirection;
            }
        } else {}

        return move; // TODO modify the return statement to return your agent move
    }
}