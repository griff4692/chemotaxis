package chemotaxis.g4;

import java.awt.Point;
import java.sql.Array;
import java.util.*;
// import java.util.List;
// import java.util.ArrayList;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;

public class Controller extends chemotaxis.sim.Controller {

	ArrayList<Point> path = null;
	ArrayList<Point> placementCells = null;
	ArrayList<ChemicalType> colorPath = null;
	Integer index = 0;


	Integer time_interval = 4;
	Integer path_interval = 5;
	Integer chemical_color = 0;

	/**
	 * Controller constructor
	 *
	 * @param start       start cell coordinates
	 * @param target      target cell coordinates
	 * @param size           grid/map size
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

	Boolean pointInBounds(Integer length, Point p){
		if(p.x >= 1 && p.x <= length && p.y >= 1 && p.y <= length){
			return true;
		}
		return false;
	}

	static Integer getIntervals(ArrayList<Point> current){
		ArrayList<Integer> values = new ArrayList<>();
		for (int i=1;i<current.size();i=i+1) {
			Point curr = current.get(i);
			Integer curr_x = curr.x;
			Integer curr_y = curr.y;
			Point prev = current.get(i - 1);
			Integer prev_x = prev.x;
			Integer prev_y = prev.y;

			if (curr_x - prev_x < 0) {
				values.add(1);
			} else if (curr_x - prev_x > 0) {
				values.add(2);
			} else if (curr_y - prev_y < 0) {
				values.add(3);
			} else {
				values.add(4);
			}
		}

		Integer numIntervals = 0;

		for (int i=1;i<values.size();i=i+1){
			Integer curr = values.get(i);
			Integer prev = values.get(i);
			if (curr == prev) {
				continue;
			}
			else{
				numIntervals = numIntervals + 1;
			}
		}

		return numIntervals;
	}

	static class CustomIntegerComparator implements Comparator<ArrayList<Point>> {

		@Override
		public int compare(ArrayList<Point> o1, ArrayList<Point> o2) {
			Integer numIntervalso1 = getIntervals(o1);
			Integer numIntervalso2 = getIntervals(o2);
			if (numIntervalso1 < numIntervalso2){
				return -1;}
			else{
				return 1;
			}
		}
	}

	ArrayList<Point> getPath(ChemicalCell[][] grid){
		int length = grid.length;

		ArrayList<Point> path = new ArrayList<Point>();
		path.add(start);

		PriorityQueue<ArrayList<Point>> q = new PriorityQueue(new CustomIntegerComparator());
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


	ArrayList<Point> getPathOld(ChemicalCell[][] grid){
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

			placementCells = new ArrayList<Point>();
			for (int i=1; i<path.size(); i++){
				if(i%path_interval == 0){
					placementCells.add(path.get(i));
				}
			}
			if(path.size()%4!=0){
				placementCells.add(path.get(path.size()-1));
			}

			colorPath = new ArrayList<ChemicalType>();
			for (int i=0; i<placementCells.size(); i++) {
				if (i % 3 == 0) {
					colorPath.add(ChemicalType.RED);
				} else if (i % 3 == 1) {
					colorPath.add(ChemicalType.GREEN);
				} else {
					colorPath.add(ChemicalType.BLUE);
				}
			}
		}


		Point currentLocation = placementCells.get(index%placementCells.size());
		chemicalPlacement.location = currentLocation;
		ArrayList<ChemicalType> chemicals = new ArrayList<>();
		chemicals.add(colorPath.get(index%colorPath.size()));
		chemicalPlacement.chemicals = chemicals;
		index = index + 1;


		return chemicalPlacement;
	}
}