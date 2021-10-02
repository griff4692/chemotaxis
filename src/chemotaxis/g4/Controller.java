package chemotaxis.g4;

import java.awt.Point;
import java.sql.Array;
import java.util.*;
// import java.util.List;
// import java.util.ArrayList;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;

public class Controller extends chemotaxis.sim.Controller {

	ArrayList<Point> path = null;
	ArrayList<Point> placementCells = null;
	ArrayList<ChemicalType> colorPath = null;
	int[] placementPadding;
	ArrayList<Integer> pcIndexes = null;
	ArrayList<Integer> refreshPadding; 
	Analysis analyzer = null;


	// Integer time_interval = 4;
	Integer drop_interval = 5;
	Integer roll_interval = 10;
	Point firstDrop;
	Integer chemical_color = 0;
	Integer arrayLength = 20;
	Integer pathComplete;
	double threshold = 0.05;
	int vacantRound = 0;
	int refresh_added = 0;


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

		if(path == null) {
			simPrinter.println("creating path");
			path = getPath(grid);
		}

		ArrayList<AnalysisData> data;
		if(analyzer == null){
			analyzer = new Analysis(grid);
			analyzer.setThreshold(threshold); //default is 0.05

			data = analyzer.analyzePath(path, arrayLength);
		}
		else{
			data = analyzer.analyzePath(path, arrayLength);
		}

		placementCells = new ArrayList<Point>();
		// Analyze the selected path and get all turning points as initial placement cells
		getIntervals(path, true, grid);

		placementPadding = new int[placementCells.size()];
		placementPadding[0] = 0;
		int cur = path.indexOf(placementCells.get(0));
		
		int prev = 0;
		// For each placement cells check the following two cells
		// and find the one with largest percentage of remaining as local max.
		for(int i=0; i<placementCells.size(); i++){
			cur = path.indexOf(placementCells.get(i));
			// Distance from A to B]
			int gap = cur-prev;

			int next = cur;
			if(i < placementCells.size()-1 && gap < roll_interval){
				next = path.indexOf(placementCells.get(i+1));
				if(data.get(next).maxDistance>=next-cur && i < placementCells.size()-2){
					int nextNext = path.indexOf(placementCells.get(i+2));
					if(data.get(nextNext).maxDistance > nextNext-cur){
						placementCells.remove(i+1);
					}
				}
			}

			int turningIdx = cur;
			int bestPoint = cur;
			int j=1;
			while((cur+j)<path.size() && j<=data.get(cur+j).maxDistance && gap!=1){
				if(data.get(bestPoint).isMaxPercentage<data.get(cur+j).isMaxPercentage && (next==cur || (next-cur)>(j+1))){
					bestPoint = cur+j;
				}
				j++;
			}
			placementCells.set(i, path.get(bestPoint));
			cur = bestPoint;

			if(gap<data.get(cur).maxDistance){
				placementPadding[i] += 2 * prev - cur;
			}
			else{
				placementPadding[i] += 2 * turningIdx - cur - 1;
			}
			
			prev = cur;
		}

		for(int k=0; k<placementPadding.length; k++)

		firstDrop = placementCells.get(0);

		// Calculate the turns needed to finish one round of chemical placing
		if(placementCells.size()>0){
			pathComplete = placementPadding[placementCells.size()-1];
		}
		else
			pathComplete = 1;

		colorPath = new ArrayList<ChemicalType>();
		for (int i=0; i<placementCells.size(); i++) {
			if (i % 3 == 0) {
				colorPath.add(ChemicalType.RED);
			} else if (i % 3 == 1) {
				colorPath.add(ChemicalType.GREEN);
			} else {
				colorPath.add(ChemicalType.BLUE);
			}
		}

		pcIndexes = new ArrayList<Integer>();
		refreshPadding = new ArrayList<Integer>();
		refreshPadding.add(0);
		pcIndexes.add(0);
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

	Boolean pointInBounds(Integer length, Point p){
		if(p.x >= 1 && p.x <= length && p.y >= 1 && p.y <= length){
			return true;
		}
		return false;
	}

	Boolean checkIfSame(Point p, Point q) {
		return ((p.x==q.x) && (p.y==q.y));
	}

