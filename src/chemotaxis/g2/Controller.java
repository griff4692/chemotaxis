package chemotaxis.g2;

import java.awt.Point;
import java.util.*;
import java.lang.Math;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.DirectionType;
import chemotaxis.sim.SimPrinter;

public class Controller extends chemotaxis.sim.Controller {

	private List<Point> shortestPath;
	private Set<Point> shortestPathSet;
	private List<Point> corners;
	private Set<Point> blockedLocations;
	private int m;
	private int n;
	private final Point target;
	private String[][] policy;
	private Double[][] values;
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
	public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter, Integer agentGoal, Integer spawnFreq) {
		super(start, target, size, grid, simTime, budget, seed, simPrinter, agentGoal, spawnFreq);
		this.m = grid.length;
		this.n = grid[0].length;
		this.shortestPath = this.getShortestPath(start, target, grid);
		this.shortestPathSet = new HashSet<Point>(this.shortestPath);
		this.corners = this.findCorners(shortestPath);
		this.corners.add(shortestPath.get(0)); // Added the starting point as a corner?
		this.blockedLocations = this.findBlockedLocations(grid);
		// Print map in console
		for (int i = 0; i < this.m; i ++){
			for (int j = 0; j < this.n; j ++){
				if (shortestPathSet.contains(new Point(i + 1, j + 1))){
					if (corners.contains(new Point(i + 1, j  + 1))){
						System.out.print("C" + " ");
					}else if(target.x == i + 1 && target.y == j + 1){
						System.out.print("O" + " ");
					}else {
						System.out.print("S" + " ");
					}
				}
				else if (blockedLocations.contains(new Point(i, j))){
					System.out.print("*" + " ");
				}else{
					System.out.print("." + " ");
				}
			}
			System.out.println();
		}
		this.target = target;
		this.findPolicy(0.8);
	}


	private void findPolicy(double gamma){
		String[][] init = this.extractPolicy(new double[this.m][this.n], gamma);
		System.out.println("Init");
		for (int i = 0; i < this.m; i ++){
			for (int j = 0; j < this.n; j ++){
				System.out.print(init[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println("New Policy");

		this.policy = policyIteration(init, new double[this.m][this.n], gamma);
		for (int i = 0; i < this.m; i ++){
			for (int j = 0; j < this.n; j ++){
				System.out.print(policy[i][j] + " ");
			}
			System.out.println();
		}
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
		boolean[][] visited = new boolean[this.m][this.n];
		Node start = new Node(p.x, p.y);
		queue.add(start);
		List<Point> path = new ArrayList<Point>();
		int targetX = target.x, targetY = target.y;
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
		return (x >= 0) && (x < this.m) && (y >= 0) && (y < this.n) && grid[x][y].isOpen() && !visited[x][y];
	}

	private List<Point> findCorners(List<Point> shortestPath){

		List<Point> corners = new ArrayList<Point>();
		if (shortestPath.size() <= 2){
			return corners;
		}
		Point a = shortestPath.get(0), b = shortestPath.get(1);
		for(int i = 2; i < shortestPath.size() ; i++){
			Point c = shortestPath.get(i);
			int aX = a.x, aY = a.y, cX = c.x, cY = c.y;
			if (aX - cX != 0 && aY - cY != 0){
				corners.add(b);
			}
			a = b;
			b = c;
		}
		System.out.println(corners);
		System.out.println(corners.size());
		return corners;
	}

	private List<String> actions(int x, int y){
		ArrayList<String> res = new ArrayList<String>();
		if(x == target.x - 1 && y == target.y - 1){ // Target point is 1-index
			res.add("G");
		}else if(this.blockedLocations.contains(new Point(x, y))){ //Block locations are 0-indexed
			res.add("X");
		}else{
			res = new ArrayList<String>(Arrays.asList("<", ">", "^", "v", "C"));
		}
		return res;
//		return new ArrayList<DirectionType>(Arrays.asList(DirectionType.EAST, DirectionType.NORTH, DirectionType.SOUTH, DirectionType.WEST, DirectionType.CURRENT));
	}

	/**
	 * Compute the Q-value for the given state-action pair,
	 * given a set of values for the problem and discount factor gamma.
	 * Living reward is -2
	 *
	 * @param values	The current state values
	 * @param gamma		The discount factor
	 * @return			a value for state (x, y)
	 */
	private double Qvalue(int x, int y, String action, double[][] values, double gamma){
		// Multiply values by the discount factor gamma
		double[][] gValues = this.matrixMul(values, gamma);
		int livingReward = -10;
		//Handle obstacle, goal, and optimal path states
		if (action.equals("G")) return 100;
		if (action.equals("X")) return -100;
//		if (this.shortestPathSet.contains(new Point(x + 1, y + 1))) return 30; // shortestPathSet is 1-indexed

		// All possible successor states
		int westX = x, westY = Math.max(y - 1, 0);
		int eastX = x, eastY = Math.min(y + 1, values[0].length - 1);
		int northX = Math.max(x - 1, 0), northY = y;
		int southX = Math.min(x + 1, values.length - 1), southY = y;

		if (action.equals("<")){
			return gValues[westX][westY] + livingReward;
		}
		if (action.equals(">")) {
			return gValues[eastX][eastY] + livingReward;
		}
		if (action.equals("^")){
			return gValues[northX][northY] + livingReward;
		}
		if (action.equals("v")){
			return gValues[southX][southY] + livingReward;
		}
		return gValues[x][y] + livingReward;
	}

	private double[][] matrixMul(double[][] values, double num){
		double[][] res = new double[values.length][values[0].length];
		for (int i = 0; i < values.length; i ++){
			for (int j = 0; j < values[0].length; j ++){
				res[i][j] = values[i][j] * num;
			}
		}
		return res;
	}

	private double[][] matrixAdd(double[][] m1, double[][] m2) throws Exception{

		if (m1.length != m2.length && m1[0].length != m2[0].length) {
			throw new Exception("matrix dimensions do not match");
		}
		double[][] res = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i ++){
			for (int j = 0; j < m1[0].length; j ++){
				res[i][j] = m1[i][j] + m2[i][j];
			}
		}
		return res;
	}

	/**
	 * Given the current state values, extract the best policy for each state
	 *
	 * @param values 	current state values
	 * @param gamma		discount factor
	 * @return			2d array of string containing new policy
	 */
	private String[][] extractPolicy(double[][] values, double gamma){

		String[][] policy = new String[m][n];
		for (int i = 0; i < this.m; i ++){
			for (int j = 0; j < this.n; j++){
				double bestValue = -99999999.9;
				for (String action: this.actions(i, j)){
					double newValue = this.Qvalue(i, j, action, values, gamma);
					if (newValue > bestValue) {
						bestValue = newValue;
						policy[i][j] = action;
					}
				}
			}
		}
		return policy;
	}

	/**
	 *  Given the current policy and values, evaluate the policy
	 * @param policy 		current poliy
	 * @param values		current state values
	 * @param gamma			discount  factor
	 * @param threshold		threshold for value convergence
	 * @return				new values after doing the new policy
	 */
	private double[][] evaluatePolicy(String[][] policy, double[][]values, double gamma, double threshold){
		double maxDiff = threshold + 1.0;
		while (maxDiff >= threshold){
			maxDiff = -999999.9;
			double[][] newValues = new double[m][n];
			for (int i = 0; i < this.m; i ++){
				for (int j = 0; j < this.n; j++) {
					newValues[i][j] = this.Qvalue(i, j, policy[i][j], values, gamma);
					maxDiff = Math.max(maxDiff, Math.abs(newValues[i][j] - values[i][j]));
					}
			}
			values = newValues;
		}
		return values;
	}

	private String[][] policyIteration(String[][] policy, double[][] values, double gamma){
		while (true){
//			for (int i = 0; i < this.m; i ++){
//				for (int j = 0; j < this.n; j ++){
//					System.out.print(policy[i][j] + " ");
//				}
//				System.out.println();
//			}
			values = this.evaluatePolicy(policy, values, gamma, 0.00001);
			String[][] newPolicy = this.extractPolicy(values, gamma);

			if (this.comparePolicy(newPolicy, policy)){
				return policy;
			}
			policy = newPolicy;
		}
	}

	private Boolean comparePolicy(String[][] p1, String[][] p2){
		for (int i = 0; i < p1.length; i ++){
			for (int j = 0; j < p1[0].length; j++) {
				if (! p1[i][j].equals(p2[i][j])){
					return false;
				}
			}
		}
		return true;
	}


	/**
	 * Find the coordinates of the obstacle
	 * @param grid 		game grid
	 * @return			a set of obstacles coordinates (0-indexed)
	 */
	private Set<Point> findBlockedLocations(ChemicalCell[][] grid){
		Set<Point> res = new HashSet<Point>();
		for (int i = 0; i < grid.length; i++){
			for (int j = 0; j < grid[0].length; j++){
				if (grid[i][j].isBlocked()){
					res.add(new Point(i, j));
				}
			}
		}
		return res;
	}
	private boolean inPath(Point agent) {
		int pathLength = shortestPath.size();
		int pX = agent.x;
		int pY = agent.y;
		for (int i = 0; i < pathLength; i++){
			int tempX = shortestPath.get(i).x;
			int tempY = shortestPath.get(i).y;
			if (pX == tempX && pY == tempY) {
				return true;
			}
		}
		return false;
	}

	private Point nextCell(Point location) {
		int pathLength = shortestPath.size();
		int pX = location.x;
		int pY = location.y;
		boolean flag = false;
		for (int i =0; i < pathLength; i++) {
			if (flag) {
				return shortestPath.get(i);
			}
			int tempX = shortestPath.get(i).x;
			int tempY = shortestPath.get(i).y;
			if (pX == tempX && pY == tempY) {
				flag = true;
			}
		}
		return new Point(0, 0);
	}

	private int closestPathPoint(Point agent) {
		int closestDistance = 9999999;
		int closestIdx = 0;
		for(int i = 0; i < shortestPath.size(); i++) {
			int x = shortestPath.get(i).x;
			int y = shortestPath.get(i).y;
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
		int closestIdx = this.closestToTarget(locations);
		int x = locations.get(closestIdx).x;
		int y = locations.get(closestIdx).y;

		int xDelta = this.target.x - x;
		int yDelta = this.target.y - y;

		if(xDelta < 0) {
			xDelta = -1;
		} else if(xDelta > 0) {
			xDelta = 1;
		}

		if(yDelta < 0) {
			yDelta = -1;
		} else if(yDelta > 0) {
			yDelta = 1;
		}

		List<ChemicalType> chemicals = new ArrayList<>();
		chemicals.add(ChemicalType.BLUE);

		int newX = x + xDelta;
		int newY = y + yDelta;

		chemicalPlacement.location = new Point(newX, newY);
		chemicalPlacement.chemicals = chemicals;

		return chemicalPlacement;
	}
}