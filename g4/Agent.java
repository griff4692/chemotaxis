package chemotaxis.g4; // TODO modify the package name to reflect your team

import java.util.Map;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;
import java.util.Random;

public class Agent extends chemotaxis.sim.Agent {
    private DirectionType[] directions = new DirectionType[]{DirectionType.CURRENT, DirectionType.NORTH, DirectionType.SOUTH, DirectionType.EAST, DirectionType.WEST};
   
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
      Move move = new Move();
      move.currentState = previousState;

    
      ChemicalType chosenChemicalType = ChemicalType.BLUE;
    
      //detect if any chemical is placed by controller
      for(DirectionType directionType : neighborMap.keySet()) {
         if (neighborMap.get(directionType).getConcentration(ChemicalType.BLUE) == 1) {
           
            move.currentState = (byte)directionType.ordinal();
            move.currentState = (byte)(move.currentState.byteValue() - 13);
         }
         else if (neighborMap.get(directionType).getConcentration(ChemicalType.RED) == 1) {
           // NE-N = 5
           // NE-E = 6
           // SE-N = 7
           // SE-E = 8
           // NW-N = 9
           // NW-W = 10
           // SW-S = 11
           // SW-S = 12
           if (directionType == DirectionType.NORTH) {
             move.currentState = (byte)(5-13);
           }
           if (directionType == DirectionType.SOUTH) {
            move.currentState = (byte)(7-13);
          }
         }
         else if (neighborMap.get(directionType).getConcentration(ChemicalType.GREEN) == 1) {
          if (directionType == DirectionType.NORTH) {
            move.currentState = (byte)(9-13);
          }
          if (directionType == DirectionType.SOUTH) {
           move.currentState = (byte)(11-13);
         }
        }
       }


      if (move.currentState < 0){ //following path

        byte state = (byte)(move.currentState + 13);
        // if state > 4 then we're moving on a diagonal
        if (state > 4) {
          if (state == 5) {
            move.directionType = DirectionType.NORTH;
            move.currentState = (byte)(state + 1 - 13);
          }
          else if (state == 6) {
            move.directionType = DirectionType.EAST;
            move.currentState = (byte)(state - 1 - 13);
          }
          else if (state == 7) {
            move.directionType = DirectionType.SOUTH;
            move.currentState = (byte)(state + 1 - 13);
          }
          else if (state == 8) {
            move.directionType = DirectionType.EAST;
            move.currentState = (byte)(state - 1 - 13);
          }
          else if (state == 9) {
            move.directionType = DirectionType.NORTH;
            move.currentState = (byte)(state + 1 - 13);
          }
          else if (state == 10) {

            move.directionType = DirectionType.WEST;
            move.currentState = (byte)(state - 1 - 13);
          }
          else if (state == 11) {
            move.directionType = DirectionType.SOUTH;
            move.currentState = (byte)(state + 1 - 13);
          }
          else if (state == 12) {
            move.directionType = DirectionType.WEST;
            move.currentState = (byte)(state - 1 - 13);
          }
        }
        else {
          move.directionType = DirectionType.values()[move.currentState + 13];

        }

        return move;
      } 

      previousState = previousState;
      int wallDir = 0;
      if (previousState >= 50 && previousState < 100) {
        wallDir = (previousState - 50) / 10;
        previousState = (byte)((previousState % 10) + 50);
      }
      
      //determine if the nubmer and location of blocked neighbors
      int w = 0;
      int b = 0;
      if (previousState % 10 < 2){
        if (neighborMap.get(DirectionType.WEST).isBlocked()) {w = w+1;}
        if (neighborMap.get(DirectionType.EAST).isBlocked()) {w = w+1;}
      }
      if (previousState % 10 >=2){
        if (neighborMap.get(DirectionType.NORTH).isBlocked()) {w = w+1;}
        if (neighborMap.get(DirectionType.SOUTH).isBlocked()) {w = w+1;}
      }

      if (w == 1){
        if (neighborMap.get(DirectionType.NORTH).isBlocked()) {b = 0;}
        if (neighborMap.get(DirectionType.SOUTH).isBlocked()) {b = 10;}
        if (neighborMap.get(DirectionType.EAST).isBlocked()) {b = 20;}
        if (neighborMap.get(DirectionType.WEST).isBlocked()) {b = 30;}
      }
      
