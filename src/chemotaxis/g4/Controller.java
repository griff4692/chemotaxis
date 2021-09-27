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
	int[] rollPadding;
	ArrayList<Integer> pcIndexes = null;
	ArrayList<Integer> refreshPadding; 
	Analysis analyzer = null;


	// Integer time_interval = 4;
	Integer drop_interval = 5;
	Integer roll_interval = 10;
	int first_interval = drop_interval;
	Integer chemical_color = 0;
	Integer arrayLength = 20;
	Integer pathComplete;

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
				System.out.println();
				System.out.println();
			}
		}
		else{
			data = analyzer.analyzePath(path, arrayLength);
		}

		placementCells = new ArrayList<Point>();
		getIntervals(path, true);

		int last = path.indexOf(placementCells.get(placementCells.size()-1));
		if(path.size()-1 != last){
			if(last >= path.size()-3){
				placementCells.remove(placementCells.size()-1);
			}
			placementCells.add(path.get(path.size()-1));
		}

		ArrayList<int[]> rollOnIdxes = new ArrayList<int[]>();
		for(int i=0; i<placementCells.size()-1; i++){
			int cur = path.indexOf(placementCells.get(i));
			if(i==0 && ((cur>spawnFreq && cur>drop_interval) || cur>roll_interval)){
				first_interval = Math.min(cur/2, drop_interval);
				placementCells.add(0, path.get(first_interval));
				i--;
				continue;
			}
			else if (i==0){
				first_interval = cur;
			}

			int next = path.indexOf(placementCells.get(i+1));
			int gap = next-cur;
			if(gap <= 3)
				continue;
			else if(gap > roll_interval){
				int[] tmp = {i+1, gap};
				rollOnIdxes.add(tmp);
			}

			int bestPoint = cur;
			if(data.get(bestPoint).isMaxPercentage<=data.get(cur+1).isMaxPercentage){
				bestPoint = cur+1;
			}
			if(data.get(bestPoint).isMaxPercentage<=data.get(cur+2).isMaxPercentage){
				bestPoint = cur+2;
			}
			placementCells.set(i, path.get(bestPoint));

			System.out.print(" " + path.get(bestPoint) + " ");
		}
		System.out.print("\n");

		rollPadding = new int[placementCells.size()];
		for(int i=0; i<rollOnIdxes.size(); i++){
			rollPadding[rollOnIdxes.get(i)[0]] = rollOnIdxes.get(i)[1]-1;
		}

		if(placementCells.size()>1){
			pathComplete = 2 * path.indexOf(placementCells.get(placementCells.size()-2)) 
								- path.indexOf(placementCells.get(placementCells.size()-1)) + first_interval + rollPadding[placementCells.size()-1];
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

		int refreshTimes = Math.min(pathComplete/spawnFreq, agentGoal);
		System.out.println(refreshTimes);
		pcIndexes = new ArrayList<Integer>();
		refreshPadding = new ArrayList<Integer>();
		for(int j=0; j<refreshTimes; j++){
			if(j==0){
				refreshPadding.add(0);
				pcIndexes.add(0);
			}
			else{
				for(int k=2; k<placementCells.size(); k++){
					int curIndex = path.indexOf(placementCells.get(k));
					if(curIndex > Math.max(j*(pathComplete/refreshTimes), 15)){
						int preIndex = path.indexOf(placementCells.get(k));
						refreshPadding.add(2 * preIndex - curIndex + drop_interval);
						pcIndexes.add(0);
						break;
					}
				}
			}

			System.out.println("refresh padding idx:" + j + "; padding:" + refreshPadding.get(j%refreshTimes));
		}
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

	void setUp(){

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
			Integer numIntervalso1 = getIntervals(o1, false);
			Integer numIntervalso2 = getIntervals(o2, false);
			if (numIntervalso1 < numIntervalso2){
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
 
        return determineLocation(currentTurn, 0);

	}

	ChemicalPlacement determineLocation(Integer currentTurn, Integer idx){
		ChemicalPlacement chemicalPlacement = new ChemicalPlacement();

		int pcIndex = pcIndexes.get(idx);
		Point currentLocation = placementCells.get(pcIndex);
        int curIndex = path.indexOf(currentLocation);
 
        int turnToPlace = refreshPadding.get(idx) + rollPadding[pcIndex];
        if(pcIndexes.get(idx)==0){
            turnToPlace += 1;
        }
        else{
            int preIndex = path.indexOf(placementCells.get(pcIndexes.get(idx)-1));
            turnToPlace += 2 * preIndex - curIndex + first_interval;
			// System.out.println("preIndex " + (2 * preIndex - curIndex + drop_interval));
        }

		System.out.println("idx " + idx);
		System.out.println("pcIndexes[idx] " + pcIndex);
		System.out.println("currentLocation " + currentLocation);
		System.out.println("turnToPlace " + turnToPlace);
		System.out.println("currentTurn " + currentTurn);
		// System.out.println("!!!!!!!!!!!!!!!!!" + grid[3][14].getConcentration(ChemicalType.BLUE));
		// System.out.println("!!!!!!!!!!!!!!!!!" + grid[14][4].getConcentration(ChemicalType.BLUE));
		// System.out.println("!!!!!!!!!!!!!!!!!" + grid[14][5].getConcentration(ChemicalType.BLUE));
		// System.out.println("!!!!!!!!!!!!!!!!!" + grid[14][6].getConcentration(ChemicalType.BLUE));
        
        if(currentTurn >= turnToPlace){
            chemicalPlacement.location = currentLocation;
            ArrayList<ChemicalType> chemicals = new ArrayList<>();
            chemicals.add(colorPath.get(pcIndex));
            chemicalPlacement.chemicals = chemicals;
            pcIndexes.set(idx, ++pcIndex);
			System.out.println("Chemical Placed at " + currentLocation);
			if(pcIndex>=placementCells.size()){
				int padding = refreshPadding.get(idx);
				if(idx!=0)
					System.err.println("WARNING: Chemical placement sequence messed up");
				pcIndexes.remove(0);
				refreshPadding.remove(0);
				pcIndexes.add(pcIndex%placementCells.size());
				refreshPadding.add(padding + pathComplete);
			}
        }
		else{
			if(pcIndexes.get(idx)/3 > pcIndexes.get((idx+1)%pcIndexes.size())/3)
				chemicalPlacement = determineLocation(currentTurn, idx+1);
		}
		return chemicalPlacement;
	}
}