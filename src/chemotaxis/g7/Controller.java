package chemotaxis.g7;

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.DirectionType;
import chemotaxis.sim.SimPrinter;

public class Controller extends chemotaxis.sim.Controller {
    // the directions are 4 directions which each is 1-step away from x, y and has a directionType
    public static final ArrayList<MoveDirection> directions = new ArrayList<MoveDirection>();
    static {
        directions.add(new MoveDirection(1, 0, DirectionType.EAST));
        directions.add(new MoveDirection(-1, 0, DirectionType.WEST));
        directions.add(new MoveDirection(0, 1, DirectionType.NORTH));
        directions.add(new MoveDirection(0, -1, DirectionType.SOUTH));
    }

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

    // TODO: weigh the points, not must be the closest point to the target, we can give this function another name like chooseOnePoint
    // I think for now we can just choose the point which is the closest of all the points that need to turn
    // and maybe in the future, we can take chemicals diffusion into consideration
    public int closestToTarget(ArrayList<Point> locations) {
        int closestDistance = 9999999;
        int closestIdx = 0;
        for(int i = 0; i < locations.size(); i++) {
            int x = locations.get(i).x;
            int y = locations.get(i).y;
            int distance = Math.abs(x - this.target.x) + Math.abs(y - this.target.y);
            if(distance > 0 && distance < closestDistance) {
                closestIdx = i;
                closestDistance = distance;
            }
        }
        return closestIdx;
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
    // TODO choose one point and put the chemicals(G:left, R:right, B:attract) to guide it
    @Override
    public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid) {
        ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
        // choose a point that needs to turn and apply the chemicals
        int closestIdx = this.closestToTarget(locations);
        Point currentLocation = locations.get(closestIdx);
        int currentX = currentLocation.x;
        int currentY = currentLocation.y;

        int leftEdgeX = Math.max(1, currentX - 5);
        int rightEdgeX = Math.min(size, currentX + 5);
        int topEdgeY = Math.max(1, currentY - 5);
        int bottomEdgeY = Math.min(size, currentY + 5);

        int randomX = this.random.nextInt(rightEdgeX - leftEdgeX + 1) + leftEdgeX;
        int randomY = this.random.nextInt(bottomEdgeY - topEdgeY + 1) + topEdgeY ;

        List<ChemicalType> chemicals = new ArrayList<>();
        // TODO: now we only use BLUE which means attracting, maybe in the future, we can use different types
        chemicals.add(ChemicalType.BLUE);

        chemicalPlacement.location = new Point(randomX, randomY);
        chemicalPlacement.chemicals = chemicals;

        return chemicalPlacement;
    }

    // use queue to do DFS shortest path search
    // search from current position to Controller.target, select a path which has the least turns
    private Node getShortestPathLeastTurns(Point start, ChemicalCell[][] grid) {
        Queue<Node> deque = new LinkedList<>();
        int m = grid.length;
        int n = grid[0].length;
        // to remember which points have been visited and never go back
        boolean[][] visited = new boolean[m][n];
        visited[start.x][start.y] = true;

        Node startNode = new Node(start);
        deque.add(startNode);
        // stores result
        Node result = new Node(start, Integer.MAX_VALUE);

        while (!deque.isEmpty()) {
            Node node = deque.remove();
            // if arriving at target, we choose the shortest path with the least turns
            if (node.currentPosition == target) {
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
            for (MoveDirection direction : directions) {
                int new_x = currentPosition.x + direction.dx;
                int new_y = currentPosition.y + direction.dy;
                if (validCell(new_x, new_y, visited, grid)) {
                    visited[new_x][new_y] = true;
                    Point newPosition = new Point(new_x, new_y);
                    ArrayList<Point> newPath = node.getPath();
                    int turns = node.getTurns();
                    newPath.add(newPosition);
                    if (node.directionType!= direction.directionType) {
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

        public Node(Point currentPosition) {
            this.currentPosition = currentPosition;
            this.path = new ArrayList<Point>();
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
}

