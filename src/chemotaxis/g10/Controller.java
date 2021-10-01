package chemotaxis.g10;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.lang.Math;
import java.util.Collections;

import chemotaxis.sim.*;

public class Controller extends chemotaxis.sim.Controller {

   private TurnGridNode [][] turnGrid;
   private ArrayList<Point[]> turnsOnPath; //Point[] = {agentLocationBeforeTurn, agentLocationAfterTurn}
   private ArrayList<Point> agentsLastLocation;
   private ArrayList<DirectionType> agentsLastDir;
   private ChemicalCell.ChemicalType[] chemicalList = new ChemicalCell.ChemicalType[]{ChemicalCell.ChemicalType.RED, ChemicalCell.ChemicalType.GREEN, ChemicalCell.ChemicalType.BLUE};

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
   public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter, Integer agentGoal, Integer spawnFreq) {
      super(start, target, size, grid, simTime, budget, seed, simPrinter, agentGoal, spawnFreq);

      this.turnGrid = computeTurnGrid(grid, start);
      printTurnGrid();
      findDesiredPath(grid, start, target);
      agentsLastLocation = new ArrayList<>();
      agentsLastDir = new ArrayList<>();
//      lastChemPlaced = ChemicalCell.ChemicalType.GREEN;
   }

   /**
    * Computes the shortest turn paths from all unblocked cells in the grid
    * and required number of explicit turns from each cell to the target.
    *
    * @param grid        game grid/map
    * @param startCell   Cell to start computation
    *
    */
   private TurnGridNode[][] computeTurnGrid(ChemicalCell [][] grid, Point startCell) {
      turnGrid = new TurnGridNode[this.size][this.size];

      // Start calculating from start node
      Point start0Ind = new Point(startCell.x - 1, startCell.y - 1);
      turnGrid[start0Ind.x][start0Ind.y] = new TurnGridNode(0, start0Ind, start0Ind);

      // Low turn cells have priority
      PriorityQueue<TurnGridNode> frontier = new PriorityQueue<TurnGridNode>(this.size * this.size);
      frontier.add(turnGrid[start0Ind.x][start0Ind.y]);

      TurnGridNode cur;
      int maxTurns = 0;
      int deltaXY[][] = {{1, 0}, {0, -1}, {-1, 0}, {0, 1}}; // right, up, left, down

      while (!frontier.isEmpty()) {
         cur = frontier.poll();

         for (int dir = 0; dir < 4; dir++) {
            int x = cur.getGridPoint().x;
            int y = cur.getGridPoint().y;

            while (x >= 0 && x < this.size && y >= 0 && y < this.size && grid[x][y].isOpen()) {

               int turns = cur.getTurns() + 1;

               // If there's 2 surrounding walls, this is a forced movement (no chem req)
               if (numSurroundingWalls(cur.getGridPoint().x, cur.getGridPoint().y, grid) == 2)
                  turns -= 1;

               maxTurns = Math.max(maxTurns, turns);

               if (turnGrid[x][y] == null) {
                  TurnGridNode n = new TurnGridNode(turns, x, y, cur.getGridPoint());
                  turnGrid[x][y] = n;
                  frontier.add(n);
               }
               else
               {
                  if (turns < turnGrid[x][y].getTurns())
                  {
                     turnGrid[x][y].setTurns(turns);
                     turnGrid[x][y].setParentPoint(cur.getGridPoint());
                     frontier.add(turnGrid[x][y]);
                  }

                  // Update parent point if there's a closer cell with same number of turns.
                  // Number of turns doesn't change, but manhattan distance is minimized
                  if (!turnGrid[x][y].getGridPoint().equals(cur.getGridPoint()) && cur.getTurns() < turnGrid[x][y].getTurns() &&
                          turnGrid[x][y].getGridPoint().distanceSq(cur.getGridPoint()) < turnGrid[x][y].getGridPoint().distanceSq(turnGrid[x][y].getParentPoint()))
                  {
                     turnGrid[x][y].setParentPoint(cur.getGridPoint());
                  }
               }
               x += deltaXY[dir][0];
               y += deltaXY[dir][1];
            }
         }
      }

      // start cell is 0, and decreases to target cell
      for(int i = 0;i < this.size;i++) {
         for (int j = 0; j < this.size; j++) {
            if (turnGrid[i][j] != null) {
               turnGrid[i][j].setTurns(-turnGrid[i][j].getTurns());
            }
         }
      }

      // Alg computes path from target to start, we need start to target, so reverse this.
      cur = turnGrid[target.x - 1][target.y-1];
      ArrayList<Point> path = new ArrayList<>();
      Point parent;
      while(!cur.getGridPoint().equals(turnGrid[start0Ind.x][start0Ind.y].getGridPoint()))
      {
         path.add(cur.getGridPoint());
         parent = cur.getParentPoint();
         cur = turnGrid[parent.x][parent.y];
      }
      path.add(start0Ind);
      Collections.reverse(path);

      Point p;
      for(int i = 0;i<path.size()-1;i++)
      {
         p = path.get(i);
         turnGrid[p.x][p.y].setParentPoint(path.get(i+1));
      }
      p = path.get(path.size()-1);
      turnGrid[p.x][p.y].setParentPoint(p);

      return turnGrid;
   }

   private int numSurroundingWalls(int i, int j, ChemicalCell [][] grid)
   {
      int deltaXY[][] = {{1, 0}, {0, -1}, {-1, 0}, {0, 1}}; // right, up, left, down
      int x, y, nWalls = 0;
      for(int [] dir : deltaXY)
      {
         x = i + dir[0];
         y = j + dir[1];
         if(x < 0 || x >= this.size || y < 0 || y >= this.size || grid[x][y].isBlocked())
            nWalls++;
      }
      return nWalls;
   }

   private void printTurnGrid()
   {
      for(int i = 0;i<this.size;i++)
      {
         for(int j = 0;j<this.size;j++)
         {
            if(turnGrid[i][j] == null)
               System.out.format(" x ");
            else
               System.out.format("%2d ", turnGrid[i][j].getTurns());
         }
         System.out.println();
      }
   }

   private Object getCellInDirection(Object[][] grid, Point agentLocation, DirectionType directionWanted) {
      if (directionWanted == DirectionType.NORTH) {
         if (agentLocation.x != 1) {
            return grid[agentLocation.x - 2][agentLocation.y - 1];
         } else {
            return null;
         }
      } else if (directionWanted == DirectionType.EAST) {
         if (agentLocation.y != grid.length) {
            return grid[agentLocation.x - 1][agentLocation.y];
         } else {
            return null;
         }
      } else if (directionWanted == DirectionType.SOUTH) {
         if (agentLocation.x != grid[0].length) {
            return grid[agentLocation.x][agentLocation.y - 1];
         } else {
            return null;
         }
      } else if (directionWanted == DirectionType.WEST) {
         if (agentLocation.y != 1) {
            return grid[agentLocation.x - 1][agentLocation.y - 2];
         } else {
            return null;
         }
      } else return grid[agentLocation.x - 1][agentLocation.y - 1];
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
      if (lastPoint.x == currentPoint.x && lastPoint.y == currentPoint.y) return DirectionType.CURRENT;
      if (lastPoint.x == currentPoint.x) {
         return (lastPoint.y < currentPoint.y) ? DirectionType.EAST : DirectionType.WEST;
      } else if (lastPoint.y == currentPoint.y) {
         return (lastPoint.x < currentPoint.x) ? DirectionType.SOUTH : DirectionType.NORTH;
      }
      return null;
   }

   private Point getNextBestLocation(Point agentLocation, int currNumTurns) {
      int minNumTurns = currNumTurns;
      Point bestLocation = agentLocation;
      TurnGridNode agentCell = (TurnGridNode) getCellInDirection(turnGrid, agentLocation, DirectionType.CURRENT);
      ArrayList<TurnGridNode> cells = new ArrayList<>();
      cells.add((TurnGridNode) getCellInDirection(turnGrid, agentLocation, DirectionType.NORTH));
      cells.add((TurnGridNode) getCellInDirection(turnGrid, agentLocation, DirectionType.EAST));
      cells.add((TurnGridNode) getCellInDirection(turnGrid, agentLocation, DirectionType.SOUTH));
      cells.add((TurnGridNode) getCellInDirection(turnGrid, agentLocation, DirectionType.WEST));

      for (TurnGridNode cell : cells) {
         if (cell != null && cell.getTurns() < minNumTurns) {
            minNumTurns = cell.getTurns();
            bestLocation = agentCell.getParentPoint();
            bestLocation = new Point(bestLocation.x + 1, bestLocation.y + 1);
         }
      }

      return bestLocation;
   }

   private boolean isSameLocation(Point location1, Point location2) {
      return location1.x == location2.x && location1.y == location2.y;
   }

   private boolean isBlockedOnDirection(Point agentLocation, DirectionType dir) {
      return getCellInDirection(turnGrid, agentLocation, dir) == null;
   }


   private boolean doesMoveRequireChemical(ChemicalCell[][] grid, Point agentLastLocation, Point agentLocation, Point agentNextLocation) {
      int numNeighborsBlocked = getNumNeighborsBlocked(grid, agentLocation);
      DirectionType agentLastDirection = getAgentDirection(agentLastLocation, agentLocation);
      DirectionType agentCurrDirection = getAgentDirection(agentLocation, agentNextLocation);
      if (numNeighborsBlocked == 3) return false;
      if (numNeighborsBlocked == 2 && agentCurrDirection != getOppositeDirection(agentLastDirection)) return false;

      if (agentLastDirection == DirectionType.NORTH && isBlockedOnDirection(agentLocation, DirectionType.NORTH) && agentCurrDirection == DirectionType.EAST) {
         return false;
      } else if (agentLastDirection == DirectionType.EAST && isBlockedOnDirection(agentLocation, DirectionType.EAST) && agentCurrDirection == DirectionType.SOUTH) {
         return false;
      } else if (agentLastDirection == DirectionType.SOUTH && isBlockedOnDirection(agentLocation, DirectionType.SOUTH) && agentCurrDirection == DirectionType.WEST) {
         return false;
      } else if (agentLastDirection == DirectionType.WEST && isBlockedOnDirection(agentLocation, DirectionType.WEST) && agentCurrDirection == DirectionType.NORTH) {
         return false;
      }

      return true;
   }


