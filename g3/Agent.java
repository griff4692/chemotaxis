package chemotaxis.g3;

import java.util.Map;
import chemotaxis.g3.Language.Translator;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;

public class Agent extends chemotaxis.sim.Agent {

    private Translator trans = null;
    private int rand = 0;

    /**
     * Agent constructor
     *
     * @param simPrinter  simulation printer
     *
     */
	public Agent(SimPrinter simPrinter) {
        super(simPrinter);
        trans = Translator.getInstance();
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
        Move move = new Move();
        this.rand = randomNum;
        Integer prevByte = (int) previousState;
        char[] nextState = new char[9];
        
        // check for new instruction first
        String instructionCheck = checkForInstructions(currentCell, neighborMap);
        String prevState = null;
        
        // if instruction exists, get the new state
        if (instructionCheck != null) {
            prevState = trans.getState(instructionCheck, prevByte);
            simPrinter.println("New instruction: " + instructionCheck + " " + prevState);
        }
        else {
            // if no instruction exists, keep running with the previous state
            prevState = trans.getState(prevByte);
            simPrinter.println("No instruction: " + prevState);
        }

        //  X    ±    N   [.*]  Y    ±    M   [.*] [C/R]
        // '0', '+', '0', '*', '0', '+', '0', '.', 'C'
        //  0    1    2    3    4    5    6    7    8
        
        // based on the prevState, check the surroundings and find an opening for the next move 
        if (prevState.equals("pause")) {
            simPrinter.println("Agent is paused");
            move.directionType = DirectionType.CURRENT;
            nextState = "pause".toCharArray();
        }
        if (prevState.equals("0+0*0+0.R") && instructionCheck == null) {
            simPrinter.println("Agent left with no instructionn, wandering");
            // nextState="0+0*0+0.R".toCharArray();
            // move.directionType = DirectionType.CURRENT;
            if (upOpen(neighborMap)) {
                nextState = "0+0.0+1*R".toCharArray();
                move.directionType = DirectionType.EAST;
            }
            else if (downOpen(neighborMap)) {
                nextState = "0+0.0-1*R".toCharArray();
                move.directionType = DirectionType.WEST;
            }
            else if (rightOpen(neighborMap)) {
                nextState = "0+1*0+0.R".toCharArray();
                move.directionType = DirectionType.SOUTH;
            }
            else if (leftOpen(neighborMap)) {
                nextState = "0-1*0+0.R".toCharArray();
                move.directionType = DirectionType.NORTH;
            }
            
        }
        else if (mobilityUp(prevState, neighborMap)) {
            simPrinter.println("Agent can + should move east/up");
            nextState =  moveInY(nextState, prevState);
            move.directionType = DirectionType.EAST;
        }
        else if (mobilityDown(prevState, neighborMap)) {
            simPrinter.println("Agent can + should move west/down");
            nextState =  moveInY(nextState, prevState);
            move.directionType = DirectionType.WEST;
        }
        else if (mobilityLeft(prevState, neighborMap)) {
            simPrinter.println("Agent can + should move north/left");
            nextState = moveInX(nextState, prevState);
            move.directionType = DirectionType.NORTH;
        }
        else if (mobilityRight(prevState, neighborMap)) {
            simPrinter.println("Agent can + should move south/right");
            nextState = moveInX(nextState, prevState);
            move.directionType = DirectionType.SOUTH;
        }
        else if (mobilityUpCycle(prevState, neighborMap)) {
            simPrinter.println("Agent may be blocked, repeating cycle up");
            nextState =  moveInY(nextState, prevState);
            move.directionType = DirectionType.EAST;
        }
        else if (mobilityDownCycle(prevState, neighborMap)) {
            simPrinter.println("Agent may be blocked, repeating cycle down");
            nextState =  moveInY(nextState, prevState);
            move.directionType = DirectionType.WEST;
        }
        else if (mobilityLeftCycle(prevState, neighborMap)) {
            simPrinter.println("Agent may be blocked, repeating cycle left");
            nextState = moveInX(nextState, prevState);
            move.directionType = DirectionType.NORTH;
        }
        else if (mobilityRightCycle(prevState, neighborMap)) {
            simPrinter.println("Agent may be blocked, repeating cycle right");
            nextState = moveInX(nextState, prevState);
            move.directionType = DirectionType.SOUTH;
        }
        // else if (blocked(prevState, neighborMap)) {
        //     nextState = moveWithBlock(prevState, neighborMap);
        //     move.directionType = blockMoveDirection(nextState);
        // }
        else {
            simPrinter.println("Agent was paused");
            nextState = "pause".toCharArray();
            move.directionType = DirectionType.CURRENT;
        }
        
        // translate to Byte for memory 
        Byte nextByte = trans.getByte(nextState);
        simPrinter.println("Byte for next round: " + nextByte);
        simPrinter.println("State for next round: " + String.valueOf(nextState));
        move.currentState = nextByte;
		return move;
    }
    
