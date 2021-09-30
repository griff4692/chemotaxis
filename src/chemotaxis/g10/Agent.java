package chemotaxis.g10; // TODO modify the package name to reflect your team

import java.util.ArrayList;
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

   private final ChemicalCell.ChemicalType [] CHEM_TYPES = {
           ChemicalCell.ChemicalType.RED,
           ChemicalCell.ChemicalType.GREEN,
           ChemicalCell.ChemicalType.BLUE
   };

   private final ChemicalCell.ChemicalType [] DIR_TYPES = {
           ChemicalCell.ChemicalType.RED,
           ChemicalCell.ChemicalType.GREEN,
           ChemicalCell.ChemicalType.BLUE
   };

   final double CHANGE_CHEM_THRESHOLD = 0.01;


   public Agent(SimPrinter simPrinter) {
      super(simPrinter);
   }

   private DirectionType getDirectionFromState(Byte previousState) {
      if (previousState == 0){
         return DirectionType.CURRENT;
      }
      else if (previousState == 1){
         return DirectionType.NORTH;
      }
      else if (previousState == 2){
         return DirectionType.SOUTH;
      }
      else if (previousState == 3){
         return DirectionType.EAST;
      }
      else {
         return DirectionType.WEST;
      }
   }

   private Byte getStateFromDirection(DirectionType direction) {
      if (direction == DirectionType.CURRENT) {
         return 0;
      }
      if (direction == DirectionType.NORTH){
         return 1;
      }
      else if (direction == DirectionType.SOUTH){
         return 2;
      }
      else if (direction == DirectionType.EAST){
         return 3;
      }
      else{
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
   public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap)
   {
      Move move = new Move();

      // Extract data from memory
      int chemState = previousState & 3;
      ChemicalCell.ChemicalType chemType = CHEM_TYPES[chemState];
      int prevMoveDirState = (previousState >> 2) & 7;
      DirectionType opPrevMoveDir = getOppositeDirection(getDirectionFromState((byte) prevMoveDirState));

      // Check if at top of grad
      Double currConc = currentCell.getConcentration(chemType);
      boolean top = true;
      for (Map.Entry<DirectionType, ChemicalCell> entry : neighborMap.entrySet()) {
         if (entry.getKey() != opPrevMoveDir && currConc <= entry.getValue().getConcentration(chemType)) {
            top = false;
            break;
         }
      }

      if (top) {
         chemState = (chemState + 1) % 3;
         chemType = CHEM_TYPES[chemState];
      }

      // Check for open cells (pipe-case)
      ArrayList<Map.Entry<DirectionType, ChemicalCell>> openCells = new ArrayList<>();
      for (Map.Entry<DirectionType, ChemicalCell> entry : neighborMap.entrySet()) {
         if (entry.getValue().isOpen()) {
            openCells.add(entry);
         }
      }

      if(openCells.size() == 1) {
         move.directionType = openCells.get(0).getKey();
         move.currentState = (byte) ((getStateFromDirection(move.directionType) << 2) | chemState);
      }
      else if(openCells.size() == 2) {
         for(Map.Entry<DirectionType, ChemicalCell> entry : openCells) {
            if(entry.getKey() != opPrevMoveDir) {
               move.directionType = entry.getKey();
               move.currentState = (byte) ((getStateFromDirection(move.directionType) << 2) | chemState);
               break;
            }
         }
      }
      else {
         // Walk up grad
         Double maxConc = currentCell.getConcentration(chemType);
         DirectionType dirToMove = DirectionType.CURRENT;

         Double conc;
         DirectionType dir;
         for (Map.Entry<DirectionType, ChemicalCell> entry : neighborMap.entrySet()) {
            conc = entry.getValue().getConcentration(chemType);
            dir = entry.getKey();
            if (/*dir != opPrevMoveDir &&*/ conc > maxConc) {
               maxConc = conc;
               dirToMove = dir;
            }
         }

         move.directionType = dirToMove;
         move.currentState = (byte) ((getStateFromDirection(move.directionType) << 2) | chemState);

         System.out.format("Ending function: dir=%s state=%s\n", move.directionType.toString(), move.currentState.toString());
      }
      return move;
   }



   private DirectionType getOppositeDirection(DirectionType dir) {
      if (dir == DirectionType.NORTH) return DirectionType.SOUTH;
      if (dir == DirectionType.SOUTH) return DirectionType.NORTH;
      if (dir == DirectionType.EAST) return DirectionType.WEST;
      if (dir == DirectionType.WEST) return DirectionType.EAST;
      return DirectionType.CURRENT;
   }
}
