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
	int first_interval = drop_interval;
	Point firstDrop;
	Integer chemical_color = 0;
	Integer arrayLength = 20;
	Integer pathComplete;
	double threshold = 0.1;
	int vacantRound = 0;


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
			analyzer.setThreshold(0.05); //default is 0.05

			data = analyzer.analyzePath(path, arrayLength);

			for(AnalysisData dataPoint:data){
				System.out.printf("for point %d, %d:\n", dataPoint.p.x, dataPoint.p.y);
				System.out.println("isMax:");
				for(boolean b: dataPoint.isMax){
					System.out.print(b);
				}
				System.out.println();

				System.out.println("hasGoodGradient:");
				for(boolean b: dataPoint.goodGradient){
					System.out.print(b);
				}
				System.out.println();

				System.out.println("distance reached:");
				for(int i: dataPoint.distanceReached){
					System.out.printf("%d ", i);
				}
				System.out.println();
				System.out.printf("is max percentage: %f\n", dataPoint.isMaxPercentage);
				System.out.printf("has good gradient percentage: %f\n", dataPoint.isGoodGradientPercentage);

				System.out.println("Max reached:");
				System.out.println(dataPoint.maxDistance);
				System.out.println(dataPoint.willReach(4));
				System.out.println(dataPoint.turnsWillReach(4)[0]);
				System.out.println(dataPoint.turnsWillReach(4)[1]);
				System.out.println();
				System.out.println();
			}
		}
		else{
			data = analyzer.analyzePath(path, arrayLength);
		}

		placementCells = new ArrayList<Point>();
		// Analyze the selected path and get all turning points as initial placement cells
		getIntervals(path, true);
		// Make sure that the target is included
		// If there is turnning point inside target cell's cover range, remove it.
		int last = path.indexOf(placementCells.get(placementCells.size()-1));
		if(path.size()-1 != last){
			if(last >= path.size()-2){
				placementCells.remove(placementCells.size()-1);
			}
			placementCells.add(path.get(path.size()-1));
		}

		placementPadding = new int[placementCells.size()];
		int cur = path.indexOf(placementCells.get(0));
		placementPadding[0] = 0;
		if(cur>roll_interval){
			placementCells.add(0, path.get(first_interval));
			cur = first_interval;
		}
		first_interval = cur;
		int prev = 0;
		System.out.println(prev);
		// For each placement cells check the following two cells
		// and find the one with largest percentage of remaining as local max.
		for(int i=0; i<placementCells.size(); i++){
			cur = path.indexOf(placementCells.get(i));
			// Distance from A to B]
			int gap = cur-prev;
			System.out.print(" " + placementCells.get(i) + " ");

			int next = cur;
			if(i < placementCells.size()-1 && gap < roll_interval){
				next = path.indexOf(placementCells.get(i+1));
				if(data.get(next).maxDistance>=next-cur && i < placementCells.size()-2){
					int nextNext = path.indexOf(placementCells.get(i+2));
					System.out.print("nn-cur: " + (nextNext-cur));
					if(data.get(nextNext).maxDistance > nextNext-cur){
						System.out.print(" removed" + placementCells.get(i+1) + " ");
						placementCells.remove(i+1);
					}
				}
			}
			
			// Say A --15 cells --> B; so the rolling mode is on
			// Turn the rolling mode on
			if(gap >= roll_interval){ 
				placementPadding[i] += 2*(gap-1);
			}

			int bestPoint = cur;
			if(i != placementCells.size()-1){
				if(data.get(bestPoint).isMaxPercentage<=data.get(cur+1).isMaxPercentage && (next-cur)>2){
					bestPoint = cur+1;
				}
				if(data.get(bestPoint).isMaxPercentage<=data.get(cur+2).isMaxPercentage && (next-cur)>3){
					bestPoint = cur+2;
				}
				placementCells.set(i, path.get(bestPoint));
			}
			System.out.print(" " + path.get(bestPoint) + " ");

			if(i!=0){
				System.out.print("i:" + i + " padding compute" + prev + " " + cur + " " + first_interval + "; ");
				placementPadding[i] += 2 * prev - cur + first_interval;
			}
			
			System.out.println("result " + placementPadding[i]);
			prev = cur;
		}
		System.out.print("\n");

		for(int k=0; k<placementPadding.length; k++)
			System.out.println(placementPadding[k]);

		firstDrop = placementCells.get(0);

		// Calculate the turns needed to finish one round of chemical placing
		if(placementCells.size()>0){
			pathComplete = placementPadding[placementCells.size()-1];
		}
		else
			pathComplete = 1;
		System.out.println("Turns for controller to complete a singel path: "+ pathComplete + "\n");

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

		// to keep track the refreshing turn, i.e. refreshing btw finishing the first (after the first round finish, it will start looping agian)
		// The goal is to refreshing without interfering the previous one
		// So multiple boundaries are set. The numbers are chosen carefully, but totally open to adjustment
		// To-Do: Didn't find a way that it can guarantee the agentGoal.

		// System.out.println(pathComplete);
		// int refreshTimes = Math.min(pathComplete/spawnFreq, agentGoal);
		// System.out.println(refreshTimes);
		pcIndexes = new ArrayList<Integer>();
		refreshPadding = new ArrayList<Integer>();
		refreshPadding.add(0);
		pcIndexes.add(0);
		// for(int j=0; j<refreshTimes; j++){
		// 	if(j==0){
		// 		refreshPadding.add(0);
		// 		pcIndexes.add(0);
		// 	}
		// 	else{
		// 		for(int k=2*j; k<placementCells.size(); k++){
		// 			int curIndex = path.indexOf(placementCells.get(k));
		// 			if(curIndex > Math.max(j*(pathComplete/refreshTimes), j*15)){
		// 				int preIndex = path.indexOf(placementCells.get(k));
		// 				int padding = 2 * preIndex - curIndex + first_interval;
		// 				if((pathComplete - padding) > 15){
		// 					refreshPadding.add(padding);
		// 					pcIndexes.add(0);
		// 					System.out.println("refresh padding idx:" + j + "; padding:" + refreshPadding.get(j%refreshTimes));
		// 				}
		// 				break;
		// 			}
		// 		}
		// 	}
		// }

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


	Integer getIntervals(ArrayList<Point> current){
		ArrayList<Integer> values = new ArrayList<>();
		for (int i=1;i<current.size();i=i+1) {
			Point curr = current.get(i);
			Integer curr_x = curr.x;
			Integer curr_y = curr.y;
			Point prev = current.get(i - 1);
			Integer prev_x = prev.x;
			Integer prev_y = prev.y;

			if (curr_x - prev_x < 0) {
				values.add(1);
			} else if (curr_x - prev_x > 0) {
				values.add(2);
			} else if (curr_y - prev_y < 0) {
				values.add(3);
			} else {
				values.add(4);
			}
		}

		Integer numIntervals = 0;

		for (int i=1;i<values.size();i=i+1){
			Integer curr = values.get(i);
			Integer prev = values.get(i-1);
			if (curr == prev) {
				continue;
			}
			else{
				numIntervals = numIntervals + 1;
			}
		}

		return numIntervals;
	}

	Integer getIntervals(ArrayList<Point> current, boolean setTurning){
		ArrayList<Integer> values = new ArrayList<>();
		for (int i=1;i<current.size();i=i+1) {
			Point curr = current.get(i);
			Integer curr_x = curr.x;
			Integer curr_y = curr.y;
			Point prev = current.get(i - 1);
			Integer prev_x = prev.x;
			Integer prev_y = prev.y;

			if (curr_x - prev_x < 0) {
				values.add(1);
			} else if (curr_x - prev_x > 0) {
				values.add(2);
			} else if (curr_y - prev_y < 0) {
				values.add(3);
			} else {
				values.add(4);
			}
		}

		Integer numIntervals = 0;

		for (int i=1;i<values.size();i=i+1){
			Integer curr = values.get(i);
			Integer prev = values.get(i-1);
			if (curr == prev) {
				continue;
			}
			else{
				numIntervals = numIntervals + 1;
				if(setTurning){
					placementCells.add(current.get(i+1));
					System.out.println("Point after Turning " + path.get(i+1) + " ");
				}
			}
		}

		return numIntervals;
	}

	class CustomIntegerComparator implements Comparator<ArrayList<Point>> {

		@Override
		public int compare(ArrayList<Point> o1, ArrayList<Point> o2) {
			Integer numIntervalso1 = getIntervals(o1);
			Integer numIntervalso2 = getIntervals(o2);
			Integer numNeighborso1 = countNeighborsInPath(o1);
			Integer numNeighborso2 = countNeighborsInPath(o2);
			if (numIntervalso1 - (int)0.5*numNeighborso1 + o1.size() < numIntervalso2 - (int)0.5*numNeighborso2 + o2.size()){
				return -1;}
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
			refreshPadding.add(currentTurn);
			pcIndexes.add(0);
			System.out.println("Refreshing! grid[firstDrop.x-1][firstDrop.y]: " + firstDrop.x + ", " + firstDrop.y + "; Concentration " + curFirstCnct);
		}

 
		if(pcIndexes.size()>0)
        	return determineLocation(currentTurn, 0);
		else{
			vacantRound++;
			if(vacantRound>50){
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

		// System.out.println("idx " + idx);
		// System.out.println("pcIndexes[idx] " + pcIndex);
		// System.out.println("currentLocation " + currentLocation);
		// System.out.println("turnToPlace " + turnToPlace);
		// System.out.println("currentTurn " + currentTurn);

        
        if(currentTurn >= turnToPlace){
            chemicalPlacement.location = currentLocation;
            ArrayList<ChemicalType> chemicals = new ArrayList<>();
            chemicals.add(colorPath.get(pcIndex));
            chemicalPlacement.chemicals = chemicals;
            pcIndexes.set(idx, ++pcIndex);
			System.out.println("Chemical Placed at " + currentLocation);
			if(pcIndex>=placementCells.size()){
				// int padding = refreshPadding.get(idx);
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