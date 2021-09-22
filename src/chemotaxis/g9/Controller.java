package chemotaxis.g9;

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;

public class Controller extends chemotaxis.sim.Controller {
	List<Point> shortestPathList;
	private int incrementBy;
	private int colorCounter = 0;
	private int currentPathIndex = 1;
	private int idealChemicalIncrement = 10;
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
		this.shortestPathList = shortestPath(grid);	
		this.incrementBy = decidePlacementStrategy(this.shortestPathList, idealChemicalIncrement, budget);
	}
	
	// will this need to keep track of simTime?
	public int decidePlacementStrategy(List<Point> shortestPathList, int idealChemicalIncrement, int budget) {
		budget --; // take out for the beginning chemical
		budget --; // take out another for the ending chemical
		int pathLength = shortestPathList.size();
		float singlePathIncrement = pathLength / budget;
		// ideal chemical increment will not make it the full path.
		if (singlePathIncrement > idealChemicalIncrement)
			return (int) Math.ceil(singlePathIncrement);
		// here, we need to choose some criteria to lower the idealChemicalIncrement based on the expected loops (single path or multiple) / saturation we want.
		return idealChemicalIncrement;
	};


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
		List<ChemicalType> chemicals = new ArrayList<>();
		ChemicalType[] rotation = {ChemicalType.RED, ChemicalType.GREEN, ChemicalType.BLUE};
		
		// drop at current location
		Point pointToPlace;
		pointToPlace = this.shortestPathList.get(currentPathIndex);
		chemicalPlacement.location = new Point(pointToPlace.x + 1, pointToPlace.y + 1);
		
		// pick right chemical in sequence
		chemicals.add(rotation[colorCounter]);
		colorCounter = (colorCounter + 1) % 3;
		
		// increment steps
		// handle wraparound cases 
		int pathLength = this.shortestPathList.size();
		int nextPathIndex = currentPathIndex + incrementBy;
		// if we are on the last step, move to the first step, and make sure we reset the color counter
		if (currentPathIndex == pathLength - 1) {
			nextPathIndex = 1;
			colorCounter = 0;
		} else if (nextPathIndex > pathLength - 1) { // if we are about to wrap around, put us at the last step.
			nextPathIndex = pathLength - 1;
		}
		
		currentPathIndex = nextPathIndex;
		chemicalPlacement.chemicals = chemicals;

		return chemicalPlacement;
	}

	

	public ArrayList<Point> shortestPath(ChemicalCell[][] grid) {
		 ArrayList<ArrayList> queue = new ArrayList<>();
		 ArrayList<Point> start = new ArrayList<>();
		 HashMap<Point, Boolean> visited = new HashMap<Point, Boolean>();
		 int[][] moves = {{-1,0}, {1,0}, {0,1}, {0,-1}};
		 // account off by one
		 Point startPoint = new Point(this.start.x - 1, this.start.y - 1);
		 Point targetPoint = new Point(this.target.x - 1, this.target.y - 1);
		 start.add(startPoint);
		 queue.add(start);
		 while (queue.size() > 0) {
			 ArrayList<Point> curPath = queue.remove(0);
			 Point endOfPath = curPath.get(curPath.size() - 1);
			 for (int[] move : moves) {
				 int deltaX = move[0];
				 int deltaY = move[1];
				 Point neighbor = new Point(endOfPath.x + deltaX, endOfPath.y + deltaY);
				 if (
					neighbor.x >= 0 && 
					neighbor.x < this.size && 
					neighbor.y >= 0 && 
					neighbor.y < this.size &&
					!grid[neighbor.x][neighbor.y].isBlocked() &&
					!visited.getOrDefault(neighbor, false)) {
					 visited.put(neighbor, true);
					 curPath.add(neighbor);
					 if (neighbor.equals(targetPoint)) {
						 return curPath;
					 } else {
						 queue.add(new ArrayList<Point>(curPath));
					 }
					 curPath.remove(curPath.size() - 1);
				 }
			 }
			
		 }
		 throw new Error("shouldn't get here");
	}
	
/*
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
	public Point findValidMidpoint(ChemicalCell[][] grid) {
		 Point midpoint = new Point((this.start.x + this.target.x) / 2, (this.start.y + this.target.y ) / 2);
		 ArrayList<Point> cellList =  new ArrayList<>();

		 for (int i = 0; i < this.size; i ++) {
			 for (int j = 0; j < this.size; j ++) {
				 if (!grid[i][j].isBlocked()) {
					 cellList.add(new Point(i,j));
				 }
			 }
		 }

		 Collections.sort(cellList, new Comparator<Point>() {
			 @Override
			 public int compare(Point lhs, Point rhs) {
				 int lhsDistance = Math.abs(lhs.x - midpoint.x) + Math.abs(lhs.y - midpoint.y);
				 int rhsDistance = Math.abs(rhs.x - midpoint.x) + Math.abs(rhs.y - midpoint.y);
				 if (lhsDistance == rhsDistance) {
					 return 0;
				 }
				return lhsDistance > rhsDistance ? 1 : -1;
			 }
		 });
		 return cellList.get(0);
	}
*/
}
