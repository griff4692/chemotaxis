package chemotaxis.g9;

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

//		ArrayList<Point> bestPath = shortestPath(grid);
//		Integer midPoint = bestPath.size() / 2;

		if (currentTurn % 2 == 1) {
			chemicals.add(ChemicalType.BLUE);
//			chemicalPlacement.location = bestPath.get(bestPath.size()-1);
			chemicalPlacement.location = this.target;
		} else {
			chemicals.add(ChemicalType.GREEN);
//			chemicalPlacement.location = bestPath.get(bestPath.size() / 2);

			Point midPoint = findValidMidpoint(grid);
			Point adjustedMidpoint = new Point(midPoint.x + 1, midPoint.y + 1);
			chemicalPlacement.location  = adjustedMidpoint;
		}

		chemicalPlacement.chemicals = chemicals;

		return chemicalPlacement;
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

	public ArrayList<Point> shortestPath(ChemicalCell[][] grid) {
		 ArrayList<ArrayList> queue = new ArrayList<>();
		 ArrayList<Point> start = new ArrayList<>();
		 start.add(this.start);
		 queue.add(start);

		 while (queue.size() > 0) {
			 ArrayList<Point> curPath = queue.remove(0);
			 Point endOfPath = curPath.get(curPath.size() - 1);
			 for(int i = -1; i < 2; i += 2) {
				 for (int j = -1; j < 2; j += 2) {
					 Point neighbor = new Point(endOfPath.x + i, endOfPath.y + j);
					 ArrayList<Point> newPath = new ArrayList<>(curPath);
					 newPath.add(neighbor);
//					 System.out.println(newPath.size());
					 if (neighbor.equals(this.target)) {
						 return newPath;
					 }
					 if (neighbor.x >= 0 && neighbor.x < this.size && neighbor.y >= 0 && neighbor.y < this.size) {
						 if (!grid[neighbor.x][neighbor.y].isBlocked()) {
							 if (!curPath.contains(neighbor)) {
								 queue.add(newPath);
							 }
						 }
					 }
				 }
			 }
		 }
		 throw new Error("shouldn't get here");
	}
}
