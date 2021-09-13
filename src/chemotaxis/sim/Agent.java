package chemotaxis.sim;

import java.util.Map;


public abstract class Agent {

    public SimPrinter simPrinter;

    /**
     * Agent constructor
     *
     * @param simPrinter  simulation printer
     *
     */
    public Agent(SimPrinter simPrinter) {
        this.simPrinter = simPrinter;
	}

    /**
     * Move agent
     *
     * @param randomNum        random number available for agents
     * @param previousState    byte of previous state
     * @param currentCell  	   current cell
     * @param neighborMap      map of cell's neighbors
     * @return                 agent move
     *
     */
    public abstract Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap);
}