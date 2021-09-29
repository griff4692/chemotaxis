package chemotaxis.g1;

import java.awt.Point;
import java.util.*;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.SimPrinter;
import chemotaxis.g1.GameState;
public class Controller extends chemotaxis.sim.Controller {
    /**
     *dist[i][j][d] = x (x>0) means it takes agents x steps to go from the starting point to the cell at line x and column y facing direction d
     *dist[i][j][d] = 0 means agent can't go to cell x,y from direction d yet
     *dist[i][j][d] = -1 means there is a block at x,y
     **/
    private ArrayList<int[][][]> dist_record = new ArrayList<>();
    private int[][][] dist;
    Point modifiedStart = new Point();
    Point modifiedTarget = new Point();
    // Key is number of chemical (paid) turns
    // The largest key that exists in the map will be the shortest route
    // If there's a key n+1 in the Map, it's distance is less than the distance of key n
    private Map<Integer, ArrayList<Point>> routes=new HashMap<>();
    private Map<Integer, ArrayList<Integer>> turnAt=new HashMap<>();
    private Map<Integer, ArrayList<Integer>> turnAt_simpleForm = new HashMap<>();
    private ArrayList<TriInteger> agents = new ArrayList<>();
    private ArrayList<TriInteger> agentsSimOutput;
    private enum StrategyChoice {
        strong,
        weak;
    }
    private enum Color{
        blue,
        red,
        green;
        public Color next() {
            switch (this) {
                case blue: return Color.red;
                case red: return Color.green;
                default: return Color.blue;
            }
        }
    }
    private StrategyChoice strategy;
    private int selectedRoute;
    // a final schedule if the strategy is strong
    // finalSchedule[i]=j means put a chemical at j+1 cell on the route on turn i
    private Map<Integer,Integer> strongStrategy = new HashMap<>();
    // a initial schedule if the strategy is weak
    // initialScheduleWeak[i]=color_a means put a color_a chemical at i+1 cell on the route
    private Map<Integer, Color> initialScheduleWeak = new HashMap<>();

    // Game State for IDS lookahead
    private GameState gameState;

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
        Point adjustedStart = new Point(start.x-1, start.y-1);
        Point adjustedTarget = new Point(target.x-1, target.y-1);
        this.gameState = new GameState(adjustedStart, adjustedTarget, agentGoal, spawnFreq, budget, grid);

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
        // Run the shortest paths algorithm. Results are stored in `routes`
        // and keyed by the number of turns necessary for the path.

        findshortestpath(grid,budget);

        // Select the fastest route within our budget.
        // Routes with more turns are faster, otherwise `findshortestpath` will terminate
        // without adding a route for that number of turns. Therefore, if key `5` exists in
        // `routes`, it's route is strictly shorter than the route for key `4`.

        for (int i=0;i<budget;i++) {
            if (!routes.containsKey(i)) {
                break;
            }
            ArrayList<Point> route = routes.get(i);
            Collections.reverse(route);
            routes.put(i, route);
            setTurnAt(grid, i);
        }

        this.selectedRoute = budget / agentGoal - 1;
        while (!routes.containsKey(this.selectedRoute)) {
            this.selectedRoute-=1;
        }
        if (routes.get(this.selectedRoute).size()==0) {
            strategy=StrategyChoice.weak;
            return;
        }

