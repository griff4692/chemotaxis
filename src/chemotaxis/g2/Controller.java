package chemotaxis.g2;

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

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

	private List<Point> getShortestPath(Point p, Point target, ChemicalCell[][] grid,) {
		Queue<Node> queue = new LinkedList<Node>();
		boolean[][] visited = new boolean[grid.length][grid[0].length];
		Node start = new Node((int) p.getX(), (int) p.getY());
		queue.add(start);
		List<Point> path = new ArrayList<Point>();

		while (!queue.isEmpty()) {
			Node curNode = queue.poll();
			if (curNode.getX() == target.getX() && curNode.getY() == target.getY()) {
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
		if (isCellValid(grid, visited, x - 1, y - 1)){
			neighbors.add(new Node(x - 1, y - 1));
		}
		if (isCellValid(grid, visited, x + 1, y + 1)){
			neighbors.add(new Node(x + 1, y + 1));
		}
		if (isCellValid(grid, visited, x + 1, y - 1)){
			neighbors.add(new Node(x + 1, y - 1));
		}
		if (isCellValid(grid, visited, x - 1, y + 1)){
			neighbors.add(new Node(x - 1, y + 1));
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
		ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
		int closestIdx = this.closestToTarget(locations);
 		Point currentLocation = locations.get(closestIdx);
		int currentX = currentLocation.x;
		int currentY = currentLocation.y;

		int leftEdgeX = Math.max(1, currentX - 5);
		int rightEdgeX = Math.min(size, currentX + 5);
		int topEdgeY = Math.max(1, currentY - 5);
		int bottomEdgeY = Math.min(size, currentY + 5);

		int randomX = this.random.nextInt(rightEdgeX - leftEdgeX + 1) + leftEdgeX;
		int randomY = this.random.nextInt(bottomEdgeY - topEdgeY + 1) + topEdgeY ;

		List<ChemicalType> chemicals = new ArrayList<>();
		chemicals.add(ChemicalType.BLUE);

		chemicalPlacement.location = new Point(randomX, randomY);
		chemicalPlacement.chemicals = chemicals;

		return chemicalPlacement;
	}
}
