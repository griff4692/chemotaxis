package chemotaxis.g4;
import chemotaxis.sim.*;
import java.awt.Point;
import java.util.*;


public class Analysis {

    static ChemicalCell[][] grid;
    Double threshold = 0.05;
    ChemicalCell.ChemicalType dye = ChemicalCell.ChemicalType.BLUE;
    int allowedNotMax = 1;
    int numGradientCells = 2;


    public Analysis(ChemicalCell[][] grid){
        this.grid = grid;
    }

    public void setThreshold(double threshold){
        this.threshold = threshold;
    }

    public static ChemicalCell[][] createNewGrid(ChemicalCell[][] grid){
        int size = grid.length;
        ChemicalCell[][] newGrid = new ChemicalCell[size][size];
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(grid[i][j].isOpen()){
                    newGrid[i][j] = new ChemicalCell();
                    ChemicalCell.ChemicalType[] chemicalTypes = ChemicalCell.ChemicalType.values();
                    for(ChemicalCell.ChemicalType chemicalType : chemicalTypes)
                        newGrid[i][j].setConcentration(chemicalType, 0.0);

                }
                else {
                    newGrid[i][j] = new ChemicalCell(false);
                }
            }

        }

        return newGrid;
    }
    public boolean isInBounds(int size, int row, int col){
        if(row >= 0 && col >= 0 && row < size && col < size) return true;
        return false;
    }
    public boolean isMax(ChemicalCell[][] grid, Point p){
        int row = p.x - 1;
        int col = p.y - 1;
        ChemicalCell cell = grid[row][col];
        double concentration = cell.getConcentration(dye);
        if(concentration >= threshold){
            for(int i = -1; i <= 1; i++){
                for(int j = -1; j <= 1; j++){
                    if(i == j || i == -j){
                        continue;
                    }
                    if(isInBounds(grid.length, row + i, col + j)){
                        if(grid[row + i][col + j].getConcentration(dye) >= concentration){
                            return false;
                        }
                    }
                }
            }
        }
        else{
            return false;
        }
        return true;
    }

    public boolean gradientAlongPath(ChemicalCell[][] newGrid, ArrayList<Point> path, int index){
        Point p = path.get(index);
        Point nextP = path.get(index + 1); //next cell along path
        ChemicalCell cell = newGrid[p.x - 1][p.y - 1];
        double concentration = cell.getConcentration(dye);
        double nextPathConcentration = newGrid[nextP.x - 1][nextP.y - 1].getConcentration(dye);
        if(concentration == 0) return true; //dye has not reached cell yet
        if(concentration > nextPathConcentration){ //gradient is backward
            return false;
        }
        for(int i = -1; i <= 1; i++){
            for(int j = -1; j <= 1; j++){
                if(i == j || i == -j) continue;
                if(p.x + i == nextP.x && p.y + j == nextP.y){
                    continue;
                }
                if(isInBounds(newGrid.length, p.x - 1 + i, p.y - 1 + j)) {
                    if (newGrid[p.x - 1 + i][p.y - 1 + j].getConcentration(dye) > nextPathConcentration){
                        return false;
                    }
                }
            }
        }
        return true;
    }
    public boolean hasGoodGradient(ChemicalCell[][] newGrid, ArrayList<Point> path, int index){
        if(index < numGradientCells){
            return false;
        }

        for(int i = 1; i <= numGradientCells; i++){
            if(!gradientAlongPath(newGrid, path, index - i)){
                return false;
            }
        }
        return true;
    }

    public int calculateReached(ChemicalCell[][] newGrid, ArrayList<Point> path, int index){
        if(index == 0) return 0;
        for(int i = 1; i <= index; i++){
            Point p = path.get(index - i);
            if(newGrid[p.x - 1][p.y - 1].getConcentration(dye) < threshold) return i - 1;
        }
        return index;
    }

    public ArrayList<AnalysisData> analyzePath(ArrayList<Point> path, int arrayLength){
        ArrayList<AnalysisData> data = new ArrayList<AnalysisData>();
        for(Point p: path){
            ChemicalCell[][] newGrid = createNewGrid(grid);
            int turnCount = 1;
            int index = path.indexOf(p);
            int[] reached = new int[arrayLength];
            boolean[] isMax = new boolean[arrayLength];
            boolean[] goodGradient = new boolean[arrayLength];

            if(newGrid[p.x - 1][p.y - 1].isOpen()) {
                newGrid[p.x - 1][p.y - 1].applyConcentration(dye);
            }

            for(; turnCount <= arrayLength; turnCount++){
                newGrid = diffuseCells(newGrid);
                reached[turnCount - 1] = calculateReached(newGrid, path, index);
                isMax[turnCount - 1] = isMax(newGrid, p);
                goodGradient[turnCount - 1] = hasGoodGradient(newGrid, path, index);
            }
            AnalysisData dataPoint = new AnalysisData(p, arrayLength);
            dataPoint.distanceReached = reached;
            dataPoint.isMax = isMax;
            dataPoint.goodGradient = goodGradient;
            dataPoint.getIsMaxPercentage();
            dataPoint.getIsGoodGradientPercentage();
            dataPoint.getMaxDistance();
            data.add(dataPoint);
        }
        return data;
    }

    private static ChemicalCell[][] diffuseCells(ChemicalCell[][] grid) {
        ChemicalCell[][] newGrid = createNewGrid(grid);

        for(int i = 0; i < newGrid.length; i++) {
            for(int j = 0; j < newGrid[0].length; j++) {
                ChemicalCell cell = newGrid[i][j];

                if(cell.isBlocked()){
                    continue;
                }

                for(ChemicalCell.ChemicalType chemicalType : ChemicalCell.ChemicalType.values()) {
                    double concentrationSum = grid[i][j].getConcentration(chemicalType);
                    int numUnblockedCells = 1;

                    if(i > 0 && !grid[i - 1][j].isBlocked()) {
                        concentrationSum += grid[i - 1][j].getConcentration(chemicalType);
                        numUnblockedCells++;
                    }
                    if(i < grid.length - 1 && !grid[i + 1][j].isBlocked()) {
                        concentrationSum += grid[i + 1][j].getConcentration(chemicalType);
                        numUnblockedCells++;
                    }
                    if(j > 0 && !grid[i][j - 1].isBlocked()) {
                        concentrationSum += grid[i][j - 1].getConcentration(chemicalType);
                        numUnblockedCells++;
                    }
                    if(j < grid.length - 1 && !grid[i][j + 1].isBlocked()) {
                        concentrationSum += grid[i][j + 1].getConcentration(chemicalType);
                        numUnblockedCells++;
                    }

                    double averageConcentration = concentrationSum / numUnblockedCells;
                    if(averageConcentration < 0.001) averageConcentration = 0.0;
                    cell.setConcentration(chemicalType, averageConcentration);
                }
            }
        }
        return newGrid;
    }
}
