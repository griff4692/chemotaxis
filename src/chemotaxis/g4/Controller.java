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
	Integer index = 0;
	Analysis analyzer = null;


	Integer time_interval = 4;
	Integer path_interval = 5;
	Integer chemical_color = 0;
	Integer arrayLength = 20;

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
		if(analyzer == null){
			analyzer = new Analysis(grid);
			analyzer.setThreshold(0.05); //default is 0.05

			ArrayList<AnalysisData> data = analyzer.analyzePath(path, arrayLength);


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
		Point p = new Point(11,6);
		int index = path.indexOf(p);
		System.out.println("from controller:");
		System.out.println(grid[p.x - 1][p.y - 1].getConcentration(ChemicalType.BLUE));
		chemicalPlacement.location = p;
		ArrayList<ChemicalType> chemicals = new ArrayList<ChemicalType>();
		if(currentTurn == 1) chemicals.add(ChemicalType.BLUE);
		else{
			if(analyzer.hasGoodGradient(grid, path, index)){
				System.out.printf("still a good gradient on turn: %d\n", currentTurn);
			}
			else System.out.printf("not a good gradient on turn: %d\n", currentTurn);
			if(analyzer.isMax(grid, p)){
				System.out.printf("still a max on turn: %d\n", currentTurn);
			}
			else System.out.printf("not a max on turn: %d\n", currentTurn);
			System.out.printf("reached on turn %d: %d\n", currentTurn, analyzer.calculateReached(grid, path, index));

		}
		chemicalPlacement.chemicals = chemicals;

		return chemicalPlacement;

		//simPrinter = new SimPrinter(true);





		//return chemicalPlacement;
	}
}
