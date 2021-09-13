package chemotaxis.g3;

import java.awt.Point;
import java.util.*; 
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.SimPrinter;
import java.lang.Math;
import chemotaxis.g3.Language.Translator;

public class PathFinder {

    private Translator trans = Translator.getInstance();
    private static SimPrinter simPrinter = new SimPrinter(true);
    
    public PathFinder(Point start, Point target, ChemicalCell[][] grid, Integer size) {
        ;
    }

    public static List<Point> getPath(Point start, Point target, ChemicalCell[][] grid, Integer size) {
        List<Point> path = new ArrayList<Point>();
        List<Point> temp = new ArrayList<Point>();
        Map<Point, Point> parents = new HashMap<Point, Point>();
        boolean targetReached = false;
        Point end = null;

        temp.add(start);
        parents.put(start, null);
        
        while (temp.size() > 0 && !targetReached) {
            Point currentPt = temp.remove(0);
            List<Point> children = getChildren(currentPt, grid, size);
            for (Point child : children) {
                if (!parents.containsKey(child)) {
                    parents.put(child, currentPt);

                    if (!child.equals(target)) {
                        temp.add(child);
                    } 
                    else {
                        temp.add(child);
                        targetReached = true;
                        end = child;
                        break;
                    }
                }
            }
        }

        Point pt = end;
        while (pt != null) {
            path.add(0, pt);
            pt = parents.get(pt);
        }

        return path;
    }

    public static List<Point> getChildren(Point parent, ChemicalCell[][] grid, Integer size) {
        List<Point> children = new ArrayList<Point>();
        int x = (int) parent.getX();
        int y = (int) parent.getY();

        if (0 <= x && x < size) {
            if (grid[x][y - 1].isOpen() == true) {
                children.add(new Point(x + 1, y));
            }
        }
        if (0 <= x - 2 && x - 2 < size) {
            if (grid[x - 2][y - 1].isOpen() == true) {
                children.add(new Point(x - 1, y));
            }
        }
        if (0 <= y && y < size) {
            if (grid[x - 1][y].isOpen() == true) {
                children.add(new Point(x, y + 1));
            }
        }
        if (0 < y - 2 && y - 2 < size) {
            if (grid[x - 1][y - 2].isOpen() == true) {
                children.add(new Point(x, y - 1));
            }
        }

        return children;
    }

    public static List<Point> cleanPath(List<Point> path) {
        Point a = null;
        Point b = null;
        Point c = null;
        double angle = 45f;
        List<Point> ret = new ArrayList<Point>();

        for (int i = 2; i < path.size() - 2; i++) {
            a = path.get(i - 2);
            b = path.get(i);
            c = path.get(i + 2);
            if (sameDirection(a,b,c)) {
                continue;
            }
            ret.add(b);
        }
        ret.add(path.get(path.size()-1));
        return ret;
    }

    public static List<Point> triPath(List<Point> path) {
           return null;
    }
    
    private static boolean sameAngle(Point a, Point b, Point c) {
        double angle1 = Math.toDegrees(Math.atan2(b.y - a.y, b.y - a.y));
        double angle2 = Math.toDegrees(Math.atan2(c.y - b.y, c.y - b.y));
        return angle1 == angle2;
    }

    private static boolean sameDirection(Point a, Point b, Point c) {
        Point ab = new Point(b.x - a.x, b.y - a.y);
        Point bc = new Point(c.x - b.x, c.y - b.y);
        if ((ab.x == 0 && bc.x == 0 && ab.y > 0 && bc.y > 0)
            || (ab.x == 0 && bc.x == 0 && ab.y < 0 && bc.y < 0)
            || (ab.x > 0 && bc.x > 0 && ab.y == 0 && bc.y == 0)
            || (ab.x < 0 && bc.x < 0 && ab.y == 0 && bc.y == 0)
            || (ab.x > 0 && bc.x > 0 && ab.y > 0 && bc.y > 0)
            || (ab.x < 0 && bc.x < 0 && ab.y > 0 && bc.y > 0)
            || (ab.x > 0 && bc.x > 0 && ab.y < 0 && bc.y < 0)
            || (ab.x < 0 && bc.x < 0 && ab.y < 0 && bc.y < 0))
            return true;
        return false;
    }

}
