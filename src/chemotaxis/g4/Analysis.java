package chemotaxis.g4;
import chemotaxis.sim.*;
import java.awt.Point;
import java.util.*;

public class Analysis {

    ChemicalCell[][] grid;
    Double threshold = 0.05;
    ChemicalCell.ChemicalType dye = ChemicalCell.ChemicalType.BLUE;


    public Analysis(ChemicalCell[][] grid){
        this.grid = grid;
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
                        if(grid[row + i][col + j].getConcentration(dye) >= concentration) return false;
                    }
                }
            }
        }
        else{
            return false;
        }
        return true;


    }

    public int simulatePoint(Point p){
        ChemicalCell[][] newGrid = createNewGrid(grid);
        int row = p.x - 1;
        int col = p.y - 1;
        ChemicalCell cell = newGrid[row][col];
        int turnCount = 0;
        int notMaxCount = 0;
        int allowedNotMax = 1;

        if(cell.isOpen()){
            cell.applyConcentration(dye);
        }


        for(int i = 0; i < 100; i++){
            if(isMax(newGrid, p)){
                turnCount++;
                notMaxCount = 0;
            }
            else{
                notMaxCount++;
                turnCount++;
                if(notMaxCount > allowedNotMax) {
                    return turnCount - (allowedNotMax + 1);
                }
            }
            newGrid = diffuseCells(newGrid);


        }
        return turnCount;
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
                    /**
                    if(averageConcentration > 0){
                        System.out.println("average concentration is");
                        System.out.println(averageConcentration);
                        System.out.println(i);
                        System.out.println(j);
                    }
                     */
                }
            }
        }
        return newGrid;
    }
}
