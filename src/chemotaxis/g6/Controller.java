package chemotaxis.g6;

import java.awt.Point;
import java.util.*;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;

public class Controller extends chemotaxis.sim.Controller {

	ArrayList<Point> path = null;
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

	Boolean pointInBounds(Integer length, Point p){
		if(p.x >= 1 && p.x <= length && p.y >= 1 && p.y <= length){
			return true;
		}
		return false;
	}
	ArrayList<Point> getPath(ChemicalCell[][] grid){
		int length = grid.length;

		ArrayList<Point> path = new ArrayList<Point>();
		path.add(start);

		Queue<ArrayList<Point>> q = new LinkedList<>();
		q.add(path);

		Set<Point> reached = new HashSet<Point>();

		while(true) {
			path = q.remove();
			Point p = path.get(path.size() - 1);
			if(p.x == target.x && p.y == target.y){
				return path;
			}

			ArrayList<Point> neighbors = new ArrayList<Point>();
			neighbors.add(new Point(p.x, p.y + 1));
			neighbors.add(new Point(p.x, p.y - 1));
			neighbors.add(new Point(p.x + 1, p.y));
			neighbors.add(new Point(p.x - 1, p.y));

			for(Point neighbor: neighbors){
				if(pointInBounds(length, neighbor) && !reached.contains(neighbor) && grid[neighbor.x - 1][neighbor.y - 1].isOpen()){
					ArrayList<Point> newPath = new ArrayList<Point>(path);
					newPath.add(neighbor);
					q.add(newPath);
					reached.add(neighbor);
				}
			}
		}
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


		simPrinter = new SimPrinter(true);

		if(path == null){
			simPrinter.println("creating path");
			path = getPath(grid);
		}



		if(currentTurn%10 == 1){
			List<ChemicalType> chemicals = new ArrayList<>();
			chemicals.add(ChemicalType.RED);
	
			chemicalPlacement.location = new Point(4, 4);
			chemicalPlacement.chemicals = chemicals;	
		}
		else if(currentTurn%10 == 5){
			List<ChemicalType> chemicals = new ArrayList<>();
			chemicals.add(ChemicalType.GREEN);
	
			chemicalPlacement.location = new Point(8, 8);
			chemicalPlacement.chemicals = chemicals;	
		}
		
		return chemicalPlacement;
	}
}
