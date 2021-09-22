package chemotaxis.g1;

import java.awt.Point;
import java.util.*;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.SimPrinter;
public class Controller extends chemotaxis.sim.Controller {
    /**
     *dist[i][j][d] = x (x>0) means it takes agents x steps to go from the starting point to the cell at line x and column y facing direction d
     *dist[i][j][d] = 0 means agent can't go to cell x,y from direction d yet
     *dist[i][j][d] = -1 means there is a block at x,y
     **/
    private static int[][][] dist;
    Point modifiedStart = new Point();
    Point modifiedTarget = new Point();

    // Key is number of chemical (paid) turns
    // The largest key that exists in the map will be the shortest route
    // If there's a key n+1 in the Map, it's distance is less than the distance of key n
    private static Map<Integer, ArrayList<Point>> routes=new HashMap<>();
    private static Map<Integer, ArrayList<Integer>> turnAt=new HashMap<>();

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
    // This is the constructor from vaibhav's branch but it reports error
    /*
    public Controller(Point start, Point target, Integer size, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
        super(start, target, size, simTime, budget, seed, simPrinter);
    }
    */

    /*
    The construction from dummy
     */
    public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
        super(start, target, size, grid, simTime, budget, seed, simPrinter);
        dist = new int[size][size][4];
        modifiedStart.x=start.x-1;
        modifiedStart.y=start.y-1;
        modifiedTarget.x=target.x-1;
        modifiedTarget.y=target.y-1;
        for (int i=0;i<size;i++) {
            for (int j = 0; j < size; j++) {
                for (int d = 0; d < 4; d++) {
                    dist[i][j][d] = grid[i][j].isBlocked() ? -1:0;
                }
            }
        }