    private String checkForInstructions(ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        char[] directions = new char[] {'_','_','_','_'};

        if (currentCell.getConcentration(ChemicalType.RED) == 1)
            directions[1] = 'R';
        if (currentCell.getConcentration(ChemicalType.GREEN) == 1)
            directions[2] = 'G';
        if (currentCell.getConcentration(ChemicalType.BLUE) == 1)
            directions[3] = 'B';

        char temp = ' ';
        for (DirectionType directionType : neighborMap.keySet()) {
            if (directionType == DirectionType.SOUTH) 
                temp = 'r';
            else if (directionType == DirectionType.NORTH) 
                temp = 'l';
            else if (directionType == DirectionType.EAST) 
                temp = 'u';
            else
                temp = 'd';

            if (neighborMap.get(directionType).getConcentration(ChemicalType.RED) == 1) {
                directions[0] = temp;
                directions[1] = 'R';
            }
            if (neighborMap.get(directionType).getConcentration(ChemicalType.GREEN) == 1) {
                directions[0] = temp;
                directions[2] = 'G';
            }
            if (neighborMap.get(directionType).getConcentration(ChemicalType.BLUE) == 1) {
                directions[0] = temp;
                directions[3] = 'B';
            }
            if (!(String.valueOf(directions).equals("____"))) 
                break;
        }

        if (!(String.valueOf(directions).equals("____")))
            return String.valueOf(directions);
        return null;
    }

    private Boolean mobilityUp(String state, Map<DirectionType, ChemicalCell> surroundings) {
        // There's space above this cell
        // and the instuction is moving in that direction
        // and moving up is possible according to state
        // and either – not resetting AND you can move up in this cycle 
        //         or – are resetting AND you have not maxed out moves in that axis for this cycle 
        if (followingWall(state)) return false;
        return (state.charAt(5) == '+'
                && ((state.charAt(8) == 'C' && state.charAt(4) <= state.charAt(6) - 1) 
                    || (state.charAt(8) == 'R' && state.charAt(4) == state.charAt(6) - 1))
                && upOpen(surroundings)
        );
    }

    private Boolean mobilityUpCycle(String state, Map<DirectionType, ChemicalCell> surroundings) {
        if (followingWall(state)) return false;
        return (state.charAt(5) == '+' && state.charAt(6) >= '1' && upOpen(surroundings));
    }

    private Boolean upOpen(Map<DirectionType, ChemicalCell> surroundings) {
        return surroundings.get(DirectionType.EAST).isOpen();
    }

    private Boolean mobilityDown(String state, Map<DirectionType, ChemicalCell> surroundings) {
        if (followingWall(state)) return false;
        return (state.charAt(5) == '-'
                && ((state.charAt(8) == 'C' && state.charAt(4) <= state.charAt(6) - 1) 
                    || (state.charAt(8) == 'R' && state.charAt(4) == state.charAt(6) - 1))
                && downOpen(surroundings)
        );
    }

    private Boolean mobilityDownCycle(String state, Map<DirectionType, ChemicalCell> surroundings) {
        if (followingWall(state)) return false;
        return (state.charAt(5) == '-' && state.charAt(6) >= '1' && downOpen(surroundings));
    }

    private Boolean downOpen(Map<DirectionType, ChemicalCell> surroundings) {
        return surroundings.get(DirectionType.WEST).isOpen();
    }

    private Boolean mobilityLeft(String state, Map<DirectionType, ChemicalCell> surroundings) {
        if (followingWall(state)) return false;
        return (state.charAt(1) == '-'
                && ((state.charAt(8) == 'C' && state.charAt(0) <= state.charAt(2) - 1) 
                    || (state.charAt(8) == 'R' && state.charAt(0) == state.charAt(2) - 1))
                && leftOpen(surroundings)
        );
    }