        scheduleAllAgents(this.selectedRoute,simTime,spawnFreq,agentGoal);

    }

 /*   private void simWeak(Point start, Point target, int agentGoal, int spawnFreq,
                         int chemicalsRemaining, int maxtime, ChemicalCell[][] grid) {
        GameState diffusionSim = new GameState(start,target,agentGoal,spawnFreq,budget,grid);
        for (int i=budget;i>0;i++) {
            if (!routes.containsKey(i)) {
                continue;
            }
            boolean valid = true;
            ChemicalCell.ChemicalType currentType = ChemicalCell.ChemicalType.BLUE;
            int time=0;
            ArrayList<Point> route = routes.get(i);
            ArrayList<Integer> turn = turnAt.get(i);
            ArrayList<Integer> turn_simple = turnAt_simpleForm.get(i);
            while (time<maxtime) {
                time+=1;
                if (time<turn.size() && turn.get(time)!=0) {
                    ChemicalPlacement chem = new ChemicalPlacement();
                    chem.location = new Point(route.get(time));
                    chem.chemicals.add(currentType);
                    diffusionSim.placeChemicalAndStep(chem);
                    switch (currentType) {
                        case BLUE:
                            currentType = ChemicalCell.ChemicalType.RED;
                            break;
                        case RED:
                            currentType = ChemicalCell.ChemicalType.GREEN;
                            break;
                        default:
                            currentType = ChemicalCell.ChemicalType.BLUE;
                            break;
                    }
                }
                else {

                }
            }



        }
    }
*/



    private void scheduleAllAgents(int turnChoice, int simTime, int spawnFreq, int agentGoal) {
        int currentOnPath=0;
        int time=-1;
        int estimateEnd=0;
        int atStart=0;
        while (time<=simTime) {
            if (estimateEnd>simTime) {
                strategy = StrategyChoice.weak;
                return;
            }
            if (currentOnPath==agentGoal) {
                strategy = StrategyChoice.strong;
                return;
            }
            time+=1;
            if (time % spawnFreq==0) {
                atStart+=1;
            }
            if (atStart==0) {
                continue;
            }

            boolean readyToGo = true;
            if (strongStrategy.containsKey(time)) {
                continue;
            }

            for (int i=0;i<turnAt_simpleForm.get(turnChoice).size();i++) {
                if (strongStrategy.containsKey(time+turnAt_simpleForm.get(turnChoice).get(i))) {
                    readyToGo=false;
                }
            }

            if (readyToGo) {
                strongStrategy.put(time,0);
                for (int i=0;i<turnAt_simpleForm.get(turnChoice).size();i++) {
                    strongStrategy.put(time+turnAt_simpleForm.get(turnChoice).get(i),turnAt_simpleForm.get(turnChoice).get(i));
                }
                atStart-=1;
                currentOnPath+=1;
                estimateEnd = time + routes.get(turnChoice).size();
            }
        }
    }


    //sim==0, no simulation
    //sim=x>0, sim for x turns
    private void trackAgents(ChemicalCell[][] grid, ArrayList<Point> agentLoc, Point start, Point end, int sim) {

        int toRemove = -1;
        boolean spawnPointOccupied = false;
        for (int i=0;i<agents.size();i++) {
            agents.set(i,agentMovementSim(grid, agents.get(i)));
            if (agents.get(i).x==end.x && agents.get(i).y==end.y) {
                toRemove = i;
            }
            if (agents.get(i).x==start.x && agents.get(i).y==start.y) {
                spawnPointOccupied = true;
            }
        }
        agents.remove(toRemove);
        if (agentLoc.contains(start) && !spawnPointOccupied) {
            agents.add(new TriInteger(start.x,start.y,0));
        }
        while (sim>0) {
            sim-=1;
            agentsSimOutput = new ArrayList<>(agents);
            toRemove = -1;
            for (int i=0;i<agentsSimOutput.size();i++) {
                agentsSimOutput.set(i,agentMovementSim(grid, agentsSimOutput.get(i)));
                if (agentsSimOutput.get(i).x==end.x && agentsSimOutput.get(i).y==end.y) {
                    toRemove = i;
                }
            }
            agentsSimOutput.remove(toRemove);

        }
    }
    private boolean agentAt(int x, int y) {
        for (TriInteger agent : agents) {
            if (agent.x==x && agent.y==y) {
                return  true;
            }
        }
        return false;
    }
    private TriInteger agentMovementSim(ChemicalCell[][] grid,TriInteger agent) {
        // Assume no chemical in the grid. let's talk about chemical during meeting
        TriInteger des = new TriInteger(agent);
        ArrayList<Integer> allDirections = new ArrayList<>(Arrays.asList(0,1,-1));
        for (int j : allDirections) {
            int newDir = des.d+j;
            if (!mapHasBlockAt(grid,des.x + movement(newDir).x, des.y + movement(newDir).y)) {
                if (agentAt(des.x + movement(newDir).x, des.y + movement(newDir).y)) {
                    break;
                }
                des.d = (des.d+j+4) % 4;
                des.x += movement(newDir).x;
                des.y += movement(newDir).y;
                break;
            }
        }
        return new TriInteger(des);
    }

    private void setTurnAt(ChemicalCell[][] grid, final int turn) {
        final ArrayList<Point> route = routes.get(turn);
        // List of turns in the route
        ArrayList<Integer> turns = new ArrayList<>();
        int currentStep = 0;
        int d = 0;
        ArrayList<Integer> allDirections = new ArrayList<>(Arrays.asList(0,1,-1));
        while (currentStep<routes.get(turn).size()-1) {
            Point current = new Point(route.get(currentStep).x,route.get(currentStep).y);
            int defaultDir=d;
            for (int j : allDirections) {
                int newDir = (d+j+4) % 4;
                if (!mapHasBlockAt(grid,current.x + movement(newDir).x, current.y + movement(newDir).y)) {
                    defaultDir = (d+j+4) % 4;
                    break;
                }
            }
            int actualDir = 0;
            for (int j=0;j<4;j++) {
                if ((route.get(currentStep+1).x - route.get(currentStep).x == movement(j).x) &&
                    (route.get(currentStep+1).y - route.get(currentStep).y == movement(j).y)) {
                    actualDir = j;
                    break;
                }
            }
            if (actualDir==defaultDir) {
                turns.add(0);
            }
            else {
                turns.add(actualDir - defaultDir);
            }
            currentStep+=1;
            d = actualDir;
        }

        turnAt_simpleForm.put(turn,new ArrayList<>());

        if (!turns.isEmpty()) {
            turnAt.put(turn, new ArrayList<>(turns));
            for (int i=0;i<turnAt.get(turn).size();i++) {
                if (turnAt.get(turn).get(i)!=0) {
                    turnAt_simpleForm.get(turn).add(i);
                }
            }
        }
        else {
            turnAt.put(turn, new ArrayList<>());
        }
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
     * Internal chemical placement function. Used to capture result and update game state.
     * @param currentTurn
     * @param chemicalsRemaining
     * @param locations
     * @param grid
     * @return
     */
    private ChemicalPlacement _applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid) {
        ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
        if (strategy==StrategyChoice.strong) {
            currentTurn-=1;
            if (strongStrategy.containsKey(currentTurn)) {
                chemicalPlacement.location = new Point(routes.get(this.selectedRoute).get(strongStrategy.get(currentTurn)+1).x+1,
                    routes.get(this.selectedRoute).get(strongStrategy.get(currentTurn)+1).y+1);
                if (strongStrategy.get(currentTurn)==0) {
                    chemicalPlacement.chemicals.add(ChemicalCell.ChemicalType.GREEN);
                    return chemicalPlacement;
                }
                else {
                    chemicalPlacement.chemicals.add(ChemicalCell.ChemicalType.BLUE);
                    return chemicalPlacement;
                }
            }
            return chemicalPlacement ;
        }
        if (currentTurn == 1) {
            chemicalPlacement.location = start;
            chemicalPlacement.chemicals.add(ChemicalCell.ChemicalType.GREEN);
            return chemicalPlacement;
        }
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
        for (int turnIx =  turns.size()-1; turnIx >=0; --turnIx) {
            if (turns.get(turnIx) == 0) {
                continue;
            }
            boolean found=false;
            for (Point agentLocation : locations) {
                Point turn = route.get(turnIx);
                // Fix this annoying 1-based map indexing
                Point zeroAgentLocation = new Point(agentLocation.x - 1, agentLocation.y - 1);
                if (turn.equals(zeroAgentLocation)) {
                    furthestTurnIx = turnIx;
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }
        if (furthestTurnIx >= 0) {
            // Place the chemical on the next step on the path
            Point loc = route.get(furthestTurnIx + 1);
            chemicalPlacement.location = new Point(loc.x + 1, loc.y + 1);
            chemicalPlacement.chemicals.add(ChemicalCell.ChemicalType.BLUE);
        }
        return chemicalPlacement;
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
        // TODO (etm): Debug only, remove this validation.
        //   Throws an exception if the game state deviates from expected.
        try {
            this.gameState.validateEquivalence(currentTurn, chemicalsRemaining, locations, grid);
        } catch (RuntimeException e) {
            System.err.println("" + e);
        }

        ChemicalPlacement chemicalPlacement = this._applyChemicals(currentTurn, chemicalsRemaining, locations, grid);

        this.gameState = this.gameState.placeChemicalAndStep(chemicalPlacement);
        return chemicalPlacement;
    }

    // whether the agent can reach cell x,y facing direction d within s steps
    private Boolean isBestPath(int x, int y, int d, int s){
        if (modifiedStart.x==x && modifiedStart.y==y) {
            return false;
        }
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
        int[][][] localdist = dist;
        int localturn = turn;
        if (step<10001) {
            while (!((modifiedStart.x==current.x)&&(modifiedStart.y==current.y))) {


                route.add(new Point(current));
                boolean endwhile = false;
                while (!endwhile) {
                    for (int i = 0; i < 4; i++) {
                        int j = (i + direction + 4) % 4;
                        if (localdist[current.x][current.y][j] == step) {
                            direction = j;
                            step -= 1;
                            endwhile = true;
                            break;
                        }
                    }
                    if (!endwhile) {
                        localturn -= 1;
                        localdist = dist_record.get(localturn);
                    }
                }
                current.x -= movement(direction).x;
                current.y -= movement(direction).y;
            }

            route.add(new Point(current));
        }

        routes.put(turn, new ArrayList<>(route));
    }

    /*
    find the shortest path with 0 to maxturn turns, store data in dist
     */
    private void findshortestpath(ChemicalCell[][] grid, int maxturn) {
        Queue<TriInteger> currentpoints = new LinkedList<>();
        currentpoints.add(new TriInteger(modifiedStart.x,modifiedStart.y,0));
        // pointsSavedForNextTurn contains the points you can reach with `turn` chemicals
        // Usually this is just a list of points in a straight line, but could include
        // other points if there is a wall that gives you a "free" turn in some direction.
        Queue<TriInteger> pointsSavedForNextTurn = new LinkedList<>();
        pointsSavedForNextTurn.add(new TriInteger(modifiedStart.x,modifiedStart.y,0));
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
                    if (!mapHasBlockAt(grid,modifiedStart.x+1,modifiedStart.y)){
                        if (isBestPath(modifiedStart.x+1,modifiedStart.y,2,1)){
                            currentpoints.add(new TriInteger(modifiedStart.x+1,modifiedStart.y,2));
                            dist[modifiedStart.x+1][modifiedStart.y][2] = 1;
                        }
                    }
                }
                pointsSavedForNextTurn.addAll(currentpoints);
            }

            while (!currentpoints.isEmpty()) {
                TriInteger basepoint = currentpoints.poll();
                ArrayList<Integer> allDirections = new ArrayList<>(Arrays.asList(0,1,-1));
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

            if (pointsSavedForNextTurn.isEmpty()&&turn>0) {
                return;
            }
            int [][][] temp = new int[size][size][4];
            for (int i=0;i<size;i++) {
                for (int j = 0; j < size; j++) {
                    for (int k = 0; k < 4; k++) {
                        temp[i][j][k] = dist[i][j][k];
                    }
                }
            }
            dist_record.add(temp);

            if (shortestdist<10001) {
                saveRoute(turn);
            }
            else {
                routes.put(turn, new ArrayList<>());
            }
        }
    }
}

