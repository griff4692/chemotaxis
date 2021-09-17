package chemotaxis.g10;

import java.awt.Point;

public class TurnGridNode {
    private int turns;
    private Point parentPoint;

    public TurnGridNode(int turns, Point parentPoint){
        this.turns = turns;
        this.parentPoint = parentPoint;
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
}
