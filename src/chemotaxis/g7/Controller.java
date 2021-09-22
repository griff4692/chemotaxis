package chemotaxis.g7;

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import chemotaxis.sim.*;
import chemotaxis.sim.ChemicalCell.ChemicalType;

public class Controller extends chemotaxis.sim.Controller {
    // the directions are 4 directions which each is 1-step away from x, y and has a directionType
    public static final ArrayList<MoveDirection> directions = new ArrayList<MoveDirection>();
    static {
        directions.add(new MoveDirection(0, 1, DirectionType.EAST));
        directions.add(new MoveDirection(0, -1, DirectionType.WEST));
        directions.add(new MoveDirection(-1, 0, DirectionType.NORTH));
        directions.add(new MoveDirection(1, 0, DirectionType.SOUTH));
    }

    public static final Map<String, Integer> turnDirections = new HashMap<>();
    static {
        turnDirections.put("LEFT", 1);
        turnDirections.put("RIGHT", 2);
        turnDirections.put("ATTRACT", 3);
    }

    ArrayList<Point> previousLocations = new ArrayList<Point>();
    /**
     * Controller constructor
     *
     * @param start       start cell coordinates
     * @param target      target cell coordinates
     * @param size     	  grid/map size
     * @param grid        game grid/map
     * @param simTime     simulation time
     * @param budget      chemical budget
     * @param seed        random seed
     * @param simPrinter  simulation printer
     *
     */
    public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
        super(start, target, size, grid, simTime, budget, seed, simPrinter);
    }

    // weigh the points, not must be the closest point to the target
    // I think for now we can just choose the point which is the closest of all the points that need to turn
    // and maybe in the future:
    // we can use two chemicals at the same point to guide many agents
    // we can take chemicals diffusion into consideration
    public ArrayList<Integer> chooseOnePointNeedToTurn(ArrayList<Point> locations, ChemicalCell[][] grid) {
        int chooseIdx = -1;
        int distance = Integer.MAX_VALUE;
        int turnDirection = -1;
        int nextX = -1;
        int nextY = -1;

        ArrayList<Integer> result = new ArrayList<Integer>();

        for (int i = 0; i < locations.size(); i ++) {
            Point location = new Point(locations.get(i).x - 1, locations.get(i).y - 1);
            int numberOfAvailableNeighbours = findNumberOfAvailableNeighbours(location,grid);
            Log.writeToLogFile("Agent"+(i)+" number of available neighbours:"+numberOfAvailableNeighbours);
            if (location.x == target.x - 1 && location.y == target.y - 1)
                continue;

            DirectionType beforeDirection = DirectionType.CURRENT;
            // if currentLocation not equal to start
            if (location.x != start.x - 1 || location.y != start.y - 1) {
                // debug
                if (i >= previousLocations.size()) {
                    System.out.println(i);
                    System.out.println(previousLocations);
                    System.out.println(locations);
                } else {
                    Point previousLocation = new Point(previousLocations.get(i).x - 1, previousLocations.get(i).y - 1);
                    beforeDirection = this.getMoveDirections(previousLocation, location);
                }
            }

            // get the shortest path and the next node position
            Node node = this.getShortestPathLeastTurns(location, grid, beforeDirection);
            ArrayList<Point> path = node.getPath();
            Point nextPosition = path.get(1);
            DirectionType nowDirection = this.getMoveDirections(location, nextPosition);
            if (nowDirection != beforeDirection) {
                // if we make sure that the agent can turn itself, we can put no chemical
                if (beforeDirection != DirectionType.CURRENT) {
                    if (!isOppositeDirection(beforeDirection, nowDirection) && numberOfAvailableNeighbours == 2)
                        continue;

                    if (isOppositeDirection(beforeDirection, nowDirection) && numberOfAvailableNeighbours == 3)
                        continue;
                }

                if (distance > path.size() - 1) {
                    distance = path.size() - 1;
                    chooseIdx = i;
                    turnDirection = this.getChemicalType(beforeDirection, nowDirection);
                    nextX = nextPosition.x + 1;
                    nextY = nextPosition.y + 1;
                }
            }
        }
        if (chooseIdx != -1) {
            result.add(chooseIdx);
            result.add(turnDirection);
            result.add(nextX);
            result.add(nextY);
        }
        return result;
    }

    public int findNumberOfAvailableNeighbours(Point currentAgent,ChemicalCell[][] grid){
        int numberOfAvailableNeighbours = 0;
        if (currentAgent.y+1<grid.length && grid[currentAgent.x][currentAgent.y+1].isOpen()){
            numberOfAvailableNeighbours += 1;
        }
        if (currentAgent.y-1>=0 && grid[currentAgent.x][currentAgent.y-1].isOpen()){
            numberOfAvailableNeighbours += 1;
        }
        if (currentAgent.x+1<grid.length && grid[currentAgent.x+1][currentAgent.y].isOpen()){
            numberOfAvailableNeighbours += 1;
        }
        if (currentAgent.x-1>=0 && grid[currentAgent.x-1][currentAgent.y].isOpen()){
            numberOfAvailableNeighbours += 1;
        }
        return numberOfAvailableNeighbours;
    }
    /**
     * Apply chemicals to the map
     *
     * @param currentTurn         current turn in the simulation
     * @param chemicalsRemaining  number of chemicals remaining
     * @param locations     current locations of the agents
     * @param grid                game grid/map
     * @return                    a cell location and list of chemicals to apply
     *
     */
    // choose one point and put the chemicals(G:left, R:right, B:attract) to guide it
    // In the beginning, we use blue to guide the agent in the right direction, in the further process, we use G/R to guide it
    // As a result, for the agent which is at the start point, the priority is B > G = R
    // in the afterwards, the priority is G = R > B
    @Override
    public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid) {
        ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
        // choose a point that needs to turn and apply the chemicals
        ArrayList<Integer> result = this.chooseOnePointNeedToTurn(locations, grid);
        System.out.println("the result of applyChemicals are" + result.toString());
        if (result.isEmpty()) {
            previousLocations = locations;
            return chemicalPlacement;
        }

        int chooseIdx = result.get(0);
        int turnDirection = result.get(1);
        int nextX = result.get(2);
        int nextY = result.get(3);
        Point nowLocation = locations.get(chooseIdx);

        List<ChemicalCell.ChemicalType> chemicals = new ArrayList<>();

        switch (turnDirection) {
            case 1:
                chemicals.add(ChemicalCell.ChemicalType.GREEN);
                chemicalPlacement.location = new Point(nowLocation.x, nowLocation.y);
                break;
            case 2:
                chemicals.add(ChemicalCell.ChemicalType.RED);
                chemicalPlacement.location = new Point(nowLocation.x, nowLocation.y);
                break;

            case 3:
                chemicals.add(ChemicalCell.ChemicalType.BLUE);
                chemicalPlacement.location = new Point(nextX, nextY);
                break;
        }

        chemicalPlacement.chemicals = chemicals;
        previousLocations = locations;
        return chemicalPlacement;
    }

    // use queue to do BFS to find the shortest path from current position to the target
    // and if the length of the path is the same , we select a path which has the least turns,
    // and we will also take previous DirectionType into consideration so as make sure not to make extra turns
    private Node getShortestPathLeastTurns(Point start, ChemicalCell[][] grid, DirectionType previousDirection) {
        ArrayList<MoveDirection> newDirections = sortDirections(directions, previousDirection);
        Queue<Node> deque = new LinkedList<>();
        int m = grid.length;
        int n = grid[0].length;
        // to remember which points have been visited and never go back
        boolean[][] visited = new boolean[m][n];
        visited[start.x][start.y] = true;
        ArrayList<Point> path = new ArrayList<Point>();
        path.add(start);

        Node startNode = new Node(start, path);
        deque.add(startNode);
        // stores result
        Node result = new Node(start, Integer.MAX_VALUE);

        while (!deque.isEmpty()) {
            Node node = deque.remove();
            // if arriving at target, we choose the shortest path with the least turns
            if (node.currentPosition.x == target.x - 1 && node.currentPosition.y == target.y - 1) {
                if (result.getTurns() == Integer.MAX_VALUE || result.getPath().size() > node.getPath().size()) {
                    result.setTurns(node.getTurns());
                    result.setPath(node.getPath());
                } else if (result.getPath().size() == node.getPath().size()) {
                    if (result.getTurns() > node.getTurns()) {
                        result.setTurns(node.getTurns());
                        result.setPath(node.getPath());
                    }
                }
                continue;
            }

            Point currentPosition = node.getCurrentPosition();
            for (MoveDirection direction : newDirections) {
                int new_x = currentPosition.x + direction.dx;
                int new_y = currentPosition.y + direction.dy;
                // we always set the target to be false because it can be visited several times
                if (new_x == target.x - 1 && new_y == target.y - 1) {
                    visited[new_x][new_y] = false;
                }
                if (this.validCell(new_x, new_y, visited, grid)) {
                    visited[new_x][new_y] = true;
                    Point newPosition = new Point(new_x, new_y);
                    ArrayList<Point> newPath = (ArrayList<Point>) node.getPath().clone();
                    int turns = node.getTurns();
                    newPath.add(newPosition);
                    if (node.directionType != DirectionType.CURRENT && node.directionType != direction.directionType) {
                        turns += 1;
                    }
                    Node neighbor = new Node(newPosition, newPath, direction.directionType, turns);
                    deque.add(neighbor);
                }
            }
        }

        return result;
    }

    private boolean validCell(Integer x, Integer y, boolean[][] visited, ChemicalCell[][] grid) {
        int m = grid.length;
        int n = grid[0].length;
        return x >= 0 && x < m && y >= 0 && y < n && !visited[x][y] && !grid[x][y].isBlocked();
    }

    static class Node {
        Point currentPosition;
        ArrayList<Point> path;
        DirectionType directionType;
        Integer turns;

        public Node(Point currentPosition, ArrayList<Point> path) {
            this.currentPosition = currentPosition;
            this.path = path;
            this.directionType = DirectionType.CURRENT;
            this.turns = 0;
        }

        public Node(Point currentPosition, Integer turns) {
            this.currentPosition = currentPosition;
            this.path = new ArrayList<Point>();
            this.directionType = DirectionType.CURRENT;
            this.turns = turns;
        }

        public Node(Point currentPosition, ArrayList<Point> path, DirectionType directionType, Integer turns) {
            this.currentPosition = currentPosition;
            this.path = path;
            this.directionType = directionType;
            this.turns = turns;
        }

        public Point getCurrentPosition() {
            return currentPosition;
        }

        public void setCurrentPosition(Point currentPosition) {
            this.currentPosition = currentPosition;
        }

        public ArrayList<Point> getPath() {
            return path;
        }

        public void setPath(ArrayList<Point> path) {
            this.path = path;
        }

        public DirectionType getDirectionType() {
            return directionType;
        }

        public void setDirectionType(DirectionType directionType) {
            this.directionType = directionType;
        }

        public Integer getTurns() {
            return turns;
        }

        public void setTurns(Integer turns) {
            this.turns = turns;
        }
    }

    static class MoveDirection {
        int dx;
        int dy;
        DirectionType directionType;

        public MoveDirection(int dx, int dy, DirectionType directionType) {
            this.dx = dx;
            this.dy = dy;
            this.directionType = directionType;
        }
    }

    public DirectionType getMoveDirections(Point previousLocation, Point currentLocation) {
        for (MoveDirection moveDirection: directions) {
            int dx = moveDirection.dx;
            int dy = moveDirection.dy;
            if (currentLocation.x - previousLocation.x == dx && currentLocation.y - previousLocation.y == dy) {
                return moveDirection.directionType;
            }
        }
        // used to debug
        if (previousLocation.x != currentLocation.x || previousLocation.y != currentLocation.y) {
            System.out.println(previousLocation);
            System.out.println(currentLocation);
        }
        return DirectionType.CURRENT;
    }

    // Green: left: 1, Red: right: 2, Blue: attract : 3
    public int getChemicalType(DirectionType previousDirection, DirectionType nowDirection) {
        if (previousDirection == DirectionType.NORTH && nowDirection == DirectionType.WEST)
            return turnDirections.get("LEFT");

        if (previousDirection == DirectionType.WEST && nowDirection == DirectionType.SOUTH)
            return turnDirections.get("LEFT");

        if (previousDirection == DirectionType.SOUTH && nowDirection == DirectionType.EAST)
            return turnDirections.get("LEFT");

        if (previousDirection == DirectionType.EAST && nowDirection == DirectionType.NORTH)
            return turnDirections.get("LEFT");

        if (previousDirection == DirectionType.NORTH && nowDirection == DirectionType.EAST)
            return turnDirections.get("RIGHT");

        if (previousDirection == DirectionType.EAST && nowDirection == DirectionType.SOUTH)
            return turnDirections.get("RIGHT");

        if (previousDirection == DirectionType.SOUTH && nowDirection == DirectionType.WEST)
            return turnDirections.get("RIGHT");

        if (previousDirection == DirectionType.WEST && nowDirection == DirectionType.NORTH)
            return turnDirections.get("RIGHT");

        if (previousDirection == DirectionType.EAST && nowDirection == DirectionType.WEST)
            return turnDirections.get("ATTRACT");

        if (previousDirection == DirectionType.WEST && nowDirection == DirectionType.EAST)
            return turnDirections.get("ATTRACT");

        if (previousDirection == DirectionType.SOUTH && nowDirection == DirectionType.NORTH)
            return turnDirections.get("ATTRACT");

        if (previousDirection == DirectionType.NORTH && nowDirection == DirectionType.SOUTH)
            return turnDirections.get("ATTRACT");

        if (previousDirection == DirectionType.CURRENT && nowDirection != DirectionType.CURRENT)
            return turnDirections.get("ATTRACT");

        return 0;
    }

    private boolean isOppositeDirection(DirectionType previousDirection, DirectionType nowDirection) {
        if (previousDirection == DirectionType.WEST && nowDirection == DirectionType.EAST)
            return true;

        if (previousDirection == DirectionType.EAST && nowDirection == DirectionType.WEST)
            return true;

        if (previousDirection == DirectionType.NORTH && nowDirection == DirectionType.SOUTH)
            return true;

        if (previousDirection == DirectionType.SOUTH && nowDirection == DirectionType.NORTH)
            return true;

        return false;
    }

    private ArrayList<MoveDirection> sortDirections(ArrayList<MoveDirection> oldDirections,DirectionType previousDirection) {
        ArrayList<MoveDirection> newDirections = new ArrayList<MoveDirection>();
        for (MoveDirection moveDirection : oldDirections) {
            if (moveDirection.directionType == previousDirection)
                newDirections.add(0, moveDirection);
            else
                newDirections.add(moveDirection);

        }
        return newDirections;
    }
}

