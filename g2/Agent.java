package chemotaxis.g2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CheckedInputStream;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;

import java.util.Random;

public class Agent extends chemotaxis.sim.Agent {

    private final double COLOR_THRESHOLD = 1.0;
    private final int RANDOM_CUTOFF = 5;
    /**
     * Agent constructor
     *
     * @param simPrinter  simulation printer
     *
     */
    public Agent(SimPrinter simPrinter) {
        super(simPrinter);
    }

    /**
     * Move agent
     *
     * @param randomNum        random number available for agents
     * @param previousState    byte of previous state
     * @param currentCell      current cell
     * @param neighborMap      map of cell's neighbors
     * @return                 agent move
     *
     */
    @Override
    public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        simPrinter.println("Agent turn: ");
        ArrayList<DirectionType> possibleDirections = getPossibleDirections(neighborMap);
        DirectionType[] prevDirs = getPrevAndPrevOrthDir(previousState);
        DirectionType prevDir = prevDirs[0];
        DirectionType prevOrthDir = prevDirs[1];

        boolean onOurOwn = getChemicalStatus(previousState);

        if (possibleDirections.size() == 1) {
            return buildMove(possibleDirections.get(0), previousState, onOurOwn);
        }

        Random rand = new Random();
        //choose random direction and orientation
        //to do: random orientation
        if(onOurOwn && rand.nextInt(100) < RANDOM_CUTOFF) {
            int ranIndex = rand.nextInt(possibleDirections.size());
            DirectionType randomDir = possibleDirections.get(ranIndex);
            return buildMove(randomDir, previousState, true);
            //prevDir = 
        }

