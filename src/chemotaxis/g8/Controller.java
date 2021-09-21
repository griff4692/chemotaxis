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



	public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
		super(start, target, size, grid, simTime, budget, seed, simPrinter);
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
		res.chemicals.add(ChemicalType.BLUE);

		// this should check to make sure all agents are on right path
		for (int i=0; i<locations.size(); i++) {
			Point agent_location = locations.get(i);
			if (!shortestPath.contains(agent_location)){
				Point above = new Point(agent_location.x, agent_location.y - 1);
				Point below = new Point(agent_location.x, agent_location.y + 1);
				Point right = new Point(agent_location.x + 1, agent_location.y);
				Point left = new Point(agent_location.x - 1, agent_location.y);
				if (shortestPath.contains(above)) {
					res.location = above;
					return res;
				}
				if (shortestPath.contains(below)) {
					res.location = below;
					return res;
				}
				if (shortestPath.contains(right)) {
					res.location = right;
					return res;
				}
				if (shortestPath.contains(left)) {
					res.location = left;
					return res;
				}
			}
		}

		// start a new round when previous one is over
		if (currentTurn > INTERVAL * selectedCells.size() + offset) {
			if (locations.contains(start)) {
				offset = currentTurn;
			}
		}


		if ((currentTurn - offset) % INTERVAL == 0) {
			int d = (currentTurn - offset) / INTERVAL;
			if (d < selectedCells.size()) {
				res.location = selectedCells.get(d);
				return res;
			}
		}

		return res;
	}
}
