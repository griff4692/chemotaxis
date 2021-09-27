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

	List<Point> path ;
	List<Point> corners ;

	public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter, Integer agentGoal, Integer spawnFreq) {
		super(start, target, size, grid, simTime, budget, seed, simPrinter, agentGoal, spawnFreq);
		path = getShortestPath(start, target, grid);
		corners = findCorners(path);
		corners.add(path.get(0));
	}

	public int closestToTarget(List<Point> locations) {
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
		private int x, y;
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
		int targetX = (int)target.getX(), targetY = (int)target.getY();
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
	 * Gets the neighbor of the current cell
	 *
	 * @param n         current node (a cell in the grid)
	 * @param grid      game grid/map
	 * @param visited   a 2d array indicating whether a grid has been visited during BFS
	 * @return          a list of nodes that represents possible neighbors of the current cell
	 */
	private List<Node> getNeighbors(Node n, ChemicalCell[][] grid, boolean[][] visited){
		List<Node> neighbors = new ArrayList<Node>();
		int x = n.getX() - 1, y = n.getY() - 1;
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


	private List<Point> findCorners(List<Point> shortestPath){

		List<Point> corners = new ArrayList<Point>();
		if (shortestPath.size() <= 2){
			return corners;
		}
		Point a = shortestPath.get(0), b = shortestPath.get(1);
		Point prev;
		for (int i = 2; i < shortestPath.size() ; i++){
			Point c = shortestPath.get(i);
			int aX = (int)a.getX(), aY = (int)a.getY(), cX = (int)c.getX(), cY = (int)c.getY();
			if (aX - cX != 0 && aY - cY != 0){
				corners.add(b);
			}
			a = b;
			b = c;
		}
		return corners;
	}



	private boolean inPath(Point agent) {
		int pathLength = path.size();
		int pX = agent.x;
		int pY = agent.y;
		for (int i = 0; i < pathLength; i++){
			int tempX = path.get(i).x;
			int tempY = path.get(i).y;
			if (pX == tempX && pY == tempY) {
				return true;
			}
		}
		return false;
	}

	private Point nextCell(Point location) {
		int pathLength = path.size();
		int pX = location.x;
		int pY = location.y;
		boolean flag = false;
		for (int i =0; i < pathLength; i++) {
			if (flag) {
				return path.get(i);
			}
			int tempX = path.get(i).x;
			int tempY = path.get(i).y;
			if (pX == tempX && pY == tempY) {
				flag = true;
			}
		}
		return new Point(0, 0);
	}

	private int closestPathPoint(Point agent) {
		int closestDistance = 9999999;
		int closestIdx = 0;
		for(int i = 0; i < path.size(); i++) {
			int x = path.get(i).x;
			int y = path.get(i).y;
			int distance = Math.abs(x - agent.x) + Math.abs(y - agent.y);
			if(distance > 0 && distance < closestDistance) {
				closestIdx = i;
				closestDistance = distance;
			}
		}
		return closestIdx;
	}

	private boolean cellOccupied(Point p, List<Point> locations) {
		int x = p.x;
		int y = p.y;
		for (int i=0; i< locations.size(); i++) {
			if ( x == locations.get(i).x && y == locations.get(i).y) {
				return true;
			}
		}
		return false;
	}

	private List<Point> needSignal (List<Point> locations) {
		int x;
		int y;
		int tempX;
		int tempY;
		List<Point> agents = new ArrayList<>();
		for (int i =0; i < locations.size(); i++) {
			if (! inPath(locations.get(i))) {
				agents.add(locations.get(i));
			}
			x = locations.get(i).x;
			y = locations.get(i).y;
			for (int j =0; j < corners.size(); j++) {
				tempX = corners.get(j).x;
				tempY = corners.get(j).y;
				if (x == tempX && y == tempY) {
					agents.add(locations.get(i));
				}
			}
		}
		return agents;
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
		int newX = 0 ;
		int newY = 0 ;
		List<Point> lostAgents ;



			lostAgents = needSignal(locations);

			if (lostAgents.size() == 0) {
				return chemicalPlacement;
			} else {
				Point agent = lostAgents.remove(closestToTarget(lostAgents));
				Point next;
				if (inPath(agent)) {
					next = nextCell(agent);
					if (cellOccupied(next, locations)) {
						next = nextCell(next);
					}
					newX = next.x;
					newY = next.y;
				} else {
					int closestPathPointId = closestPathPoint(agent);
					next = path.get(closestPathPointId);
					int xDelta = next.x - agent.x;
					int yDelta = next.y - agent.y;
					if (xDelta < 0) {
						xDelta = -1;
					} else if (xDelta > 0) {
						xDelta = 1;
					}
					if (yDelta < 0) {
						yDelta = -1;
					} else if (yDelta > 0) {
						yDelta = 1;
					}
					newX = agent.x + xDelta;
					newY = agent.y + yDelta;
					while (cellOccupied(new Point(newX, newY), locations)) {
						newX = agent.x + xDelta;
						newY = agent.y + yDelta;
					}

				}
			}


		List<ChemicalType> chemicals = new ArrayList<>();
		chemicals.add(ChemicalType.BLUE);

		chemicalPlacement.location = new Point(newX, newY);
		chemicalPlacement.chemicals = chemicals;

		return chemicalPlacement;
	}
}
