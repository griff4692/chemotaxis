package chemotaxis.g9;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;
public class Controller extends chemotaxis.sim.Controller {
	List<Point> shortestPathList;
	private int incrementBy;
	private int colorCounter = 0;
	private int currentPathIndex = -1;
	private int idealChemicalIncrement = 5;
	private int beginningChems = 0;
	private int waitCounter = 1;
	private boolean reverse = false;
	private List<Point> placementPath;

	/**
	 * Controller constructor
	 *
	 * @param start    start cell coordinates
	 * @param target   target cell coordinates
	 * @param size   	 grid/map size
	 * @param grid    game grid/map
	 * @param simTime   simulation time
	 * @param budget   chemical budget
	 * @param seed    random seed
	 * @param simPrinter simulation printer
	 *
	 */
	public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter, Integer agentGoal, Integer spawnFreq) {
		super(start, target, size, grid, simTime, budget, seed, simPrinter, agentGoal, spawnFreq);
		System.out.println("hello");
		this.shortestPathList = shortestPath(grid);	
		this.incrementBy = decidePlacementStrategy(this.shortestPathList, idealChemicalIncrement, budget, spawnFreq);
		this.placementPath = generatePlacementPath(this.shortestPathList, this.incrementBy);
	}
	
	public List<Point> generatePlacementPath(List<Point> shortestPath, int incrementBy) {
		List<Point> placementList = new ArrayList<Point>();
		int i = 0;
		while (true) {
			i += incrementBy;
			if (i >= shortestPath.size()) {
				i = shortestPath.size() - 1;
			}
			placementList.add(shortestPath.get(i));
			if (i == shortestPath.size() - 1)
				break;
		}
		return placementList;
	}
	
	// will this need to keep track of simTime?
	public int decidePlacementStrategy(List<Point> shortestPathList, int idealChemicalIncrement, int budget, int spawnFreq) {
		/*
		if(spawnFreq < idealChemicalIncrement){
			beginningChems = idealChemicalIncrement / spawnFreq;
        	System.out.println("Beginning chemicals: " + beginningChems);
        	budget -= beginningChems;
		}*/
		// budget --; // take out for the beginning chemical
		budget --; // take out another for the ending chemical
		int pathLength = shortestPathList.size();
		int singlePathIncrement = (int) Math.ceil(pathLength / budget);
		System.out.format("%d, %d, %d", pathLength, budget, singlePathIncrement);
		// ideal chemical increment will not make it the full path.
		return (singlePathIncrement > idealChemicalIncrement) ? singlePathIncrement : idealChemicalIncrement;
		// here, we need to choose some criteria to lower the idealChemicalIncrement based on the expected loops (single path or multiple) / saturation we want.
	};
  /**
   * Apply chemicals to the map
   *
   * @param currentTurn     current turn in the simulation
   * @param chemicalsRemaining number of chemicals remaining
   * @param locations   current locations of the agents
   * @param grid        game grid/map
   * @return          a cell location and list of chemicals to apply
   *
   */
 	@Override
	public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid) {
 		int waitAfter = 3;
 		waitCounter += 1;
 		if (waitCounter % (waitAfter + 1) < waitAfter) {
			return new ChemicalPlacement();
		}
		if (!reverse) {
			currentPathIndex += 1;
			if (currentPathIndex >= this.placementPath.size()) {
				reverse = !reverse;
				currentPathIndex = this.placementPath.size() - 2;
				incrementColor(reverse);
				incrementColor(reverse);
			}
		} else {
			currentPathIndex -= 1;
			if (currentPathIndex < 0) {
				reverse = !reverse;
				currentPathIndex = 1;
				incrementColor(reverse);
				incrementColor(reverse);

			}
		}
 		
 		return placeChemical(reverse);
 		
 	}
 	
 	public ChemicalType incrementColor(boolean isReverse) {
		ChemicalType[] rotation = {ChemicalType.RED, ChemicalType.GREEN, ChemicalType.BLUE};		
		ChemicalType colorToPick = rotation[colorCounter];
		colorCounter = (colorCounter + ((!isReverse) ? 1 : -1)) % rotation.length;

		if (colorCounter < 0) {
			colorCounter += rotation.length;
		}
		return colorToPick;
 	}
 	
 	public ChemicalPlacement placeChemical(boolean isReverse) {	
 		ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
		List<ChemicalType> chemicals = new ArrayList<>();
		// drop at current location
		/*if(currentPathIndex < incrementBy && beginningChems > 0){
            currentPathIndex += beginningChems;
        } else {
        	currentPathIndex += incrementBy;
        }*/		
		Point pointToPlace = this.placementPath.get(currentPathIndex);
		chemicalPlacement.location = new Point(pointToPlace.x + 1, pointToPlace.y + 1);
		
		// pick right chemical in sequence
		chemicals.add(incrementColor(isReverse));
		
		chemicalPlacement.chemicals = chemicals;
		return chemicalPlacement;
	}
	
	public ArrayList<Point> shortestPath(ChemicalCell[][] grid) {
		 ArrayList<ArrayList> queue = new ArrayList<>();
		 ArrayList<Point> start = new ArrayList<>();
		 HashMap<Point, Boolean> visited = new HashMap<Point, Boolean>();
		 int[][] moves = {{-1,0}, {1,0}, {0,1}, {0,-1}};
		 // account off by one
		 Point startPoint = new Point(this.start.x - 1, this.start.y - 1);
		 Point targetPoint = new Point(this.target.x - 1, this.target.y - 1);
		 start.add(startPoint);
		 queue.add(start);
		 while (queue.size() > 0) {
			 ArrayList<Point> curPath = queue.remove(0);
			 Point endOfPath = curPath.get(curPath.size() - 1);
			 for (int[] move : moves) {
				 int deltaX = move[0];
				 int deltaY = move[1];
				 Point neighbor = new Point(endOfPath.x + deltaX, endOfPath.y + deltaY);
				 if (
					neighbor.x >= 0 && 
					neighbor.x < this.size && 
					neighbor.y >= 0 && 
					neighbor.y < this.size &&
					!grid[neighbor.x][neighbor.y].isBlocked() &&
					!visited.getOrDefault(neighbor, false)) {
					 visited.put(neighbor, true);
					 curPath.add(neighbor);
					 if (neighbor.equals(targetPoint)) {
						 return curPath;
					 } else {
						 queue.add(new ArrayList<Point>(curPath));
					 }
					 curPath.remove(curPath.size() - 1);
				 }
			 }
			
		 }
		 System.err.print("Should not have been here");
		 return new ArrayList<>();
	} 	}