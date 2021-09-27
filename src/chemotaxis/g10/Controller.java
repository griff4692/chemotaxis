package chemotaxis.g10;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.HashMap;

import chemotaxis.sim.*;

public class Controller extends chemotaxis.sim.Controller {

   private TurnGridNode [][] turnGrid;
   private ArrayList<Point> turnsOnPath;
   private ArrayList<Integer> agentsLastNumTurns;
   private ArrayList<Point> agentsLastLocation;
   private ArrayList<DirectionType> agentsLastDir;
//   private ChemicalCell.ChemicalType lastChemPlaced;

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
      findDesiredPath(grid, start);
      agentsLastNumTurns = new ArrayList<>();
      agentsLastLocation = new ArrayList<>();
      agentsLastDir = new ArrayList<>();
//      lastChemPlaced = ChemicalCell.ChemicalType.GREEN;
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


   private void findDesiredPath(ChemicalCell[][] grid, Point start) {
      turnsOnPath = new ArrayList<>();
      Point lastAgentLocation = start;
      Point agentLocation = start;
      int numTurnsLeft = turnGrid[start.x - 1][start.y - 1].getTurns();

      while (numTurnsLeft > 1) {
         Point bestLocation = agentLocation;
         if (agentLocation.x != 1 && turnGrid[agentLocation.x - 2][agentLocation.y - 1] != null && turnGrid[agentLocation.x - 2][agentLocation.y - 1].getTurns() < numTurnsLeft) {
            numTurnsLeft = turnGrid[agentLocation.x - 2][agentLocation.y - 1].getTurns();
            bestLocation = new Point(agentLocation.x - 1, agentLocation.y);
         }
         if (agentLocation.y != turnGrid.length && turnGrid[agentLocation.x - 1][agentLocation.y] != null && turnGrid[agentLocation.x - 1][agentLocation.y].getTurns() < numTurnsLeft) {
            numTurnsLeft = turnGrid[agentLocation.x - 1][agentLocation.y].getTurns();
            bestLocation = new Point(agentLocation.x, agentLocation.y + 1);
         }
         if (agentLocation.x != turnGrid[0].length && turnGrid[agentLocation.x][agentLocation.y - 1] != null && turnGrid[agentLocation.x][agentLocation.y - 1].getTurns() < numTurnsLeft) {
            numTurnsLeft = turnGrid[agentLocation.x][agentLocation.y - 1].getTurns();
            bestLocation = new Point(agentLocation.x + 1, agentLocation.y);
         }
         if (agentLocation.y != 1 && turnGrid[agentLocation.x - 1][agentLocation.y - 2] != null && turnGrid[agentLocation.x - 1][agentLocation.y - 2].getTurns() < numTurnsLeft) {
            numTurnsLeft = turnGrid[agentLocation.x - 1][agentLocation.y - 2].getTurns();
            bestLocation = new Point(agentLocation.x, agentLocation.y - 1);
         }

         // Making sure this turn is a new move and not in the same direction as before
         if (!(agentLocation.x == bestLocation.x && agentLocation.y == bestLocation.y)) {
            if ((agentLocation.x == start.x && agentLocation.y == start.y && getNumNeighborsBlocked(grid, agentLocation) < 3) || (getNumNeighborsBlocked(grid, agentLocation) < 2 && !((lastAgentLocation.x == agentLocation.x && agentLocation.x == bestLocation.x) || (lastAgentLocation.y == agentLocation.y && agentLocation.y == bestLocation.y)))) {
               turnsOnPath.add(agentLocation);
            }
         } else { // agent will move in same direction as before if not blocked
            if (lastAgentLocation.x == agentLocation.x) {
               if (lastAgentLocation.y < agentLocation.y) { // moving east
                  if (agentLocation.y != grid.length && grid[agentLocation.x - 1][agentLocation.y].isOpen()) {
                     bestLocation = new Point(agentLocation.x, agentLocation.y + 1);
                  } else {
                     bestLocation = getBestOrthogonalLocation(turnGrid[agentLocation.x - 2][agentLocation.y - 1], turnGrid[agentLocation.x][agentLocation.y - 1], agentLocation);
                  }
               } else { // moving west
                  if (agentLocation.y != 1 && grid[agentLocation.x - 1][agentLocation.y - 2].isOpen()) {
                     bestLocation = new Point(agentLocation.x, agentLocation.y - 1);
                  } else {
                     bestLocation = getBestOrthogonalLocation(turnGrid[agentLocation.x][agentLocation.y - 1], turnGrid[agentLocation.x - 2][agentLocation.y - 1], agentLocation);
                  }
               }
            } else if (lastAgentLocation.y == bestLocation.y) {
               if (lastAgentLocation.x < agentLocation.x) { // moving south
                  if (agentLocation.x != grid[0].length && grid[agentLocation.x][agentLocation.y - 1].isOpen()) {
                     bestLocation = new Point(agentLocation.x + 1, agentLocation.y);
                  } else {
                     bestLocation = getBestOrthogonalLocation(turnGrid[agentLocation.x - 1][agentLocation.y], turnGrid[agentLocation.x - 1][agentLocation.y - 2], agentLocation);
                  }
               } else { // moving north
                  if (agentLocation.x != 1 && grid[agentLocation.x - 2][agentLocation.y - 1].isOpen()) {
                     bestLocation = new Point(agentLocation.x - 1, agentLocation.y);
                  } else {
                     bestLocation = getBestOrthogonalLocation(turnGrid[agentLocation.x - 1][agentLocation.y - 2], turnGrid[agentLocation.x - 1][agentLocation.y], agentLocation);
                  }
               }
            }
         }

         lastAgentLocation = agentLocation;
         agentLocation = bestLocation;
      }
   }


