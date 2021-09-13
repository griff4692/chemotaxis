package chemotaxis.g5;

import java.awt.GridBagConstraints;
import java.util.Queue;

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;

public class ControllerShaoHua extends chemotaxis.sim.Controller {

	List<Point> routeList = new ArrayList<Point>();
	List<Integer> corners = new ArrayList<Integer>();
	int previousDirection;
	boolean endReached = false;
	Point previousPoint = null;
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

	public ControllerShaoHua(Point start, Point target, Integer size, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
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
		/*
		ChemicalPlacement chemicalPlacement = new ChemicalPlacement();

		if (currentTurn == 1) {
 			routeList = getShortestPath(start, target, grid);
 			
 			System.out.println("Path before");
 			for (Point p: routeList) {
 				System.out.println(p.x+" "+p.y);
 			}
 			routeList = recursiveImprovePath(routeList, grid);
 			System.out.println("Path after");
 			for (Point p: routeList) {
 				System.out.println(p.x+" "+p.y);
 			}
 			corners = getCorners(routeList);
 			int needChemicals = corners.size()+1;
 			if (needChemicals > chemicalsRemaining) {
				 simPrinter.println("No enough chemicals, need: "+needChemicals+", have: "+chemicalsRemaining);
				 
			}
 			int initialDirection = nextDirection(routeList.get(1), currentLocation);
 			if (initialDirection == 1) {
 				chemicalPlacement.chemicals.add(ChemicalType.RED);
 			} else if (initialDirection == 2) {
 				chemicalPlacement.chemicals.add(ChemicalType.GREEN);
 			} else if (initialDirection == 3) {
 				chemicalPlacement.chemicals.add(ChemicalType.BLUE);
 			}
 			if (initialDirection != 4) {
 				chemicalPlacement.location = currentLocation;
 			}
 			previousDirection = initialDirection;
 		}
		else {
			int corner = -1;
			for (int i: corners) {
				if (routeList.get(i).x == currentLocation.x && routeList.get(i).y == currentLocation.y) {
					corner = i;
				}
			}
			if (corner != -1) {
				Point next = routeList.get(corner+1);
				int nextDirection = nextDirection(next, currentLocation);
				if (previousDirection - nextDirection == 1 || previousDirection - nextDirection == -3) {
					chemicalPlacement.chemicals.add(ChemicalType.RED);
				} else if (previousDirection - nextDirection == -1 || previousDirection - nextDirection == 3) {
					chemicalPlacement.chemicals.add(ChemicalType.GREEN);
				}
				chemicalPlacement.location = currentLocation;
				previousDirection = nextDirection;
			}
		}
		previousPoint = currentLocation;
		return chemicalPlacement;
		*/
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
	
	public List<Integer> getCorners(List<Point> path) {
		List<Integer> corners = new ArrayList<Integer>();
		for (int i = 1; i<path.size()-1; i++) {
			Point before = path.get(i-1);
			Point after = path.get(i+1);
			if (after.x - before.x != 0 && after.y - before.y != 0) {
				corners.add(i);
			}
		}
		return corners;
	}
	
	public int nextDirection(Point next, Point currentLocation) {
		if (next.x- currentLocation.x == 1) {
			return 3;
		} else if (next.x - currentLocation.x == -1) {
			return 1;
		} else if (next.y - currentLocation.y == 1) {
			return 2;
		} else {
			return 4;
		}
	}
	
	public List<Point> recursiveImprovePath(List<Point> path, ChemicalCell[][] grid) {
		List<Integer> corners = getCorners(path);
		corners.add(0, 0);
		corners.add(path.size()-1);
		List<Point> newPath = new LinkedList();
		boolean allDone = true;
		while (corners.size() > 3) {
			int s = corners.get(0);
			int t = corners.get(3);
			Point start = path.get(s);
			Point target = path.get(t);
			List<Point> improvedPart = shortestPathLimitTurns(start, target, grid, t-s);
			if (improvedPart != null) {
				if (corners.get(3) != path.size()-1) {
					improvedPart.addAll(path.subList(corners.get(3)+1, path.size()));
				}
				path = improvedPart;
				allDone = false;
				break;
			} else {
				newPath.addAll(path.subList(0, corners.get(1)));
				path = path.subList(corners.get(1), path.size());
				corners = getCorners(path);
				corners.add(0, 0);
				corners.add(path.size()-1);
			}
		}
		if (allDone) {
			newPath.addAll(path);
			return newPath;
		}
		newPath.addAll(recursiveImprovePath(path, grid));
		return newPath;
	}
	
	public List<Point> getCornersAsPoints(List<Point> path, List<Integer> corners) {
		List<Point> cornerPoints = new LinkedList<Point>();
		for (int i: corners) {
			cornerPoints.add(path.get(i));
		}
		return cornerPoints;
	}

	public List<Point> getShortestPath(Point start, Point target, ChemicalCell[][] grid) {
		
		boolean[][] visited = new boolean[grid.length][grid[0].length];
		
		Node source = new Node(start.x, start.y);
		Queue<Node> queue = new LinkedList<Node>(); 
		queue.add(source);
		Node solution = null; 
		while (!queue.isEmpty()) {
			Node popped = queue.poll(); 
			if (popped.x == target.x && popped.y == target.y) {
				solution = popped;
				break;
			}
			else if (!visited[popped.x-1][popped.y-1] && !grid[popped.x-1][popped.y-1].isBlocked()) {
				visited[popped.x-1][popped.y-1] = true;
				List<Node> neighborList = addNeighbors(popped, grid, visited);

				queue.addAll(neighborList);
			}
		}
		List<Point> path = new LinkedList<Point>(); 
		while (solution != null) {
			path.add(new Point(solution.x, solution.y));
			solution = solution.parent;
			
		}
		Collections.reverse(path);
		return path;
	}
	
	public List<Point> shortestPathLimitTurns(Point start, Point target, ChemicalCell[][] grid, int depthLimit) {
		
		boolean[][] visited = new boolean[grid.length][grid[0].length];
		LNode source = new LNode(start.x, start.y, false, 0);
		Queue<LNode> queue = new LinkedList<LNode>();
		queue.add(source);
		LNode solution = null;
		while (!queue.isEmpty()) {
			LNode popped = queue.poll();
			if (popped.x == target.x && popped.y == target.y) {
				solution = popped;
				break;
			}
			else if (!visited[popped.x-1][popped.y-1] && !grid[popped.x-1][popped.y-1].isBlocked()) {
				visited[popped.x-1][popped.y-1] = true;
				List<LNode> neighborList = addLimitedNeighbors(popped, grid, visited, depthLimit);
				
				queue.addAll(neighborList);
			}
		}
		if (solution == null) {
			//System.out.println("Can't imporve between: "+start.x+" "+start.y+"; "+target.x+" "+target.y);
			return null;
		}
		List<Point> path = new LinkedList<Point>(); 
		while (solution != null) {
			path.add(new Point(solution.x, solution.y));
			solution = solution.parent;
			
		}
		Collections.reverse(path);
		
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
	
	private List<LNode> addLimitedNeighbors(LNode current, ChemicalCell[][] grid, boolean[][] visited, int depthLimit) {
		List<LNode> list = new LinkedList<LNode>();
		if (current.depth >= depthLimit) {
			return list;
		}
		else if (current.parent == null){
			if((current.x - 1 > 0) && !visited[current.x - 2][current.y - 1]) {
				LNode currNode = new LNode(current.x-1, current.y, current.turned, current.depth+1);
				currNode.parent = current;
				list.add(currNode);
				//simPrinter.println("added 1: " + currNode.x + " " + currNode.y);
			}
			if((current.x + 1 <= grid.length) && !visited[current.x][current.y - 1]) {
				LNode currNode = new LNode(current.x+1, current.y, current.turned, current.depth+1);
				currNode.parent = current;
				list.add(currNode);
				//simPrinter.println("added 2: " + currNode.x + " " + currNode.y);
			}
			if((current.y - 1 > 0) && !visited[current.x - 1][current.y - 2]) {
				LNode currNode = new LNode(current.x, current.y - 1, current.turned, current.depth+1);
				currNode.parent = current;
				list.add(currNode);
				//simPrinter.println("added 3: " + currNode.x + " " + currNode.y);
			}
			if((current.y + 1 <= grid.length) && !visited[current.x - 1][current.y]) {
				LNode currNode = new LNode(current.x, current.y + 1, current.turned, current.depth+1);
				currNode.parent = current;
				list.add(currNode);
				//simPrinter.println("added 4: " + currNode.x + " " + currNode.y);

			}
		}
		else {
			if (current.parent.x - current.x == 1) {
				if((current.x - 1 > 0) && !visited[current.x - 2][current.y - 1]) {
					LNode currNode = new LNode(current.x-1, current.y, current.turned, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 1: " + currNode.x + " " + currNode.y);
				}
			} else if (!current.turned){
				if((current.x - 1 > 0) && !visited[current.x - 2][current.y - 1]) {
					LNode currNode = new LNode(current.x-1, current.y, true, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 1: " + currNode.x + " " + currNode.y);
				}
			}
			if (current.parent.x - current.x == -1) {
				if((current.x + 1 <= grid.length) && !visited[current.x][current.y - 1]) {
					LNode currNode = new LNode(current.x+1, current.y, current.turned, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 2: " + currNode.x + " " + currNode.y);
				}
			} else if (!current.turned) {
				if((current.x + 1 <= grid.length) && !visited[current.x][current.y - 1]) {
					LNode currNode = new LNode(current.x+1, current.y, true, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 2: " + currNode.x + " " + currNode.y);
				}
			}
			if (current.parent.y - current.y == 1) {
				if((current.y - 1 > 0) && !visited[current.x - 1][current.y - 2]) {
					LNode currNode = new LNode(current.x, current.y - 1, current.turned, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 3: " + currNode.x + " " + currNode.y);
				}
			} else if (!current.turned) {
				if((current.y - 1 > 0) && !visited[current.x - 1][current.y - 2]) {
					LNode currNode = new LNode(current.x, current.y - 1, true, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 3: " + currNode.x + " " + currNode.y);
				}
			}
			
			if (current.parent.y - current.y == -1) {
				if((current.y + 1 <= grid.length) && !visited[current.x - 1][current.y]) {
					LNode currNode = new LNode(current.x, current.y + 1, current.turned, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 4: " + currNode.x + " " + currNode.y);
				}
			} else if (!current.turned) {
				if((current.y + 1 <= grid.length) && !visited[current.x - 1][current.y]) {
					LNode currNode = new LNode(current.x, current.y + 1, true, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 4: " + currNode.x + " " + currNode.y);
				}
			}
				
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
	
	class LNode {
		int x;
		int y;
		boolean turned;
		int depth;
		LNode parent;
		
		public LNode(int x, int y, boolean turned, int depth) {
			this.x = x;
			this.y = y;
			this.turned = turned;
			this.depth = depth;
		}
	}
}