//   private Point getNextLocationOnPath(Point agentLastLocation, Point agentLocation) {
//      Point bestLocation = agentLocation;
//      TurnGridNode nextCell = (TurnGridNode) getCellInDirection(turnGrid, agentLocation, getAgentDirection(agentLastLocation, agentLocation));
//      if (nextCell == null) {
//         DirectionType lastDirection = getAgentDirection(agentLastLocation, agentLocation);
//         if (lastDirection == DirectionType.NORTH) {
//            Point nextCellPoint = ((TurnGridNode) getCellInDirection(turnGrid, agentLocation, DirectionType.EAST)).getGridPoint();
//            bestLocation = new Point(nextCellPoint.x + 1, nextCellPoint.y + 1);
//         } else if (lastDirection == DirectionType.EAST) {
//            Point nextCellPoint = ((TurnGridNode) getCellInDirection(turnGrid, agentLocation, DirectionType.SOUTH)).getGridPoint();
//            bestLocation = new Point(nextCellPoint.x + 1, nextCellPoint.y + 1);
//         } else if (lastDirection == DirectionType.SOUTH) {
//            Point nextCellPoint = ((TurnGridNode) getCellInDirection(turnGrid, agentLocation, DirectionType.WEST)).getGridPoint();
//            bestLocation = new Point(nextCellPoint.x + 1, nextCellPoint.y + 1);
//         } else if (lastDirection == DirectionType.WEST) {
//            Point nextCellPoint = ((TurnGridNode) getCellInDirection(turnGrid, agentLocation, DirectionType.NORTH)).getGridPoint();
//            bestLocation = new Point(nextCellPoint.x + 1, nextCellPoint.y + 1);
//         }
//      } else {
//         bestLocation = nextCell.getGridPoint();
//         bestLocation = new Point(bestLocation.x + 1, bestLocation.y + 1);
//      }
//      return bestLocation;
//   }


   private void findDesiredPath(ChemicalCell[][] grid, Point start, Point target) {
      turnsOnPath = new ArrayList<>();
      Point agentLastLocation = start;
      Point agentLocation = start;
      int numTurnsLeft = turnGrid[start.x - 1][start.y - 1].getTurns();

      while (!isSameLocation(agentLocation, target)) {
         Point bestLocation = getNextBestLocation(agentLocation, numTurnsLeft);
         numTurnsLeft = ((TurnGridNode) getCellInDirection(turnGrid, agentLocation, DirectionType.CURRENT)).getTurns();

         if (!isSameLocation(agentLocation, bestLocation)) { // some orthogonal cell was a better move
            if (doesMoveRequireChemical(grid, agentLastLocation, agentLocation, bestLocation)) {
               DirectionType currDirection = getAgentDirection(agentLocation, bestLocation);
               Point nextCellPoint = ((TurnGridNode) getCellInDirection(turnGrid, agentLocation, currDirection)).getGridPoint();
               turnsOnPath.add(new Point[] {agentLocation, new Point(nextCellPoint.x + 1, nextCellPoint.y + 1)});
            }
         } else {
            bestLocation = turnGrid[bestLocation.x - 1][bestLocation.y - 1].getParentPoint();
            bestLocation = new Point(bestLocation.x + 1, bestLocation.y + 1);
         }

         agentLastLocation = agentLocation;
         agentLocation = bestLocation;
      }
   }


   private Point getBestOrthogonalLocation(TurnGridNode leftTurnGridNode, TurnGridNode rightTurnGridNode, Point agentLocation) {
      if (leftTurnGridNode == null) {
         Point rightPoint = rightTurnGridNode.getGridPoint();
         Point orthogonalLocation = turnGrid[rightPoint.x][rightPoint.y].getParentPoint();
         return new Point(orthogonalLocation.x + 1, orthogonalLocation.y + 1);
      } else if (rightTurnGridNode == null) {
         Point leftPoint = leftTurnGridNode.getGridPoint();
         Point orthogonalLocation = turnGrid[leftPoint.x][leftPoint.y].getParentPoint();
         return new Point(orthogonalLocation.x + 1, orthogonalLocation.y + 1);
      }

      Point orthogonalLocation = null;
      Point leftPoint = leftTurnGridNode.getGridPoint();
      Point rightPoint = rightTurnGridNode.getGridPoint();
      Point leftParentPoint = leftTurnGridNode.getParentPoint();
      Point rightParentPoint = rightTurnGridNode.getParentPoint();
      if ((Math.abs(leftParentPoint.x - leftPoint.x) + Math.abs(leftParentPoint.y - leftPoint.y)) < (Math.abs(rightParentPoint.x - rightPoint.x) + Math.abs(rightParentPoint.y - rightPoint.y))) {
         turnsOnPath.add(new Point[] {agentLocation, new Point(leftPoint.x + 1, leftPoint.y + 1)}); // don't do for right turn since right turn is default
         orthogonalLocation = turnGrid[leftPoint.x][leftPoint.y].getParentPoint();
      } else {
         orthogonalLocation = turnGrid[rightPoint.x][rightPoint.y].getParentPoint();
      }
      return new Point(orthogonalLocation.x + 1, orthogonalLocation.y + 1);
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

            if (i == agentsLastLocation.size()) {
               agentsLastLocation.add(agentLocation);
               agentsLastDir.add(DirectionType.CURRENT);
            }
            agentsLastDir.set(i, getAgentDirection(agentsLastLocation.get(i), agentLocation));

            int turnIndex = getTurnIndex(agentLocation);
            if (turnIndex != -1) {
               ChemicalCell.ChemicalType currentColor = chemicalList[turnIndex % chemicalList.length];
               DirectionType agentExpectedDir = (DirectionType) Agent.findOptimalMove(agentsLastDir.get(i), currentColor, getAgentNeighborMap(grid, agentLocation), grid[agentLocation.x - 1][agentLocation.y - 1])[0];
               DirectionType agentOptimalDir = getAgentDirection(agentLocation, turnsOnPath.get(turnIndex)[1]);
//               System.out.println("CONTROLLER: Agent " + String.valueOf(i) + "'s calculated optimal move: " + agentExpectedDir + " " + currentColor.name());
//               System.out.println("CONTROLLER: Agent " + String.valueOf(i) + "'s wanted move: " + agentOptimalDir);

               if (agentExpectedDir != agentOptimalDir) {
                  chemPlacement = new ChemicalPlacement();
                  chemPlacement.location = turnsOnPath.get(turnIndex)[1];

                  chemPlacement.chemicals.add(currentColor);
                  agentsLastLocation.set(i, agentLocation);
                  return chemPlacement;
               }
            }
            agentsLastLocation.set(i, agentLocation);
         }
      }

      return new ChemicalPlacement();
   }


   private int getTurnIndex(Point agentLocation) {
      for (int i = 0; i < turnsOnPath.size(); i++) {
         if (turnsOnPath.get(i)[0].x == agentLocation.x && turnsOnPath.get(i)[0].y == agentLocation.y) {
            return i;
         }
      }
      return -1;
   }


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