   private Point getBestOrthogonalLocation(TurnGridNode leftTurnGridNode, TurnGridNode rightTurnGridNode, Point agentLocation) {
      if (leftTurnGridNode == null) {
         Point rightPoint = rightTurnGridNode.getGridPoint();
         return new Point(rightPoint.x + 1, rightPoint.y + 1);
      } else if (rightTurnGridNode == null) {
         Point leftPoint = leftTurnGridNode.getGridPoint();
         return new Point(leftPoint.x + 1, leftPoint.y + 1);
      }

      Point leftPoint = leftTurnGridNode.getGridPoint();
      Point rightPoint = rightTurnGridNode.getGridPoint();
      Point leftParentPoint = leftTurnGridNode.getParentPoint();
      Point rightParentPoint = rightTurnGridNode.getParentPoint();
      turnsOnPath.add(agentLocation);
      if ((Math.abs(leftParentPoint.x - leftPoint.x) + Math.abs(leftParentPoint.y - leftPoint.y)) < (Math.abs(rightParentPoint.x - rightPoint.x) + Math.abs(rightParentPoint.y - rightPoint.y))) {
         return new Point(leftPoint.x + 1, leftPoint.y + 1);
      } else {
         return new Point(rightPoint.x + 1, rightPoint.y + 1);
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

            int turnStep = turnsOnPath.indexOf(agentLocation);
            if (turnStep != -1) {
               chemPlacement = new ChemicalPlacement();
//               Point parentPoint = agentTurnGridNode.getParentPoint();
//               if (parentPoint.x + 1 == agentLocation.x) {
//                  chemPlacement.location = new Point(agentLocation.x, (parentPoint.y > agentLocation.y - 1) ? (agentLocation.y + 1) : (agentLocation.y - 1));
//               } else if (parentPoint.y + 1 == agentLocation.y) {
//                  chemPlacement.location = new Point((parentPoint.x > agentLocation.x - 1) ? (agentLocation.x + 1) : (agentLocation.x - 1), agentLocation.y);
//               }
//
//               ChemicalCell.ChemicalType chemPlacing = lastChemPlaced == ChemicalCell.ChemicalType.GREEN ? ChemicalCell.ChemicalType.RED : ChemicalCell.ChemicalType.GREEN;
//               chemPlacement.chemicals.add(chemPlacing);
//               System.out.println("Placed chemical: " + chemPlacing);
//               this.lastChemPlaced = chemPlacing;
//               agentsLastNumTurns.set(i, agentTurnGridNode.getTurns());
//               agentsLastLocation.set(i, agentLocation);
               return chemPlacement;
            }
            agentsLastLocation.set(i, agentLocation);
         }
      }

