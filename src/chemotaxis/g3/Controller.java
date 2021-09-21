package chemotaxis.g3;

import java.awt.Point;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;

public class Controller extends chemotaxis.sim.Controller {
	ArrayList<Integer> shortest_path;
	Hashtable<Integer, Point> node_to_point;
	Hashtable<Point, Integer> point_to_node;
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
	public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
		super(start, target, size, grid, simTime, budget, seed, simPrinter);
		// dijkestra: https://www.geeksforgeeks.org/dijkstras-shortest-path-algorithm-greedy-algo-7/
		shortest_path = new ArrayList<Integer>();
		node_to_point = new Hashtable<Integer, Point>();
		point_to_node = new Hashtable<Point, Integer>();


		int k = 0;
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				if (grid[i][j].isOpen()) {
					Point curr = new Point(i, j);
					node_to_point.put(k, curr);
					point_to_node.put(curr, k);
					k++;
				}
			}
		}

		/*
		TODO:
		- Build Matrix Representation of graph
		- Dijsktra
		 */

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
		/* Inspired by Nikhilesh Belulkar's idea from class discussion:
		“Build a gradient of chemicals to guide the agent towards the
		goal block (have higher concentrations near the goal block,
		lower concentrations nearer to the spawn block)“
		 */
		ChemicalPlacement chemicalPlacement = new ChemicalPlacement();

		if (currentTurn%5 == 1) {
			List<ChemicalType> chemicals = new ArrayList<>();
			chemicals.add(ChemicalType.BLUE);
			chemicalPlacement.location = new Point(this.target.x, this.target.y);
			chemicalPlacement.chemicals = chemicals;

		}



		return chemicalPlacement;
	}
}
