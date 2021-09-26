package chemotaxis.g5;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.SimPrinter;
import chemotaxis.sim.ChemicalCell.ChemicalType;

public class Controller extends chemotaxis.sim.Controller {
    LinkedList<Point> path1 = new LinkedList<Point>();
    LinkedList<Point> path2 = new LinkedList<Point>();
    private int curPlacement = 1;
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

    public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter, Integer agentGoal, Integer spawnFreq) {
        super(start, target, size, grid, simTime, budget, seed, simPrinter, agentGoal, spawnFreq);
        //initializations

        int[][] visited = new int[grid.length][grid[0].length];
        initializeVisited(visited,grid);
        calculatePath(start, target, visited, grid, path1);

        initializeVisited(visited,grid);

        //blocking path1 and skipping target
        for (int i = 0; i < path1.size() - 1; i++)
            visited[path1.get(i).x - 1][path1.get(i).y - 1] = -1;

        calculatePath(start, target, visited, grid, path2);
    }

    private void initializeVisited(int[][] visited, ChemicalCell[][] grid){
        //0->available but not yet visited, 1->visited, -1->blocked cell
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j].isBlocked())
                    visited[i][j] = -1;
                else
                    visited[i][j] = 0;
            }
        }
    }

    private void calculatePath(Point start, Point target, int[][] visited, ChemicalCell[][] grid, LinkedList<Point> path){
        int[][] parent = new int[grid.length][grid[0].length];
        // 0->start, -1->no parent, 1->top, 2-> right, 3->down, 4->left

        for (int i = 0; i < grid.length; i++)
            for (int j = 0; j < grid[0].length; j++)
                parent[i][j] = -1;

        parent[start.x-1][start.y-1] = 0;
        visited[start.x-1][start.y-1] = 1;
        LinkedList<Point> que = new LinkedList<Point>();
        que.add(new Point(start.x-1,start.y-1));
        int i;
        int j;
        boolean found = false;
        Point temp;
        Point target_zero = new Point(target.x-1,target.y-1);


        while(!que.isEmpty() && !found)
        {
            temp = que.pollFirst();
            i=temp.x;
            j=temp.y;
            if(temp == target_zero)
                found = true;
            else{
                if(i-1>=0 && visited[i-1][j]==0)
                {
                    parent[i-1][j]=4;
                    visited[i-1][j] = 1;
                    que.add(new Point(i-1,j));
                }
                if(i+1<grid.length && visited[i+1][j]==0)
                {
                    parent[i+1][j] = 2;
                    visited[i+1][j] = 1;
                    que.add(new Point(i + 1, j));
                }
                if(j-1>=0 && visited[i][j-1]==0)
                {
                    parent[i][j-1] = 3;
                    visited[i][j-1] = 1;
                    que.add(new Point(i, j - 1));
                }
                if(j+1<grid[0].length && visited[i][j+1]==0)
                {
                    parent[i][j+1]=1;
                    visited[i][j+1] = 1;
                    que.add(new Point(i, j + 1));
                }
            }
        }

        //backtrack to find path
        i = target_zero.x;
        j = target_zero.y;

        if(parent[i][j]<1)
            System.out.println("No path found");
        else
        {
            int x = start.x - 1;
            int y = start.y - 1;

            while(!(i==x && j==y))
            {
                path.addFirst(new Point(i+1,j+1));
                if(parent[i][j]==1)
                    j=j-1;
                else if(parent[i][j]==2)
                    i=i-1;
                else if(parent[i][j]==3)
                    j=j+1;
                else if(parent[i][j]==4)
                    i=i+1;
            }
            path.addFirst(new Point(i+1,j+1));

            //printing path
            for (i = 0; i < path.size(); i++) {
                System.out.print(path.get(i).x);
                System.out.print(" ");
                System.out.print(path.get(i).y);
                System.out.println("");
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
        // get the closest point which is the one we will make go to exit:
        
        // Point closestAgent = getClosestAgent(locations);
        // if(closestAgent != null) {
        //     System.out.println("closest agent is at: " + closestAgent.toString());
        // }

        // Point locToPlace = getPosCloseToGoal(closestAgent, grid);
        Point locToPlace = this.path1.get(curPlacement++);

        // reset back to beginning
        if (curPlacement == this.path1.size()) {
            curPlacement = this.path1.size()-1;
        }

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
        return Math.abs(targetX - sourceX) + Math.abs(targetY - sourceY);
    }

    private Point getClosestAgent(ArrayList<Point> locations) {
        // return getClosestPointToTarget(target, locations);
        Point closest = null;
        int furthestDistance = -1;
        for (Point loc : locations) {
            for (int i = 0; i < this.path1.size(); i++) {
                Point trial = this.path1.get(i);
                if (loc == trial && i > furthestDistance) {
                    closest = new Point(trial);
                }
            }
        }
        return closest;
    }


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
            /* int manhattanDistance = getManhattanDistance(target.x, target.y, loc.x, loc.y);
            System.out.println(manhattanDistance);
            if(minDistance == null || manhattanDistance < minDistance.intValue()) {
                minDistance = manhattanDistance;
                retLoc = loc;
            }*/
            int nextIdx = -1;
            for (int i = 0; i < path1.size(); i++) {
                Point trial = path1.get(i);
                if (loc == trial && i > nextIdx) {
                    nextIdx = i;
                    retLoc = trial;
                } 
            }
        }
        return retLoc;
    }
}