	Boolean checkIfCrevace(Point p, ChemicalCell[][] grid, Point old) {
		int length = grid.length;
		//I only need to check if 3 of the neighbors are blocked bc in order for
		// this function to even be called, the neighbor its coming from must have been open in the first place
		ArrayList<Point> neighbors = new ArrayList<Point>();
		Integer total = 0;
		Point neighbor=new Point(p.x, p.y + 1);
		if(pointInBounds(length, neighbor) && !checkIfSame(neighbor, old)){
			neighbors.add(neighbor);}
		neighbor =new Point(p.x, p.y - 1);
		if(pointInBounds(length, neighbor) && !checkIfSame(neighbor, old)){
			neighbors.add(neighbor);}
		neighbor =new Point(p.x + 1, p.y);
		if(pointInBounds(length, neighbor) && !checkIfSame(neighbor, old)){
			neighbors.add(neighbor);}
		neighbor =new Point(p.x - 1, p.y);
		if(pointInBounds(length, neighbor) && !checkIfSame(neighbor, old)){
			neighbors.add(neighbor);}
		for (int i=0; i<neighbors.size();i=i+1){
			Point curr = neighbors.get(i);
			if (grid[curr.x-1][curr.y-1].isBlocked()){
				total = total + 1;
			}
		}
		if (total == 4) {
			return true;
		}
		else{
			return false;
		}
	}

	Integer countNeighborsInPath(ArrayList<Point> current){
		Integer total = 0;
		for (int i=1;i<current.size();i=i+1) {
			Point p = current.get(i);
			Point prev = current.get(i-1);
			ArrayList<Point> neighbors = new ArrayList<Point>();
			if (p.x != prev.x && p.y+1 != prev.y) {
				total=total+1;}
			if (p.x != prev.x && p.y-1 != prev.y) {
				total=total+1;}
			if (p.x+1 != prev.x && p.y != prev.y) {
				total=total+1;}
			if (p.x-1 != prev.x && p.y != prev.y) {
				total=total+1;}}

		return total;
	}

	void addInterval(ArrayList<Point> current, boolean setTurning, int i){
		if(setTurning){
			placementCells.add(current.get(i+2));
		}
	}

