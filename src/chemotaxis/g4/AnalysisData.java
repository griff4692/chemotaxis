package chemotaxis.g4;
import chemotaxis.sim.*;
import java.awt.Point;
import java.util.*;

public class AnalysisData {
    Point p;
    int arrayLength = 20;
    int[] distanceReached;
    int maxDistance;
    boolean[] goodGradient;
    boolean[] isMax;
    double isMaxPercentage;
    double isGoodGradientPercentage;


    public AnalysisData(Point p, int arrayLength){
        this.p = p;
        this.arrayLength = arrayLength;

    }
    public void getIsMaxPercentage(){
        int maxCount = 0;
        for(boolean b: isMax){
            if(b) maxCount++;
        }
        this.isMaxPercentage = (double)maxCount/arrayLength;
    }

    public void getIsGoodGradientPercentage(){
        int goodGradientCount = 0;
        for(boolean b: goodGradient){
            if(b) goodGradientCount++;
        }
        this.isGoodGradientPercentage = (double)goodGradientCount/arrayLength;
    }

    public void getMaxDistance(){//used to initialize maxDistance variable
        int max = 0;
        for(int i: distanceReached){
            if(i > max) max = i;
        }
        this.maxDistance = max;
    }

    public boolean willReach(int distance){ //simple boolean
        if(distance <= maxDistance) return true;
        return false;
    }

    public int[] turnsWillReach(int distance){// returns [firstTurn, lastTurn] that the dye will reach a distance in the path
        int firstTurn = -1; //returns [-1, -1] if it can never reach
        int lastTurn = -1;
        if(distance > maxDistance) return new int[]{firstTurn, lastTurn};
        for(int i = 0; i < arrayLength; i++){
            if(distanceReached[i] == distance){
                firstTurn = i;
                break;
            }
        }
        for(int i = arrayLength - 1; i >= 0; i--){
            if(distanceReached[i] == distance){
                lastTurn = i;
                break;
            }
        }
        return new int[]{firstTurn, lastTurn};
    }
}
