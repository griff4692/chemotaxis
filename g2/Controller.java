package chemotaxis.g2;

import java.awt.*;
import java.util.*;

import chemotaxis.sim.*;

public class Controller extends chemotaxis.sim.Controller {
    private final DirectionType INITIAL_AGENT_DIR = DirectionType.NORTH;
    private ArrayList<Point> shortestPath;
    private ArrayList<Point> test;
    private ArrayList<Map.Entry<Point, DirectionType>> turns;
    private Queue<Map.Entry<Point, MoveType>> movesQueue;
    private DirectionType prevDir;
    private Point prevLocation;
    /**
     * Controller constructor
     *
     * @param start       start cell coordinates
     * @param target      target cell coordinates
     * @param size       grid/map size
     * @param simTime     simulation time
     * @param budget      chemical budget
     * @param seed        random seed
     * @param simPrinter  simulation printer
     *
     */
    public Controller(Point start, Point target, Integer size, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
        super(start, target, size, simTime, budget, seed, simPrinter);
        this.prevDir = INITIAL_AGENT_DIR;
        this.movesQueue = new LinkedList<>();
    }

    /**
     * Apply chemicals to the map
     *
     * @param currentTurn         current turn in the simulation
     * @param chemicalsRemaining  number of chemicals remaining
     * @param currentLocation     current location of the agent
     * @param grid                game grid/map
     * @return                    a cell location and list of chemicals to apply
     *
     */
    // TODO: update so that it reads from this.movesList instead
    @Override
    public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, Point currentLocation, ChemicalCell[][] grid) {
        simPrinter.println("Turn #" + currentTurn.toString());
        simPrinter.println("Location: " + currentLocation.toString());

        if (currentTurn == 1) {
            getOptimalPath(grid);
            prevLocation = currentLocation;
        }
        /*else if(currentTurn == 2) {
            getPathInCaseTimeOut(grid, point start, target, prev move, prev ortho);
            (check for last chemical)
        }*/
        updateAgentAttributes(currentLocation, currentTurn);

        ChemicalPlacement cp = new ChemicalPlacement();
        if (movesQueue.isEmpty()) {
            cp.location = currentLocation;
            return cp;
        }

        Map.Entry<Point, MoveType> moveEntry = movesQueue.peek();
        Point movePoint = moveEntry.getKey();

        if (movePoint.equals(currentLocation)) {
            // TODO: handle zig zag MoveType
            MoveType nextMove = moveEntry.getValue();
            DirectionType direction = moveToDirectionType(nextMove);
            cp.location = adjPoint(movePoint, direction);

            if(chemicalsRemaining == 1 && movesQueue.size() > 1) {
                cp.chemicals.add(ChemicalCell.ChemicalType.BLUE);
            }
            else {
                cp.chemicals.add(ChemicalCell.ChemicalType.RED);
            }
            movesQueue.poll();
        }
        return cp;
    }

    private DirectionType moveToDirectionType(MoveType moveType) {
        switch (moveType) {
            case N:
                return DirectionType.NORTH;
            case E:
                return DirectionType.EAST;
            case S:
                return DirectionType.SOUTH;
            default:
                return DirectionType.WEST;
        }
    }

    private MoveType directionToMoveType(DirectionType dir) {
        switch (dir) {
            case NORTH:
                return MoveType.N;
            case EAST:
                return MoveType.E;
            case SOUTH:
                return MoveType.S;
            default:
                return MoveType.W;
        }
    }

    // TODO:
    private void updateMovesQueue(ArrayList<Point> path, ChemicalCell[][] grid) {
        ArrayList<Map.Entry<Point, DirectionType>> turnsList = getTurnsList();
        turnsListToMovesQueue(turnsList);
        applyAgentDefaultFilerToMovesList(grid);
        applyZigZagFilterToMovesList(grid);
    }

    private void turnsListToMovesQueue(ArrayList<Map.Entry<Point, DirectionType>> turnsList) {
        movesQueue = new LinkedList<>();
        for (Map.Entry<Point, DirectionType> turnEntry: turnsList) {
            Point point = turnEntry.getKey();
            MoveType moveType = directionToMoveType(turnEntry.getValue());
            movesQueue.add(new AbstractMap.SimpleEntry(point, moveType));
        }
    }

    // TODO: Joe
    private void applyAgentDefaultFilerToMovesList(ChemicalCell[][] grid) {
        MoveType agentPrevMove = MoveType.N;
        MoveType agentPrevOrthMove = MoveType.E;

        Iterator<Map.Entry<Point, MoveType>> it = movesQueue.iterator();
        while (it.hasNext()) {
            Map.Entry<Point, MoveType> moveEntry = it.next();
            Point point = moveEntry.getKey();
            MoveType moveToMake = moveEntry.getValue();
            ArrayList<MoveType> possibleMoves = new ArrayList<>();

            Point westPoint = new Point(point.x, point.y - 1);
            if (pointIsOpen(westPoint, grid)) {
                possibleMoves.add(MoveType.W);
            }
            Point eastPoint = new Point(point.x, point.y + 1);
            if (pointIsOpen(eastPoint, grid)) {
                possibleMoves.add(MoveType.E);
            }
            Point southPoint = new Point(point.x + 1, point.y);
            if (pointIsOpen(southPoint, grid)) {
                possibleMoves.add(MoveType.S);
            }
            Point northPoint = new Point(point.x - 1, point.y);
            if (pointIsOpen(northPoint, grid)) {
                possibleMoves.add(MoveType.N);
            }

            MoveType agentDefaultMove = getAgentsDefaultMove(agentPrevMove, agentPrevOrthMove, possibleMoves);
            if (agentDefaultMove == moveToMake) {
                it.remove();
            }

            // reset agents prev moves
            if (agentPrevMove == MoveType.N || agentPrevMove == MoveType.S) {
                if (moveToMake == MoveType.E || moveToMake == MoveType.W) {
                    agentPrevOrthMove = agentPrevMove;
                }
            }
            else {
                if (moveToMake == MoveType.N || moveToMake == MoveType.S) {
                    agentPrevOrthMove = agentPrevMove;
                }
            }
            agentPrevMove = moveToMake;
        }
    }

    private boolean pointIsOpen(Point p, ChemicalCell[][] grid) {
        int rowIndex = p.x - 1;
        int colIndex = p.y - 1;

        if (rowIndex >= 0 && colIndex >= 0 && rowIndex < grid.length && colIndex < grid.length) {
            ChemicalCell cell = grid[rowIndex][colIndex];
            return cell.isOpen();
        }
        return false;
    }

    private MoveType getAgentsDefaultMove(MoveType agentPrevMove,
                                               MoveType agentPrevOrthMove,
                                               ArrayList<MoveType> possibleMoves) {
        if (possibleMoves.contains(agentPrevMove)) {
            return agentPrevMove;
        }

        if (possibleMoves.contains(agentPrevOrthMove)) {
            return agentPrevOrthMove;
        }

        MoveType oppOfPrevOrthMove = getOppositeMove(agentPrevOrthMove);
        if (possibleMoves.contains(oppOfPrevOrthMove)) {
            return oppOfPrevOrthMove;
        }

        simPrinter.println("Error in getAgentsDefaultMove => agent is repeating points!");
        return getOppositeMove(agentPrevMove);
    }

    // TODO:
    private void applyZigZagFilterToMovesList(ChemicalCell[][] grid) {

    }

    private void updateAgentAttributes(Point currentLocation, Integer currentTurn) {
        int xDiff = currentLocation.x - this.prevLocation.x;
        int yDiff = currentLocation.y - this.prevLocation.y;

        if (currentTurn == 1) {
            this.prevDir = DirectionType.NORTH;
        }
        else if (yDiff == -1) {
            this.prevDir = DirectionType.WEST;
        }
        else if (yDiff == 1) {
            this.prevDir = DirectionType.EAST;
        }
        else if (xDiff == -1) {
            this.prevDir = DirectionType.NORTH;
        }
        else if (xDiff == 1) {
            this.prevDir = DirectionType.SOUTH;
        }
        else {
            this.prevDir = DirectionType.CURRENT;
        }
        this.prevLocation = currentLocation;
    }

    private boolean chemicalIsRequiredForTurn(Point currentLocation, ChemicalCell[][] grid) {
        return true;
    }

    private Map<DirectionType, ChemicalCell.ChemicalType> getChemicalDirections() {
        Map<DirectionType, ChemicalCell.ChemicalType> chemicalDirs = new HashMap<>();
        // order of elements chemicalTypes and directionTypes is critical to making sure
        // values map correctly for both agent and controller
        ChemicalCell.ChemicalType[] chemicalTypes = {
                ChemicalCell.ChemicalType.RED,
                ChemicalCell.ChemicalType.GREEN,
                ChemicalCell.ChemicalType.BLUE
        };

        DirectionType[] directionTypes = {
                DirectionType.NORTH,
                DirectionType.EAST,
                DirectionType.SOUTH,
                DirectionType.WEST
        };

        int dirIndex = 0;
        for (int i = 0; i < chemicalTypes.length; i++) {
            if (directionTypes[dirIndex] == prevDir) {
                dirIndex++;
            }
            chemicalDirs.put(directionTypes[dirIndex], chemicalTypes[i]);
            dirIndex++;
        }
        return chemicalDirs;
    }

    // TODO:
    private void getOptimalPath(ChemicalCell[][] grid) {
        
        shortestPath = getShortestPath(grid);
        turns = getTurnsList();
        turnsListToMovesQueue(turns);
        applyAgentDefaultFilerToMovesList(grid);

        if (movesQueue.size() > budget){
            movesQueue = new LinkedList<>();
            shortestPath =  getLeastTurnsPath(start, target, grid);
            turns = getTurnsList();
            turnsListToMovesQueue(turns);
            applyAgentDefaultFilerToMovesList(grid);
        }
    }

    // source: https://www.techiedelight.com/lee-algorithm-shortest-path-in-a-maze/
    private ArrayList<Point> getShortestPath(ChemicalCell[][] grid) {
        // after finding shortest path:
        // if (pathCost > budget) => continue to next shortest path

        int[] rowPosMovs = { -1, 0, 0, 1 };
        int[] colPosMovs = { 0, -1, 1, 0 };
        // up, left, right, down

        // construct a matrix to keep track of visited cells
        boolean[][] visited = new boolean[size][size];
        Point[][] prevCell = new Point[size][size];

        // create an empty queue
        Queue<Node> q = new ArrayDeque<>();

        int i = (int) start.getX()-1;
        int j = (int) start.getY()-1;


        // mark source cell as visited and enqueue the source node
        visited[i][j] = true;
        prevCell[i][j] = null;
        q.add(new Node(i, j , 0));

        int min_dist = Integer.MAX_VALUE;

        // loop till queue is empty
        while (!q.isEmpty())
        {
            // pop front node from queue and process it
            Node node = q.poll();

            // (i, j) represents current cell and dist stores its
            // minimum distance from the source
            i = node.x;
            j = node.y;
            int dist = node.dist;

            // if destination is found, update min_dist and stop
            if (i == (int) target.getX()-1 && j == (int) target.getY()-1)
            {
                min_dist = dist;
                break;
            }

            
            // get previous move. (i's parent). move in same direction first.
            if (!(i == (int) start.getX()-1 && j==(int) start.getY()-1)){ 
                Point grandparent = prevCell[i][j];
                int directionR = i - (int) (grandparent.getX() - 1.0);
                int directionC = j - (int) (grandparent.getY() - 1.0);

                if (directionR == 0) {
                    if (directionC == 1){
                        rowPosMovs[0] = 0;
                        colPosMovs[0] = 1;
                        rowPosMovs[1] = -1;
                        colPosMovs[1] = 0;
                        rowPosMovs[2] = 0;
                        colPosMovs[2] = -1;
                        rowPosMovs[3] = 1;
                        colPosMovs[3] = 0;
                    }
                    else if (directionC == -1){
                        rowPosMovs[0] = 0;
                        colPosMovs[0] = -1;
                        rowPosMovs[1] = -1;
                        colPosMovs[1] = 0;
                        rowPosMovs[2] = 0;
                        colPosMovs[2] = 1;
                        rowPosMovs[3] = 1;
                        colPosMovs[3] = 0;
                    }
                } 
                else if (directionC == 0) {
                    if (directionR == 1){
                        rowPosMovs[0] = 1;
                        colPosMovs[0] = 0;
                        rowPosMovs[1] = -1;
                        colPosMovs[1] = 0;
                        rowPosMovs[2] = 0;
                        colPosMovs[2] = -1;
                        rowPosMovs[3] = 0;
                        colPosMovs[3] = 1;
                    }
                    else if (directionR == -1){
                        rowPosMovs[0] = -1;
                        colPosMovs[0] = 0;
                        rowPosMovs[1] = 1;
                        colPosMovs[1] = 0;
                        rowPosMovs[2] = 0;
                        colPosMovs[2] = -1;
                        rowPosMovs[3] = 0;
                        colPosMovs[3] = 1;
                    }
                }
            }

            // check for all 4 possible movements from current cell
            // and enqueue each valid movement
            for (int k = 0; k < 4; k++)
            {
                // check if it is possible to go to position
                // (i + row[k], j + col[k]) from current position
                if (isValid(grid, visited, i + rowPosMovs[k], j + colPosMovs[k]))
                {
                    // mark next cell as visited and enqueue it

                    visited[i + rowPosMovs[k]][j + colPosMovs[k]] = true;
                    prevCell[i + rowPosMovs[k]][j + colPosMovs[k]] = new Point(i + 1, j + 1);
                    q.add(new Node(i + rowPosMovs[k], j + colPosMovs[k], dist + 1));
                }
            }
        }

        if (min_dist != Integer.MAX_VALUE) {
            ArrayList<Point> sp = new ArrayList<Point>();
            sp.add(target);
            Point cur = prevCell[(int) target.getX()-1][(int) target.getY()-1];    
            while (cur != null){
                sp.add(0, cur);
                cur = prevCell[(int) cur.getX()-1][(int) cur.getY()-1];
            }
            return sp;
        }
        else {
            return null;
        }

    }

    // TODO:
    private ArrayList<Point> getLeastTurnsPath(Point st, Point end, ChemicalCell[][] grid) {
       
        boolean[][] visited = new boolean[size][size];
        int[][] noOfTurns = new int[size][size];
        ArrayList<Point>[][] fullPath = new ArrayList[size][size]; 
        Map<Point, Map<Point, ArrayList<Point>>> multiSetPath = new HashMap();
        Map<Point, Map<Point, MoveType[]>> multiSetMove = new HashMap();
        Map<Point, Map<Point, ArrayList<Point>>> oldSetPath = new HashMap();
        Map<Point, Map<Point, MoveType[]>> oldSetMove = new HashMap();
        MoveType[][] prevMove = new MoveType[size][size];
        MoveType[][] prevOrthMove = new MoveType[size][size];


        int[] rowPosMovs = { -1, 0, 1, 0};
        int[] colPosMovs = { 0, 1, 0, -1};

        Queue<Node> q = new ArrayDeque<>();

        int i = (int) st.getX()-1;
        int j = (int) st.getY()-1;

        noOfTurns[i][j] = 0;
        visited[i][j] = true;
        ArrayList<Point> path = new ArrayList<Point>();
        path.add(new Point(i+1, j+1));
        fullPath[i][j] = path; 
        prevMove[i][j] = MoveType.N;
        prevOrthMove[i][j] = MoveType.E;

        ArrayList<MoveType>  possibleMoves = getPosMoves(grid, visited, i, j, rowPosMovs, colPosMovs);
        MoveType agentDef = getAgentsDefaultMove(prevMove[i][j], prevOrthMove[i][j], possibleMoves);


        for (int k = 0; k < 4; k++) {
            if (isValid(grid, visited, i + rowPosMovs[k], j + colPosMovs[k])){
                q.add(new Node(i + rowPosMovs[k], j + colPosMovs[k], 0));
                visited[i + rowPosMovs[k]][j + colPosMovs[k]] = true;

                // create path
                path = new ArrayList<Point>();
                for (Point p: fullPath[i][j]){
                    path.add(p);
                }
                path.add(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));
                fullPath[i + rowPosMovs[k]][j + colPosMovs[k]] = path;

                if (rowPosMovs[k] == 1 && colPosMovs[k] == 0) {
                    prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] = MoveType.S;
                    prevOrthMove[i + rowPosMovs[k]][j + colPosMovs[k]] = MoveType.E;
                } 
                else if (rowPosMovs[k] == 0 && colPosMovs[k] == -1) {
                    prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] = MoveType.W;
                    prevOrthMove[i + rowPosMovs[k]][j + colPosMovs[k]] = MoveType.N;
                } 
                else if (rowPosMovs[k] == 0 && colPosMovs[k] == 1) {
                    prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] = MoveType.E;
                    prevOrthMove[i + rowPosMovs[k]][j + colPosMovs[k]] = MoveType.N;
                } 
                else if (rowPosMovs[k] == -1 && colPosMovs[k] == 0) {
                    prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] = MoveType.N;
                    prevOrthMove[i + rowPosMovs[k]][j + colPosMovs[k]] = MoveType.E;
                } 

                if (prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == agentDef){
                   noOfTurns[i + rowPosMovs[k]][j + colPosMovs[k]] = noOfTurns[i][j]; 
                }
                else {
                    noOfTurns[i + rowPosMovs[k]][j + colPosMovs[k]] = noOfTurns[i][j] + 1;
                }                         
            }
        }

        while (!q.isEmpty()) {
            //System.out.println(q.size());

            Node node = q.poll();
            i = node.x;
            j = node.y;

            possibleMoves = getPosMoves(grid, visited, i, j, rowPosMovs, colPosMovs);

            for (int k = 0; k < 4; k++) {
                if (isValid(grid, visited, i + rowPosMovs[k], j + colPosMovs[k])){
                    q.add(new Node(i + rowPosMovs[k], j + colPosMovs[k], 0));
                    visited[i + rowPosMovs[k]][j + colPosMovs[k]] = true;

                    if (rowPosMovs[k] == 1 && colPosMovs[k] == 0) {
                        prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] = MoveType.S;
                    } 
                    else if (rowPosMovs[k] == 0 && colPosMovs[k] == -1) {
                        prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] = MoveType.W;
                    } 
                    else if (rowPosMovs[k] == 0 && colPosMovs[k] == 1) {
                        prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] = MoveType.E;
                    } 
                    else if (rowPosMovs[k] == -1 && colPosMovs[k] == 0) {
                        prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] = MoveType.N;
                    } 

                    if (multiSetPath.containsKey(new Point(i + 1, j + 1))){
                        Map<Point, ArrayList<Point>> mP = multiSetPath.get(new Point(i + 1, j + 1));
                        Map<Point, MoveType[]> mM = multiSetMove.get(new Point(i + 1, j + 1));
                        int minNum;
                        ArrayList<Point> pMin = fullPath[i][j];
                        MoveType orthMin;
                        MoveType prevMin = prevMove[i][j];
                        
                        agentDef = getAgentsDefaultMove(prevMove[i][j], prevOrthMove[i][j], possibleMoves);
                        if (prevMove[i][j] == MoveType.N || prevMove[i][j] == MoveType.S) {
                            if (prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == MoveType.E || 
                                prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == MoveType.W) {
                                orthMin = prevMove[i][j];
                            }
                            else {
                                orthMin = prevOrthMove[i][j];
                            }
                        }
                        else {
                            if (prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == MoveType.N || 
                                prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == MoveType.S) {
                                orthMin = prevMove[i][j];
                            }
                            else {
                                orthMin = prevOrthMove[i][j];
                            }
                        } 

                        if (prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == prevMove[i][j]) {
                            minNum = noOfTurns[i][j]; 
                        }
                        else {
                            if (prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == agentDef){
                                minNum = noOfTurns[i][j]; 
                            }
                            else {
                                minNum = noOfTurns[i][j] + 1;
                            }  
                        } 

                        for (Map.Entry elt : mP.entrySet()) { 
                            Point pKey = (Point) elt.getKey(); 
                            ArrayList<Point> value = (ArrayList<Point>) elt.getValue(); 
                            MoveType[] key =  mM.get(pKey);

                            agentDef = getAgentsDefaultMove(key[0], key[1], possibleMoves);  
                            MoveType orthMove;
                            int numT;

                            if (key[0] == MoveType.N || key[0] == MoveType.S) {
                                if (prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == MoveType.E || 
                                    prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == MoveType.W) {
                                    orthMove = key[0];
                                }
                                else {
                                    orthMove = key[1];
                                }
                            }
                            else {
                                if (prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == MoveType.N || 
                                    prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == MoveType.S) {
                                    orthMove = key[0];
                                }
                                else {
                                    orthMove = key[1];
                                }
                            } 

                            if (prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == key[0]) {
                                numT = noOfTurns[i][j]; 
                            }
                            else {
                                if (prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == agentDef){
                                    numT = noOfTurns[i][j]; 
                                }
                                else {
                                    numT = noOfTurns[i][j] + 1;
                                }  
                            } 
                            if (numT < minNum) {
                                minNum = numT;
                                pMin = value;
                                orthMin = orthMove;
                            }
                        }                                         
                        noOfTurns[i + rowPosMovs[k]][j + colPosMovs[k]] = minNum; 
                        prevOrthMove[i + rowPosMovs[k]][j + colPosMovs[k]] = orthMin;
                        
                        path = new ArrayList<Point>();
                        for (Point p: pMin){
                            path.add(p);
                        }
                        path.add(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));
                        fullPath[i + rowPosMovs[k]][j + colPosMovs[k]] = path; 
                    }
                    else {
                        path = new ArrayList<Point>();
                        for (Point p: fullPath[i][j]){
                            path.add(p);
                        }
                        path.add(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));
                        fullPath[i + rowPosMovs[k]][j + colPosMovs[k]] = path;

                        agentDef = getAgentsDefaultMove(prevMove[i][j], prevOrthMove[i][j], possibleMoves);

                        if (prevMove[i][j] == MoveType.N || prevMove[i][j] == MoveType.S) {
                            if (prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == MoveType.E || 
                                prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == MoveType.W) {
                                prevOrthMove[i + rowPosMovs[k]][j + colPosMovs[k]]  = prevMove[i][j];
                            }
                            else {
                                prevOrthMove[i + rowPosMovs[k]][j + colPosMovs[k]]  = prevOrthMove[i][j];
                            }
                        }
                        else {
                            if (prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == MoveType.N || 
                                prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == MoveType.S) {
                                prevOrthMove[i + rowPosMovs[k]][j + colPosMovs[k]] = prevMove[i][j];
                            }
                            else {
                                prevOrthMove[i + rowPosMovs[k]][j + colPosMovs[k]]  = prevOrthMove[i][j]; 
                            }
                        } 

                        if (prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == prevMove[i][j]) {
                            noOfTurns[i + rowPosMovs[k]][j + colPosMovs[k]] = noOfTurns[i][j]; 
                        }
                        else {
                            if (prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] == agentDef){
                                noOfTurns[i + rowPosMovs[k]][j + colPosMovs[k]] = noOfTurns[i][j]; 
                            }
                            else {
                                noOfTurns[i + rowPosMovs[k]][j + colPosMovs[k]] = noOfTurns[i][j] + 1;
                            }  
                        }
                    }        
                    
                }

                else if (isAlmostValid(grid, visited, i + rowPosMovs[k], j + colPosMovs[k])){
                    MoveType temp = null;   
                    int newNumTurns = 0;           

                    if (rowPosMovs[k] == 1 && colPosMovs[k] == 0) {
                        temp = MoveType.S;
                    } 
                    else if (rowPosMovs[k] == 0 && colPosMovs[k] == -1) {
                        temp = MoveType.W;
                    } 
                    else if (rowPosMovs[k] == 0 && colPosMovs[k] == 1) {
                        temp = MoveType.E;
                    } 
                    else if (rowPosMovs[k] == -1 && colPosMovs[k] == 0) {
                        temp = MoveType.N;
                    } 

                    if (multiSetPath.containsKey(new Point(i + 1, j + 1))){
                        Map<Point, ArrayList<Point>> mP = multiSetPath.get(new Point(i + 1, j + 1));
                        Map<Point, MoveType[]> mM = multiSetMove.get(new Point(i + 1, j + 1));
                        int minNum;
                        ArrayList<Point> pMin = fullPath[i][j];
                        MoveType orthMin;
                        MoveType prevMin = prevMove[i][j];

                        agentDef = getAgentsDefaultMove(prevMove[i][j], prevOrthMove[i][j], possibleMoves);  

                        if (prevMove[i][j] == MoveType.N || prevMove[i][j] == MoveType.S) {
                            if (temp== MoveType.E || temp == MoveType.W) {
                                orthMin = prevMove[i][j];
                            }
                            else {
                                orthMin = prevOrthMove[i][j];
                            }
                        }
                        else {
                            if (temp == MoveType.N || 
                                temp == MoveType.S) {
                                orthMin = prevMove[i][j];
                            }
                            else {
                                orthMin = prevOrthMove[i][j];
                            }
                        } 

                        if (temp == prevMove[i][j]) {
                            minNum = noOfTurns[i][j]; 
                        }
                        else {
                            if (temp == agentDef){
                                minNum = noOfTurns[i][j]; 
                            }
                            else {
                                minNum = noOfTurns[i][j] + 1;
                            }  
                        } 

                        for (Map.Entry elt : mP.entrySet()) { 
                            Point pKey = (Point) elt.getKey(); 
                            ArrayList<Point> value = (ArrayList<Point>) elt.getValue(); 
                            MoveType[] key =  mM.get(pKey);

                            agentDef = getAgentsDefaultMove(key[0], key[1], possibleMoves);  
                            MoveType orthMove;
                            int numT;

                            if (key[0] == MoveType.N || key[0] == MoveType.S) {
                                if (temp== MoveType.E || 
                                    temp == MoveType.W) {
                                    orthMove = key[0];
                                }
                                else {
                                    orthMove = key[1];
                                }
                            }
                            else {
                                if (temp == MoveType.N || 
                                    temp == MoveType.S) {
                                    orthMove = key[0];
                                }
                                else {
                                    orthMove = key[1];
                                }
                            } 

                            if (temp == key[0]) {
                                numT = noOfTurns[i][j]; 
                            }
                            else {
                                if (temp == agentDef){
                                    numT = noOfTurns[i][j]; 
                                }
                                else {
                                    numT = noOfTurns[i][j] + 1;
                                }  
                            } 

                            if (numT < minNum) {
                                minNum = numT;
                                pMin = value;
                                orthMin = orthMove;
                            }
                        }   

                        newNumTurns = minNum;
                        if (newNumTurns < noOfTurns[i + rowPosMovs[k]][j + colPosMovs[k]]){
                            noOfTurns[i + rowPosMovs[k]][j + colPosMovs[k]] = newNumTurns; 
                            prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] = temp;
                            prevOrthMove[i + rowPosMovs[k]][j + colPosMovs[k]] = orthMin;
                        
                            path = new ArrayList<Point>();
                            for (Point p: pMin){
                                path.add(p);
                            }
                            path.add(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));
                            fullPath[i + rowPosMovs[k]][j + colPosMovs[k]] = path; 

                            multiSetPath.remove(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));
                            multiSetMove.remove(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));

                            q.add(new Node(i + rowPosMovs[k], j + colPosMovs[k], 0));
                        }
                        else if (newNumTurns == noOfTurns[i + rowPosMovs[k]][j + colPosMovs[k]]){
                            path = new ArrayList<Point>();
                            for (Point p: pMin){
                                path.add(p);
                            }

                            if (!path.contains(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1))) {
                                if (multiSetPath.containsKey(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1))){  
                                    Map<Point, ArrayList<Point>> tPath = multiSetPath.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));
                                    Map<Point, MoveType[]> tMove = multiSetMove.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));
                                    MoveType[] b = new MoveType[2];
                                    b[0] = temp;
                                    b[1] = orthMin;

                                    tMove.put(new Point(i+1, j+1),b);

                                    path.add(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));

                                    tPath.put(new Point(i+1, j+1),path);

                                    multiSetPath.put(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), tPath);
                                    multiSetMove.put(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), tMove);
                                }
                                else {
                                    Map<Point, ArrayList<Point>> tPath = new HashMap();
                                    Map<Point, MoveType[]> tMove = new HashMap();
                                    MoveType[] b = new MoveType[2];
                                    b[0] = temp;
                                    b[1] = orthMin;

                                    tMove.put(new Point(i+1, j+1),b);

                                    path.add(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));

                                    tPath.put(new Point(i+1, j+1),path);

                                    multiSetPath.put(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), tPath);
                                    multiSetMove.put(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), tMove);
                                }

                                if (oldSetPath.containsKey(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1))){
                                    if (!(multiSetPath.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1)).equals(oldSetPath.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1)))
                                        && multiSetMove.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1)).equals(oldSetMove.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1))))) {
                                        q.add(new Node(i + rowPosMovs[k], j + colPosMovs[k], 0));
                                        oldSetPath.replace(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), multiSetPath.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1)));
                                        oldSetMove.replace(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), multiSetMove.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1)));
                                    }
                                }
                                else {
                                    q.add(new Node(i + rowPosMovs[k], j + colPosMovs[k], 0));
                                    oldSetPath.put(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), multiSetPath.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1)));
                                    oldSetMove.put(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), multiSetMove.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1)));
                                }
                            }

                        }
                    }
                    else {
                        agentDef = getAgentsDefaultMove(prevMove[i][j], prevOrthMove[i][j], possibleMoves);
                        MoveType orthTemp;

                        if (prevMove[i][j] == MoveType.N || prevMove[i][j] == MoveType.S) {
                            if (temp == MoveType.E || temp == MoveType.W) {
                                orthTemp = prevMove[i][j];
                            }
                            else {
                               orthTemp = prevOrthMove[i][j];
                            }
                        }
                        else {
                            if (temp == MoveType.N || temp == MoveType.S) {
                                orthTemp = prevMove[i][j];
                            }
                            else {
                                orthTemp = prevOrthMove[i][j]; 
                            }
                        } 

                        if (temp == prevMove[i][j]) {
                            newNumTurns = noOfTurns[i][j]; 
                        }
                        else {
                            if (temp == agentDef){
                                newNumTurns = noOfTurns[i][j]; 
                            }
                            else {
                                newNumTurns = noOfTurns[i][j] + 1;
                            }  
                        }

                        if (newNumTurns < noOfTurns[i + rowPosMovs[k]][j + colPosMovs[k]]){
                            noOfTurns[i + rowPosMovs[k]][j + colPosMovs[k]] = newNumTurns; 
                            prevMove[i + rowPosMovs[k]][j + colPosMovs[k]] = temp;
                            prevOrthMove[i + rowPosMovs[k]][j + colPosMovs[k]] = orthTemp;
                        
                            path = new ArrayList<Point>();
                            for (Point p: fullPath[i][j]){
                                path.add(p);
                            }
                            path.add(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));
                            fullPath[i + rowPosMovs[k]][j + colPosMovs[k]] = path; 

                            multiSetPath.remove(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));
                            multiSetMove.remove(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));

                            q.add(new Node(i + rowPosMovs[k], j + colPosMovs[k], 0)); 
                        }
                        else if (newNumTurns == noOfTurns[i + rowPosMovs[k]][j + colPosMovs[k]]){
                            path = new ArrayList<Point>();
                            for (Point p: fullPath[i][j]){
                                path.add(p);
                            }

                            if (!path.contains(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1))){
                                if (multiSetPath.containsKey(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1))){  
                                    Map<Point, ArrayList<Point>> tPath = multiSetPath.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));
                                    Map<Point, MoveType[]> tMove = multiSetMove.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));
                                    MoveType[] b = new MoveType[2];
                                    b[0] = temp;
                                    b[1] = orthTemp;

                                    tMove.put(new Point(i+1, j+1),b);

                                    path.add(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));

                                    tPath.put(new Point(i+1, j+1),path);

                                    multiSetPath.put(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), tPath);
                                    multiSetMove.put(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), tMove);
                                }
                                else {
                                    Map<Point, ArrayList<Point>> tPath = new HashMap();
                                    Map<Point, MoveType[]> tMove = new HashMap();
                                    MoveType[] b = new MoveType[2];
                                    b[0] = temp;
                                    b[1] = orthTemp;

                                    tMove.put(new Point(i+1, j+1),b);

                                    path.add(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1));

                                    tPath.put(new Point(i+1, j+1),path);

                                    multiSetPath.put(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), tPath);
                                    multiSetMove.put(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), tMove);
                                }
                                if (oldSetPath.containsKey(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1))){
                                    if (!(multiSetPath.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1)).equals(oldSetPath.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1)))
                                        && multiSetMove.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1)).equals(oldSetMove.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1))))) {
                                        q.add(new Node(i + rowPosMovs[k], j + colPosMovs[k], 0));
                                        oldSetPath.replace(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), multiSetPath.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1)));
                                        oldSetMove.replace(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), multiSetMove.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1)));
                                    }
                                }
                                else {
                                    q.add(new Node(i + rowPosMovs[k], j + colPosMovs[k], 0));
                                    oldSetPath.put(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), multiSetPath.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1)));
                                    oldSetMove.put(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1), multiSetMove.get(new Point(i+rowPosMovs[k]+1, j+colPosMovs[k]+1)));
                                }
                            }
                                
                        }     
                    }     

                }
            }
        }
        return fullPath[(int) end.getX()-1][(int) end.getY()-1];
    }

    private boolean isValid(ChemicalCell grid[][], boolean visited[][], int i, int j) {
        return (i >= 0) && (i < size) && (j >= 0) && (j < size) && grid[i][j].isOpen() && !visited[i][j];
    }

    private boolean isAlmostValid(ChemicalCell grid[][], boolean visited[][], int i, int j) {
        return (i >= 0) && (i < size) && (j >= 0) && (j < size) && grid[i][j].isOpen() && visited[i][j];
    }

    private ArrayList<MoveType> getPosMoves(ChemicalCell grid[][], boolean visited[][], int i, int j, int[] r, int[] c) {
        ArrayList<MoveType>  temp = new ArrayList<>();

        for (int k = 0; k < 4; k++) {
            if (isValid(grid, visited, i + r[k], j + c[k]) || isAlmostValid(grid, visited, i + r[k], j + c[k])){
                if (r[k] == -1 && c[k] == 0){
                    temp.add(MoveType.N);
                }
                else if (r[k] == 1 && c[k] == 0){
                    temp.add(MoveType.S);
                }
                else if (r[k] == 0 && c[k] == -1){
                    temp.add(MoveType.W);
                }
                else if (r[k] == 0 && c[k] == 1){
                    temp.add(MoveType.E);
                }
            }
        }
        return temp;
    }


    private ArrayList<Map.Entry<Point, DirectionType>> getTurnsList() {

        ArrayList<Map.Entry<Point, DirectionType>> turns = new ArrayList<>();

        //edge case
        if(shortestPath.size() < 2) {
            return turns;
        }

        //first direction
        DirectionType lastDir = findDirection(shortestPath.get(0), shortestPath.get(1));
        turns.add(new AbstractMap.SimpleEntry(shortestPath.get(0), lastDir));

        //for each point, determine direction to next point
        for(int i = 1; i < shortestPath.size()-1; i++) {
            DirectionType curDir = findDirection(shortestPath.get(i), shortestPath.get(i+1));

            //if the direction changed, record the point and the new direction
            if(lastDir != curDir) {
                turns.add(new AbstractMap.SimpleEntry(shortestPath.get(i), curDir));
                lastDir = curDir;
            }
        }

        return turns;
    }

    //from start and end points, determine direction
    private DirectionType findDirection(Point start, Point end) {
        //up
        if(start.getX() < end.getX()) {
            return DirectionType.SOUTH;
        }

        //down
        if(start.getX() > end.getX()) {
            return DirectionType.NORTH;
        }

        //right
        if(start.getY() < end.getY()) {
            return DirectionType.EAST;
        }

        //left
        if(start.getY() > end.getY()) {
            return DirectionType.WEST;
        }

        return DirectionType.CURRENT;
    }

    private Point adjPoint(Point point, DirectionType dir) {
        Point adjacent = new Point();
        adjacent.x = point.x;
        adjacent.y = point.y;
        switch (dir) {
            case NORTH:
                adjacent.x = point.x - 1;
                break;
            case SOUTH:
                adjacent.x = point.x + 1;
                break;
            case EAST:
                adjacent.y = point.y + 1;
                break;
            case WEST:
                adjacent.y = point.y - 1;
                break;
        }
        simPrinter.println("Adj: " + adjacent.toString());
        return adjacent;
    }

    private MoveType getOppositeMove(MoveType moveType) {
        switch (moveType) {
            case N:
                return MoveType.S;
            case S:
                return MoveType.N;
            case W:
                return MoveType.E;
            case E:
                return MoveType.W;
            default:
                simPrinter.println("ERROR in getOppositeMove: returning default MoveType.S");
                return MoveType.S;
        }
    }

    private int getPathCost(ArrayList<Point> path) {

        ArrayList<Map.Entry<Point, DirectionType>> turns = new ArrayList<>();

        //edge case
        if(path.size() < 2) {
            return turns.size();
        }

        //first direction
        DirectionType lastDir = findDirection(path.get(0), path.get(1));
        turns.add(new AbstractMap.SimpleEntry(path.get(0), lastDir));

        //for each point, determine direction to next point
        for(int i = 1; i < path.size()-1; i++) {
            DirectionType curDir = findDirection(path.get(i), path.get(i+1));

            //if the direction changed, record the point and the new direction
            if(lastDir != curDir) {
                turns.add(new AbstractMap.SimpleEntry(path.get(i), curDir));
                lastDir = curDir;
            }
        }

        return turns.size();
    }

    private class Node {

        // (x, y) represents matrix cell coordinates
        // dist represent its minimum distance from the source
        private int x;
        private int y; 
        private int dist;

        Node(int x, int y, int dist) {
            this.x = x;
            this.y = y;
            this.dist = dist;
        }
    }

    private void printColorMap(Map<DirectionType, ChemicalCell.ChemicalType> chemDirs) {
        for (Map.Entry<DirectionType, ChemicalCell.ChemicalType> chemDir: chemDirs.entrySet()) {
            simPrinter.println(chemDir.getKey().toString() + ": " + chemDir.getValue().toString());
        }
    }
}