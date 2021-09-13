package chemotaxis.g3;

import java.awt.Point;
import java.util.*; 
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.SimPrinter;
import java.lang.Math;

public class PathOptimizer {

    private static SimPrinter simPrinter = new SimPrinter(true);

    public static List<Point> getNewPath(List<Point> path, ChemicalCell[][] grid, Integer size) {
        List<Point> newPath = new ArrayList<Point>();
        Point turnPt = new Point();
        Point curPt = path.get(0);
        Point bestVertPt = new Point();
        Point bestHoriPt = new Point();
        //Point bestDiagPt = null;
        simPrinter.println(path);

        //add start point
        newPath.add(path.get(0));

        while (!curPt.equals(path.get(path.size()-1))) {
            for (int i = path.indexOf(curPt) + 1; i < path.size(); i++) {
                Point futPt = path.get(i);
                Point vertPt = checkVertical(curPt, futPt, grid, size);
                Point horiPt = checkHorizontal(curPt, futPt, grid, size);
                //Point diagPt = checkDiagonal(curPt, futPt, grid, size);

                if (bestVertPt.equals(null)) {
                    simPrinter.println("null");
                    bestVertPt = vertPt;
                }
                else {
                    if (Double.compare(distance(curPt, vertPt), distance(curPt, bestVertPt)) >= 0) {
                        bestVertPt = vertPt;
                    }
                }
                if (bestHoriPt.equals(null)) {
                    simPrinter.println("null");
                    bestHoriPt = horiPt;
                }
                else {
                    if (Double.compare(distance(curPt, horiPt), distance(curPt, bestHoriPt)) >= 0) {
                        bestHoriPt = horiPt;
                    }
                }
                /*
                if (bestDiagPt.equals(null)) {
                    bestDiagPt = diagPt;
                }
                else {
                    if (Double.compare(distance(curPt, diagPt), distance(curPt, bestDiagPt)) >= 0) {
                        bestDiagPt = diagPt;
                    }
                }
                */

                double maxDist = 0.0;
                List<Point> potentials = new ArrayList<Point>(); 
                potentials.add(bestVertPt);
                potentials.add(bestHoriPt);
                //potentials.add(bestDiagPt);

                for (Point pt : potentials) {
                    if (Double.compare(distance(pt, curPt), maxDist) > 0) {
                        turnPt = pt;
                        maxDist = distance(pt, curPt);
                    }
                }
                
                newPath.add(turnPt);
                curPt = turnPt;
            }

            newPath.add(turnPt);
            curPt.setLocation(turnPt);
        }

        //add target point
        newPath.add(path.get(path.size()-1));

        return newPath;
    }

    public static Point checkVertical(Point curPt, Point futPt, ChemicalCell[][] grid, Integer size) {
        //simPrinter.println(futPt);
        Point temp = curPt;
        int addVal = 0;

        if (Double.compare(temp.getY(), futPt.getY()) != 0) 
        {
            return new Point(0, 0);
        }
	    else {
            if (Double.compare(temp.getX(), futPt.getX()) < 0) {
                addVal = 1;
            }
            else {
                addVal = -1;
            }

            while (!temp.equals(futPt)) {
                temp.setLocation(temp.getX() + addVal, temp.getY());
                int x = (int) temp.getX();
                int y = (int) temp.getY();
                simPrinter.println(x);
                simPrinter.println(y);
                if (grid[x - 1][y - 1].isOpen() == false || x >= size || x < 0) {
                    return new Point(0, 0);
                }
            }
        }

	    return temp;
    }

    public static Point checkHorizontal(Point curPt, Point futPt, ChemicalCell[][] grid, Integer size) {
        //simPrinter.println(futPt);
        Point temp = curPt;
        int addVal = 0;

        if (Double.compare(temp.getX(), futPt.getX()) != 0) 
        {
            return new Point(0, 0);
        }
	    else {
            if (Double.compare(temp.getY(), futPt.getY()) < 0) {
                addVal = 1;
            }
            else {
                addVal = -1;
            }

            while (!temp.equals(futPt)) {
                temp.setLocation(temp.getX(), temp.getY() + addVal);
                int x = (int) temp.getX();
                int y = (int) temp.getY();
                simPrinter.println(x);
                simPrinter.println(y);
                if (grid[x - 1][y - 1].isOpen() == false || y >= size || y < 0) {
                    return new Point(0, 0);
                }
            }
        }

	    return temp;
    }

    public static Point checkDiagonal(Point curPt, Point futPt, ChemicalCell[][] grid, Integer size) {
        Point temp = curPt;
        int addVal = 0;

        if (Double.compare(temp.getX(), futPt.getX()) > 0) 
        {
            if (Double.compare(temp.getY(), futPt.getY()) > 0) {
                while (!temp.equals(futPt)) {
                    temp.setLocation(temp.getX() + addVal, temp.getY());
                    int x = (int) temp.getX();
                    int y = (int) temp.getY();
                    if (grid[x - 1][y - 1].isOpen() == false) {
                        return new Point(0, 0);
                    }
                }
            }
            else {

            }
        }
        else {

        }
	    
	    return temp;
    }

    public static double distance(Point a, Point b)
    {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}