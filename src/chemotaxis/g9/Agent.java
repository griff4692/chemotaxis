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

        // blue the best, green helps, hate red
        ChemicalCell.ChemicalType highPriority = ChemicalCell.ChemicalType.BLUE;
        ChemicalCell.ChemicalType medPriority = ChemicalCell.ChemicalType.GREEN;

        // if green and no blue, follow green
        // green and blue, go to blue
        double highestGreen = currentCell.getConcentration(medPriority);
        DirectionType highestGreenDirection = DirectionType.SOUTH;
        double highestBlue = currentCell.getConcentration(highPriority);
        DirectionType highestBlueDirection = null;

        for (DirectionType directionType : neighborMap.keySet()) {
            if (highestGreen <= neighborMap.get(directionType).getConcentration(medPriority)) {
                highestGreen = neighborMap.get(directionType).getConcentration(medPriority);
                highestGreenDirection = directionType;
            }
            if (highestBlue <= neighborMap.get(directionType).getConcentration(highPriority)) {
                highestBlue = neighborMap.get(directionType).getConcentration(highPriority);
                highestBlueDirection = directionType;
            }
        }

        if (highestBlue != 0) {
            move.directionType = highestBlueDirection;
        } else {
            move.directionType = highestGreenDirection;
        }

        return move; // TODO modify the return statement to return your agent move
    }
}