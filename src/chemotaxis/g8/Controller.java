package chemotaxis.g8;

import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.SimPrinter;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

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
	static int[][] DIR = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
	ArrayList<Point> shortestPath;
	ArrayList<Point> selectedCells;
	static int INTERVAL = 4;  // drop chemical for every INTERVAL steps in the shortest path
	int offset;  // the start turn number of current round of dropping chemicals over the shortest path
	Point start, target;
	boolean needReminder = false;
	Point reminder = null;
	ArrayList<Point> prevLocations = null;

	static private ArrayList<Point> generateShortestPath(Point start, Point target, Integer size, ChemicalCell[][] grid) {
		int[][] dis = new int[size + 1][size + 1];
		for (int i = 1; i <= size; ++i) for (int j = 1; j <= size; ++j) dis[i][j] = -1;
		Point[][] from = new Point[size + 1][size + 1];

		Queue<Point> q = new LinkedList<>();
		q.add(start);
		while (true) {
			Point u = q.poll();
			if (u.equals(target)) break;
			for (int i = 0; i < 4; ++i) {
				Point v = new Point(u.x + DIR[i][0], u.y + DIR[i][1]);
				if (v.x <= 0 || v.y <= 0 || v.x > size || v.y > size) continue;
				if (grid[v.x - 1][v.y - 1].isBlocked() || dis[v.x][v.y] != -1) continue;
				dis[v.x][v.y] = dis[u.x][u.y] + 1;
				from[v.x][v.y] = u;
				q.add(v);
			}
		}

		ArrayList<Point> res = new ArrayList<>();
		while (true) {
			res.add(target);
			if (target == start) break;
			target = from[target.x][target.y];
		}
		Collections.reverse(res);
		return res;
	}

	private ArrayList<Point> selectCells(ArrayList<Point> path, int interval) {
		int n = path.size();
		ArrayList<Point> res = new ArrayList<>();
		for (int i = 1; i < n; i += interval) {
			res.add(path.get(i));
		}
		if (res.get(res.size() - 1) != target) res.add(target);
		return res;
	}



	public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter, Integer agentGoal, Integer spawnFreq) {
		super(start, target, size, grid, simTime, budget, seed, simPrinter, agentGoal, spawnFreq);
		shortestPath = generateShortestPath(start, target, size, grid);
		selectedCells = selectCells(shortestPath, INTERVAL);

		offset = 1;
		this.start = start;
		this.target = target;
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
		ChemicalPlacement res = new ChemicalPlacement();

		// this should check to make sure all agents are on right path
		for (int agentIndex = 0; agentIndex < locations.size(); agentIndex++) {
			Point agentLocation = locations.get(agentIndex);

			if (!shortestPath.contains(agentLocation)){
				offset += 1;

				// TODO fix the path when the agent is one cell off from the previously correct path
//				if (prevLocations != null) {
//					Point prevAgentLocation = prevLocations.get(agentIndex);
//					if (shortestPath.contains(prevAgentLocation)) {
//						int indexOfV = shortestPath.indexOf(prevAgentLocation);
//						res.location = shortestPath.get(indexOfV + 3);
//						res.chemicals.add(ChemicalType.BLUE);
//
//						// update prevLocations
//						prevLocations = locations;
//						return res;
//					}
//				}

				for (int i = 0; i < 4; ++i) {
					Point v = new Point(agentLocation.x + DIR[i][0], agentLocation.y + DIR[i][1]);
					if (shortestPath.contains(v)) {
						int indexOfV = shortestPath.indexOf(v);
//						res.location = v;
						res.location = shortestPath.get(indexOfV + 2);
						res.chemicals.add(ChemicalType.BLUE);

						// set reminder
						needReminder = true;
						reminder = shortestPath.get(Math.min(indexOfV + 4, shortestPath.size() - 1));

						// update prevLocations
						prevLocations = locations;
						return res;
					}
				}
			}
		}

		// start a new round when previous one is over
		if (currentTurn > INTERVAL * selectedCells.size() + offset) {
			if (locations.contains(start)) {
				offset = currentTurn;
			}
		}

		// reminder
		if (needReminder && reminder != null) {
			res.chemicals.add(ChemicalType.BLUE);
			res.location = reminder;
			needReminder = false;
			reminder = null;

			// update prevLocations
			prevLocations = locations;
			return res;
		}


		if ((currentTurn - offset) % INTERVAL == 0) {
			int d = (currentTurn - offset) / INTERVAL;
			if (d < selectedCells.size()) {
				res.chemicals.add(ChemicalType.BLUE);
				res.location = selectedCells.get(d);

				// update prevLocations
				prevLocations = locations;
				return res;
			}
		}

		// update prevLocations
		prevLocations = locations;
		return res;
	}
}