        // a color is only counter if it is above or equal to the COLOR_THRESHOLD
        Map<DirectionType, ArrayList<ChemicalCell.ChemicalType>> colorsMap = filterColorMap(neighborMap);
        if (colorsMap.size() == 0) {
            return noColorMove(possibleDirections, prevDir, prevOrthDir, previousState);
        }
        else {
            return colorMove(possibleDirections, prevDir, prevOrthDir, previousState, colorsMap);
        }
    }

    // TODO: adjust color move to check for zig zag
    private Move colorMove(ArrayList<DirectionType> possibleDirections,
                                         DirectionType prevDir,
                                         DirectionType prevOrthDir,
                                         Byte previousState,
                                         Map<DirectionType, ArrayList<ChemicalCell.ChemicalType>> cellColors) {
        // as of now we expect there to only be one cell with color adjacent to the agent
        // but could change if we add more to the language
        Map.Entry<DirectionType, ArrayList<ChemicalCell.ChemicalType>> cellEntry =
                cellColors.entrySet().iterator().next();

        // Right now we expect each cell to contain only one color but
        // that could change if we add more to the language
        // Right now we only expect red to be given
        // TODO: handle cases of colors other than RED being given

        //blue indicates no more chemicals yet, introduce randomness
        ChemicalCell.ChemicalType color = cellEntry.getValue().get(0);
        if(color == ChemicalCell.ChemicalType.BLUE) {
            return buildMove(cellEntry.getKey(), previousState, true);
        }
        else {
            return buildMove(cellEntry.getKey(), previousState, false);
        }
    }

    // TODO: adjust to check for zig zag state
    private Move noColorMove(ArrayList<DirectionType> possibleDirections,
                                           DirectionType prevDir,
                                           DirectionType prevOrthDir,
                                           Byte previousState) {
        // TODO: check for states of different set moves (ex: diagonal, slope)

        // default behavior
        if (possibleDirections.contains(prevDir)) {
            return buildMove(prevDir, previousState, false);
        }

        if (possibleDirections.contains(prevOrthDir)) {
            return buildMove(prevOrthDir, previousState, false);
        }

        DirectionType oppOfPrevOrthDir = getOppositeDirection(prevOrthDir);
        if (possibleDirections.contains(oppOfPrevOrthDir)) {
            return buildMove(oppOfPrevOrthDir, previousState, false);
        }

        simPrinter.println("Error in noColorDirection => agent is repeating points!");
        return buildMove(getOppositeDirection(prevDir), previousState, false);
    }

    private Map<DirectionType, ArrayList<ChemicalCell.ChemicalType>> filterColorMap(
            Map<DirectionType, ChemicalCell> neighborMap) {
        Map<DirectionType, ArrayList<ChemicalCell.ChemicalType>> colorCells = new HashMap<>();

        for (Map.Entry<DirectionType, ChemicalCell> cellEntry: neighborMap.entrySet()) {
            DirectionType dir = cellEntry.getKey();
            ChemicalCell cell = cellEntry.getValue();
            Map<ChemicalCell.ChemicalType, Double> concentrations = cell.getConcentrations();
            for (Map.Entry<ChemicalCell.ChemicalType, Double> conc: concentrations.entrySet()) {
                if (conc.getValue() >= COLOR_THRESHOLD) {
                    if (!colorCells.containsKey(dir)) {
                        colorCells.put(dir, new ArrayList<>());
                    }
                    colorCells.get(dir).add(conc.getKey());
                }
            }
        }
        return colorCells;
    }

    // TODO: adjust to take into account not changing previousState
    private Move buildMove(DirectionType dir, Byte previousState, boolean onOurOwn) {
        Move move = new Move();
        move.directionType = dir;

        // can add more changes to the state byte here
        Byte newState = updatePrevDirBits(previousState, dir, onOurOwn);

        move.currentState = newState;
        simPrinter.println("AGENT IS GOING " + dir.toString());
        return move;
    }

    private Map<ChemicalCell.ChemicalType, DirectionType> getChemicalDirections(DirectionType prevDir) {
        Map<ChemicalCell.ChemicalType, DirectionType> chemicalDirs = new HashMap<>();
        // order of elements chemicalTypes and directionTypes is critical to making sure
        // values map correctly for both agent and controller
        ChemicalCell.ChemicalType[] chemicalTypes = {
                ChemicalCell.ChemicalType.RED,
                ChemicalCell.ChemicalType.GREEN,
                ChemicalCell.ChemicalType.BLUE
        };

        DirectionType[] directionTypes = {
                DirectionType.NORTH,
                DirectionType.EAST,
                DirectionType.SOUTH,
                DirectionType.WEST
        };

        int dirIndex = 0;
        for (int i = 0; i < chemicalTypes.length; i++) {
            if (directionTypes[dirIndex] == prevDir) {
                dirIndex++;
            }
            chemicalDirs.put(chemicalTypes[i], directionTypes[dirIndex]);
            dirIndex++;
        }
        return chemicalDirs;
    }

    private Byte updatePrevDirBits(Byte previousState, DirectionType dir, boolean onOurOwn) {
        byte b = previousState.byteValue();
        String prevStateStr = String.format("%8s", Integer.toBinaryString(b & 0xFF))
                .replace(' ', '0');
        char[] prevStateChars = prevStateStr.toCharArray();

        if (dir == DirectionType.NORTH || dir == DirectionType.SOUTH) {
            prevStateChars[5] = '0';
            prevStateChars[6] = dirToNSChar(dir);
        }
        else if (dir == DirectionType.EAST || dir == DirectionType.WEST) {
            prevStateChars[5] = '1';
            prevStateChars[7] = dirToEWChar(dir);
        }

        if(onOurOwn) {
            prevStateChars[4] = '1';
        }

        String newStateStr = String.valueOf(prevStateChars);
        return Byte.parseByte(newStateStr, 2);
    }

    private ArrayList<DirectionType> getPossibleDirections(Map<DirectionType, ChemicalCell> neighborMap) {
        ArrayList<DirectionType> directions = new ArrayList<>();
        for (Map.Entry<DirectionType, ChemicalCell> cellEntry: neighborMap.entrySet()) {
            ChemicalCell cell = cellEntry.getValue();
            if (cell.isOpen()) {
                directions.add(cellEntry.getKey());
            }
        }
        return directions;
    }


    private DirectionType[] getPrevAndPrevOrthDir(Byte prevState) {
        byte b = prevState.byteValue();
        String prevStateStr = String.format("%8s", Integer.toBinaryString(b & 0xFF))
                .replace(' ', '0');
        DirectionType[] prevDirs = new DirectionType[2];

        if (prevStateStr.charAt(5) == '0') {
            prevDirs[0] = charToNSDir(prevStateStr.charAt(6));
            prevDirs[1] = charToEWDir(prevStateStr.charAt(7));
        }
        else {
            prevDirs[0] = charToEWDir(prevStateStr.charAt(7));
            prevDirs[1] = charToNSDir(prevStateStr.charAt(6));
        }
        return prevDirs;
    }

    private boolean getChemicalStatus(Byte previousState) {
        byte b = previousState.byteValue();
        String prevStateStr = String.format("%8s", Integer.toBinaryString(b & 0xFF))
                .replace(' ', '0');

        if(prevStateStr.charAt(4) == '0') {
            return false;
        }
        else {
            return true;
        }
    }

    DirectionType charToNSDir(char c) {
        switch (c) {
            case '0':
                return DirectionType.NORTH;
            case '1':
                return DirectionType.SOUTH;
            default:
                simPrinter.println("Error in passing to charToNSDir");
                return DirectionType.NORTH;
        }
    }

    char dirToNSChar(DirectionType dir) {
        switch (dir) {
            case NORTH:
                return '0';
            case SOUTH:
                return '1';
            default:
                simPrinter.println("Error in passing to dirToNSChar");
                return '0';
        }
    }

    DirectionType charToEWDir(char c) {
        switch (c) {
            case '0':
                return DirectionType.EAST;
            case '1':
                return DirectionType.WEST;
            default:
                simPrinter.println("Error in passing to charToEWDir");
                return DirectionType.EAST;
        }
    }

    char dirToEWChar(DirectionType dir) {
        switch (dir) {
            case EAST:
                return '0';
            case WEST:
                return '1';
            default:
                simPrinter.println("Error in passing to dirToEWChar");
                return '0';
        }
    }

    private byte directionToByte(DirectionType dir) {
        switch (dir) {
            case NORTH: return (byte) 0;
            case EAST: return (byte) 1;
            case SOUTH: return (byte) 2;
            case WEST: return (byte) 3;
        }
        return (byte) 0;
    }

    private DirectionType[] getOrthogonalDirections(DirectionType dir) {
        DirectionType[] horizontal = {DirectionType.WEST, DirectionType.EAST};
        DirectionType[] vertical = {DirectionType.NORTH, DirectionType.SOUTH};

        switch (dir) {
            case NORTH:
            case SOUTH:
                return horizontal;
            default:
                return vertical;
        }
    }

    private DirectionType getOppositeDirection(DirectionType dir) {
        switch (dir) {
            case NORTH:
                return DirectionType.SOUTH;
            case SOUTH:
                return DirectionType.NORTH;
            case WEST:
                return DirectionType.EAST;
            case EAST:
                return DirectionType.WEST;
            default:
                return DirectionType.CURRENT;
        }
    }

    private ChemicalCell[] directionsToChemCell(DirectionType[] dirs, Map<DirectionType, ChemicalCell> neighborMap) {
        ChemicalCell[] cells = new ChemicalCell[dirs.length];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = neighborMap.get(dirs[i]);
        }
        return cells;
    }
}