        // TODO: divide budget by 3 since we need to move 3 agents by default
        findshortestpath(grid,budget);
        for (int i=0;i<budget;i++) {
            if (!routes.containsKey(i)){
                break;
            }
            ArrayList<Point> temp = new ArrayList<>(routes.get(i));
            Collections.reverse(temp);
            routes.put(i, new ArrayList<>(temp));
            setTurnAt(i,grid);
            schedule(1000);
            //System.out.println(routes.get(i));
            //System.out.println(turnAt.get(i));
        }
    }

    public static void schedule(int maxTime) {
        for (int i=1;i<=10001;i++) {
            ArrayList<ArrayList<Integer>> schedule = new ArrayList<>();
            if (!turnAt.containsKey(i)){
                break;
            }
            if (turnAt.get(i).size()==0){
                continue;
            }
            for (int j=0;j<maxTime;j++) {
                schedule.add(new ArrayList<>());
            }
            for (int j=0;j<turnAt.get(i).size();j++) {
                if (turnAt.get(i).get(j)!=0) {
                    ArrayList<Integer> temp = new ArrayList<>(schedule.get(j));
                    temp.add(turnAt.get(i).get(j));
                    schedule.set(j,temp);
                }
            }
            System.out.println(schedule);

        }
    }
    /*
     *

     * Apply chemicals to the map
     *
     * @param currentTurn         current turn in the simulation
     * @param chemicalsRemaining  number of chemicals remaining
     * @param locations           current locations of the agents

     * @param grid                game grid/map
     * @return                    a cell location and list of chemicals to apply
     *
     */

    /****************************************/
    /*****Call This Function To Get Path*****/
    /****************************************/
    public ArrayList<Point> getRoute(int turn) {
        return routes.get(turn);
    }
    public void setTurnAt(int turn,ChemicalCell[][] grid) {
        ArrayList<Integer> turns = new ArrayList<>();
        int i = routes.get(turn).size()-1;
        int d = 0;
        ArrayList<Integer> allDirections = new ArrayList<>(Arrays.asList(0,1,-1,2));

        while (i>0) {
            Point current = new Point(routes.get(turn).get(i).x,routes.get(turn).get(i).y);
            int defalutDir=d;
            for (int j=0;j<4;j++) {
                int newDir = d+j;
                if (!mapHasBlockAt(grid, current.x + movement(newDir).x, current.y + movement(newDir).y)) {
                    defalutDir = d+j;
                    break;
                }
            }
            int actualDir = 0;
            for (int j=0;j<4;j++) {
                if ((routes.get(turn).get(i-1).x - routes.get(turn).get(i).x == movement(j).x) &&
                    (routes.get(turn).get(i-1).y - routes.get(turn).get(i).y == movement(j).y)) {
                    actualDir = j;
                    break;
                }
            }
            if (actualDir==defalutDir) {
                turns.add(0);
            }
            else {
                turns.add(actualDir - d);
            }
            i-=1;
            d = actualDir;
        }
        turnAt.put(turn,new ArrayList<>(turns));
    }

    /**
     * Returns a point to be added to the current location, effectively moving in a direction.
     * @param direction
     * @return
     */
    private Point movement(int direction){
        switch (direction){
            // North
            case 0: return new Point(1,0);
            // East
            case 1:return new Point(0,1);
            // South
            case 2:return new Point(-1,0);
            // West
            default:return new Point(0,-1);
        }
    }

    @Override
    public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid) {
        // TODO add your code here to apply chemicals
        ChemicalPlacement chemicalPlacement = new ChemicalPlacement();

        return chemicalPlacement; // TODO modify the return statement to return your chemical placement
    }

    // whether the agent can reach cell x,y facing direction d within s steps
    private Boolean betterPath(int x, int y, int d, int s){
        if (!(modifiedTarget.x==x && modifiedTarget.y==y)) {
            return dist[x][y][d] == 0 || dist[x][y][d]>s;
        }
        else {
            for (int i=0;i<4;i++) {
                if (dist[x][y][i] != 0 && dist[x][y][i]<=s){
                    return false;
                }
            }
            return true;
        }
    }

    // Whether the map has a open cell at line:x, column:y
    private Boolean mapHasBlockAt(ChemicalCell[][] grid, int x, int y) {
        return !(x >= 0 && x < size && y >= 0 && y < size && !grid[x][y].isBlocked());
    }

    /*
    a data structure saving 3 integers
     */
    public static class TriInteger{
        int x;
        int y;
        int d;
        TriInteger(int i,int j,int k) {
            x=i;
            y=j;
            d=k;
        }
        TriInteger(TriInteger tri) {
            x=tri.x;
            y=tri.y;
            d=tri.d;
        }

    }

    /*
        read data from dist save results from findshortestpath to the maproutes
     */
    private void saveRoute(int turn){
        ArrayList<Point> route = new ArrayList<>();
        Point current = new Point(modifiedTarget.x,modifiedTarget.y);
        int step = 10001;
        int direction = 0;
        for (int i=0;i<4;i++) {
            if (dist[modifiedTarget.x][modifiedTarget.y][i]>0 && dist[modifiedTarget.x][modifiedTarget.y][i]<step){
                step = dist[modifiedTarget.x][modifiedTarget.y][i];
            }
        }

        if (step<10001) {
            System.out.println(step);
            while (!((modifiedStart.x==current.x)&&(modifiedStart.y==current.y))) {
                /*System.out.print("(");
                System.out.print(current.x+1);
                System.out.print(",");
                System.out.print(current.y+1);
                System.out.print(")<-");
                */
                route.add(new Point(current));
                for (int i=0;i<4;i++) {
                    int j = (i + direction +4) % 4;
                    if (dist[current.x][current.y][j] == step) {
                        direction = j;
                        step-=1;
                        break;
                    }
                }
                current.x -= movement(direction).x;
                current.y -= movement(direction).y;
            }
            /*System.out.print("(");
            System.out.print(current.x+1);
            System.out.print(",");
            System.out.print(current.y+1);
            System.out.print(")<-");
            System.out.println("");
            */
            route.add(new Point(current));
        }
        routes.put(turn, new ArrayList<>(route));
    }

    /*
    find the shortest path with 0 to maxturn turns, store data in dist
     */
    private void findshortestpath(ChemicalCell[][] grid, Integer maxturn) {
        Queue<TriInteger> currentpoints = new LinkedList<>();
        currentpoints.add(new TriInteger(modifiedStart.x,modifiedStart.y,0));
        // pointsSavedForNextTurn contains the points you can reach with `turn` chemicals
        // Usually this is just a list of points in a straight line, but could include
        // other points if there is a wall that gives you a "free" turn in some direction.
        Queue<TriInteger> pointsSavedForNextTurn = new LinkedList<>();
        for (int turn=0;turn<maxturn;turn++) {
            if (turn>0) {
                while (!pointsSavedForNextTurn.isEmpty()) {
                    TriInteger basePoint = pointsSavedForNextTurn.poll();
                    // for n in [-1, 1]
                    // for turn in [left, right]
                    for (int i=-1;i<=1;i+=2) {
                        // Basically turns left or right
                        int newDirection = (basePoint.d + i +4) % 4;
                        // New location after turn and move 1
                        TriInteger targetPoint = new TriInteger(basePoint.x+movement(newDirection).x,basePoint.y+movement(newDirection).y,newDirection);
                        if (!mapHasBlockAt(grid,targetPoint.x,targetPoint.y)) {
                            // If the current path is the best path
                            if (betterPath(targetPoint.x,targetPoint.y,targetPoint.d,dist[basePoint.x][basePoint.y][basePoint.d]+1)) {
                                currentpoints.add(new TriInteger(targetPoint));
                                dist[targetPoint.x][targetPoint.y][targetPoint.d] = dist[basePoint.x][basePoint.y][basePoint.d]+1;
                            }
                        }
                    }
                }
                // Special case for turning around at the beginning since the default direction is north
                if (turn == 1) {
                    // Not blocked at south
                    if (!mapHasBlockAt(grid,modifiedStart.x-1,modifiedStart.y)){
                        if (betterPath(modifiedStart.x-1,modifiedStart.y,2,1)){
                            currentpoints.add(new TriInteger(modifiedStart.x-1,modifiedStart.y,2));
                            dist[modifiedStart.x-1][modifiedStart.y][2] = 1;
                        }
                    }
                }
                pointsSavedForNextTurn.addAll(currentpoints);
            }
            while (!currentpoints.isEmpty()) {
                TriInteger basepoint = currentpoints.poll();
                ArrayList<Integer> allDirections = new ArrayList<>(Arrays.asList(0,1,-1,2));
                // For each direction (straight, left, right backwards)
                for (int iterDirection : allDirections) {
                    int direction = (basepoint.d +4+iterDirection) % 4;
                    TriInteger targetpoint = new TriInteger(basepoint.x+movement(direction).x,basepoint.y+movement(direction).y,direction);
                    if (mapHasBlockAt(grid,targetpoint.x,targetpoint.y)){
                        // If straight is blocked, continue on to right, if right is blocked continue to left, etc
                        continue;
                    }

                    if (betterPath(targetpoint.x,targetpoint.y,targetpoint.d,dist[basepoint.x][basepoint.y][basepoint.d]+1)){
                        dist[targetpoint.x][targetpoint.y][targetpoint.d] = dist[basepoint.x][basepoint.y][basepoint.d]+1;
                        pointsSavedForNextTurn.add(new TriInteger(targetpoint));
                        currentpoints.add(new TriInteger(targetpoint));
                    }
                    break;
                }
            }
            int shortestdist = 100 * 100 + 1;
            for (int i=0;i<4;i++) {
                if (dist[modifiedTarget.x][modifiedTarget.y][i]>0 && dist[modifiedTarget.x][modifiedTarget.y][i]<shortestdist) {
                    shortestdist = dist[modifiedTarget.x][modifiedTarget.y][i];
                }
            }
            if (pointsSavedForNextTurn.isEmpty()) {
                return;
            }
            saveRoute(turn);
        }
    }
}

