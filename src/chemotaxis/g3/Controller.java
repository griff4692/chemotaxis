package chemotaxis.g3;
import java.awt.Point;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;
public class Controller extends chemotaxis.sim.Controller {
	static class Graph {
		//fields
		int vertices;
		int matrix[][];
		//constructor
		public Graph(int size) {
			this.vertices = size;
			matrix = new int[size][size];
		}
		// add an edge for undirected && unweighted graph
		public void addEdge(int source, int dest) {
			matrix[source][dest] = 1;
			matrix[dest][source] = 1;
		}
		public static final int NO_PARENT = -1;
		public static ArrayList<Integer> dijkstra(int[][] adjacencyMatrix, int startVertex, int targetVertex) {
			int nVertices = adjacencyMatrix[0].length;
			// shortestDistances[i] holds shortest distance from src to i
			int[] shortestDistances = new int[nVertices];
			// added[i] will true if vertex i is included / in shortest path tree
			// or shortest distance from src to i is finalized
			boolean[] added = new boolean[nVertices];
			// Initialize all distances as INFINITE and added[] as false
			for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) {
				shortestDistances[vertexIndex] = Integer.MAX_VALUE;
				added[vertexIndex] = false;
			}
			// Distance of source vertex from itself is always 0
			shortestDistances[startVertex] = 0;
			// Parent array to store shortest path tree
			int[] prev = new int[nVertices];
			prev[startVertex] = NO_PARENT;
			// Find shortest path for all vertices
			for (int i = 1; i < nVertices; i++) {
				// Pick the minimum distance vertex from the set of vertices not yet
				// processed. nearestVertex is always equal to startNode in
				// first iteration.
				int nearestVertex = -1;
				int shortestDistance = Integer.MAX_VALUE;
				for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) {
					if (!added[vertexIndex] && shortestDistances[vertexIndex] < shortestDistance) {
						nearestVertex = vertexIndex;
						shortestDistance = shortestDistances[vertexIndex];
					}
				}
				// Mark the picked vertex as processed
				added[nearestVertex] = true;
				// Update dist value of the adjacent vertices of the
				// picked vertex.
				for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) {
					int edgeDistance = adjacencyMatrix[nearestVertex][vertexIndex];
					if (edgeDistance > 0 && ((shortestDistance + edgeDistance) < shortestDistances[vertexIndex])) {
						prev[vertexIndex] = nearestVertex;
						shortestDistances[vertexIndex] = shortestDistance + edgeDistance;
					}
				}
			}
			return returnPath(targetVertex, prev);
		}
		public static ArrayList<Integer> returnPath(int target, int[] prev) {
			ArrayList<Integer> result = new ArrayList<Integer>();
			while (prev[target] != NO_PARENT) {
				result.add(target);
				target = prev[target];
			};
			return result;
		}
	}
	ArrayList<Integer> shortest_path;
	Hashtable<Integer, Point> node_to_point;
	Hashtable<Point, Integer> point_to_node;
	Point first_pt;
	Point second_pt;
	int period;
	int agentReached;
	/**
	 * Controller constructor
	 *
	 * @param start       start cell coordinates
	 * @param target      target cell coordinates
	 * @param size        grid/map size
	 * @param grid        game grid/map
	 * @param simTime     simulation time
	 * @param budget      chemical budget
	 * @param seed        random seed
	 * @param simPrinter  simulation printer
	 *
	 */
	public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter, Integer agentGoal, Integer spawnFreq) {
		super(start, target, size, grid, simTime, budget, seed, simPrinter, agentGoal, spawnFreq);
		// dijkestra: https://www.geeksforgeeks.org/dijkstras-shortest-path-algorithm-greedy-algo-7/
		shortest_path = new ArrayList<Integer>();
		node_to_point = new Hashtable<Integer, Point>();
		point_to_node = new Hashtable<Point, Integer>();
		int k = 0;
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				if (grid[i][j].isOpen()) {
					Point curr = new Point( i + 1, j + 1);  // reversed to match grid's orientation
					node_to_point.put(k, curr);
					point_to_node.put(curr, k);
					k++;
				}
			}
		}
		Graph g = new Graph(node_to_point.size());
		for(int i = 0; i < node_to_point.size(); i++)
		{
			Point p = node_to_point.get(i);
			int x_val = p.x;
			int y_val = p.y;
			Point above = new Point(x_val, y_val-1);
			Point below = new Point(x_val,y_val+1);
			Point left = new Point(x_val-1, y_val);
			Point right = new Point(x_val+1,y_val);
			if(point_to_node.containsKey(above))
			{
				g.addEdge(i, point_to_node.get(above));
			}
			if(point_to_node.containsKey(below))
			{
				g.addEdge(i, point_to_node.get(below));
			}
			if(point_to_node.containsKey(left))
			{
				g.addEdge(i, point_to_node.get(left));
			}
			if(point_to_node.containsKey(right))
			{
				g.addEdge(i, point_to_node.get(right));
			}
		}
		Integer startNode = point_to_node.get(start);
		Integer targetNode = point_to_node.get(target);
        /*
        call dijkstra to record the shortest path from start to target
            - NOTE: the shortest path array is reversed and does not include the start and end points.
            (i.e. if the shortest path from 0 to 7 is 0, 1, 5, 4, 7
            in the shortest_path arraylist it is recorded as 4, 5, 1)
         */
		ArrayList<Integer> shortest_path = g.dijkstra(g.matrix, startNode, targetNode);
		int index_one = shortest_path.size() * 2 / 3;
		int index_two = shortest_path.size() / 3;
		int first_node = shortest_path.get(index_one);
		int second_node = shortest_path.get(index_two);
		first_pt = node_to_point.get(first_node);
		System.out.println(first_pt);
		second_pt = node_to_point.get(second_node);
		System.out.println(second_pt);
		//agent goal 50
		//spawnfreq 5
		//at turn 250 last agent spawned
		//budget 9
		//this.period = 81
		//run out of chemicals too fas, risk not holding up the gradient
		//if we delay the chemical placement, we are afraid that the chemicals won't reach the agent fast enough
		this.period = ((spawnFreq * agentGoal) / this.budget) * 3;
		if(this.period <3){
			this.period = 3;
		}
		System.out.println("spawnFreq is " + spawnFreq);
		System.out.println("agentGoal is " + agentGoal);
		System.out.println("budget is " + this.budget);
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
        /* Inspired by Nikhilesh Belulkar's idea from class discussion:
        “Build a gradient of chemicals to guide the agent towards the
        goal block (have higher concentrations near the goal block,
        lower concentrations nearer to the spawn block)“
         */
		ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
		System.out.println("period is " + this.period);
		if(currentTurn%this.period == 1){
			List<ChemicalType> chemicals = new ArrayList<>();
			chemicals.add(ChemicalType.RED);
			chemicalPlacement.location = new Point(this.first_pt.x, this.first_pt.y);
			chemicalPlacement.chemicals = chemicals;
		}
		// 1 2 0
		// 1 period/2 0
		else if(currentTurn%this.period == (this.period/2)+1){
			List<ChemicalType> chemicals = new ArrayList<>();
			chemicals.add(ChemicalType.GREEN);
			chemicalPlacement.location = new Point(this.second_pt.x, this.second_pt.y);
			chemicalPlacement.chemicals = chemicals;
		}
		else if (currentTurn%this.period == 0) {
			List<ChemicalType> chemicals = new ArrayList<>();
			chemicals.add(ChemicalType.BLUE);
			chemicalPlacement.location = new Point(this.target.x, this.target.y);
			chemicalPlacement.chemicals = chemicals;
			//this.period = (spawnFreq * agentNottoGoal) / chemicalRemaining *3;
		}
		return chemicalPlacement;
	}
}