      //System.out.println(previousState);
      ////no chemical placed so far, agent navigates itself
      int[] pairs = new int[] {1, 0, 3, 2};

      //if was previously moving along a wall
      if (previousState >= 50) {
        //int leftDir = (int)left[previousState % 50];
        //DirectionType leftNeighbor = DirectionType.values()[leftDir];
        //int rightDir = (int)right[previousState % 50];
        //DirectionType rightNeighbor = DirectionType.values()[rightDir];
        boolean prevTwoWalls = (previousState >= 100) && (w<2);
        boolean prevOneWall = (previousState < 100) && (w==0);

        //if an opening is found in the wall, randomly make a turn (decice to turn left/right or move forward)
        if (prevTwoWalls || prevOneWall) {
          //System.out.println("opening found");
          Random rand = new Random();
          boolean makeTurn = false;
          for (int i = 0; i < 20; i++){
            int randomDir = rand.nextInt(4);
            DirectionType dir = DirectionType.values()[randomDir];
            if (neighborMap.get(dir).isOpen() && ((int)pairs[previousState % 50]!=randomDir) && ((int)pairs[wallDir]!=randomDir)){ //turn left or turn right
              //System.out.println(dir);
              move.directionType = dir;
              int curState = randomDir + w*50 + b;
              move.currentState = (byte)curState;
              makeTurn = true;
              return move;
            }
            /*if (!makeTurn) { //deadend, turn around 180 deg
              move.currentState = (byte)(pairs[previousState % 50] + w*50);
              move.directionType = DirectionType.values()[move.currentState % 50];
            }*/
            
          }
          
        } /*else { //no opening on wall
          //continue to move forward if not dead end
          DirectionType previous_direction = DirectionType.values()[previousState % 50];
          if (neighborMap.get(previous_direction).isOpen()) {
            move.currentState = (byte)(previousState % 50 + w*50);
            move.directionType = DirectionType.values()[move.currentState % 50];
            return move;
          }
        }*/
      }

      
      if (previousState < 5 || previousState >= 50) {
        DirectionType previous_direction = DirectionType.values()[previousState % 50];
        if (neighborMap.get(previous_direction).isBlocked()) { //wall encountered, randomly make a turn
          //System.out.println("wall");
          Random rand = new Random();
          boolean makeTurn = false;
          for (int i = 0; i < 30; i++){
            int randomDir = rand.nextInt(4);
            DirectionType dir = DirectionType.values()[randomDir];
            if (neighborMap.get(dir).isOpen() && (((int)pairs[previousState % 50]) != randomDir)){ //turn left or turn right
              move.directionType = dir;
              int curState = randomDir + w*50 + b;
              move.currentState = (byte)curState;
              makeTurn = true;
              break;
            }   
          }
          if (!makeTurn) { //deadend, turn around 180 deg
              //System.out.println("turning 180");
              move.currentState = (byte)(pairs[previousState % 50] + w*50 + b);
              move.directionType =  DirectionType.values()[(move.currentState - b) % 50];
            }
          return move;
        } else { //no wall ahead, continue to move in same direction
          move.currentState = (byte)(previousState % 50 + w*50 + b);
          move.directionType = DirectionType.values()[(move.currentState - b) % 50];
          return move;
        }
      }


      move.currentState = (byte)(previousState % 50 + w*50 + b);
      move.directionType =  DirectionType.values()[(move.currentState - b) % 50];
      /*for(int i=0; i<4; i++) {
         DirectionType dir = DirectionType.values()[i];
         if (neighborMap.get(dir).isOpen() && (dir != DirectionType.values()[previousState % 50])){
          move.directionType = dir;
          move.currentState = (byte)(i + w*50);

         }
      }*/
      return move;
      //if no chemical placed, agent navigates itself and explore around
      //case I: not following a wall, continue to head 
      //if (blockedNeighbors == 0) {
      //  if () {
      //
      //  }
      //}
      
      //randomDirection = randomNum % 4;

      //neighborMap.keySet()
    }
}