      return new ChemicalPlacement();
   }

//   @Override
//   public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid) {
//      ChemicalPlacement chemPlacement;
//      if (chemicalsRemaining > 0) {
//         for (int i = 0; i < locations.size(); i++) {
//            Point agentLocation = locations.get(i);
//            TurnGridNode agentTurnGridNode = turnGrid[agentLocation.x - 1][agentLocation.y - 1];
//
//            if (agentsLastLocation.size() != locations.size()) {
//               agentsLastNumTurns.add(agentTurnGridNode.getTurns());
//               agentsLastLocation.add(agentLocation);
//               agentsLastDir.add(DirectionType.CURRENT);
//            }
//            agentsLastDir.set(i, getAgentDirection(agentsLastLocation.get(i), agentLocation));
//
//
//
//            DirectionType agentExpectedDir = (DirectionType) Agent.findOptimalMove(agentsLastDir.get(i), lastChemPlaced == ChemicalCell.ChemicalType.RED ? ChemicalCell.ChemicalType.GREEN : ChemicalCell.ChemicalType.RED, getAgentNeighborMap(grid, agentLocation))[0];
//            DirectionType agentOptimalDir = getAgentOptimalDirection(agentLocation, agentsLastDir.get(i));
//            Point agentExpectedLocation = getAgentExpectedLocation(agentLocation, agentExpectedDir);
//            System.out.println("CONTROLLER: Agent " + String.valueOf(i) + "'s calculated optimal move: " + agentExpectedDir);
//            System.out.println("CONTROLLER: Agent " + String.valueOf(i) + "'s wanted move: " + agentOptimalDir);
//
//            if ((agentLocation.x != target.x || agentLocation.y != target.y) && (getNumNeighborsBlocked(grid, agentLocation) < 2 || (getNumNeighborsBlocked(grid, agentLocation) == 2 && getAgentDirection(agentsLastLocation.get(i), agentLocation) == getOppositeDirection(agentsLastDir.get(i)))) && (!agentExpectedDir.equals(agentOptimalDir) || grid[agentExpectedLocation.x - 1][agentExpectedLocation.y - 1].isBlocked())) {
//               chemPlacement = new ChemicalPlacement();
//               Point parentPoint = agentTurnGridNode.getParentPoint();
//               if (parentPoint.x + 1 == agentLocation.x) {
//                  chemPlacement.location = new Point(agentLocation.x, (parentPoint.y > agentLocation.y - 1) ? (agentLocation.y + 1) : (agentLocation.y - 1));
//               } else if (parentPoint.y + 1 == agentLocation.y) {
//                  chemPlacement.location = new Point((parentPoint.x > agentLocation.x - 1) ? (agentLocation.x + 1) : (agentLocation.x - 1), agentLocation.y);
//               }
//
//               ChemicalCell.ChemicalType chemPlacing = lastChemPlaced == ChemicalCell.ChemicalType.GREEN ? ChemicalCell.ChemicalType.RED : ChemicalCell.ChemicalType.GREEN;
//               chemPlacement.chemicals.add(chemPlacing);
//               System.out.println("Placed chemical: " + chemPlacing);
//               this.lastChemPlaced = chemPlacing;
//               agentsLastNumTurns.set(i, agentTurnGridNode.getTurns());
//               agentsLastLocation.set(i, agentLocation);
//               return chemPlacement;
//            }
//            agentsLastLocation.set(i, agentLocation);
//         }
//      }
//
//      return new ChemicalPlacement();
//   }


   private DirectionType getAgentOptimalDirection(Point agentLocation, DirectionType agentDir) {
      int currTurns = turnGrid[agentLocation.x - 1][agentLocation.y - 1].getTurns();
      int totalMinTurns = currTurns;
      DirectionType optimalAgentDirection = agentDir;

      if (agentLocation.y != 1 && turnGrid[agentLocation.x - 1][agentLocation.y - 2] != null && turnGrid[agentLocation.x - 1][agentLocation.y - 2].getTurns() < totalMinTurns) {
         totalMinTurns = turnGrid[agentLocation.x - 1][agentLocation.y - 2].getTurns();
         optimalAgentDirection = DirectionType.WEST;
      }
      if (agentLocation.x != turnGrid[0].length && turnGrid[agentLocation.x][agentLocation.y - 1] != null && turnGrid[agentLocation.x][agentLocation.y - 1].getTurns() < totalMinTurns) {
         totalMinTurns = turnGrid[agentLocation.x][agentLocation.y - 1].getTurns();
         optimalAgentDirection = DirectionType.SOUTH;
      }
      if (agentLocation.y != turnGrid.length && turnGrid[agentLocation.x - 1][agentLocation.y] != null && turnGrid[agentLocation.x - 1][agentLocation.y].getTurns() < totalMinTurns) {
         totalMinTurns = turnGrid[agentLocation.x - 1][agentLocation.y].getTurns();
         optimalAgentDirection = DirectionType.EAST;
      }
      if (agentLocation.x != 1 && turnGrid[agentLocation.x - 2][agentLocation.y - 1] != null && turnGrid[agentLocation.x - 2][agentLocation.y - 1].getTurns() < totalMinTurns) {
         optimalAgentDirection = DirectionType.NORTH;
      }
      return optimalAgentDirection;
   }


   private Point getAgentExpectedLocation(Point agentLocation, DirectionType dir) {
      switch (dir) {
         case NORTH:
            if (agentLocation.y != 1) return new Point(agentLocation.x - 1, agentLocation.y);
            break;
         case EAST:
            if (agentLocation.x != turnGrid[0].length) return new Point(agentLocation.x, agentLocation.y + 1);
            break;
         case SOUTH:
            if (agentLocation.y != turnGrid.length) return new Point(agentLocation.x + 1, agentLocation.y);
            break;
         case WEST:
            if (agentLocation.x != 1) return new Point(agentLocation.x, agentLocation.y - 1);
            break;
      }
      return agentLocation;
   }


   private int getNumNeighborsBlocked(ChemicalCell[][] grid, Point agentLocation) {
      int numNeighborsBlocked = 0;
      if (agentLocation.x == 1 || (agentLocation.x > 1 && grid[agentLocation.x - 2][agentLocation.y - 1].isBlocked())) numNeighborsBlocked++;
      if (agentLocation.y == 1 || (agentLocation.y > 1 && grid[agentLocation.x - 1][agentLocation.y - 2].isBlocked())) numNeighborsBlocked++;
      if (agentLocation.x == grid[0].length || (grid[0].length > agentLocation.x && grid[agentLocation.x][agentLocation.y - 1].isBlocked())) numNeighborsBlocked++;
      if (agentLocation.y == grid.length || grid.length > agentLocation.y && grid[agentLocation.x - 1][agentLocation.y].isBlocked()) numNeighborsBlocked++;
      return numNeighborsBlocked;
   }


   private Map<DirectionType, ChemicalCell> getAgentNeighborMap(ChemicalCell[][] grid, Point agentLocation) {
      Map<DirectionType, ChemicalCell> neighborMap = new HashMap<>();

      if(agentLocation.y == 1)
         neighborMap.put(DirectionType.WEST, new ChemicalCell(false));
      else
         neighborMap.put(DirectionType.WEST, grid[agentLocation.x - 1][agentLocation.y - 2]);

      if(agentLocation.y == grid.length)
         neighborMap.put(DirectionType.EAST, new ChemicalCell(false));
      else
         neighborMap.put(DirectionType.EAST, grid[agentLocation.x - 1][agentLocation.y]);

      if(agentLocation.x == grid[0].length)
         neighborMap.put(DirectionType.SOUTH, new ChemicalCell(false));
      else
         neighborMap.put(DirectionType.SOUTH, grid[agentLocation.x][agentLocation.y - 1]);

      if(agentLocation.x == 1)
         neighborMap.put(DirectionType.NORTH, new ChemicalCell(false));
      else
         neighborMap.put(DirectionType.NORTH, grid[agentLocation.x - 2][agentLocation.y - 1]);

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
