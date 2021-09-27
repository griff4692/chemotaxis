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

        // if in any given state and see only larger values adjacent, stay in that state and move to larger
        // if in any given state and see only smaller values adjacent, agent reached local max so change color

        if (previousState == 0) { // red
            double highestRed   = currentCell.getConcentration(ChemicalCell.ChemicalType.RED);
            DirectionType highestRedDirection = null;

            for (DirectionType directionType : neighborMap.keySet()) {
                double redCnt = neighborMap.get(directionType).getConcentration(ChemicalCell.ChemicalType.RED);

                if (highestRed < redCnt) {
                    highestRed = redCnt;
                    highestRedDirection = directionType;
                }
            }

            if (highestRedDirection == null) {
                move.currentState = 1;
            } else {
                move.currentState = 0;
                move.directionType = highestRedDirection;
            }
        } else if (previousState == 1) { // green
            double highestGreen = currentCell.getConcentration(ChemicalCell.ChemicalType.GREEN);
            DirectionType highestGreenDirection = null;

            for (DirectionType directionType : neighborMap.keySet()) {
                double greenCnt = neighborMap.get(directionType).getConcentration(ChemicalCell.ChemicalType.GREEN);

                if (highestGreen < greenCnt) {
                    highestGreen = greenCnt;
                    highestGreenDirection = directionType;
                }
            }

            if (highestGreenDirection == null) {
                move.currentState = 2;
            } else {
                move.currentState = 1;
                move.directionType = highestGreenDirection;
            }
        } else if (previousState == 2) { // blue
            double highestBlue = currentCell.getConcentration(ChemicalCell.ChemicalType.BLUE);
            DirectionType highestBlueDirection = null;

            for (DirectionType directionType : neighborMap.keySet()) {
                double blueCnt = neighborMap.get(directionType).getConcentration(ChemicalCell.ChemicalType.BLUE);

                if (highestBlue < blueCnt) {
                    highestBlue = blueCnt;
                    highestBlueDirection = directionType;
                }
            }

            if (highestBlueDirection == null) {
                move.currentState = 0;
            } else {
                move.currentState = 2;
                move.directionType = highestBlueDirection;
            }
        } else {}

        return move; // TODO modify the return statement to return your agent move
    }
}