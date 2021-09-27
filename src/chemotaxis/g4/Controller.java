package chemotaxis.g4;

import java.awt.Point;
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
	ArrayList<Integer> pcIndexes;
	ArrayList<Integer> refreshPadding;
	Analysis analyzer = null;


	Integer time_interval = 4;
	Integer path_interval = 5;
	Integer chemical_color = 0;
	Integer arrayLength = 20;
	Integer pathComplete;

	int spawnFreq = 10;
	int agentGoal = 3;

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

		System.out.print("placement Cells:");
		placementCells = new ArrayList<Point>();
		for (int i=1; i<path.size(); i++){
			if(i%path_interval == 0){
				int bestPoint = i;
				// if(data.get(i).p !=  path.get(i)){
				// 	System.out.println("WARNING!!! NOT EQUAL" + data.get(i).p.x + "," + data.get(i).p.y + " " + path.get(i).x + "," + path.get(i).y);
				// }
				if(data.get(bestPoint).isMaxPercentage<data.get(i-1).isMaxPercentage){
					bestPoint = i-1;
				}
				if(data.get(bestPoint).isMaxPercentage<data.get(i+1).isMaxPercentage){
					bestPoint = i+1;
				}
				placementCells.add(path.get(bestPoint));
				System.out.print(" " + path.get(bestPoint) + " ");
			}
		}
		if((path.size()-1)%path_interval!=0){
			placementCells.add(path.get(path.size()-1));
		}
		System.out.print("\n");
		if(placementCells.size()>1){
			pathComplete = 2 * path.indexOf(placementCells.get(placementCells.size()-2)) 
								- path.indexOf(placementCells.get(placementCells.size()-1)) + path_interval;
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
		pcIndexes = new ArrayList<Integer>();
		refreshPadding = new ArrayList<Integer>();
		for(int j=0; j<refreshTimes; j++){
			if(j==0)
				refreshPadding.add(1);
			else{
				for(int k=2; k<placementCells.size(); k++){
					int curIndex = path.indexOf(placementCells.get(k));
					if(curIndex > j*(pathComplete/refreshTimes)){
						int preIndex = path.indexOf(placementCells.get(k));
						refreshPadding.add(2 * preIndex - curIndex + path_interval);
						break;
					}
				}
			}
			pcIndexes.add(0);
			System.out.println("refresh padding " + j + " " + refreshPadding.get(j));
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
	ArrayList<Point> getPath(ChemicalCell[][] grid){
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
 
        int turnToPlace = refreshPadding.get(idx);
        if(pcIndexes.get(idx)==0){
            turnToPlace += 1;
        }
        else{
            int preIndex = path.indexOf(placementCells.get(pcIndexes.get(idx)-1));
            turnToPlace += 2 * preIndex - curIndex + path_interval;
        }
		System.out.println("idx " + idx);
		System.out.println("pcIndexes[idx] " + pcIndex);
		System.out.println("currentLocation " + currentLocation);
		System.out.println("turnToPlace " + turnToPlace);
		System.out.println("currentTurn " + currentTurn);
        
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
				refreshPadding.add(padding + (currentTurn/pathComplete) * pathComplete);
			}
        }
		else{
			if(pcIndexes.get(idx)/3 > pcIndexes.get((idx+1)%pcIndexes.size())/3)
				chemicalPlacement = determineLocation(currentTurn, idx+1);
		}

		return chemicalPlacement;
	}
}
