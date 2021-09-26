package chemotaxis.g10; // TODO modify the package name to reflect your team

import java.util.Map;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;

public class Agent extends chemotaxis.sim.Agent {

   /**
    * Agent constructor
    *
    * @param simPrinter  simulation printer
    *
    */
   public Agent(SimPrinter simPrinter) {
      super(simPrinter);
   }

   private DirectionType getDirectionFromState(Byte previousState){
      if (previousState == null){
         return DirectionType.CURRENT;
      }

      if (previousState == 1){
         return DirectionType.EAST;
      }
      else if (previousState == 2){
         return DirectionType.WEST;
      }
      else if (previousState == 3){
         return DirectionType.NORTH;
      }
      else if (previousState == 4){
         return DirectionType.SOUTH;
      }
      else {
         return DirectionType.CURRENT;
      }
   }

   private Byte getStateFromDirection(DirectionType direction){
      if (direction == DirectionType.CURRENT){
         return 0;
      }
      else if (direction == DirectionType.EAST){
         return 1;
      }
      else if (direction == DirectionType.WEST){
         return 2;
      }
      else if (direction == DirectionType.NORTH){
         return 3;
      }
      else {
         return 4;
      }
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
      ChemicalCell.ChemicalType chosenChemicalType = ChemicalCell.ChemicalType.RED;
      DirectionType directionToMove = getDirectionFromState(previousState);
      Byte newState = previousState;

      int numNeighborsBlocked = 0;
      for (DirectionType directionType : neighborMap.keySet()) {
         if (neighborMap.get(directionType).isBlocked()) {
            numNeighborsBlocked++;
         }
         else if(neighborMap.get(directionType).getConcentration(chosenChemicalType) == 1.0){
            directionToMove = directionType;
            newState = getStateFromDirection(directionToMove);
         }
      }

      if (numNeighborsBlocked == 3 || numNeighborsBlocked == 2) {
         for (DirectionType directionType : neighborMap.keySet()) {
            DirectionType oppDirectionToMove = null;
            if (previousState == 1 || previousState == 3) {
               oppDirectionToMove = getDirectionFromState((byte) (previousState + 1));
            } else if (previousState == 2 || previousState == 4) {
               oppDirectionToMove = getDirectionFromState((byte) (previousState - 1));
            }

            if ((numNeighborsBlocked == 3 && neighborMap.get(directionType).isOpen()) || (numNeighborsBlocked == 2 && neighborMap.get(directionType).isOpen() && directionType != oppDirectionToMove)) {
               directionToMove = directionType;
               newState = getStateFromDirection(directionToMove);
            }
         }
      }

      move.directionType = directionToMove;
      move.currentState = newState;

      return move;
   }
}