    private Boolean mobilityLeftCycle(String state, Map<DirectionType, ChemicalCell> surroundings) {
        if (followingWall(state)) return false;
        return (state.charAt(1) == '-' && state.charAt(2) >= '1' && leftOpen(surroundings));
    }

    private Boolean leftOpen(Map<DirectionType, ChemicalCell> surroundings) {
        return surroundings.get(DirectionType.NORTH).isOpen();
    }

    private Boolean mobilityRight(String state, Map<DirectionType, ChemicalCell> surroundings) {
        if (followingWall(state)) return false;
        return (state.charAt(1) == '+'
                && ((state.charAt(8) == 'C' && state.charAt(0) <= state.charAt(2) - 1) 
                    || (state.charAt(8) == 'R' && state.charAt(0) == state.charAt(2) - 1))
                && rightOpen(surroundings)
        );
    }

    private Boolean mobilityRightCycle(String state, Map<DirectionType, ChemicalCell> surroundings) {
        if (followingWall(state)) return false;
        return (state.charAt(1) == '+' && state.charAt(2) >= '1' && rightOpen(surroundings));
    }

    private Boolean rightOpen(Map<DirectionType, ChemicalCell> surroundings) {
        return surroundings.get(DirectionType.SOUTH).isOpen();
    }

    private Boolean blocked(String state, Map<DirectionType, ChemicalCell> surroundings) {
        return (followingWall(state) || blockedInX(surroundings) || blockedInY(surroundings));
    }
    
    private char[] moveWithBlock(String prevState, Map<DirectionType, ChemicalCell> surroundings) {
        // [±X] [±Y] [BXxYy] [UDLR] W
        //  O    1      2      3    4 
        // previously blocked --> W 
        if (followingWall(prevState)) {
            if (blockedInX(surroundings) || blockedInY(surroundings)) { // currently blocked?
                // getOrder prioritizes different movement directions
                // based on where you came from last 
                simPrinter.println("Previously blocked, still blocked");
                char[] order = getOrder(prevState);
                for (char o : order) {
                    if (o == 'U' && upOpen(surroundings)) {
                        return new char[] {prevState.charAt(0), prevState.charAt(1), prevState.charAt(2),'U','W'};
                    }
                    else if (o == 'D' && downOpen(surroundings)) {
                        return new char[] {prevState.charAt(0), prevState.charAt(1), prevState.charAt(2),'D','W'};
                    }
                    else if (o == 'L' && leftOpen(surroundings)) {
                        return new char[] {prevState.charAt(0), prevState.charAt(1), prevState.charAt(2),'L','W'};
                    }
                    else if (o == 'R' && rightOpen(surroundings)) {
                        return new char[] {prevState.charAt(0), prevState.charAt(1), prevState.charAt(2),'R','W'};
                    }
                }
            }
            else { // no longer blocked?
                // translate out of blocked notation 
                char[] order = getOrder(prevState);
                char[] partial = getDirection(prevState);
                simPrinter.println("Previously blocked, no longer blocked: " + String.valueOf(order) + String.valueOf(partial));
                for (char o : order) {
                    if (o == 'U' && upOpen(surroundings)) {
                        char[] ret = new char[9];
                        simPrinter.println("U: " + String.valueOf(moveInY(ret,String.valueOf(partial))));
                        return moveInY(ret,String.valueOf(partial));
                    }
                    else if (o == 'D' && downOpen(surroundings)) {
                        char[] ret = new char[9];
                        simPrinter.println("D: " + String.valueOf(moveInY(ret,String.valueOf(partial))));
                        return moveInY(ret,String.valueOf(partial));
                    }
                    else if (o == 'L' && leftOpen(surroundings)) {
                        char[] ret = new char[9];
                        simPrinter.println("L: " + String.valueOf(moveInX(ret,String.valueOf(partial))));
                        return moveInX(ret,String.valueOf(partial));
                    }
                    else if (o == 'R' && rightOpen(surroundings)) {
                        char[] ret = new char[9];
                        simPrinter.println("R: " + String.valueOf(moveInX(ret,String.valueOf(partial))));
                        return moveInX(ret,String.valueOf(partial));
                    }
                }
            }          
        }
        else { // not previously blocked --> C/R 
            // translate to blocked notation
            simPrinter.println("Not previously blocked, now blocked");
            String translatedState = translateBlocked(prevState);
            char[] order = getOrder(translatedState);
            for (char o : order) {
                if (o == 'U' && upOpen(surroundings)) {
                    return new char[] {translatedState.charAt(0), translatedState.charAt(1), translatedState.charAt(2),'U','W'};
                }
                else if (o == 'D' && downOpen(surroundings)) {
                    return new char[] {translatedState.charAt(0), translatedState.charAt(1), translatedState.charAt(2),'D','W'};
                }
                else if (o == 'L' && leftOpen(surroundings)) {
                    return new char[] {translatedState.charAt(0), translatedState.charAt(1), translatedState.charAt(2),'L','W'};
                }
                else if (o == 'R' && rightOpen(surroundings)) {
                    return new char[] {translatedState.charAt(0), translatedState.charAt(1), translatedState.charAt(2),'R','W'};
                }
            }
        }
        return null;
    }
    
