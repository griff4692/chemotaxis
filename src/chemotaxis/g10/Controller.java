package chemotaxis.g10;

import java.awt.Point;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.List;

import chemotaxis.sim.*;

public class Controller extends chemotaxis.sim.Controller {

   private TurnGridNode [][] turnGrid;

   private final int PLACEMENT_DISTACE = 7;

   private ArrayList<Point> path;
   private int pathIndex;
   private int PATH_INDEX_START = 3; // this cna be a problem
   private Integer lastChem;

   private final ChemicalCell.ChemicalType [] CHEM_TYPES = {
           ChemicalCell.ChemicalType.RED,
           ChemicalCell.ChemicalType.GREEN,
           ChemicalCell.ChemicalType.BLUE
   };
   private int chemIndex;


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

      path = new ArrayList<Point>(); //1-indexed
      pathIndex = PATH_INDEX_START;
      chemIndex = 0;
      lastChem = null;

      path.add(start);


      Point curPoint, parentPoint1Ind;
      while(!(curPoint = path.get(path.size() - 1)).equals(target))
      {
         parentPoint1Ind = (Point) turnGrid[curPoint.x - 1][curPoint.y - 1].getParentPoint().clone();
         parentPoint1Ind.x++;
         parentPoint1Ind.y++;

         if(parentPoint1Ind.x < curPoint.x)
            path.add(new Point(curPoint.x - 1, curPoint.y));
         else if(parentPoint1Ind.x > curPoint.x)
            path.add(new Point(curPoint.x + 1, curPoint.y));
         else if(parentPoint1Ind.y < curPoint.y)
            path.add(new Point(curPoint.x, curPoint.y - 1));
         else if(parentPoint1Ind.y > curPoint.y)
            path.add(new Point(curPoint.x, curPoint.y + 1));
      }
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
      ChemicalPlacement cp = new ChemicalPlacement();

      if(chemicalsRemaining > 0) {

         if(lastChem == null) {
            // Make sure to place chemical on the target
            if (pathIndex >= path.size() && pathIndex - PLACEMENT_DISTACE != path.size() - 1)
               pathIndex = path.size() - 1;

            if (pathIndex < path.size()) {

               if (pathIndex == path.size() - 1)
                  lastChem = chemIndex;

               cp.location = path.get(pathIndex);

               cp.chemicals = new ArrayList<>();
               cp.chemicals.add(CHEM_TYPES[chemIndex]);

               chemIndex = (chemIndex + 1) % CHEM_TYPES.length;
               pathIndex += PLACEMENT_DISTACE;
               
            }
            if(lastChem != null)
            {
               pathIndex = PATH_INDEX_START;
               chemIndex = 0;
            }
         }
         else
         {
            if(currentTurn % 3 == 0)
            {
               cp.location = path.get(pathIndex);

               cp.chemicals = new ArrayList<>();
               cp.chemicals.add(CHEM_TYPES[chemIndex]);

               chemIndex = (chemIndex + 1) % CHEM_TYPES.length;
               pathIndex += PLACEMENT_DISTACE;

               if(pathIndex > path.size())
               {
                  pathIndex = PATH_INDEX_START;
                  chemIndex = 0;
               }
            }
            else if(currentTurn % 3 == 1)
            {
               cp.location = path.get(path.size() - 1);

               cp.chemicals = new ArrayList<>();
               cp.chemicals.add(CHEM_TYPES[lastChem]);
            }
         }
      }

      return cp;
   }
}

/*
Notes:
Agent's don't know where target is even if they're next to it. So placing lots of chem near target is usually good,
but due to the moving local max, placing lots of chem on the target doesn't always work.

With field-based approach, agents can often get stuck looking for max and colliding with other agents.

Problems when agent think it hits max and turns around to find

Turn based may use a lot of chem, but with smart agent/controller this can be reduced
 */
