package chemotaxis.g7;

import java.awt.Point;
import java.util.Collections;
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
        turnDirections.put("OPPOSITE", 3);
        turnDirections.put("ATTRACT", 4);
    }

    ArrayList<Point> previousLocations = new ArrayList<Point>();
    Map<Integer, Node> agentsPath = new HashMap<>();
    Node initialPath = new Node();

    /**
     * Controller constructor
     *
     * @param start      start cell coordinates
     * @param target     target cell coordinates
     * @param size       grid/map size
     * @param grid       game grid/map
     * @param simTime    simulation time
     * @param budget     chemical budget
     * @param seed       random seed
     * @param simPrinter simulation printer
     */
    public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter, Integer agentGoal, Integer spawnFreq) {
        super(start, target, size, grid, simTime, budget, seed, simPrinter, agentGoal, spawnFreq);
        // compute the shortest path form start to target in the beginning to save time
        this.initialAgentPath();
    }

    private void initialAgentPath() {
        Point startNode = new Point(start.x - 1, start.y - 1);
        initialPath = this.getFourDirectionShortestPath(startNode, grid, DirectionType.CURRENT);
    }

    // weigh the points, not must be the closest point to the target
    // I think for now we can just choose the point which is the closest of all the points that need to turn
    // we can take chemicals diffusion into consideration
    public ArrayList<Integer> chooseOnePointNeedToTurn(ArrayList<Point> locations, ChemicalCell[][] grid) {
        int chooseIdx = -1;
        int distance = Integer.MAX_VALUE;
        int turnDirection = -1;
        int nextX = -1;
        int nextY = -1;

        ArrayList<Integer> result = new ArrayList<Integer>();
        Map<Point, Integer> agentTurnDirections = new HashMap<>();
        int agentAlreadyInGoal = 0;
        for (Point location : locations) {
            if (location.equals(target)) {
                agentAlreadyInGoal += 1;
            }
        }
        // we can only care about (agentGoal + agentAlreadyInGoal) * 2 agents in case of wasting chemicals
        for (int i = 0; i < Math.min(locations.size(), agentGoal * 2 + agentAlreadyInGoal * 2); i++) {
            Point location = new Point(locations.get(i).x - 1, locations.get(i).y - 1);
            int numberOfAvailableNeighbours = findNumberOfAvailableNeighbours(location, grid);
            Log.writeToLogFile("Agent" + (i) + " number of available neighbours:" + numberOfAvailableNeighbours);

            // get expected path from agentPaths
            // we use agentPaths to store each agents' now path connected with supposed path instead of computing the shortest path every time
            Node beforeNode = new Node();
            if (location.x == target.x - 1 && location.y == target.y - 1) {
                continue;
            }

            if (agentsPath.containsKey(i)) {
                beforeNode = agentsPath.get(i);
            } else {
                // if agentsPath not contains i, then this agent must be a new agent
                if (location.x == start.x - 1 || location.y == start.y - 1) {
                    agentsPath.put(i, new Node(initialPath));
                    beforeNode = agentsPath.get(i);
                    if (beforeNode.getIndex() != -1) {
                        beforeNode.setIndex(-1);
                    }
                } else {
                    System.out.println("agentpath: some errors here! need debug");
                }
            }

            // get before direction
            DirectionType beforeDirection = DirectionType.CURRENT;
            int beforeIndex = beforeNode.getIndex();
            ArrayList<Point> expectPath = beforeNode.getPath();

            // if currentLocation not equal to start, we should compute the before direction
            if (location.x != start.x - 1 || location.y != start.y - 1) {
                Point previousLocation = expectPath.get(beforeIndex);
                beforeDirection = this.getMoveDirections(previousLocation, location);
            }
            // the nextPosition means the expected next position
            Point nextPosition = new Point();

            Point expectedPosition = expectPath.get(beforeIndex + 1);
            int distanceToTarget = -1;
            // if expectedPosition == location, means the agent goes on the expected way,
            // we don't need to compute getShortestPathLeastTurns again
            if (expectedPosition.equals(location)) {
                nextPosition = expectPath.get(beforeIndex + 2);
                distanceToTarget = expectPath.size() - beforeIndex - 1;
            } else {
                // the agent goes on the unexpected way, we need to compute the shortest path again and renew the agentsPath map
                Node node;
                if (beforeDirection == DirectionType.CURRENT) {
                    Point beforebeforeLocation = location;
                    for (int k = beforeIndex; k >= 0; k --) {
                        beforebeforeLocation = expectPath.get(k);
                        if (!beforebeforeLocation.equals(location)) {
                            break;
                        }
                    }
                    DirectionType beforebeforeDirection = this.getMoveDirections(beforebeforeLocation, location);
                    node = this.getShortestPathLeastTurns(location, grid, beforebeforeDirection);
                } else {
                    node = this.getShortestPathLeastTurns(location, grid, beforeDirection);
                }
                ArrayList<Point> path = node.getPath();
                nextPosition = path.get(1);
                System.out.println("agent" + String.valueOf(i) + "go unexpected way, now location:" + location.toString() + "before direction" + beforeDirection.toString());
                System.out.println("now position index:" + String.valueOf(beforeIndex + 1) + ",expected:" + expectPath.toString());
                System.out.println("new path:" + path.toString());
                ArrayList<Point> newPath = new ArrayList<Point>(expectPath.subList(0, beforeIndex + 1));
                newPath.addAll(path);
                beforeNode.setPath(newPath);
                distanceToTarget = path.size() - 1;
            }
            // refresh the index so that we know agent goes on to which step
            beforeNode.setIndex(beforeIndex + 1);
            agentsPath.put(i, beforeNode);
            DirectionType nowDirection = this.getMoveDirections(location, nextPosition);

            // if agent in the start position,
            // make sure applying a nearby blue chemical won't affect the agent in the cell if there is an agent here
            // if it is affecting another agent, we should apply a blue chemical but let the agent go random step
            if (location.x == start.x - 1 && location.y == start.y - 1) {
                Point anotherAgent = new Point(nextPosition.x + 1, nextPosition.y + 1);
                if (locations.contains(anotherAgent)) {
                    int intendTurnDirection = this.getIntendTurnDirection(grid, nextPosition, nowDirection);
                    if (agentTurnDirections.containsKey(nextPosition)) {
                        int anotherAgentIntendTurnDirection = agentTurnDirections.get(nextPosition);
                        if (intendTurnDirection != anotherAgentIntendTurnDirection) {
                            continue;
                        }
                    } else {
                        // debug
                        System.out.println("an agent next to a start agent doesn't in the agentTurnDirections");
                        System.out.println(agentTurnDirections.toString());
                    }
                }
            }
            // intendTurnDirection means how the chemical will affect the agent,
            // while supposeTurnDirection means which direction we expect it to turn
            // if the two is equal, which means we don't need to apply a chemical
            int intendTurnDirection;
            int supposeTurnDirection;
            if ((location.x == start.x - 1 && location.y == start.y - 1) || beforeDirection != DirectionType.CURRENT) {
                intendTurnDirection = this.getIntendTurnDirection(grid, location, beforeDirection);
                supposeTurnDirection = this.getChemicalType(beforeDirection, nowDirection);
            } else{
                Point beforebeforeLocation = location;
                for (int k = beforeIndex; k >= 0; k --) {
                    beforebeforeLocation = expectPath.get(k);
                    if (!beforebeforeLocation.equals(location)) {
                        break;
                    }
                }
                DirectionType beforebeforeDirection = this.getMoveDirections(beforebeforeLocation, location);
                intendTurnDirection = this.getIntendTurnDirection(grid, location, beforebeforeDirection);
                supposeTurnDirection = this.getChemicalType(beforebeforeDirection, nowDirection);
                System.out.println("agent" + i + beforebeforeDirection + "stuck, intend to move" + intendTurnDirection + "suppose:" + supposeTurnDirection);
            }

            if ((location.x != start.x -1 || location.y != start.y - 1) && beforeDirection == DirectionType.CURRENT) {
                System.out.println(String.valueOf(i) + "stopped, now:" + nowDirection.toString());
                System.out.println("intend:" + intendTurnDirection + "suppose:" + supposeTurnDirection);
                Point beforebeforeLocation = location;
                int k;
                for (k = beforeIndex; k >= 0; k --) {
                    beforebeforeLocation = expectPath.get(k);
                    if (!beforebeforeLocation.equals(location)) {
                        break;
                    }
                }
                if (beforeIndex - k  >= 3 && supposeTurnDirection != 4) {
                    supposeTurnDirection = 3;
                }
            }
            // if the location isn't the start point, and we find that the agent goes blocked(beforeDirection = CURRENT),
            // we will know that there must be two agents want to the opposite way,
            // so we apply a blue chemical to let the agent go opposite to avoid the collision
            if (location.x != start.x - 1 || location.y != start.y - 1) {
                if (supposeTurnDirection == 4)
                    continue;
//                    if (beforeDirection != DirectionType.CURRENT || intendTurnDirection != 4)
//                        continue;
//                    else
//                        supposeTurnDirection = 3;
            }
            // if expectedDirection(nowDirection) equals to the before direction, but it tries to turn,
            // actually we need to fix it afterwards, afraid of using a chemical can cause other agents go unexpectedly
            if (nowDirection == beforeDirection) {
                if (intendTurnDirection != 0) {
                    System.out.println("agent" + String.valueOf(i) + "should go" + String.valueOf(supposeTurnDirection));
                    System.out.println("but it intend to go" + String.valueOf(intendTurnDirection));
                }
            } else {
                if (beforeDirection != DirectionType.CURRENT) {
                    // if we make sure that the agent can turn itself, we can put no chemical
                    if (!isOppositeDirection(beforeDirection, nowDirection) && numberOfAvailableNeighbours == 2)
                        continue;

                    if (isOppositeDirection(beforeDirection, nowDirection) && numberOfAvailableNeighbours == 1)
                        continue;
                } else {
                    if (numberOfAvailableNeighbours == 1) {
                        continue;
                    }
                }

                if (intendTurnDirection == supposeTurnDirection) {
                    continue;
                }
                // we always choose the one that needs to turn with the closet distance
                if (distance > distanceToTarget) {
                    distance = distanceToTarget;
                    chooseIdx = i;
                    turnDirection = supposeTurnDirection;
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

    public int findNumberOfAvailableNeighbours(Point currentAgent, ChemicalCell[][] grid) {
        int numberOfAvailableNeighbours = 0;
        int m = grid.length;
        int n = grid[0].length;

        if (currentAgent.y + 1 < n && grid[currentAgent.x][currentAgent.y + 1].isOpen()) {
            numberOfAvailableNeighbours += 1;
        }
        if (currentAgent.y - 1 >= 0 && grid[currentAgent.x][currentAgent.y - 1].isOpen()) {
            numberOfAvailableNeighbours += 1;
        }
        if (currentAgent.x + 1 < m && grid[currentAgent.x + 1][currentAgent.y].isOpen()) {
            numberOfAvailableNeighbours += 1;
        }
        if (currentAgent.x - 1 >= 0 && grid[currentAgent.x - 1][currentAgent.y].isOpen()) {
            numberOfAvailableNeighbours += 1;
        }
        return numberOfAvailableNeighbours;
    }

    /**
     * Apply chemicals to the map
     *
     * @param currentTurn        current turn in the simulation
     * @param chemicalsRemaining number of chemicals remaining
     * @param locations          current locations of the agents
     * @param grid               game grid/map
     * @return a cell location and list of chemicals to apply
     */
    // choose one point and put the chemicals(G:left, R:right, B:attract/opposite) to guide it
    // In the beginning, we use blue in the nearby cell to guide the agent in the right direction
    // in the further process, we use G/R/B in the agent cell itself to guide it turn left/right/opposite
    // As a result, for the agent which is at the start point, the priority is B > G = R
    // in the afterwards, we should decide which concentration is the most, then follow the chemical rule
    @Override
    public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid) {
        ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
        // choose a point that needs to turn and apply the chemicals
        ArrayList<Integer> result = this.chooseOnePointNeedToTurn(locations, grid);
        System.out.println("the result of applyChemicals is" + result.toString());
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
                chemicalPlacement.location = new Point(nowLocation.x, nowLocation.y);
                break;
            case 4:
                chemicals.add(ChemicalCell.ChemicalType.BLUE);
                chemicalPlacement.location = new Point(nextX, nextY);
                break;
        }

        chemicalPlacement.chemicals = chemicals;
        previousLocations = locations;
        return chemicalPlacement;
    }

    private Node getFourDirectionShortestPath(Point start, ChemicalCell[][] grid, DirectionType previousDirection) {
        Node result = new Node(start, Integer.MAX_VALUE);
        if (previousDirection == DirectionType.CURRENT) {
            for (MoveDirection direction: directions) {
                Node tempResult = this.getShortestPathLeastTurns(start, grid, direction.directionType);
                if (result.getTurns() == Integer.MAX_VALUE || result.getPath().size() > tempResult.getPath().size()) {
                    result.setTurns(tempResult.getTurns());
                    result.setPath(tempResult.getPath());
                } else if (result.getPath().size() == tempResult.getPath().size()) {
                    if (result.getTurns() > tempResult.getTurns()) {
                        result.setTurns(tempResult.getTurns());
                        result.setPath(tempResult.getPath());
                    }
                }
            }
        } else {
            result = this.getShortestPathLeastTurns(start, grid, previousDirection);
        }
        return result;
    }

    // use queue to do BFS to find the shortest path from current position to the target
    // and if the length of the path is the same , we select a path which has the least turns,
    // and we will also take previous DirectionType into consideration so as make sure not to make extra turns
    private Node getShortestPathLeastTurns(Point start, ChemicalCell[][] grid, DirectionType previousDirection) {
        ArrayList<MoveDirection> newDirections = directions;
        if (previousDirection != DirectionType.CURRENT)
            newDirections = sortDirections(directions, previousDirection);

        Queue<Node> deque = new LinkedList<>();
        int m = grid.length;
        int n = grid[0].length;
        // to remember which points have been visited and never go back
        boolean[][] visited = new boolean[m][n];
        visited[start.x][start.y] = true;
        ArrayList<Point> path = new ArrayList<Point>();
        path.add(start);
        boolean firstTime = true;

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
            if (firstTime) {
                if (previousDirection != DirectionType.CURRENT)
                    newDirections = sortDirections(directions, previousDirection);
            } else {
                ArrayList<Point> nowPath = node.getPath();
                Point previousLocation = nowPath.get(nowPath.size() - 2);
                previousDirection = this.getMoveDirections(previousLocation, currentPosition);
                newDirections = sortDirections(directions, previousDirection);
            }

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

            firstTime = false;
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
        Integer index;

        public Node() {
            this.currentPosition = new Point();
            this.path = new ArrayList<Point>();
            this.directionType = DirectionType.CURRENT;
            this.turns = 0;
            this.index = -1;
        }

        public Node(Point currentPosition, ArrayList<Point> path) {
            this.currentPosition = currentPosition;
            this.path = path;
            this.directionType = DirectionType.CURRENT;
            this.turns = 0;
            this.index = -1;
        }

        public Node(Point currentPosition, Integer turns) {
            this.currentPosition = currentPosition;
            this.path = new ArrayList<Point>();
            this.directionType = DirectionType.CURRENT;
            this.turns = turns;
            this.index = -1;
        }

        public Node(Point currentPosition, ArrayList<Point> path, DirectionType directionType, Integer turns) {
            this.currentPosition = currentPosition;
            this.path = path;
            this.directionType = directionType;
            this.turns = turns;
            this.index = -1;
        }

        public Node(Node oldNode) {
            this.currentPosition = oldNode.getCurrentPosition();
            this.path = oldNode.getPath();
            this.directionType = oldNode.getDirectionType();
            this.turns = oldNode.getTurns();
            this.index = oldNode.getIndex();
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

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
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
        for (MoveDirection moveDirection : directions) {
            int dx = moveDirection.dx;
            int dy = moveDirection.dy;
            if (currentLocation.x - previousLocation.x == dx && currentLocation.y - previousLocation.y == dy) {
                return moveDirection.directionType;
            }
        }
        // debug
        if (previousLocation.x != currentLocation.x || previousLocation.y != currentLocation.y) {
            System.out.println("getMoveDirections some errors here, need debug");
            System.out.println(previousLocation);
            System.out.println(currentLocation);
        }
        return DirectionType.CURRENT;
    }

    // Green: left -> 1, Red: right -> 2, Blue: go opposite -> 3, Blue: attract(means from stopped to move) -> 4
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
            return turnDirections.get("OPPOSITE");

        if (previousDirection == DirectionType.WEST && nowDirection == DirectionType.EAST)
            return turnDirections.get("OPPOSITE");

        if (previousDirection == DirectionType.SOUTH && nowDirection == DirectionType.NORTH)
            return turnDirections.get("OPPOSITE");

        if (previousDirection == DirectionType.NORTH && nowDirection == DirectionType.SOUTH)
            return turnDirections.get("OPPOSITE");

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

    private ArrayList<MoveDirection> sortDirections(ArrayList<MoveDirection> oldDirections, DirectionType previousDirection) {
        ArrayList<MoveDirection> newDirections = new ArrayList<MoveDirection>();
        for (MoveDirection moveDirection : oldDirections) {
            if (moveDirection.directionType == previousDirection)
                newDirections.add(0, moveDirection);
            else
                newDirections.add(moveDirection);

        }
        return newDirections;
    }

    // return integer to indicate which directionType it will turn
    private Integer getIntendTurnDirection(ChemicalCell[][] grid, Point currentPosition, DirectionType beforeDirection) {
        ArrayList<ChemicalType> chemicals = new ArrayList<ChemicalType>();
        ChemicalCell currentCell;
        int numberOfColors = 3;

        // row: green->red->blue col: currentPosition->previousPosition->nextPosition
        ArrayList<ArrayList<Double>> chemicalConcentrations = new ArrayList<ArrayList<Double>>();


        for (int i = 0; i < numberOfColors; i++) {
            chemicalConcentrations.add(new ArrayList<Double>());
        }
        chemicals.add(ChemicalType.GREEN);
        chemicals.add(ChemicalType.RED);
        chemicals.add(ChemicalType.BLUE);


        int dx = 0;
        int dy = 0;

        for (MoveDirection direction : directions) {
            if (direction.directionType == beforeDirection) {
                dx = direction.dx;
                dy = direction.dy;
                break;
            }
        }
        int m = grid.length;
        int n = grid[0].length;

        int newX = currentPosition.x;
        int newY = currentPosition.y;
        for (int i = 0; i < numberOfColors; i++) {
            if (i == 1) {
                newX = currentPosition.x + dx;
                newY = currentPosition.y + dy;
            } else if (i == 2) {
                newX = currentPosition.x - dx;
                newY = currentPosition.y - dy;
            }
            for (int j = 0; j < numberOfColors; j++) {
                double concentration = 0;
                if (newX >= 0 && newX < m && newY >= 0 && newY < n){
                    currentCell = grid[newX][newY];
                    concentration = currentCell.getConcentration(chemicals.get(j));
                    if (concentration < 0.001) {
                        concentration = 0;
                    }
                }
                ArrayList<Double> colorChemical = chemicalConcentrations.get(j);
                colorChemical.add(concentration);
                chemicalConcentrations.set(j, colorChemical);
            }
        }

        ArrayList<Double> maxLocal = new ArrayList<Double>();
        int color = 0;
        int equal = 0;
        // we will find the local maximum in the previousDirection line
        for (int i = 0; i < numberOfColors; i++) {
            ArrayList<Double> colorChemical = chemicalConcentrations.get(i);
            double maxConcentration = Collections.max(colorChemical);
            if (maxConcentration != colorChemical.get(0)) {
                maxLocal.add((double) 0);
            } else {
                if (maxLocal == chemicalConcentrations.get(1) || maxLocal == chemicalConcentrations.get(2))
                    maxLocal.add((double) 0);
                else
                    maxLocal.add(maxConcentration);
            }
        }

        // if three/two chemicals are local maximum and have equal concentration, it means agent keep-moving, or it should follow the highest chemical
        double maxColorConcentration = Collections.max(maxLocal);
        for (int i = 0; i < numberOfColors; i++) {
            if (maxLocal.get(i) == (double) 0) {
                continue;
            }
            if (maxColorConcentration == maxLocal.get(i)) {
                color = i + 1;
                equal++;
            }
        }

        // keep-moving
        if (equal == 0 || equal >= 2) {
            return 0;
        }

        if (beforeDirection == DirectionType.CURRENT && color == 3) {
            color = 4;
        }
        return color;
    }

}

