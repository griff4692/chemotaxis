package chemotaxis.g2;

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Collections;
import java.util.LinkedList;


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
	
	private class Node {
		private int x = 0;
		private int y = 0;
		private Node prev;

		public Node(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public Node getPrev(){
			return this.prev;
		}

		public int getX(){
			return this.x;
		}
		public int getY(){
			return this.y;
		}

	}

	private List<Point> getShortestPath(Point p, Point target, ChemicalCell[][] grid) {
		Queue<Node> queue = new LinkedList<Node>();
		boolean[][] visited = new boolean[grid.length][grid[0].length];
		Node start = new Node((int) p.getX(), (int) p.getY());
		queue.add(start);
		List<Point> path = new ArrayList<Point>();
		int targetX = (int)target.getX();
		int targetY = (int)target.getY();
		while (!queue.isEmpty()) {
			Node curNode = queue.poll();
			System.out.println("Cur node: " + curNode.getX() + " and " + curNode.getY());
			if (curNode.getX() == targetX && curNode.getY() == targetY) {
				while (curNode != null) {
					path.add(new Point(curNode.getX(), curNode.getY()));
					curNode = curNode.prev;
				}
				Collections.reverse(path);
				break;
			}
			for (Node nei : getNeighbors(curNode, grid, visited)){
				visited[nei.getX()][nei.getY()] = true;
				queue.add(nei);
			}
		}
		return path;
	}
	
	private List<Node> getNeighbors(Node n, ChemicalCell grid[][], boolean[][] visited){
		List<Node> neighbors = new ArrayList<Node>();
		int x = n.getX();
		int y = n.getY();
		if (isCellValid(grid, visited, x - 1, y)){
			Node next = new Node(x - 1, y);
			next.prev = n;
			neighbors.add(next);
		}
		if (isCellValid(grid, visited, x + 1, y )){
			Node next = new Node(x + 1, y );
			next.prev = n;
			neighbors.add(next);
		}
		if (isCellValid(grid, visited, x , y - 1)){
			Node next = new Node(x , y - 1);
			next.prev = n;
			neighbors.add(next);
		}
		if (isCellValid(grid, visited, x , y + 1)){
			Node next = new Node(x , y + 1);
			next.prev = n;
			neighbors.add(next);
		}
		return neighbors;
	}
	
	private boolean isCellValid(ChemicalCell grid[][], boolean visited[][], int x, int y) {
		return (x >= 0) && (x < grid.length) && (y >= 0) && (y < grid[0].length) && grid[x][y].isOpen() && !visited[x][y];
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

		List<Point> path = new ArrayList<Point>();

		path = this.getShortestPath(start, target, grid);

		ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
		int pathLength = path.size();
		int tempTurn;
		if ( currentTurn <= pathLength) {
			tempTurn = currentTurn -1;
		} else {
			tempTurn = (currentTurn -1) % pathLength;
		}
		tempTurn = tempTurn -1;
		int newX = path.get(tempTurn).x;
		int newY = path.get(tempTurn).y;

		List<ChemicalType> chemicals = new ArrayList<>();
		chemicals.add(ChemicalType.BLUE);

		chemicalPlacement.location = new Point(newX, newY);
		chemicalPlacement.chemicals = chemicals;

		return chemicalPlacement;
	}
}