    private String translateBlocked(String s) {
        char[] ret = {'+','+','.','.','W'};
        if (s.charAt(1) == '-') ret[0] = '-';
        if (s.charAt(5) == '-') ret[1] = '-';
        if (s.charAt(1) == '+' && s.charAt(3) == '*') ret[3] = 'R';
        else if (s.charAt(1) == '-' && s.charAt(3) == '*') ret[3] = 'L';
        else if (s.charAt(5) == '+' && s.charAt(7) == '*') ret[3] = 'U';
        else if (s.charAt(5) == '-' && s.charAt(7) == '*') ret[3] = 'D';
        if (s.charAt(2) == '1' && s.charAt(6) == '1') ret[2] = 'B';
        else if (s.charAt(2) == '3' && s.charAt(6) == '1') ret[2] = 'x';
        else if (s.charAt(2) == '2' && s.charAt(6) == '1') ret[2] = 'x';
        else if (s.charAt(2) == '1' && s.charAt(6) == '3') ret[2] = 'y';
        else if (s.charAt(2) == '1' && s.charAt(6) == '2') ret[2] = 'y';
        else if (s.charAt(2) == '1' && s.charAt(6) == '0') { ret[2] = 'X'; ret[1] = '.'; }
        else if (s.charAt(2) == '0' && s.charAt(6) == '1') { ret[2] = 'Y'; ret[0] = '.'; }
        return String.valueOf(ret);
    }

    private DirectionType blockMoveDirection(char[] nextState) {
        if (nextState[4] == 'W') {
            if (nextState[3] == 'R') return DirectionType.SOUTH;
            else if (nextState[3] == 'L') return DirectionType.NORTH;
            else if (nextState[3] == 'U') return DirectionType.EAST;
            else return DirectionType.WEST;
        }
        else {
            if (nextState[3] == '*' && nextState[7] == '.') {
                if (nextState[1] == '+') return DirectionType.SOUTH;
                else return DirectionType.NORTH;
            }
            else {
                if (nextState[5] == '+') return DirectionType.EAST;
                else return DirectionType.WEST;
            }
        }
    }

    private Boolean blockedInX(Map<DirectionType, ChemicalCell> surroundings) {
        return (!rightOpen(surroundings) || !leftOpen(surroundings));
    }

    private Boolean blockedInY(Map<DirectionType, ChemicalCell> surroundings) {
        return (!upOpen(surroundings) || !downOpen(surroundings));
    }

    // creates new state string according to translation laws, preserving direction
    private char[] moveInX(char[] nextState, String prevState) {
        if (needsToRepeat(prevState)) {
            nextState = new char[] { '0', prevState.charAt(1), prevState.charAt(2), '*',
                                     '0', prevState.charAt(5), prevState.charAt(6), '.', 'C'};
            if (nextState[2] == '0' || nextState[6] == '0')
                nextState[8] = 'R';
        }
        else {
            nextState = new char[] { (char)((int) prevState.charAt(0) + 1), prevState.charAt(1), prevState.charAt(2), '*',
                                     prevState.charAt(4), prevState.charAt(5), prevState.charAt(6), '.', 'C'};
            if ((nextState[4] == (char)((int)nextState[6] - 1) && nextState[0] == nextState[2]) ||
                (nextState[0] == (char)((int)nextState[2] - 1) && nextState[4] == nextState[6]))
                nextState[8] = 'R';
        } 
        if (!trans.validByte(nextState)) {
            simPrinter.println("Invalid state was created: " + nextState);
            nextState = new char[] { '0', prevState.charAt(1), prevState.charAt(2), '*',
                                     '0', prevState.charAt(5), prevState.charAt(6), '.', 'C'};
            if (nextState[2] == '0' || nextState[6] == '0')
                nextState[8] = 'R';
        }
        return nextState;
    }

