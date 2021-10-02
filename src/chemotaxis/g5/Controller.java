package chemotaxis.g5;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.SimPrinter;
import chemotaxis.sim.ChemicalCell.ChemicalType;

import static java.lang.Math.min;

public class Controller extends chemotaxis.sim.Controller {
    LinkedList<Point> path1 = new LinkedList<Point>();
    LinkedList<Point> path2 = new LinkedList<Point>();
    LinkedList<Point> chemAtFork = new LinkedList<Point>();
    LinkedList<Point> finalPath = new LinkedList<Point>();
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
        LinkedList<Point> turns1 = new LinkedList<Point> ();
        if(path1.size()>0)
            calculateTurn(path1,turns1,grid);
        initializeVisited(visited,grid);

        //blocking path1 and skipping target
        for (int i = 0; i < path1.size() - 1; i++)
            visited[path1.get(i).x - 1][path1.get(i).y - 1] = -1;

        calculatePath(start, target, visited, grid, path2);
        LinkedList<Point> turns2 = new LinkedList<Point> ();
        if(path2.size()>0)
            calculateTurn(path2,turns2,grid);

        if(path2.size()==0 || turns1.size()<=turns2.size()) {
            chemAtFork = turns1;
            finalPath = path1;
        }

        else if(turns1.size()>turns2.size()) {
            chemAtFork = turns2;
            finalPath = path2;

        }

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

        if(parent[i][j]>=1)
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
        }
    }

    private void calculateTurn(LinkedList<Point> path, LinkedList<Point> turn, ChemicalCell[][] grid)
    {
        int x = path.get(0).x;
        int y = path.get(0).y;
        int forkx = 0;
        int forky = 0;
        int vertical = 0;
        if(path.size()>4)
            turn.add(path.get(3));
        if(x==path.get(1).x)
            vertical = 1;
        for (int i = 1; i < path.size()-1;i++) {
            if(x!=path.get(i).x && vertical==1)
            {
                forkx = 0;
                vertical = 0;
                if(x-2>=0 && grid[x-2][y-1].isOpen())
                    forkx+=1;
                if(x<grid.length && grid[x][y-1].isOpen())
                    forkx+=1;
                if(y-2>=0 && grid[x-1][y-2].isOpen())
                    forkx+=1;
                if(y<grid[0].length && grid[x-1][y].isOpen())
                    forkx+=1;

                if(forkx>2)
                    turn.add(path.get(i));
            }
            else if(y!=path.get(i).y && vertical==0 )
            {
                forkx = 0;
                vertical = 1;
                if(x-2>=0 && grid[x-2][y-1].isOpen())
                    forkx+=1;
                if(x<grid.length && grid[x][y-1].isOpen())
                    forkx+=1;
                if(y-2>=0 && grid[x-1][y-2].isOpen())
                    forkx+=1;
                if(y<grid[0].length && grid[x-1][y].isOpen())
                    forkx+=1;

                if(forkx>2)
                    turn.add(path.get(i));
            }
            x = path.get(i).x;
            y = path.get(i).y;
        }
        Point last = turn.get(turn.size()-1);
        Point goal = path.get(path.size()-1);
        if(Math.abs(last.x-goal.x+last.y-goal.x)<=2)
            turn.removeLast();
        turn.add(goal);
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
        ChemicalType chosenChemicalType = ChemicalType.GREEN;
        ChemicalPlacement ret = new ChemicalPlacement();
        if(currentTurn==1)
        {
            ret.location = finalPath.get(1);
            ret.chemicals.add(chosenChemicalType);
        }
        else{
            int currPosition = agentOnTurn(locations,chemAtFork, chosenChemicalType, grid);
            if(currPosition!=-1)
            {
                ret.location = chemAtFork.get(currPosition);
                ret.chemicals.add(chosenChemicalType);
            }
        }
        return ret;
    }
    private int agentOnTurn(ArrayList<Point> locations,LinkedList<Point> turn, ChemicalType chosenChemicalType, ChemicalCell[][] grid){
        int found = -1;
        int x,y, nextX = turn.get(turn.size()-1).x, nextY = turn.get(turn.size()-1).y;
        for(int i=turn.size()-2;i>=0;i--)
        {
            x = turn.get(i).x;
            y = turn.get(i).y;
            for(Point loc : locations)
                if(loc.x==x && loc.y==y && grid[nextX - 1][nextY - 1].getConcentration(chosenChemicalType) < 0.03)
                    return i+1;
            nextX = x;
            nextY = y;
        }

        return found;
    }

}