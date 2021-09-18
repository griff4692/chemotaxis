package chemotaxis.g10;

import java.awt.Point;

public class TurnGridNode implements Comparable<TurnGridNode> {
    private int turns;
    private Point gridPoint; // 0-indexed
    private Point parentPoint; // 0-indexed

    public TurnGridNode(int turns, Point gridPoint, Point parentPoint){
        this.turns = turns;
        this.gridPoint = (Point) gridPoint.clone();
        this.parentPoint = (Point) parentPoint.clone();
    }

    public TurnGridNode(int turns, int gridX, int gridY, Point parentPoint){
        this.turns = turns;
        this.gridPoint = new Point(gridX, gridY);
        this.parentPoint = (Point) parentPoint.clone();
    }

    public int getTurns() {
        return this.turns;
    }

    public void setTurns(int turns){
        this.turns = turns;
    }

    public void setParentPoint(Point parentPoint){
        this.parentPoint = parentPoint;
    }

    public Point getParentPoint(){
        return this.parentPoint;
    }

    public void setGridPoint(Point parentPoint){
        this.gridPoint = gridPoint;
    }

    public Point getGridPoint(){
        return this.gridPoint;
    }

    @Override
    public int compareTo(TurnGridNode o) {
        return this.turns - o.turns;
    }
}