    private char[] moveInY(char[] nextState, String prevState) {
        if (needsToRepeat(prevState)) {
            nextState = new char[] { '0', prevState.charAt(1), prevState.charAt(2), '.',
                                     '0', prevState.charAt(5), prevState.charAt(6), '*', 'C'};
            if (nextState[2] == '0' || nextState[6] == '0')
                nextState[8] = 'R';
        }
        else {
            nextState = new char[] { prevState.charAt(0), prevState.charAt(1), prevState.charAt(2), '.',
                                     (char)((int) prevState.charAt(4) + 1), prevState.charAt(5), prevState.charAt(6), '*', 'C'};
            if ((nextState[4] == (char)((int)nextState[6] - 1) && nextState[0] == nextState[2]) ||
                (nextState[0] == (char)((int)nextState[2] - 1) && nextState[4] == nextState[6]))
                nextState[8] = 'R';
        }
        if (!trans.validByte(nextState)) {
            simPrinter.println("Invalid state was created: " + nextState);
            nextState = new char[] { '0', prevState.charAt(1), prevState.charAt(2), '.',
                                     '0', prevState.charAt(5), prevState.charAt(6), '*', 'C'};
            if (nextState[2] == '0' || nextState[6] == '0')
                nextState[8] = 'R';
        }
        return nextState;
    }

    private Move followGradient(ChemicalCell cellPositon, Map<DirectionType, ChemicalCell> cellMap) {
        Move gradientPath = new Move();
        ChemicalType chemicalRed = ChemicalType.BLUE;
        gradientPath.directionType = getGradientDirection(chemicalRed, cellPositon, cellMap);
        return gradientPath;
    }

    private DirectionType getGradientDirection(ChemicalType chemicalColor, ChemicalCell cellPositon, Map<DirectionType, ChemicalCell> cellMap) {
        DirectionType maxGradientDirection = DirectionType.CURRENT;
        double highestConcentration = cellPositon.getConcentration(chemicalColor);
		for(DirectionType directionType : cellMap.keySet()) {
			if(highestConcentration < cellMap.get(directionType).getConcentration(chemicalColor)) {
				highestConcentration = cellMap.get(directionType).getConcentration(chemicalColor);
				maxGradientDirection = directionType;
			}
        }
        return maxGradientDirection;
    }


    private Boolean needsToRepeat(String state) {
        return state.charAt(8) == 'R';
    }

    private Boolean followingWall(String state) {
        return state.charAt(4) == 'W';
    }

    private char getMajorAxis(String state) {
        if (state.charAt(8) == 'R' || state.charAt(8) == 'C') {
            if (state.charAt(2) > state.charAt(6)) return 'X';
            else if (state.charAt(2) < state.charAt(6)) return 'Y';
            else if (rand >= 0) return 'X';
            return 'Y';
        }
        else if (state.charAt(8) == 'W') {
            if (state.charAt(4) == '*') return 'X';
            return 'Y';
        }
        else {
            if (rand >= 0) return 'X';
            return 'Y';
        }
    }

