package chemotaxis.g5;

import java.awt.GridBagConstraints;
import java.util.Queue;

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;

public class Controller extends chemotaxis.sim.Controller {

	List<Point> routeList = new ArrayList<Point>();
	int colorIndex = 0;
	int lastSpotIndex = 0;
	boolean endReached = false;
	int cornerIndex = 0; 
	Point previousPoint = null;
	List<Point> corners;
	/**
	 * Controller constructor
	 *
	 * @param start       start cell coordinates
	 * @param target      target cell coordinates
	 * @param size        grid/map size
	 * @param simTime     simulation time
	 * @param budget      chemical budget
	 * @param seed        random seed
	 * @param simPrinter  simulation printer
	 *
	 */

	public Controller(Point start, Point target, Integer size, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
		super(start, target, size, simTime, budget, seed, simPrinter);
	}

	/**
	 * Apply chemicals to the map
	 *
	 * @param currentTurn         current turn in the simulation
	 * @param chemicalsRemaining  number of chemicals remaining
	 * @param currentLocation     current location of the agent
	 * @param grid                game grid/map
	 * @return                    a cell location and list of chemicals to apply
	 *
	 */
	@Override
	public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, Point currentLocation, ChemicalCell[][] grid) {

		Point nextCorner, nextPlacement;

		List<ChemicalType> chemicals = new ArrayList<>();

		ChemicalPlacement chemicalPlacement = new ChemicalPlacement();

		if (currentTurn == 1) { 
			simPrinter.println(currentLocation.x + "-" + currentLocation.y);
			simPrinter.println("Test1");
			simPrinter.println(grid[1][2].isBlocked());
			simPrinter.println(grid[5][3].isBlocked());
			recursiveMazeSearch(1,1,1,2,grid);
			simPrinter.println("Test2");
			/*
			chemicalPlacement.location = new Point(target.x, target.y);
			chemicals.add(ChemicalType.RED);
			chemicalPlacement.chemicals = chemicals;
			return chemicalPlacement;
			*/
		}


	
		if (currentTurn != 1) { 
			int currentXIndex = currentLocation.x - 1;
			int currentYIndex = currentLocation.y - 1;
			int prevXIndex = previousPoint.x - 1;
			int prevYIndex = previousPoint.y - 1;
			int delta_x = 0; 
			int delta_y = 0;
			if ((currentXIndex - prevXIndex == 1)) {
				simPrinter.println("hit condition 1");
				if (currentXIndex + 1 < grid.length && !recursiveMazeSearch(currentXIndex, currentYIndex, currentXIndex + 1, currentYIndex, grid))  {
					chemicals.add(ChemicalType.GREEN);
					chemicalPlacement.location = new Point(currentLocation.x+ 1, currentLocation.y);
				}
			} 
			else if (currentYIndex - prevYIndex == 1) {
				simPrinter.println("hit condition 2");
				if (currentYIndex + 1 < grid[0].length && !recursiveMazeSearch(currentXIndex, currentYIndex, currentXIndex, currentYIndex + 1, grid)) {
					simPrinter.println("hit condition 2 should be true");
					chemicals.add(ChemicalType.GREEN);
					chemicalPlacement.location = new Point(currentLocation.x, currentLocation.y + 1);
				}
			}
			else if (currentYIndex - prevYIndex == -1) {
				simPrinter.println("hit condition 3");
				if (currentYIndex - 1 >= 0 && !recursiveMazeSearch(currentXIndex, currentYIndex, currentXIndex, currentYIndex - 1, grid)) {
					chemicals.add(ChemicalType.GREEN);
					chemicalPlacement.location = new Point(currentLocation.x, currentLocation.y - 1);
				}
			}
			else {
				if (currentXIndex - 1 >= 0 && !recursiveMazeSearch(currentXIndex, currentYIndex, currentXIndex - 1, currentYIndex, grid)) {
					simPrinter.println("hit condition 4");
					chemicals.add(ChemicalType.GREEN);
					chemicalPlacement.location = new Point(currentLocation.x - 1, currentLocation.y);
				}
			}		
		}

		chemicalPlacement.chemicals = chemicals;
		previousPoint = currentLocation;
		return chemicalPlacement;
	}
	 	
	private boolean dfsRecursiveSearch(int x, int y, boolean[][] visited, ChemicalCell[][] grid) {
		simPrinter.println(x + "-" + y);
		if (x == target.x && y == target.y) { 
			simPrinter.println("true hit");
			return true;
		}
		if (x < 0 || x >= grid.length || y < 0 || y >= grid[0].length || grid[x][y].isBlocked() || visited[x][y]) { 
			simPrinter.println("false hit");
			return false;
		}
		visited[x][y] = true; 
		return dfsRecursiveSearch(x-1, y, visited, grid) || dfsRecursiveSearch(x+1, y, visited, grid) || dfsRecursiveSearch(x, y+1, visited, grid) || dfsRecursiveSearch(x, y-1, visited, grid); 
	}


	private boolean recursiveMazeSearch(int x, int y, int nextX, int nextY, ChemicalCell[][] grid) { 
		simPrinter.println(x + "-" + y + ", to " + nextX + " " + nextY);
		boolean[][] visited = new boolean[grid.length][grid[0].length];
		visited[x][y] = true;
		return dfsRecursiveSearch(nextX, nextY, visited, grid);
	}

	public void setNextPlacement(Point nextCorner, ChemicalPlacement chemicalPlacement, List<ChemicalType> chemicals){
		
		Point nextSpot = new Point();

		for (int i = 0; i < 5; i++){
			nextSpot = routeList.get(i + lastSpotIndex);
			
			if (nextSpot.equals(this.target)){
				endReached = true;
				break;
			}
			else if (nextSpot.equals(nextCorner))
				break;
		}

		chemicalPlacement.location = nextSpot;
		lastSpotIndex = routeList.indexOf(nextSpot);
		//simPrinter.println("Drop Point: " +nextSpot.x + " " + nextSpot.y + " At: " + lastSpotIndex);

		switch (colorIndex) {
			case 0: chemicals.add(ChemicalType.RED);
					colorIndex = 1;
					break; 
			case 1:	chemicals.add(ChemicalType.BLUE);
					colorIndex = 2;
					break;
			case 2: chemicals.add(ChemicalType.GREEN);
					colorIndex = 0;
					break;	
		}
		chemicalPlacement.chemicals = chemicals;
	}

	

	public void populateRouteList(){
		for (int j = 1; j < 5; j++){
			Point k = new Point();
			k.x = 1;
			k.y = j;
			routeList.add(k);
		}
		for (int j = 1; j < 13; j++){
			Point k = new Point();
			k.x = 1 + j;
			k.y = 4;
			routeList.add(k);
		}
		for (int j = 1; j < 6; j++){
			Point k = new Point();
			k.x = 13;
			k.y = 4 + j;
			routeList.add(k);
		}
		for (int j = 1; j < 9; j++){
			Point k = new Point();
			k.x = 13 + j;
			k.y = 9;
			routeList.add(k);
		}
		for (int j = 1; j < 17; j++){
			Point k = new Point();
			k.x = 21;
			k.y = 9+j;
			routeList.add(k);
		}
		for (int j = 1; j < 5; j++){
			Point k = new Point();
			k.x = 21 + j;
			k.y = 25;
			routeList.add(k);
		}
	}

	public List<Point> getShortestPath(Point start, Point target, ChemicalCell[][] grid) {
		
		boolean[][] visited = new boolean[grid.length][grid[0].length];
		
		Node source = new Node(start.x, start.y);
		Queue<Node> queue = new LinkedList<Node>(); 
		queue.add(source);
		//simPrinter.println(start.x);
		//simPrinter.println(start.y);
		Node solution = null; 
		while (!queue.isEmpty()) {
			Node popped = queue.poll(); 
			if (popped.x == target.x && popped.y == target.y) {
				//simPrinter.println("reached target: ");
				solution = popped;
				break;
			}
			else if (!visited[popped.x-1][popped.y-1] && !grid[popped.x-1][popped.y-1].isBlocked()) {
				visited[popped.x-1][popped.y-1] = true;
				List<Node> neighborList = addNeighbors(popped, grid, visited);

				queue.addAll(neighborList);
			}
		}
		//simPrinter.println("PRE");
		List<Point> path = new LinkedList<Point>(); 
		while (solution != null) {
			path.add(new Point(solution.x, solution.y));
			//simPrinter.println(solution.x + " " + solution.y);
			solution = solution.parent;
			
		}
		//simPrinter.println("POST");
		Collections.reverse(path);
		for (Point p: path) {
 				//simPrinter.println(p.x + " " + p.y);
 		}
		return path;
	}
	
	private List<Node> addNeighbors(Node current, ChemicalCell[][] grid, boolean[][] visited) {
		List<Node> list = new LinkedList<Node>();
		//simPrinter.println("entered method");
		//simPrinter.println(current.x);
		//simPrinter.println(current.y);
		
		if((current.x - 1 > 0) && !visited[current.x - 2][current.y - 1]) {
			Node currNode = new Node(current.x-1, current.y);
			currNode.parent = current;
			list.add(currNode);
			//simPrinter.println("added 1: " + currNode.x + " " + currNode.y);
		}
		if((current.x + 1 <= grid.length) && !visited[current.x][current.y - 1]) {
			Node currNode = new Node(current.x+1, current.y);
			currNode.parent = current;
			list.add(currNode);
			//simPrinter.println("added 2: " + currNode.x + " " + currNode.y);
		}
		if((current.y - 1 > 0) && !visited[current.x - 1][current.y - 2]) {
			Node currNode = new Node(current.x, current.y - 1);
			currNode.parent = current;
			list.add(currNode);
			//simPrinter.println("added 3: " + currNode.x + " " + currNode.y);
		}
		if((current.y + 1 <= grid.length) && !visited[current.x - 1][current.y]) {
			Node currNode = new Node(current.x, current.y + 1);
			currNode.parent = current;
			list.add(currNode);
			//simPrinter.println("added 4: " + currNode.x + " " + currNode.y);

		}		
		return list;
	}
	
	class Node {
	    int x;
	    int y; 
	    Node parent;
	    
	    public Node(int x, int y) {
	    	this.x = x;
	    	this.y = y;
	    }
	}

	public Point getNextCorner(){
		int i = 0;
		Point lastSpot = routeList.get(lastSpotIndex);
		//simPrinter.println("Point: " +lastSpot.x + " " + lastSpot.y + " At: " + lastSpotIndex);
		if (!lastSpot.equals(target))
			while (lastSpot.x == routeList.get(lastSpotIndex + i).x || lastSpot.y == routeList.get(lastSpotIndex + i).y){
				if (routeList.get(lastSpotIndex + i).equals(target)){
					break;
				}
				i++;
			}
		return routeList.get(i + lastSpotIndex);
	}

	public boolean isCornerInVicinity(ChemicalCell[][] grid, Point currentLocation, Point nextCorner) {
		if (currentLocation.x == nextCorner.x && currentLocation.y == nextCorner.y - 1) {
			return true;
		}
		if (currentLocation.x == nextCorner.x && currentLocation.y == nextCorner.y + 1) {
			return true;
		}
		if (currentLocation.x == nextCorner.x - 1 && currentLocation.y == nextCorner.y) {
			return true;
		}
		if (currentLocation.x == nextCorner.x + 1 && currentLocation.y == nextCorner.y - 1) {
			return true;
		}
		return false;
	}

	public List<Point> getCompactPathRepresentation() {
		 
		Point curr = routeList.get(0);
		int prevDelta = 0; 
		int UP = 1;
		int DOWN = 2; 
		int LEFT = 3; 
		int RIGHT = 4; 
		List<Point> changes = new LinkedList<Point>();
		for (int i = 1; i<routeList.size(); i++) { 
			Point next = routeList.get(i);
			int delta_x = next.x - curr.x;
			int delta_y = next.y - curr.y; 
			int currentDelta; 
			if (delta_x == 1) { 
				currentDelta = RIGHT;
			}
			else if (delta_x == -1) { 
				currentDelta = LEFT;
			}
			else if (delta_y == 1) { 
				currentDelta = UP;
			}
			else { 
				currentDelta = DOWN;
			}
			if (currentDelta != prevDelta) { 
				changes.add(next);
			}
			prevDelta = currentDelta;
			curr = next; 
		}
		return changes;
	}
}