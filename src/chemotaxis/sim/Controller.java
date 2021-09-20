package chemotaxis.sim;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;


public abstract class Controller {

	public Point start, target;
    public Integer size, simTime, budget, seed;
    public Random random;
    public SimPrinter simPrinter;

    /**
     * Controller constructor
     *
     * @param start       start cell coordinates
     * @param target      target cell coordinates
     * @param size        grid/map size
     * @param grid        game grid/map
     * @param simTime     simulation time
     * @param budget      chemical budget
     * @param seed        random seed
     * @param simPrinter  simulation printer
     *
     */
    public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
    	this.start = start;
    	this.target = target;
    	this.size = size;
    	this.simTime = simTime;
    	this.budget = budget;
        this.seed = seed;
        this.random = new Random(seed);
        this.simPrinter = simPrinter;
	}
    
    /**
     * Apply chemicals to the map
     *
     * @param currentTurn         current turn in the simulation
     * @param chemicalsRemaining  number of chemicals remaining
     * @param locations    current location of the agents
     * @param grid                game grid/map
     * @return                    a cell location and list of chemicals to apply
     *
     */
    public abstract ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid);
}