    private char[] getDirection(String s) {
        // [±X] [±Y] [BXxYy] [UDLR] W
        //  O    1      2      3    4 
        if (s.charAt(2) == 'B') { // 1 1 
            if (s.charAt(3) == 'U' || s.charAt(3) == 'D') 
                return new char[] {'0',s.charAt(0),'1','.','0',s.charAt(1),'1','*','C'};
            return new char[] {'0',s.charAt(0),'1','*','0',s.charAt(1),'1','.','C'};
        }
        else if (s.charAt(2) == 'X') { // 1 0 
            // if (s.charAt(3) == 'L') return new char[] {'0','-','1','*','0','+','0','.','R'};
            // return new char[] {'0','+','1','*','0','+','0','.','R'};
            // if went up or down, go diagonally in the prev direction
            if (s.charAt(3) == 'U') 
                return new char[] {'0',s.charAt(0),'1','.','0','-','1','*','C'};
            if (s.charAt(3) == 'D') 
                return new char[] {'0',s.charAt(0),'1','.','0','+','1','*','C'};
            return new char[] {'0',s.charAt(0),'1','*','0','+','0','.','R'};
        }
        else if (s.charAt(2) == 'Y') { // 0 1 
            // if (s.charAt(3) == 'D') return new char[] {'0','+','0','.','0','-','1','*','R'};
            // return new char[] {'0','+','0','.','0','+','1','*','R'};
            if (s.charAt(3) == 'R') 
                return new char[] {'0','-','1','*','0',s.charAt(1),'1','.','C'};
            if (s.charAt(3) == 'L') 
                return new char[] {'0','+','1','*','0',s.charAt(1),'1','.','C'};
            return new char[] {'0','+','0','.','0',s.charAt(1),'1','*','R'};
        }
        else if (s.charAt(2) == 'x') { // 3 1 
            if (s.charAt(3) == 'U' || s.charAt(3) == 'D') 
                return new char[] {'0',s.charAt(0),'3','.','0',s.charAt(1),'1','*','C'};
            return new char[] {'0',s.charAt(0),'3','*','0',s.charAt(1),'1','.','C'};
        }
        else if (s.charAt(2) == 'y') { // 1 3 
            if (s.charAt(3) == 'U' || s.charAt(3) == 'D') 
                return new char[] {'0',s.charAt(0),'1','.','0',s.charAt(1),'3','*','C'};
            return new char[] {'0',s.charAt(0),'1','*','0',s.charAt(1),'3','.','C'};
        }
        return null;
    }
    
