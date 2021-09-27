package chemotaxis.g10;

import java.awt.Point;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.List;

import chemotaxis.sim.*;

public class Controller extends chemotaxis.sim.Controller {

   private TurnGridNode [][] turnGrid;
   private ArrayList<Integer> agentsLastNumTurns;
   private ArrayList<Point> agentsLastLocation;
   private ArrayList<DirectionType> agentsLastDir;

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

      computeTurnGrid(grid);
      agentsLastNumTurns = new ArrayList<>();
      agentsLastLocation = new ArrayList<>();
      agentsLastDir = new ArrayList<>();
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

            int numNeighborsBlocked = 0;
            if (grid[agentLocation.x - 1][agentLocation.y].isBlocked()) numNeighborsBlocked++;
            if (grid[agentLocation.x][agentLocation.y - 1].isBlocked()) numNeighborsBlocked++;
            if (grid.length > agentLocation.x + 1 && grid[agentLocation.x + 1][agentLocation.y].isBlocked()) numNeighborsBlocked++;
            if (grid[0].length > agentLocation.y + 1 && grid[agentLocation.x][agentLocation.y + 1].isBlocked()) numNeighborsBlocked++;

            // agentsLastDir.get(i) != getAgentDirection(agentsLastLocation.get(i), agentLocation)
            if ((agentLocation.x != target.x || agentLocation.y != target.y) && (numNeighborsBlocked < 2 || (numNeighborsBlocked == 2 && getAgentDirection(agentsLastLocation.get(i), agentLocation) == getOppositeDirection(agentsLastDir.get(i)))) && ((agentLocation.x == start.x && agentLocation.y == start.y) || agentTurnGridNode.getTurns() != agentsLastNumTurns.get(i))) {
               chemPlacement = new ChemicalPlacement();
               Point parentPoint = agentTurnGridNode.getParentPoint();
               if (parentPoint.x + 1 == agentLocation.x) {
                  chemPlacement.location = new Point(agentLocation.x, (parentPoint.y > agentLocation.y - 1) ? (agentLocation.y + 1) : (agentLocation.y - 1));
               } else if (parentPoint.y + 1 == agentLocation.y) {
                  chemPlacement.location = new Point((parentPoint.x > agentLocation.x - 1) ? (agentLocation.x + 1) : (agentLocation.x - 1), agentLocation.y);
               }

               chemPlacement.chemicals.add(ChemicalCell.ChemicalType.RED);
               agentsLastNumTurns.set(i, agentTurnGridNode.getTurns());
               return chemPlacement;
            }
            agentsLastDir.set(i, getAgentDirection(agentsLastLocation.get(i), agentLocation));
            agentsLastLocation.set(i, agentLocation);
         }
      }

      return new ChemicalPlacement();
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
