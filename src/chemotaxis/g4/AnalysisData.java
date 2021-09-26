package chemotaxis.g4;
import chemotaxis.sim.*;
import java.awt.Point;
import java.util.*;

public class AnalysisData {
    Point p;
    int arrayLength = 20;
    int[] distanceReached;
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
}