    private char[] getOrder(String s) {
        // [±X] [±Y] [BXxYy] [UDLR] W
        //  O    1      2      3    4 
        char[] ret = s.toCharArray();
        if (s.charAt(2) == 'B') { // 1 1 
        // if (s.charAt(2) == 'B' || s.charAt(2) == 'y' || s.charAt(2) == 'x') { // 1 1 
            if (s.charAt(0) == '+' && s.charAt(1) == '+') {
                if (ret[3] == 'U') return new char[] {'U','R','L','D'};
                else if (ret[3] == 'D') return new char[] {'R','D','L','U'};
                else if (ret[3] == 'L') return new char[] {'U','L','D','R'};
                else if (ret[3] == 'R') return new char[] {'R','U','D','L'};
            }
            if (s.charAt(0) == '+' && s.charAt(1) == '-') {
                if (ret[3] == 'U') return new char[] {'R','U','L','D'};
                else if (ret[3] == 'D') return new char[] {'D','R','L','U'};
                else if (ret[3] == 'L') return new char[] {'D','L','U','R'};
                else if (ret[3] == 'R') return new char[] {'R','D','U','L'};
            }
            if (s.charAt(0) == '-' && s.charAt(1) == '+') {
                if (ret[3] == 'U') return new char[] {'U','L','R','D'};
                else if (ret[3] == 'D') return new char[] {'L','D','R','U'};
                else if (ret[3] == 'L') return new char[] {'L','U','D','R'};
                else if (ret[3] == 'R') return new char[] {'U','R','D','L'};
            }
            if (s.charAt(0) == '-' && s.charAt(1) == '-') {
                if (ret[3] == 'U') return new char[] {'L','U','R','D'};
                else if (ret[3] == 'D') return new char[] {'D','L','R','U'};
                else if (ret[3] == 'L') return new char[] {'L','D','U','R'};
                else if (ret[3] == 'R') return new char[] {'D','R','U','L'};
            }
        }
        else if (s.charAt(2) == 'X') { // 1 0 
            if (s.charAt(0) == '+') {
                if (ret[3] == 'U') return new char[] {'U','R','L','D'};
                else if (ret[3] == 'D') return new char[] {'D','R','L','U'};
                // else if (ret[3] == 'L') return new char[] {'U','L','D','R'};
                // else if (ret[3] == 'R') return new char[] {'U','R','D','L'};
                else if (ret[3] == 'L') return new char[] {'L','U','D','R'};
                else if (ret[3] == 'R') return new char[] {'R','U','D','L'};
            }
            if (s.charAt(0) == '-') {
                if (ret[3] == 'U') return new char[] {'U','L','R','D'};
                else if (ret[3] == 'D') return new char[] {'D','L','R','U'};
                // else if (ret[3] == 'L') return new char[] {'U','L','D','R'};
                // else if (ret[3] == 'R') return new char[] {'U','R','D','L'};
                else if (ret[3] == 'L') return new char[] {'L','U','D','R'};
                else if (ret[3] == 'R') return new char[] {'R','U','D','L'};
            }
        }
        else if (s.charAt(2) == 'Y') { // 0 1 
            if (s.charAt(1) == '+') {
                if (ret[3] == 'U') return new char[] {'U','R','L','D'};
                else if (ret[3] == 'D') return new char[] {'D','R','L','U'};
                // else if (ret[3] == 'L') return new char[] {'U','L','D','R'};
                // else if (ret[3] == 'R') return new char[] {'U','R','D','L'};
                else if (ret[3] == 'L') return new char[] {'L','U','D','R'};
                else if (ret[3] == 'R') return new char[] {'R','U','D','L'};
            }
            if (s.charAt(1) == '-') {
                if (ret[3] == 'U') return new char[] {'U','R','L','D'};
                else if (ret[3] == 'D') return new char[] {'D','R','L','U'};
                // else if (ret[3] == 'L') return new char[] {'L','D','U','R'};
                // else if (ret[3] == 'R') return new char[] {'R','D','U','L'};
                else if (ret[3] == 'L') return new char[] {'D','L','U','R'};
                else if (ret[3] == 'R') return new char[] {'D','R','U','L'};
            }
        }
        else if (s.charAt(2) == 'x') { // 3 1 
            if (s.charAt(0) == '+' && s.charAt(1) == '+') {
                if (ret[3] == 'U') return new char[] {'R','U','D','L'};
                else if (ret[3] == 'D') return new char[] {'R','D','U','L'};
                else if (ret[3] == 'L') return new char[] {'U','D','R','L'};
                else if (ret[3] == 'R') return new char[] {'R','U','D','L'};
            }
            if (s.charAt(0) == '+' && s.charAt(1) == '-') {
                if (ret[3] == 'U') return new char[] {'R','U','D','L'};
                else if (ret[3] == 'D') return new char[] {'R','D','U','L'};
                else if (ret[3] == 'L') return new char[] {'D','U','R','L'};
                else if (ret[3] == 'R') return new char[] {'R','D','U','L'};
            }
            if (s.charAt(0) == '-' && s.charAt(1) == '+') {
                if (ret[3] == 'U') return new char[] {'L','U','D','R'};
                else if (ret[3] == 'D') return new char[] {'L','D','U','R'};
                else if (ret[3] == 'L') return new char[] {'L','U','D','R'};
                else if (ret[3] == 'R') return new char[] {'U','D','L','R'};
            }
            if (s.charAt(0) == '-' && s.charAt(1) == '-') {
                if (ret[3] == 'U') return new char[] {'L','U','D','R'};
                else if (ret[3] == 'D') return new char[] {'L','D','U','R'};
                else if (ret[3] == 'L') return new char[] {'L','D','U','R'};
                else if (ret[3] == 'R') return new char[] {'D','U','L','R'};
            }
        }
        else if (s.charAt(2) == 'y') { // 1 3 
            if (s.charAt(0) == '+' && s.charAt(1) == '+') {
                if (ret[3] == 'U') return new char[] {'U','R','L','D'};
                else if (ret[3] == 'D') return new char[] {'L','R','U','D'};
                else if (ret[3] == 'L') return new char[] {'U','L','R','D'};
                else if (ret[3] == 'R') return new char[] {'U','R','L','D'};
            }
            if (s.charAt(0) == '+' && s.charAt(1) == '-') {
                if (ret[3] == 'U') return new char[] {'R','L','D','U'};
                else if (ret[3] == 'D') return new char[] {'D','R','L','U'};
                else if (ret[3] == 'L') return new char[] {'D','L','R','U'};
                else if (ret[3] == 'R') return new char[] {'D','R','L','U'};
            }
            if (s.charAt(0) == '-' && s.charAt(1) == '+') {
                if (ret[3] == 'U') return new char[] {'U','L','R','D'};
                else if (ret[3] == 'D') return new char[] {'L','R','U','D'};
                else if (ret[3] == 'L') return new char[] {'U','L','R','D'};
                else if (ret[3] == 'R') return new char[] {'U','R','L','D'};
            }
            if (s.charAt(0) == '-' && s.charAt(1) == '-') {
                if (ret[3] == 'U') return new char[] {'L','R','D','U'};
                else if (ret[3] == 'D') return new char[] {'D','L','R','U'};
                else if (ret[3] == 'L') return new char[] {'D','L','R','U'};
                else if (ret[3] == 'R') return new char[] {'D','R','L','U'};
            }
        }
        return null;
    }

}