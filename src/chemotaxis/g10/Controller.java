package chemotaxis.g10;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.HashMap;

import chemotaxis.sim.*;

public class Controller extends chemotaxis.sim.Controller {

   private TurnGridNode [][] turnGrid;
   private ArrayList<Integer> agentsLastNumTurns;
   private ArrayList<Point> agentsLastLocation;
   private ArrayList<DirectionType> agentsLastDir;
   private ChemicalCell.ChemicalType lastChemPlaced;

   /**
    * Controller constructor
    *
    * @param start       start cell coordinates
    * @param target      target cell coordinates
    * @param size     	 grid/map size
    * @param grid        game grid/map
    * @param simTime     simulation time
    * @param budget      chemical budget
    * @param seed        random seed
    * @param simPrinter  simulation printer
    *
    */
   public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
      super(start, target, size, grid, simTime, budget, seed, simPrinter);

      computeTurnGrid(grid);
      agentsLastNumTurns = new ArrayList<>();
      agentsLastLocation = new ArrayList<>();
      agentsLastDir = new ArrayList<>();
      lastChemPlaced = ChemicalCell.ChemicalType.GREEN;
   }

   /**
    * Computes shortest turn paths from all unblocked cells in the grid.
    *
    * @param grid        game grid/map
    *
    */
   private void computeTurnGrid(ChemicalCell [][] grid) {
      turnGrid = new TurnGridNode[size][size];

      Point target0Ind = new Point(target.x - 1, target.y - 1);
      turnGrid[target0Ind.x][target0Ind.y] = new TurnGridNode(0, target0Ind, target0Ind);

      PriorityQueue<TurnGridNode> frontier = new PriorityQueue<TurnGridNode>(size * size);
      frontier.add(turnGrid[target0Ind.x][target0Ind.y]);

      TurnGridNode cur;

      int deltaXY[][] = {{1, 0}, {0, -1}, {-1, 0}, {0, 1}}; // right, up, left, down

      while (!frontier.isEmpty()) {
         cur = frontier.poll();

         for (int dir = 0; dir < 4; dir++) {
            int x = cur.getGridPoint().x;
            int y = cur.getGridPoint().y;

            while (x >= 0 && x < size && y >= 0 && y < size && grid[x][y].isOpen()) {
               if (turnGrid[x][y] == null) {
                  TurnGridNode n = new TurnGridNode(cur.getTurns() + 1, x, y, cur.getGridPoint());
                  turnGrid[x][y] = n;
                  frontier.add(n);
               }
               x += deltaXY[dir][0];
               y += deltaXY[dir][1];
            }
         }
      }
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
      ChemicalPlacement chemPlacement;
      if (chemicalsRemaining > 0) {
         for (int i = 0; i < locations.size(); i++) {
            Point agentLocation = locations.get(i);
            TurnGridNode agentTurnGridNode = turnGrid[agentLocation.x - 1][agentLocation.y - 1];

            if (agentsLastLocation.size() != locations.size()) {
               agentsLastNumTurns.add(agentTurnGridNode.getTurns());
               agentsLastLocation.add(agentLocation);
               agentsLastDir.add(DirectionType.CURRENT);
            }

            agentsLastDir.set(i, getAgentDirection(agentsLastLocation.get(i), agentLocation));

            int numNeighborsBlocked = 0;
            if (agentLocation.x == 1 || (agentLocation.x > 1 && grid[agentLocation.x - 2][agentLocation.y - 1].isBlocked())) numNeighborsBlocked++;
            if (agentLocation.y == 1 || (agentLocation.y > 1 && grid[agentLocation.x - 1][agentLocation.y - 2].isBlocked())) numNeighborsBlocked++;
            if (agentLocation.x == grid[0].length || (grid[0].length > agentLocation.x && grid[agentLocation.x][agentLocation.y - 1].isBlocked())) numNeighborsBlocked++;
            if (agentLocation.y == grid.length || grid.length > agentLocation.y && grid[agentLocation.x - 1][agentLocation.y].isBlocked()) numNeighborsBlocked++;

            if ((agentLocation.x != target.x || agentLocation.y != target.y) && (numNeighborsBlocked < 2 || (numNeighborsBlocked == 2 && getAgentDirection(agentsLastLocation.get(i), agentLocation) == getOppositeDirection(agentsLastDir.get(i)))) && ((agentLocation.x == start.x && agentLocation.y == start.y) || agentTurnGridNode.getTurns() != agentsLastNumTurns.get(i))) {
////            findOptimalMove(DirectionType previousDirection, ChemicalCell.ChemicalType chosenChemicalType, Map<DirectionType, ChemicalCell> neighborMap)
//
//            DirectionType agentsNextDir = Agent.findOptimalMove(agentsLastDir.get(i), lastChemPlaced == ChemicalCell.ChemicalType.RED ? ChemicalCell.ChemicalType.GREEN : ChemicalCell.ChemicalType.RED, getAgentsNeighborMap(grid, agentLocation));
//            if(agentsNextDir.equals(DirectionType.CURRENT)) agentsNextDir = agentsLastDir.get(i);
//            System.out.println(agentLocation);
//            System.out.println(agentsNextDir);
//            Point agentsNextLocation = getAgentsNextLocation(agentLocation, agentsNextDir);
//            TurnGridNode agentsNextTurnGridNode = getAgentsNextTurnGridNode(agentLocation, agentsNextDir);
//            System.out.println("CONTROLLER: Agents calculated optimal move: " + agentsNextDir);
//            System.out.println("Agents turn grid node: (" + String.valueOf(agentsNextTurnGridNode.getGridPoint().x + 1) + ", " + String.valueOf(agentsNextTurnGridNode.getGridPoint().y + 1) + ")");
//            System.out.println("Agents next location: (" + String.valueOf(agentsNextLocation.x) + ", " + String.valueOf(agentsNextLocation.y) + ")");
//            System.out.println("is blocked: " + grid[agentsNextLocation.x - 1][agentsNextLocation.y - 1].isBlocked());
//
//            if ((agentLocation.x != target.x || agentLocation.y != target.y) && (numNeighborsBlocked < 2 || (numNeighborsBlocked == 2 && getAgentDirection(agentsLastLocation.get(i), agentLocation) == getOppositeDirection(agentsLastDir.get(i)))) && ((agentLocation.x == start.x && agentLocation.y == start.y) || (agentsNextTurnGridNode.getTurns() > agentTurnGridNode.getTurns() || grid[agentsNextLocation.x - 1][agentsNextLocation.y - 1].isBlocked()))) {
               chemPlacement = new ChemicalPlacement();
               Point parentPoint = agentTurnGridNode.getParentPoint();
               if (parentPoint.x + 1 == agentLocation.x) {
                  chemPlacement.location = new Point(agentLocation.x, (parentPoint.y > agentLocation.y - 1) ? (agentLocation.y + 1) : (agentLocation.y - 1));
               } else if (parentPoint.y + 1 == agentLocation.y) {
                  chemPlacement.location = new Point((parentPoint.x > agentLocation.x - 1) ? (agentLocation.x + 1) : (agentLocation.x - 1), agentLocation.y);
               }

               ChemicalCell.ChemicalType chemPlacing = lastChemPlaced == ChemicalCell.ChemicalType.GREEN ? ChemicalCell.ChemicalType.RED : ChemicalCell.ChemicalType.GREEN;
               chemPlacement.chemicals.add(chemPlacing);
               System.out.println("Placed chemical: " + chemPlacing);
               this.lastChemPlaced = chemPlacing;
               agentsLastNumTurns.set(i, agentTurnGridNode.getTurns());
               return chemPlacement;
            }
            agentsLastLocation.set(i, agentLocation);
         }
      }

      return new ChemicalPlacement();
   }


   private Point getAgentsNextLocation(Point agentLocation, DirectionType dir) {
      switch (dir) {
         case NORTH:
            if (agentLocation.y != 1) return new Point(agentLocation.x, agentLocation.y - 1);
            break;
         case EAST:
            if (agentLocation.x != turnGrid[0].length) return new Point(agentLocation.x + 1, agentLocation.y);
            break;
         case SOUTH:
            if (agentLocation.y != turnGrid.length) return new Point(agentLocation.x, agentLocation.y + 1);
            break;
         case WEST:
            if (agentLocation.x != 1) return new Point(agentLocation.x - 1, agentLocation.y);
            break;
      }
      return agentLocation;
   }


   private TurnGridNode getAgentsNextTurnGridNode(Point agentLocation, DirectionType agentsNextDir) {
      Point nextPoint = getAgentsNextLocation(agentLocation, agentsNextDir);
      return turnGrid[nextPoint.x - 1][nextPoint.y - 1];
   }


   private Map<DirectionType, ChemicalCell> getAgentsNeighborMap(ChemicalCell[][] grid, Point agentLocation) {
      Map<DirectionType, ChemicalCell> neighborMap = new HashMap<>();

      if(agentLocation.y == 1)
         neighborMap.put(DirectionType.NORTH, new ChemicalCell(false));
      else
         neighborMap.put(DirectionType.NORTH, grid[agentLocation.x - 1][agentLocation.y - 2]);

      if(agentLocation.y == grid.length)
         neighborMap.put(DirectionType.SOUTH, new ChemicalCell(false));
      else
         neighborMap.put(DirectionType.SOUTH, grid[agentLocation.x - 1][agentLocation.y]);

      if(agentLocation.x == grid[0].length)
         neighborMap.put(DirectionType.EAST, new ChemicalCell(false));
      else
         neighborMap.put(DirectionType.EAST, grid[agentLocation.x][agentLocation.y - 1]);

      if(agentLocation.x == 1)
         neighborMap.put(DirectionType.WEST, new ChemicalCell(false));
      else
         neighborMap.put(DirectionType.WEST, grid[agentLocation.x - 2][agentLocation.y - 1]);

      return neighborMap;
   }

   /**
    * Get direction agent moved in
    *
    * @param lastPoint           agent's location last turn
    * @param currentPoint        agent's location this turn
    * @return                    agent's direction
    *
    */
   private DirectionType getAgentDirection(Point lastPoint, Point currentPoint) {
      if (lastPoint == currentPoint) return DirectionType.CURRENT;
      if (lastPoint.x == currentPoint.x) {
         return (lastPoint.y < currentPoint.y) ? DirectionType.EAST : DirectionType.WEST;
      } else if (lastPoint.y == currentPoint.y) {
         return (lastPoint.x < currentPoint.x) ? DirectionType.SOUTH : DirectionType.NORTH;
      }
      return null;
   }

   /**
    * Get opposite direction of direction provided
    *
    * @param dir                 direction
    * @return                    opposite direction of provided direction
    *
    */
   private DirectionType getOppositeDirection(DirectionType dir) {
      if (dir == DirectionType.NORTH) return DirectionType.SOUTH;
      if (dir == DirectionType.SOUTH) return DirectionType.NORTH;
      if (dir == DirectionType.EAST) return DirectionType.WEST;
      if (dir == DirectionType.WEST) return DirectionType.EAST;
      return DirectionType.CURRENT;
   }
}
