package chemotaxis.g3;

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;

import chemotaxis.g3.Language.Translator;
import chemotaxis.g3.PathFinder;

public class Controller extends chemotaxis.sim.Controller {
    
    Point lastPoint = new Point(-1,-1);
    private Translator trans = null;
    private String lastPlacement = null;

    private List<Point> path = null;
    private Point targetLocation = null;
    private Point expectedLocation = start;
    private int steppingStone = 1;

    /**
     * Controller constructor
     *
     * @param start       start cell coordinates
     * @param target      target cell coordinates
     * @param size     	  grid/map size
     * @param simTime     simulation time
     * @param budget      chemical budget
     * @param seed        random seed
     * @param simPrinter  simulation printer
     *
     */
	public Controller(Point start, Point target, Integer size, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
        super(start, target, size, simTime, budget, seed, simPrinter);
        this.trans = Translator.getInstance();
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
 	@Override
	public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid) {
        simPrinter.println("\nRound:" + currentTurn);

        Point currentLocation = locations.get(0);
        // cell's current location

        // find path
        if (path == null) {
            path = PathFinder.getPath(currentLocation, target, grid, size);
            targetLocation = path.get(steppingStone++);
        }

        int currentX = currentLocation.x;
        int currentY = currentLocation.y;

        Boolean giveInstruction = false;

        if (currentLocation.x == targetLocation.x || currentLocation.y == targetLocation.y) {
            if (steppingStone < path.size())
                targetLocation = path.get(steppingStone++);
        }

        // check to see if we have made it where we need to
        else if (!inVicinity(currentLocation,targetLocation,1)) {
            giveInstruction = true;
            path = PathFinder.getPath(currentLocation, target, grid, size);
            steppingStone = 1;
            if (steppingStone < path.size())
                targetLocation = path.get(steppingStone++);
            simPrinter.println("new target: " + targetLocation);
            simPrinter.println("currently at: " + currentLocation);
        }

        double angle = Math.toDegrees(Math.atan2(targetLocation.y - currentY, targetLocation.x - currentX));

        if (angle < 0) 
            angle += 360;

        // Pass angle into language --> returns with where to place colors
        String placements = trans.getColor(angle);
        
        if ((giveInstruction) || ( !agentBlocked(currentLocation, grid)
            && (lastPoint.equals(currentLocation) 
            || lastPoint.equals(new Point(-1,-1)) 
            || ((angle%90 == 0) && !(placements.equals(lastPlacement)))))) {
            lastPlacement = placements;
            lastPoint.setLocation(currentX, currentY);

            ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
            List<ChemicalType> chemicals = new ArrayList<>();

            // Break apart colors to see where to place, ex => "d_GB"
            if (placements.charAt(0) == 'u') 
                chemicalPlacement.location = new Point(currentX, currentY+1);
            else if (placements.charAt(0) == 'd') 
                chemicalPlacement.location = new Point(currentX, currentY-1);
            else if (placements.charAt(0) == 'l') 
                chemicalPlacement.location = new Point(currentX-1, currentY);
            else if (placements.charAt(0) == 'r') 
                chemicalPlacement.location = new Point(currentX+1, currentY);
            else 
                chemicalPlacement.location = new Point(currentX, currentY);

            if (placements.charAt(1) == 'R') 
                chemicals.add(ChemicalType.RED);
            if (placements.charAt(2) == 'G') 
                chemicals.add(ChemicalType.GREEN);
            if (placements.charAt(3) == 'B') 
                chemicals.add(ChemicalType.BLUE);
        
            chemicalPlacement.chemicals = chemicals;
            
            return chemicalPlacement;
        }
        
        lastPoint.setLocation(currentX, currentY);
        return new ChemicalPlacement();
    } 	

    private boolean inVicinity(Point a, Point b, int c) {
        return (Math.abs(a.x - b.x) <= c && Math.abs(a.y - b.y) <= c);
    }

    private boolean agentBlocked(Point a, ChemicalCell[][] grid) {
        boolean one = true;
        boolean two = true;
        boolean three = true;
        boolean four = true;
        try { one = grid[a.x+1][a.y].isBlocked();
        } catch (Exception e) { ; }
        try { two = grid[a.x-1][a.y].isBlocked();
        } catch (Exception e) { ; }
        try { three = grid[a.x][a.y+1].isBlocked();
        } catch (Exception e) { ; }
        try { four = grid[a.x][a.y-1].isBlocked();
        } catch (Exception e) { ; }
        return (one || two || three || four); 
    }

}
