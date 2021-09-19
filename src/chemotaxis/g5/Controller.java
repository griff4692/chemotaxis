package chemotaxis.g5;

import java.awt.Point;
import java.util.ArrayList;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.SimPrinter;
import chemotaxis.sim.ChemicalCell.ChemicalType;

public class Controller extends chemotaxis.sim.Controller {

   /**
    * Controller constructor
    *
    * @param start       start cell coordinates
    * @param target      target cell coordinates
    * @param size     	 grid/map size
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
    * @param locations           current locations of the agents
    * @param grid                game grid/map
    * @return                    a cell location and list of chemicals to apply
    *
    */
   @Override
   public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid) {
      // get the closest point which is the one we will make go to exit:

      Point closestAgent = getClosestAgent(locations);
      if(closestAgent != null) {
         System.out.println("closest agent is at: " + closestAgent.toString());
      }

      Point locToPlace = getPosCloseToGoal(closestAgent, grid);

      System.out.println("Placing green at: " + locToPlace.toString());

      ChemicalPlacement ret = new ChemicalPlacement();
      ret.location = locToPlace;
      ret.chemicals.add(ChemicalType.GREEN);
      return ret;
   }

   /* Private helper methods to make the code concise
    * Examples: caching the original path along with distance to goal for each one
    */
   private int getManhattanDistance(int targetX, int targetY, int sourceX, int sourceY) {
      return Math.abs((targetX - sourceX) + Math.abs((targetY - sourceY)));
   }

   private Point getClosestAgent(ArrayList<Point> locations) {
      return getClosestPointToTarget(target, locations);
   }

   // TODO: Change this from Manhattan distance to use the path calculated and place one closer to it
   private Point getPosCloseToGoal(Point agentLoc, ChemicalCell[][] grid) {
      ArrayList<Point> availablePositions = new ArrayList<>();

      // go through each of the lateral co-ordinates if possible and watch out for 1-indexing
      int agentX = agentLoc.x;
      int agentY = agentLoc.y;
      int x = agentX - 1;
      int y = agentY - 1;
      if(x > 0 && grid[x - 1][y].isOpen()) {
         availablePositions.add(new Point(agentX-1, agentY));
      }
      if(y > 0 && grid[x][y-1].isOpen()) {
         availablePositions.add(new Point(agentX, agentY-1));
      }
      if(x+1 < grid.length && grid[x+1][y].isOpen()) {
         availablePositions.add(new Point(agentX+1, agentY));
      }
      if(y+1 < grid[0].length && grid[x][y+1].isOpen()) {
         availablePositions.add(new Point(agentX, agentY+1));
      }

      return getClosestPointToTarget(target, availablePositions);
   }

   private Point getClosestPointToTarget(Point target, ArrayList<Point> possibleSources) {
      Integer minDistance = null;
      Point retLoc = null;

      for (Point loc : possibleSources) {
         int manhattanDistance = getManhattanDistance(target.x, target.y, loc.x, loc.y);
         
         if(minDistance == null || manhattanDistance < minDistance.intValue()) {
            minDistance = manhattanDistance;
            retLoc = loc;
         } 
      }
      return retLoc;
   }
}
