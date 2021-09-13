package chemotaxis.g1; // TODO modify the package name to reflect your team

import java.awt.Point;
import java.util.*;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;

public class Controller extends chemotaxis.sim.Controller {

    private Integer currApplication; // keep track of where chemicals have been applied on path
    private Integer totalChemicals; // total number of chemicals
    private List<Point> myPath;
    private List<Point> myTurnPath;
    private List<ChemicalCell.ChemicalType> turnSignals;
    private Map<Point, List<Point>> myAllKPaths;
    private boolean onPathToTarget;

    /**
     * Controller constructor
     *
     * @param start      start cell coordinates
     * @param target     target cell coordinates
     * @param size       grid/map size
     * @param simTime    simulation time
     * @param budget     chemical budget
     * @param seed       random seed
     * @param simPrinter simulation printer
     *
     */
    public Controller(Point start, Point target, Integer size, Integer simTime, Integer budget, Integer seed,
            SimPrinter simPrinter) {
        super(start, target, size, simTime, budget, seed, simPrinter);
        this.currApplication = 1;
        this.totalChemicals = 0;
        this.myTurnPath = new ArrayList<>();
        this.myPath = new ArrayList<>();
        this.myAllKPaths = new HashMap<>();
        this.onPathToTarget = false;
        this.turnSignals = new ArrayList<>();

    }