	Integer getIntervals(ArrayList<Point> current, boolean setTurning, ChemicalCell[][] grid){
		Integer length = grid.length;
		ArrayList<Integer> values = new ArrayList<>();
		for (int i=1;i<current.size();i=i+1) {
			Point curr = current.get(i);
			Integer curr_x = curr.x;
			Integer curr_y = curr.y;
			Point prev = current.get(i - 1);
			Integer prev_x = prev.x;
			Integer prev_y = prev.y;

			if (curr_x - prev_x < 0) { //going up
				values.add(3);
			} else if (curr_x - prev_x > 0) { //going down
				values.add(4);
			} else if (curr_y - prev_y < 0) { //going left
				values.add(1);
			} else {
				values.add(2); //going right
			}
		}

		Integer numIntervals = 0;

		if(current.size() > 1){// test to see if first move is West (default movement) or some other move
			if(values.get(0) != 1){// movement is not west
				Point start = current.get(0);
				Point southStart = new Point(start.x + 1, start.y);
				Point eastStart = new Point(start.x, start.y + 1);
				Point westStart = new Point(start.x, start.y - 1);
				boolean southOpen = (pointInBounds(grid.length, southStart) && grid[southStart.x - 1][southStart.y - 1].isOpen());
				boolean eastOpen = (pointInBounds(grid.length, eastStart) && grid[eastStart.x - 1][eastStart.y - 1].isOpen());
				boolean westOpen = (pointInBounds(grid.length, westStart) && grid[westStart.x - 1][westStart.y - 1].isOpen());


				if(values.get(0) == 4){ //moving south
					if(westOpen){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, 0);
					}
				}
				else if(values.get(0) == 2) {//moving east
					if(westOpen || southOpen){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, 0);
					}
				}
				else if(values.get(0) == 3){
					if(westOpen || southOpen || eastOpen){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, 0);
					}
				}
			}
		}


		for (int i=0;i<values.size()-1;i=i+1){
			Point currpoint = current.get(i+1);
			Point wallpointcontinued;
			Point wallpointleft;
			Integer curr = values.get(i);
			Integer future = values.get(i+1);

			if (curr==3 && future==2) {
				wallpointcontinued = new Point(currpoint.x-1, currpoint.y);
				wallpointleft = new Point(currpoint.x, currpoint.y-1);
				if (pointInBounds(length,wallpointleft) && pointInBounds(length,wallpointcontinued)){
					if (grid[wallpointcontinued.x-1][wallpointcontinued.y-1].isOpen() || grid[wallpointleft.x-1][wallpointleft.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}
				else if (pointInBounds(length,wallpointleft)){
					if (grid[wallpointleft.x-1][wallpointleft.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}
				else if (pointInBounds(length,wallpointcontinued)){
					if (grid[wallpointcontinued.x-1][wallpointcontinued.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}
			}
			else if (curr==2 && future==4) {
				
				wallpointcontinued = new Point(currpoint.x, currpoint.y+1);
				wallpointleft = new Point(currpoint.x-1, currpoint.y);
				if (pointInBounds(length,wallpointleft) && pointInBounds(length,wallpointcontinued)){
					if (grid[wallpointcontinued.x-1][wallpointcontinued.y-1].isOpen() || grid[wallpointleft.x-1][wallpointleft.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}
				else if (pointInBounds(length,wallpointleft)){
					if (grid[wallpointleft.x-1][wallpointleft.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						if(setTurning){
							addInterval(current, setTurning, i);
						}
					}
				}
				else if (pointInBounds(length,wallpointcontinued)){
					if (grid[wallpointcontinued.x-1][wallpointcontinued.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}
			}
			else if (curr==4 && future==1) {
				wallpointcontinued = new Point(currpoint.x+1, currpoint.y);
				wallpointleft = new Point(currpoint.x,currpoint.y+1);
				if (pointInBounds(length,wallpointleft) && pointInBounds(length,wallpointcontinued)){
					if (grid[wallpointcontinued.x-1][wallpointcontinued.y-1].isOpen() || grid[wallpointleft.x-1][wallpointleft.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}
				else if (pointInBounds(length,wallpointleft)){
					if (grid[wallpointleft.x-1][wallpointleft.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}
				else if (pointInBounds(length,wallpointcontinued)){
					if (grid[wallpointcontinued.x-1][wallpointcontinued.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}
			}
			else if(curr==1 && future==3) {
				wallpointcontinued = new Point(currpoint.x, currpoint.y-1);
				wallpointleft = new Point(currpoint.x+1, currpoint.y);
				if (pointInBounds(length, wallpointleft) && pointInBounds(length, wallpointcontinued)) {
					if (grid[wallpointcontinued.x - 1][wallpointcontinued.y - 1].isOpen() || grid[wallpointleft.x - 1][wallpointleft.y - 1].isOpen()) {
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}
				else if (pointInBounds(length,wallpointleft)){
					if (grid[wallpointleft.x-1][wallpointleft.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}
				else if (pointInBounds(length,wallpointcontinued)){
					if (grid[wallpointcontinued.x-1][wallpointcontinued.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}
			}
			else if(curr==3 && future==1) {
				wallpointcontinued = new Point(currpoint.x-1, currpoint.y);
				if (pointInBounds(length, wallpointcontinued)) {
					if (grid[wallpointcontinued.x-1][wallpointcontinued.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}
			}
			else if(curr==1 && future==4){
				wallpointcontinued = new Point(currpoint.x, currpoint.y-1);
				if (pointInBounds(length, wallpointcontinued)) {
					if (grid[wallpointcontinued.x-1][wallpointcontinued.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}
			}
			else if(curr==4 && future==2){
				wallpointcontinued = new Point(currpoint.x+1, currpoint.y);
				if (pointInBounds(length, wallpointcontinued)) {
					if (grid[wallpointcontinued.x-1][wallpointcontinued.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}

			}
			else if(curr==2 && future==3){
				wallpointcontinued = new Point(currpoint.x, currpoint.y+1);
				if (pointInBounds(length, wallpointcontinued)) {
					if (grid[wallpointcontinued.x-1][wallpointcontinued.y-1].isOpen()){
						numIntervals = numIntervals + 1;
						addInterval(current, setTurning, i);
					}
				}

			}
		}
		return numIntervals;
	}




	class CustomIntegerComparator implements Comparator<ArrayList<Point>> {

		@Override
		public int compare(ArrayList<Point> o1, ArrayList<Point> o2) {
			Integer numIntervalso1 = getIntervals(o1, false, grid);
			Integer numIntervalso2 = getIntervals(o2, false,grid);
			Integer numNeighborso1 = countNeighborsInPath(o1);
			Integer numNeighborso2 = countNeighborsInPath(o2);
			if (numIntervalso1< numIntervalso2){
				return -1;
			}
			else if(numIntervalso1 == numIntervalso2) {
				if (o1.size() < o2.size()) {
					return -1;
				} else {
					return 1;
				}
			}
			else{
				return 1;
			}
		}
	}

	ArrayList<Point> getPath(ChemicalCell[][] grid){
		int length = grid.length;

		ArrayList<Point> path = new ArrayList<Point>();
		path.add(start);

		PriorityQueue<ArrayList<Point>> q = new PriorityQueue(new CustomIntegerComparator());
		q.add(path);


		Set<Point> reached = new HashSet<Point>();

		while(true) {
			try {
				path = q.remove();
			}
			catch(Exception e){
			}
			Point p = path.get(path.size() - 1);
			if(p.x == target.x && p.y == target.y){
				return path;
			}

			ArrayList<Point> neighbors = new ArrayList<Point>();
			neighbors.add(new Point(p.x, p.y + 1));
			neighbors.add(new Point(p.x, p.y - 1));
			neighbors.add(new Point(p.x + 1, p.y));
			neighbors.add(new Point(p.x - 1, p.y));
//
			for(Point neighbor: neighbors){
				if(pointInBounds(length, neighbor) && !reached.contains(neighbor) && grid[neighbor.x - 1][neighbor.y - 1].isOpen() && !checkIfCrevace(neighbor,grid,p)){
					ArrayList<Point> newPath = new ArrayList<Point>(path);
					newPath.add(neighbor);
					q.add(newPath);
					reached.add(neighbor);
				}
			}
		}
	}


	ArrayList<Point> getPathOld(ChemicalCell[][] grid){
		int length = grid.length;

		ArrayList<Point> path = new ArrayList<Point>();
		path.add(start);

		Queue<ArrayList<Point>> q = new LinkedList<>();
		q.add(path);

		Set<Point> reached = new HashSet<Point>();

		while(true) {
			path = q.remove();
			Point p = path.get(path.size() - 1);
			if(p.x == target.x && p.y == target.y){
				return path;
			}

			ArrayList<Point> neighbors = new ArrayList<Point>();
			neighbors.add(new Point(p.x, p.y + 1));
			neighbors.add(new Point(p.x, p.y - 1));
			neighbors.add(new Point(p.x + 1, p.y));
			neighbors.add(new Point(p.x - 1, p.y));

			for(Point neighbor: neighbors){
				if(pointInBounds(length, neighbor) && !reached.contains(neighbor) && grid[neighbor.x - 1][neighbor.y - 1].isOpen()){
					ArrayList<Point> newPath = new ArrayList<Point>(path);
					newPath.add(neighbor);
					q.add(newPath);
					reached.add(neighbor);
				}
			}
		}
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
 
        simPrinter = new SimPrinter(true);


		double curFirstCnct = grid[firstDrop.x-1][firstDrop.y-1].getConcentration(colorPath.get(0));
		
		if(curFirstCnct < threshold){
			refreshPadding.add(currentTurn + refreshPadding.size()*spawnFreq);
			pcIndexes.add(0);
		}

		if(pcIndexes.size()>0)
        	return determineLocation(currentTurn, 0);
		else{
			vacantRound++;
			if(vacantRound>spawnFreq){
				threshold += 0.05;
				vacantRound = 0;
			}
			return chemicalPlacement;
		}
	}

	// prioritize the "first" or "oldest" path placement, 
	// then when controller is free, deal with the following round placement
	ChemicalPlacement determineLocation(Integer currentTurn, Integer idx){
		ChemicalPlacement chemicalPlacement = new ChemicalPlacement();

		int pcIndex = pcIndexes.get(idx);
		Point currentLocation = placementCells.get(pcIndex);
 
        int turnToPlace = refreshPadding.get(idx) + placementPadding[pcIndex];
        
        if(currentTurn >= turnToPlace){
            chemicalPlacement.location = currentLocation;
            ArrayList<ChemicalType> chemicals = new ArrayList<>();
            chemicals.add(colorPath.get(pcIndex));
            chemicalPlacement.chemicals = chemicals;
            pcIndexes.set(idx, ++pcIndex);
			if(pcIndex>=placementCells.size()){
				if(idx!=0)
					System.err.println("WARNING: Chemical placement sequence messed up");
				pcIndexes.remove(0);
				refreshPadding.remove(0);
				// pcIndexes.add(pcIndex%placementCells.size());
				// refreshPadding.add(padding + pathComplete);
			}
        }
		else{
			// Get to next round, but make sure that the refreshing chem will alaways be three chems behind
			if((idx + 1) < pcIndexes.size() && pcIndexes.get(idx)/3 > pcIndexes.get((idx+1)%pcIndexes.size())/3)
				chemicalPlacement = determineLocation(currentTurn, idx+1);
		}
		return chemicalPlacement;
	}
}