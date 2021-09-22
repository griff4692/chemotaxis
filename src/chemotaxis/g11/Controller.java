package chemotaxis.g11;

import java.awt.Point;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.HashMap;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.SimPrinter;
import chemotaxis.sim.DirectionType;

public class Controller extends chemotaxis.sim.Controller {
    DirectionType[][] directionMap;
    HashMap<Point, DirectionType> agents;
    Point start;
    Point target;

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
    public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
        super(start, target, size, grid, simTime, budget, seed, simPrinter);
        agents = new HashMap<>();
        this.start = start;
        this.target = target;
        int endX = target.x - 1;
        int endY = target.y - 1;
        boolean[][] visited = new boolean[size][size];
        directionMap = new DirectionType[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                visited[r][c] = false;
                directionMap[r][c] = DirectionType.CURRENT;
            }
        }
        Queue<Point> queue = new LinkedList<Point>();
        queue.add(new Point(endX, endY));

        visited[endX][endY] = true;
        directionMap[endX][endY] = DirectionType.CURRENT;

        while (!queue.isEmpty()) {
            Point curr = queue.remove();
            int x = curr.x;
            int y = curr.y;
            if (x - 1 >= 0 && grid[x - 1][y].isOpen() && !visited[x - 1][y]) {
                queue.add(new Point(x - 1, y));
                visited[x-1][y] = true;
                directionMap[x - 1][y] = DirectionType.SOUTH;
            }

            if (y - 1 >= 0 && grid[x][y - 1].isOpen() && !visited[x][y - 1]) {
                queue.add(new Point(x, y - 1));
                visited[x][y-1] = true;
                directionMap[x][y - 1] = DirectionType.EAST;
            }

            if (x + 1 < size && grid[x + 1][y].isOpen() && !visited[x + 1][y]) {
                queue.add(new Point(x + 1, y));
                visited[x+1][y] = true;
                directionMap[x + 1][y] = DirectionType.NORTH;
            }

            if (y + 1 < size && grid[x][y + 1].isOpen() && !visited[x][y + 1]) {
                queue.add(new Point(x, y + 1));
                visited[x][y+1] = true;
                directionMap[x][y + 1] = DirectionType.WEST;
            }
        }

        //Prints the map that is made
        /*
        HashMap<DirectionType, Character> debugging = new HashMap<>();
        debugging.put(DirectionType.NORTH, 'N');
        debugging.put(DirectionType.SOUTH, 'S');
        debugging.put(DirectionType.EAST, 'E');
        debugging.put(DirectionType.WEST, 'W');
        debugging.put(DirectionType.CURRENT, 'C');
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                System.out.print(debugging.get(directionMap[r][c]));
            }
            System.out.println();
        }
        */
    }

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
     * @param locations           current locations of the agents
     * @param grid                game grid/map
     * @return                    a cell location and list of chemicals to apply
     *
     */
    @Override
    public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid) {
        ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
        if (locations.contains(start)) {
            agents.put(start, DirectionType.SOUTH);
        }

        if (locations.contains(target)) {
            agents.remove(target);
            locations.remove(target);
        }

        Point wrongDirectionAgent = null;
        double threshold = 0.1;
        for (Point p: locations) {
            if (!p.equals(target) && agents.get(p) != directionMap[p.x - 1][p.y - 1]) {
                if (grid[p.x - 1][p.y - 1].getConcentration(ChemicalCell.ChemicalType.BLUE) < threshold &&
                        grid[p.x - 1][p.y - 1].getConcentration(ChemicalCell.ChemicalType.GREEN) < threshold &&
                        grid[p.x - 1][p.y - 1].getConcentration(ChemicalCell.ChemicalType.RED) < threshold) {
                    wrongDirectionAgent = p;
                    break;
                }
            }
        }

        List<ChemicalCell.ChemicalType> chemicals = new ArrayList<>();
        if (wrongDirectionAgent != null) {
            DirectionType newDirection = directionMap[wrongDirectionAgent.x - 1][wrongDirectionAgent.y - 1];
            if (newDirection == DirectionType.NORTH) {
                chemicalPlacement.location = new Point(wrongDirectionAgent.x - 1, wrongDirectionAgent.y);
                agents.put(wrongDirectionAgent, DirectionType.NORTH);
                chemicals.add(ChemicalCell.ChemicalType.RED);
            }
            else if (newDirection == DirectionType.SOUTH) {
                chemicalPlacement.location = new Point(wrongDirectionAgent.x + 1, wrongDirectionAgent.y);
                agents.put(wrongDirectionAgent, DirectionType.SOUTH);
                chemicals.add(ChemicalCell.ChemicalType.BLUE);
            }
            else if (newDirection == DirectionType.EAST) {
                chemicalPlacement.location = new Point(wrongDirectionAgent.x, wrongDirectionAgent.y + 1);
                agents.put(wrongDirectionAgent, DirectionType.EAST);
                chemicals.add(ChemicalCell.ChemicalType.GREEN);
            }
            else if (newDirection == DirectionType.WEST) {
                chemicalPlacement.location = new Point(wrongDirectionAgent.x, wrongDirectionAgent.y - 1);
                agents.put(wrongDirectionAgent, DirectionType.WEST);
                chemicals.add(ChemicalCell.ChemicalType.BLUE);
                chemicals.add(ChemicalCell.ChemicalType.GREEN);
            }
            else {
                chemicalPlacement.location = new Point(wrongDirectionAgent.x, wrongDirectionAgent.y);
                agents.put(wrongDirectionAgent, DirectionType.CURRENT);
            }
        }

        HashMap<Point, DirectionType> newAgents = new HashMap<Point, DirectionType>();
        for (Point p: agents.keySet()) {
            DirectionType currentDirection = agents.get(p);
            if (currentDirection == DirectionType.NORTH) {
                newAgents.put(new Point(p.x - 1, p.y), DirectionType.NORTH);
            }
            else if (currentDirection == DirectionType.SOUTH) {
                newAgents.put(new Point(p.x + 1, p.y), DirectionType.SOUTH);
            }
            else if (currentDirection == DirectionType.WEST) {
                newAgents.put(new Point(p.x, p.y - 1), DirectionType.WEST);
            }
            else if (currentDirection == DirectionType.EAST) {
                newAgents.put(new Point(p.x, p.y + 1), DirectionType.EAST);
            }
            else {
                newAgents.put(new Point(p.x, p.y), DirectionType.CURRENT);
            }
        }
        this.agents = newAgents;
        /*
        List<ChemicalCell.ChemicalType> chemicals = new ArrayList<>();
        if (wrongDirectionAgent != null) {
            chemicals.add(ChemicalCell.ChemicalType.BLUE);
        }
         */
        chemicalPlacement.chemicals = chemicals;
        return chemicalPlacement;
    }
}