    /**
     * Apply chemicals to the map
     *
     * @param currentTurn        current turn in the simulation
     * @param chemicalsRemaining number of chemicals remaining
     * @param currentLocation    current location of the agent
     * @param grid               game grid/map
     * @return a cell location and list of chemicals to apply
     *
     */
    @Override
    public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, Point currentLocation,
            ChemicalCell[][] grid) {
        if (currentTurn == 1) {
            this.totalChemicals = chemicalsRemaining;
            this.myAllKPaths = getAllPathsFromTarget(grid, this.totalChemicals);
            this.myPath = getOptimalPath(this.start, grid, this.myAllKPaths, totalChemicals, true);
            this.myTurnPath = getTurns(this.myPath);
            this.turnSignals.add(ChemicalType.BLUE);
        }


        ChemicalPlacement chemicalPlacement = new ChemicalPlacement();

        List<ChemicalType> chemicals = new ArrayList<>();
        chemicals.add(turnSignals.get(0));

        if(!isPathsEquivalent(this.myPath, getOptimalPath(currentLocation, grid,
                this.myAllKPaths, chemicalsRemaining, false)) &&
                currentTurn != 1 && chemicals.get(0).equals(ChemicalType.BLUE) && this.onPathToTarget) {
            this.myPath = getOptimalPath(currentLocation, grid, this.myAllKPaths, chemicalsRemaining, false);
            this.myTurnPath = getTurns(this.myPath);
        }

        // If not enough chemicals to direct to the end, we assume that the agent is randomly roaming.
        // This checks if current cell can get to the end with the number of chemicals currently.
        // If so, then we start directing it to the end.
        if (!this.myTurnPath.isEmpty() && this.totalChemicals < this.myTurnPath.size()
                && !this.onPathToTarget && this.myAllKPaths.keySet().contains(currentLocation)
                && getTurns(this.myAllKPaths.get(currentLocation)).size() <= chemicalsRemaining) {
            this.myTurnPath = getTurns(this.myAllKPaths.get(currentLocation));
            this.onPathToTarget = true;
        }
        if (!myTurnPath.isEmpty() && chemicalsRemaining >= this.myTurnPath.size()) {
            if (closeToTurn(currentLocation, this.myTurnPath)) {
                chemicalPlacement.chemicals = chemicals;
                chemicalPlacement.location = this.myTurnPath.get(0);
                this.myTurnPath.remove(0);
                this.onPathToTarget = true;
                //turnSignals.remove(0);
            }
        }
        else if(!myTurnPath.isEmpty() && chemicalsRemaining < this.myTurnPath.size() && !this.onPathToTarget
                && chemicalsRemaining > 2*this.myTurnPath.size()/3) {
            this.myTurnPath = removeCornerTurns(myTurnPath, grid, chemicalsRemaining);
            if (closeToTurn(currentLocation, this.myTurnPath)) {
                chemicalPlacement.chemicals = chemicals;
                chemicalPlacement.location = this.myTurnPath.get(0);
                this.myTurnPath.remove(0);
                this.onPathToTarget = true;
                //turnSignals.remove(0);
            }
        }

        return chemicalPlacement;
    }

    private List<Point> removeCornerTurns(List<Point> turns, ChemicalCell[][] grid, Integer chemicalsLeft) {
        List<Point> res = new ArrayList<>();
        for(Point ele : turns) {
            if(grid[ele.x+1][ele.y].isBlocked() || grid[ele.x-1][ele.y].isBlocked()
                    || grid[ele.x][ele.y+1].isBlocked() || grid[ele.x][ele.y-1].isBlocked()) res.add(ele);
        }
        Collections.shuffle(res);
        for(Point ele : res) {
            if(ele != turns.get(0) && turns.size() > chemicalsLeft) turns.remove(ele);
        }
        return turns;
    }

    private Boolean isDiagonalPath(int k) {
        if (k + 1 < myTurnPath.size()) {
            if (myTurnPath.get(k).x == myTurnPath.get(k + 1).x || myTurnPath.get(k).y == myTurnPath.get(k + 1).y) {
                if (Math.abs(myTurnPath.get(k).x - myTurnPath.get(k + 1).x) == 1.0
                        || Math.abs(myTurnPath.get(k).y - myTurnPath.get(k + 1).y) == 1.0) {
                    //System.out.println("TRUE");
                    return true;
                }
            }
            //System.out.println("FALSE");
        }
        return false;
    }

    private Boolean closeToTurn(Point cur, List<Point> turnPath) {
        if (this.turnSignals.get(0) == ChemicalType.BLUE) {
            for (Point point : turnPath) {
                if (point.x == cur.x && Math.abs(point.y - cur.y) == 1)
                    return true;
                else if (point.y == cur.y && Math.abs(point.x - cur.x) == 1)
                    return true;
            }
        } else if (this.turnSignals.get(0) == ChemicalType.RED) {
            for (Point point : turnPath) {
                if (Math.abs(point.x - cur.x) == 1 && Math.abs(point.y - cur.y) == 1)
                    return true;
            }
        }
        return false;
    }

    private Boolean isPathsEquivalent(List<Point> original, List<Point> new_path) {
        List<Point> p1 = new ArrayList<>(original);
        List<Point> p2 = new ArrayList<>(new_path);

        //System.out.println("ORIGINAL: " + p1 + "\n");
        //System.out.println("NEW: " +p2 + "\n");
        if (!p1.isEmpty() && !p2.isEmpty()) {
            Set<Point> set1 = new LinkedHashSet<>(p1);
            Set<Point> set2 = new LinkedHashSet<>(p2);
            p1.clear();
            p1.addAll(set1);
            p2.clear();
            p2.addAll(set2);
            p1.remove(this.start);
            p2.remove(this.start);
            //p1.remove(0);
            //p2.remove(0);
            if (p1.size() != p2.size()-1) {
                if (!p1.contains(p2.get(0))) return false;
                if (p1.size() > p2.size()) {
                    if (!p1.contains(p2.get(0)))
                        return false;
                    else {
                        for (int i = 0; i < p1.indexOf(p2.get(0)); i++) {
                            p1.remove(i);
                        }
                    }
                } else if(p2.size() > p1.size()) {
                    if (!p2.contains(p1.get(0)))
                        return false;
                    else {
                        for (int i = 0; i < p2.indexOf(p1.get(0)); i++) {
                            p2.remove(i);
                        }
                    }
                }
            }
        }
        return true;
    }

    private List<Point> getTurns(List<Point> path) {
        List<Point> turnChemicalCells = new ArrayList<>();

        path.add(0, this.start);
        turnChemicalCells.add(path.get(1));

        for (int i = 2; i < path.size(); i++) {
            if (path.get(i - 2).x == path.get(i - 1).x && path.get(i - 1).x != path.get(i).x) {
                turnChemicalCells.add(path.get(i));
            } else if (path.get(i - 2).y == path.get(i - 1).y && path.get(i - 1).y != path.get(i).y) {
                turnChemicalCells.add(path.get(i));
            }
        }
        Set<Point> set = new LinkedHashSet<>(turnChemicalCells);
        turnChemicalCells.clear();
        turnChemicalCells.addAll(set);
        turnChemicalCells.remove(this.start);
        return turnChemicalCells;
    }

    class Node {
        int x;
        int y;
        int turns;
        String dir;
        Node prev;

        public Node() {
            this.x = 0;
            this.y = 0;
        }

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Node(int x, int y, int turns, String dir) {
            this.x = x;
            this.y = y;
            this.turns = turns;
            this.dir = dir;
        }
    }

    /**
     * Get shortest path from given start to target using BFS
     *
     * @param s    the start point
     * @param grid game grid/map
     * @return list of points showing shortest path from start to end
     *
     */
    private List<Point> getShortestPath(Point s, ChemicalCell[][] grid) {
        Queue<Node> queue = new LinkedList<Node>();
        boolean[][] visited = new boolean[grid.length][grid[0].length];
        Node start = new Node((int) s.getX(), (int) s.getY());
        queue.add(start);
        List<Point> path = new ArrayList<Point>();

        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            if (cur.x == target.x && cur.y == target.y) {
                while (cur != null) {
                    path.add(new Point(cur.x, cur.y));
                    cur = cur.prev;
                }
                Collections.reverse(path);
                break;
            }
            if (!visited[cur.x - 1][cur.y - 1]) {
                for (Node neighbor : getNeighbors(cur, grid, false))
                    if (!visited[neighbor.x - 1][neighbor.y - 1])
                        queue.add(neighbor);
                visited[cur.x - 1][cur.y - 1] = true;
            }
        }

        return path;
    }

    private List<Point> getOptimalPath(Point s, ChemicalCell[][] grid, Map<Point, List<Point>> allKTargetPaths,
            int nChemicals, boolean useTurns) {
        Node start = new Node((int) s.getX(), (int) s.getY());
        List<Point> finalPath = new ArrayList<Point>();

        for (Node neighbor : getNeighbors(start, grid, true)) {
            List<Point> directedPath = getOptimalDirectedPath(new Point(neighbor.x, neighbor.y), grid, allKTargetPaths,
                    nChemicals, useTurns);
            if (finalPath.isEmpty())
                finalPath = directedPath;
            else {
                if (getTurns(directedPath).size() <= nChemicals && finalPath.size() - 1 > directedPath.size())
                    finalPath = directedPath;
                if (getTurns(finalPath).size() - 1 > getTurns(directedPath).size())
                    finalPath = directedPath;
            }
        }

        if(!useTurns) finalPath = getOptimalDirectedPath(s, grid, allKTargetPaths, nChemicals, useTurns);
        ////System.out.println("FINAL: \n" + finalPath + "\n \n");
        Set<Point> set = new LinkedHashSet<>(finalPath);
        finalPath.clear();
        finalPath.addAll(set);
        return finalPath;
    }

    private List<Point> getOptimalDirectedPath(Point s, ChemicalCell[][] grid, Map<Point, List<Point>> allKTargetPaths,
            int nChemicals, boolean useTurns) {
        Queue<Node> queue = new LinkedList<Node>();
        boolean[][] visited = new boolean[grid.length][grid[0].length];
        Node start = new Node((int) s.getX(), (int) s.getY());
        queue.add(start);
        List<List<Point>> allPaths = new ArrayList<>();
        List<Point> finalPath = new ArrayList<Point>();
        List<Node> startNeighbors = new ArrayList<>();

        startNeighbors = getNeighbors(new Node(this.start.x, this.start.y), grid, useTurns);

        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            List<Point> path = new ArrayList<Point>();
            if (cur.x == target.x && cur.y == target.y) {
                Node tmp = cur;
                while (cur != null) {
                    path.add(new Point(cur.x, cur.y));
                    cur = cur.prev;
                }
                cur = tmp;
                Collections.reverse(path);
                if (!allPaths.contains(path)) {
                    allPaths.add(path);
                    for (List<Point> tmpPath : allPaths) {
                        if (finalPath.isEmpty())
                            finalPath = tmpPath;
                        else {
                            if (getTurns(tmpPath).size() <= nChemicals && finalPath.size() > tmpPath.size())
                                finalPath = tmpPath;
                            if (getTurns(tmpPath).size() < getTurns(finalPath).size())
                                finalPath = tmpPath;
                        }
                    }
                }
            }
            if (!visited[cur.x - 1][cur.y - 1]) {
                List<Node> neighbors = getNeighbors(cur, grid, useTurns);
                for (Node neighbor : neighbors) {
                    if((neighbor.x != this.start.x || neighbor.y != this.start.y) && !startNeighbors.contains(neighbor)
                    && !visited[neighbor.x - 1][neighbor.y - 1])
                        queue.add(neighbor);
                }
                visited[cur.x - 1][cur.y - 1] = true;
            }
        }

        // //System.out.println(getTurns(finalPath) + "\n \n");
        return finalPath;
    }

    /**
     * Get available neighbors of a cell
     *
     * @param cur  current cell represented by Node
     * @param grid game grid/map
     * @return list of points showing shortest path from start to end
     *
     */
    private List<Node> getNeighbors(Node cur, ChemicalCell[][] grid, boolean useTurns) {
        List<Node> neighbors = new ArrayList<Node>();
        int x = (int) cur.x - 1;
        int y = (int) cur.y - 1;
        if (x > 0 && grid[x - 1][y].isOpen()) {
            Node next = new Node(x, y + 1);
            if (useTurns) {
                next.dir = "UP";
                next.turns = cur.turns;
                if (!next.dir.equals(cur.dir))
                    next.turns++;
            }
            next.prev = cur;
            neighbors.add(next);
        }
        if (y > 0 && grid[x][y - 1].isOpen()) {
            Node next = new Node(x + 1, y);
            if (useTurns) {
                next.dir = "LEFT";
                next.turns = cur.turns;
                if (!next.dir.equals(cur.dir))
                    next.turns++;
            }
            next.prev = cur;
            neighbors.add(next);
        }
        if (x < grid.length - 1 && grid[x + 1][y].isOpen()) {
            Node next = new Node(x + 2, y + 1);
            if (useTurns) {
                next.dir = "DOWN";
                next.turns = cur.turns;
                if (!next.dir.equals(cur.dir))
                    next.turns++;
            }
            next.prev = cur;
            neighbors.add(next);
        }
        if (y < grid.length - 1 && grid[x][y + 1].isOpen()) {
            Node next = new Node(x + 1, y + 2);
            if (useTurns) {
                next.dir = "RIGHT";
                next.turns = cur.turns;
                if (!next.dir.equals(cur.dir))
                    next.turns++;
            }
            next.prev = cur;
            neighbors.add(next);
        }

        Comparator<Node> turnsComparatorWon = (Node n1, Node n2) -> {
            return (n1.turns) - (n2.turns);
        };

        if(useTurns) Collections.sort(neighbors, turnsComparatorWon);

        return neighbors;
    }

    /**
     * Get all paths from target to any cell within a given number of turns
     *
     * @param grid  game grid/map
     * @param turns number of turns allowed
     * @return list of points showing shortest path from start to end
     *
     */
    private Map<Point, List<Point>> getAllPathsFromTarget(ChemicalCell[][] grid, Integer turns) {
        Queue<Node> queue = new LinkedList<Node>();
        boolean[][] visited = new boolean[grid.length][grid[0].length];
        Node start = new Node((int) this.target.getX(), (int) this.target.getY(), 0, "");
        queue.add(start);
        Map<Point, List<Point>> allPaths = new HashMap<Point, List<Point>>();
        int curNumCells = 0;

        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            Point curPoint = new Point(cur.x, cur.y);

            if (cur.turns > 0 && cur.turns <= turns && !allPaths.containsKey(curPoint)) {
                List<Point> path = new ArrayList<Point>();
                Node temp = cur;
                while (cur != null) {
                    path.add(new Point(cur.x, cur.y));
                    cur = cur.prev;
                }
                path.remove(0);
                allPaths.put(curPoint, path);
                cur = temp;
            }

            if (!visited[cur.x - 1][cur.y - 1]) {
                for (Node neighbor : getNeighbors(cur, grid, true))
                    if (!visited[neighbor.x - 1][neighbor.y - 1])
                        queue.add(neighbor);
                visited[cur.x - 1][cur.y - 1] = true;
            }
        }
        // visualize(grid, allPaths);

        return allPaths;
    }

    /**
     * Visualize the results of the reverse BFS
     *
     * @param grid                game grid/map
     * @param allPaths            the paths resulting from reverse bfs
     *
     */
    private void visualize(ChemicalCell[][] grid, Map<Point, List<Point>> allPaths) {
        //System.out.println("~~~~~~~~");
        for (int i=0; i<grid.length; i++){
            for (int j=0; j<grid[0].length; j++){
                Point temp = new Point(i+1,j+1);
                if (grid[i][j].isBlocked())
                    System.out.print("B  ");
                else if (allPaths.keySet().contains(temp))
                    System.out.print("X  ");
                else if (this.target.equals(temp))
                    System.out.print("G  ");
                else
                    System.out.print("-  ");
            }
            //System.out.println();
        }
        //System.out.println("~~~~~~~~");
    }

    // Testing the BFS (run with "java chemotaxis/g1/Controller" in /src)
    public static void main(String[] args) {
        Point start = new Point(1, 1);
        Point target = new Point(5, 5);
        int size = 5;
        int simTime = 0;
        int budget = 50;
        int seed = 0;
        SimPrinter simPrinter = new SimPrinter(true);
        Controller test = new Controller(start, target, size, simTime, budget, seed, simPrinter);
        ChemicalCell closed = new ChemicalCell(false);
        ChemicalCell open = new ChemicalCell(true);
        ChemicalCell[][] grid = new ChemicalCell[size][size];
        grid[0][0] = open;
        grid[0][1] = open;
        grid[0][2] = open;
        grid[0][3] = closed;
        grid[0][4] = open;
        grid[1][0] = open;
        grid[1][1] = closed;
        grid[1][2] = closed;
        grid[1][3] = closed;
        grid[1][4] = open;
        grid[2][0] = open;
        grid[2][1] = open;
        grid[2][2] = open;
        grid[2][3] = open;
        grid[2][4] = open;
        grid[3][0] = open;
        grid[3][1] = closed;
        grid[3][2] = open;
        grid[3][3] = closed;
        grid[3][4] = closed;
        grid[4][0] = open;
        grid[4][1] = closed;
        grid[4][2] = open;
        grid[4][3] = open;
        grid[4][4] = open;

        List<Point> path = test.getShortestPath(start, grid);
        //System.out.println("START: 1, 1");
        //System.out.println("END: 5, 5");
        //System.out.println("SHORTEST PATH:");
        //System.out.println(path);

        int turns = 4;
        Map<Point, List<Point>> allPaths = test.getAllPathsFromTarget(grid, turns);
        // //System.out.println(allPaths);
        //System.out.println("~~~~~~");

        int[][] grid2 = new int[size][size];
        grid2[0][0] = 0;
        grid2[0][1] = 0;
        grid2[0][2] = 0;
        grid2[0][3] = 2;
        grid2[0][4] = 0;
        grid2[1][0] = 0;
        grid2[1][1] = 2;
        grid2[1][2] = 2;
        grid2[1][3] = 2;
        grid2[1][4] = 0;
        grid2[2][0] = 0;
        grid2[2][1] = 0;
        grid2[2][2] = 0;
        grid2[2][3] = 0;
        grid2[2][4] = 0;
        grid2[3][0] = 0;
        grid2[3][1] = 2;
        grid2[3][2] = 0;
        grid2[3][3] = 2;
        grid2[3][4] = 2;
        grid2[4][0] = 0;
        grid2[4][1] = 2;
        grid2[4][2] = 0;
        grid2[4][3] = 0;
        grid2[4][4] = 0;
        //System.out.println("GRID BEFORE (2's are blocked cells and 0's are free cells):");
        for (int[] row : grid2) {
            for (int col : row)
                System.out.print(col + "  ");
            //System.out.println();
        }
        //System.out.println("~~~~~~");

        //System.out.println("GRID AFTER (8's are cells that can reach end given " + turns + " turns):");
        for (Point p : allPaths.keySet()) {
            grid2[p.x - 1][p.y - 1] = 8;
        }
        for (int[] row : grid2) {
            for (int col : row)
                System.out.print(col + "  ");
            //System.out.println();
        }

    }
}
