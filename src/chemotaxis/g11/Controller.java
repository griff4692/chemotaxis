package chemotaxis.g11;

import java.awt.Point;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.SimPrinter;

public class Controller extends chemotaxis.sim.Controller {
    char[][] directionMap;

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
        int endX = target.x - 1;
        int endY = target.y - 1;
        boolean[][] visited = new boolean[size][size];
        directionMap = new char[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                visited[r][c] = false;
                directionMap[r][c] = '#';
            }
        }
        Queue<Point> queue = new LinkedList<Point>();
        queue.add(new Point(endX, endY));
        directionMap[endX][endY] = 'E';

        while (!queue.isEmpty()) {
            Point curr = queue.remove();
            int x = curr.x;
            int y = curr.y;
            visited[x][y] = true;
            if (x - 1 >= 0 && grid[x - 1][y].isOpen() && !visited[x - 1][y]) {
                queue.add(new Point(x - 1, y));
                directionMap[x - 1][y] = 'D';
            }

            if (y - 1 >= 0 && grid[x][y - 1].isOpen() && !visited[x][y - 1]) {
                queue.add(new Point(x, y - 1));
                directionMap[x][y - 1] = 'R';
            }

            if (x + 1 < size && grid[x + 1][y].isOpen() && !visited[x + 1][y]) {
                queue.add(new Point(x + 1, y));
                directionMap[x + 1][y] = 'U';
            }

            if (y + 1 < size && grid[x][y + 1].isOpen() && !visited[x][y + 1]) {
                queue.add(new Point(x, y + 1));
                directionMap[x][y + 1] = 'L';
            }
        }

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

        List<ChemicalCell.ChemicalType> chemicals = new ArrayList<>();
        chemicals.add(ChemicalCell.ChemicalType.BLUE);

        chemicalPlacement.location = new Point(randomX, randomY);
        chemicalPlacement.chemicals = chemicals;

        return chemicalPlacement;
    }
}