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
    private int[][][] dist;
    Point modifiedStart = new Point();
    Point modifiedTarget = new Point();

    // Key is number of chemical (paid) turns
    // The largest key that exists in the map will be the shortest route
    // If there's a key n+1 in the Map, it's distance is less than the distance of key n
    private Map<Integer, ArrayList<Point>> routes=new HashMap<>();
    private Map<Integer, ArrayList<Integer>> turnAt=new HashMap<>();
    private int selectedRoute;

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
        dist = new int[size][size][4];
        // Necessary since (0,0) on the game board is labeled (1,1)
        modifiedStart.x=start.x-1;
        modifiedStart.y=start.y-1;
        modifiedTarget.x=target.x-1;
        modifiedTarget.y=target.y-1;

        // Initialize distance map
        for (int i=0;i<size;i++) {
            for (int j = 0; j < size; j++) {
                for (int d = 0; d < 4; d++) {
                    dist[i][j][d] = grid[i][j].isBlocked() ? -1:0;
                }
            }
        }

        // TODO (etm): Update this once the constructor is privy to the number of
        //   agents that need to reach the goal.
        // Divide by 3 since the default number of agents to send to the goal is 3
        int maxTurns = budget / 3;

        // Run the shortest paths algorithm. Results are stored in `routes`
        // and keyed by the number of turns necessary for the path.
        findshortestpath(grid,maxTurns);

        // Select the fastest route within our budget.
        // Routes with more turns are faster, otherwise `findshortestpath` will terminate
        // without adding a route for that number of turns. Therefore, if key `5` exists in
        // `routes`, it's route is strictly shorter than the route for key `4`.
        for (int i=0;i<budget;i++) {
            if (!routes.containsKey(i)){
                break;
            }
            this.selectedRoute = i;
            ArrayList<Point> route = routes.get(i);
            Collections.reverse(route);
            setTurnAt(i,grid);

            // TODO (etm): Schedule is currently unused, so it's commented out
            // TODO (etm): Update this once the time allowed is known (?)
//            schedule(1000);
        }
    }

    public void schedule(int maxTime) {
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

    public void setTurnAt(final int turn,ChemicalCell[][] grid) {
        final ArrayList<Point> route = routes.get(turn);
        // List of turns in the route
        ArrayList<Integer> turns = new ArrayList<>();
        int currentStep = routes.get(turn).size()-1;
        int d = 0;
        ArrayList<Integer> allDirections = new ArrayList<>(Arrays.asList(0,1,-1,2));
        while (currentStep>0) {
            Point current = new Point(route.get(currentStep).x,route.get(currentStep).y);
            int defaultDir=d;
            for (int j : allDirections) {
                int newDir = d+j;
                if (!mapHasBlockAt(grid, current.x + movement(newDir).x, current.y + movement(newDir).y)) {
                    defaultDir = (d+j+4) % 4;
                    break;
                }
            }
            int actualDir = 0;
            for (int j=0;j<4;j++) {
                if ((route.get(currentStep-1).x - route.get(currentStep).x == movement(j).x) &&
                    (route.get(currentStep-1).y - route.get(currentStep).y == movement(j).y)) {
                    actualDir = j;
                    break;
                }
            }
            if (actualDir==defaultDir) {
                turns.add(0);
            }
            else {
                turns.add(actualDir - d);
            }
            currentStep-=1;
            d = actualDir;
        }
        Collections.reverse(turns);
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
            case 0: return new Point(-1,0);
            // East
            case 1:return new Point(0,1);
            // South
            case 2:return new Point(1,0);
            // West
            default:return new Point(0,-1);
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
        ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
        if (chemicalsRemaining == 0 || !this.routes.containsKey(this.selectedRoute)) {
            // Either no chemicals, or route doesn't exist
            return chemicalPlacement;
        }
        ArrayList<Point> route = this.routes.get(this.selectedRoute);
        ArrayList<Integer> turns = this.turnAt.get(this.selectedRoute);

        // Turns are stored in reverse order, so turns[0] is the last turn
        int furthestTurnIx = Integer.MIN_VALUE;
        // Check the location of all agents and see if any are sitting on
        // a turn point. For those that are, select the furthest turn point,
        // which is the one with the smallest index.
        for (Point agentLocation : locations) {
            for (int turnIx = 0; turnIx < turns.size(); ++turnIx) {
                if (turns.get(turnIx) == 0) {
                    continue;
                }
                Point turn = route.get(turnIx + 1);
                // Fix this annoying 1-based map indexing
                Point zeroAgentLocation = new Point(agentLocation.x - 1, agentLocation.y - 1);
                if (turn.equals(zeroAgentLocation) && turnIx > furthestTurnIx ) {
                    furthestTurnIx = turnIx;
                }
            }
        }
        if (furthestTurnIx >= 0) {
            // Place the chemical on the next step on the path
            Point loc = route.get(furthestTurnIx + 2);
            chemicalPlacement.location = new Point(loc.x + 1, loc.y + 1);
            chemicalPlacement.chemicals.add(ChemicalCell.ChemicalType.BLUE);
        }

        return chemicalPlacement;
    }

    // whether the agent can reach cell x,y facing direction d within s steps
    private Boolean isBestPath(int x, int y, int d, int s){
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
                            if (isBestPath(targetPoint.x,targetPoint.y,targetPoint.d,dist[basePoint.x][basePoint.y][basePoint.d]+1)) {
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
                        if (isBestPath(modifiedStart.x-1,modifiedStart.y,2,1)){
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

                    if (isBestPath(targetpoint.x,targetpoint.y,targetpoint.d,dist[basepoint.x][basepoint.y][basepoint.d]+1)){
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

