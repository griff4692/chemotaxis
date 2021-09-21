package chemotaxis.g2;

import java.awt.Point;
import java.util.*;

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
	 *  A private class that represents a grid's coordinate and its predecessor. Used in BFS implementation
	 */
	private class Node {
		private int x;
		private int y;
		private Node prev;

		public Node() {this(0, 0); }

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

		public void setPrev(Node prev){
			this.prev = prev;
		}

		@Override
		public String toString() {
			return "Node{" +
					"x=" + x +
					", y=" + y +
					", prev=" + prev +
					'}';
		}
	}

	/**
	 * BFS implementation that searches the shortest path between point p and the target on the grid
	 *
	 * @param p             a point on the grid/map
	 * @param target        the target point on the grid/map
	 * @param grid          game grid/map
	 * @return              a list of points that represent the shortest path between point p and the target
	 */
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
			if (curNode.getX() == targetX && curNode.getY() == targetY) {
				while (curNode != null) {
					path.add(new Point(curNode.getX(), curNode.getY()));
					curNode = curNode.getPrev();
				}
				Collections.reverse(path);
				break;
			}
			for (Node nei: getNeighbors(curNode, grid, visited)){
				visited[nei.getX() - 1][nei.getY() - 1] = true;
				queue.add(nei);
			}
		}
		return path;
	}

	/**
	 * Gets
	 *
	 * @param n         current node (a cell in the grid)
	 * @param grid      game grid/map
	 * @param visited   a 2d array indicating whether a grid has been visited during BFS
	 * @return          a list of nodes that represents possible neighbors of the current cell
	 */
	private List<Node> getNeighbors(Node n, ChemicalCell[][] grid, boolean[][] visited){
		List<Node> neighbors = new ArrayList<Node>();
		int x = n.getX() - 1;
		int y = n.getY() - 1;
		// Note: make sure to create new node based on an 1-indexed map
		if (isCellValid(grid, visited, x - 1, y)){
			Node next = new Node(x, y + 1);
			next.setPrev(n);
			neighbors.add(next);
		}
		if (isCellValid(grid, visited, x + 1, y )){
			Node next = new Node(x + 2, y + 1);
			next.setPrev(n);
			neighbors.add(next);
		}
		if (isCellValid(grid, visited, x , y - 1)){
			Node next = new Node(x + 1, y);
			next.setPrev(n);
			neighbors.add(next);
		}
		if (isCellValid(grid, visited, x , y + 1)){
			Node next = new Node(x + 1, y + 2);
			next.setPrev(n);
			neighbors.add(next);
		}
		return neighbors;
	}


	/**
	 *
	 * @param grid      game grid/map
	 * @param visited   a 2d array indicating whether a grid has been visited during BFS
	 * @param x         the x coordinate of a node (a cell on the grid)
	 * @param y         the y coordinate of a node (a cell on the grid)
	 * @return          whether the current cell is a valid cell that the agent can move to
	 */
	private boolean isCellValid(ChemicalCell grid[][], boolean visited[][], int x, int y) {
		return (x >= 0) && (x < grid.length) && (y >= 0) && (y < grid[0].length) && grid[x][y].isOpen() && !visited[x][y];
	}
}
