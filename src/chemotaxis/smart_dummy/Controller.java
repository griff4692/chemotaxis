package chemotaxis.smart_dummy;

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;

public class Controller extends chemotaxis.sim.Controller {
	
    /**
     * Controller constructor
     *
     * @param start       start cell coordinates
     * @param target      target cell coordinates
     * @param size     	  grid/map size
     * @param grid        game grid/map
     * @param simTime     simulation time
     * @param budget      chemical budget
     * @param seed        random seed
     * @param simPrinter  simulation printer
     *
     */
	public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter, Integer agentGoal, Integer spawnFreq) {
		super(start, target, size, grid, simTime, budget, seed, simPrinter, agentGoal, spawnFreq);
	}

	public int closestToTarget(ArrayList<Point> locations) {
		int closestDistance = 9999999;
		int closestIdx = 0;
		for(int i = 0; i < locations.size(); i++) {
			int x = locations.get(i).x;
			int y = locations.get(i).y;
			int distance = Math.abs(x - this.target.x) + Math.abs(y - this.target.y);
			if(distance > 0 && distance < closestDistance) {
				closestIdx = i;
				closestDistance = distance;
			}
		}
		return closestIdx;
	}

    /**
     * Apply chemicals to the map
     *
     * @param currentTurn         current turn in the simulation
     * @param chemicalsRemaining  number of chemicals remaining
     * @param locations     current locations of the agents
     * @param grid                game grid/map
     * @return                    a cell location and list of chemicals to apply
     *
     */
 	@Override
	public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid) {
 		ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
 		int closestIdx = this.closestToTarget(locations);
 		int x = locations.get(closestIdx).x;
 		int y = locations.get(closestIdx).y;

 		int xDelta = this.target.x - x;
 		int yDelta = this.target.y - y;

 		if(xDelta < 0) {
 			xDelta = -1;
		} else if(xDelta > 0) {
 			xDelta = 1;
		}

		if(yDelta < 0) {
			yDelta = -1;
		} else if(yDelta > 0) {
			yDelta = 1;
		}

 		List<ChemicalType> chemicals = new ArrayList<>();
 		chemicals.add(ChemicalType.BLUE);

 		int newX = x + xDelta;
 		int newY = y + yDelta;
 		
 		chemicalPlacement.location = new Point(newX, newY);
 		chemicalPlacement.chemicals = chemicals;
 		
 		return chemicalPlacement;
	} 	
}
