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
    *
    */
   public Agent(SimPrinter simPrinter) {
      super(simPrinter);
   }


   /* Bit pattern is as follows for the agent:
   Bits counted from the right to the left. The Bit Encoding so far is as follows:
   the first bits encode what color the agent has just seen and therefore and what color it should expect.
   the second, third and fourth bits encode what direction the agent has previously been in.
    */


   private byte directionToBits(DirectionType direction){
      if (direction == DirectionType.CURRENT) {
         return 0;
      }
      else if (direction == DirectionType.EAST) {
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


   private Byte saveState(DirectionType direction, ChemicalCell.ChemicalType currentColor){
      byte intermediateDirectionByte = this.directionToBits(direction);
      byte intermediateColorByte =  this.color2Byte(currentColor);
      /*Bit shift the direction Byte by one */
      intermediateDirectionByte = (byte) (intermediateDirectionByte<<1);
      /*Combine the two Bytes now using the bitwise OR operator*/
      byte intermediateByte =  (byte) (intermediateColorByte|intermediateDirectionByte);

      Byte returnByte = new Byte(intermediateByte);
      return returnByte;
   }

   private byte color2Byte(ChemicalCell.ChemicalType currentColor){
      if((currentColor.equals(ChemicalCell.ChemicalType.RED))){
         return 1;
      }
      else{
         return 0;
      }
   }


   private ChemicalCell.ChemicalType switchColor(ChemicalCell.ChemicalType currentColor){
      if((currentColor.equals(ChemicalCell.ChemicalType.RED))){
            return ChemicalCell.ChemicalType.GREEN;
      }
      else{
            return ChemicalCell.ChemicalType.RED;
      }
   }


   private ChemicalCell.ChemicalType getCurrentColor(Byte previousState){
      byte prevState = previousState.byteValue();
      int chemicalCellChoice = prevState&1;
      if(chemicalCellChoice==0){
         return ChemicalCell.ChemicalType.RED;
      }
      else{
         return ChemicalCell.ChemicalType.GREEN;
      }
   }

   private DirectionType getDirectionFromState(Byte previousState){
      byte prevState = previousState.byteValue();
      int direction = prevState>>1;
      if (direction == 0){
         return DirectionType.CURRENT; //this means stay there
      }
      if (direction == 1){
         return DirectionType.EAST;
      }
      else if (direction == 2){
         return DirectionType.WEST;
      }
      else if (previousState == 3){
         return DirectionType.NORTH;
      }
      else{
         return DirectionType.SOUTH;
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

   private boolean isMoveOrthogonal(DirectionType previousDirection, DirectionType potentialNewMove){
      if((previousDirection.equals(DirectionType.EAST) & potentialNewMove.equals(DirectionType.NORTH))||
              (previousDirection.equals(DirectionType.EAST) & potentialNewMove.equals(DirectionType.SOUTH))
      ){
         return true;
      }
      else if((previousDirection.equals(DirectionType.WEST) & potentialNewMove.equals(DirectionType.NORTH))|
              (previousDirection.equals(DirectionType.WEST) & potentialNewMove.equals(DirectionType.SOUTH))
      ){
         return true;
      }
      else if((previousDirection.equals(DirectionType.NORTH) & potentialNewMove.equals(DirectionType.EAST))||
              (previousDirection.equals(DirectionType.NORTH) & potentialNewMove.equals(DirectionType.WEST)))
      {
         return true;
      }
      else if((previousDirection.equals(DirectionType.SOUTH) & potentialNewMove.equals(DirectionType.EAST))||
              (previousDirection.equals(DirectionType.SOUTH) & potentialNewMove.equals(DirectionType.WEST)))
      {
         return true;
      }

      else{
         return false;
      }
   }

   private DirectionType[] getOrthogonalDirections(DirectionType previousDirection){
      if (previousDirection == DirectionType.EAST || previousDirection == DirectionType.WEST){
         return new DirectionType[] {DirectionType.NORTH, DirectionType.SOUTH};
      }
      else {
         return new DirectionType[]{DirectionType.EAST, DirectionType.WEST};
      }
   }


   private DirectionType turnRight(DirectionType previousDirection){
      if(previousDirection.equals(DirectionType.EAST)){
         return DirectionType.SOUTH;
      }
      else if(previousDirection.equals(DirectionType.WEST)){
         return DirectionType.NORTH;
      }
      else if(previousDirection.equals(DirectionType.NORTH)){
         return DirectionType.EAST;
      }
      else{
         return DirectionType.WEST;
      }

   }

   //returns null if all directions are 0 or if there are multiple directions of max concentration
   private boolean ifDirectionIsAbsoluteMax(DirectionType proposedDirection, ChemicalCell.ChemicalType chosenChemicalType, Map<DirectionType, ChemicalCell> neighborMap) {
      Double maxConcentration = neighborMap.get(proposedDirection).getConcentration(chosenChemicalType);

      for (DirectionType direction : neighborMap.keySet()) {
         ChemicalCell candidateCell = neighborMap.get(direction);
         //if blocked, move to next
         if (candidateCell.isBlocked()) {
            continue;
         }
         if (candidateCell.getConcentration(chosenChemicalType) > maxConcentration) {
            return false;
         }
      }
      if (maxConcentration > 0.0) {
         return true;
      }
      else {
         return false;
      }
   }


   private DirectionType findOptimalMove(DirectionType previousDirection, ChemicalCell.ChemicalType chosenChemicalType, Map<DirectionType, ChemicalCell> neighborMap){
      DirectionType[] orthogonalDirections = getOrthogonalDirections(previousDirection);

      for (DirectionType orthogonalDirection: orthogonalDirections) {
         if (!neighborMap.get(orthogonalDirection).isBlocked() && ifDirectionIsAbsoluteMax(orthogonalDirection, chosenChemicalType, neighborMap)){
            return orthogonalDirection;
         }
      }

      // if gets to this point, resort to default functionality -- still need to implement turn-right strategy
      return previousDirection;
   }


   @Override
   public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
      Move move = new Move();
      ChemicalCell.ChemicalType chosenChemicalType = this.getCurrentColor(previousState);
      DirectionType previousDirection = getDirectionFromState(previousState);
      DirectionType selectedMove = this.findOptimalMove(previousDirection,chosenChemicalType,neighborMap);

      //if the current cell is your selectedMove so far that means you're at a local maximum so far
      //move forward one in the previous direction and switch colors in the savedState (according to orthogonal placement)
      //switch colors and continue to find the best move on the next iteration i.e. make a turn.
      if(selectedMove.equals(DirectionType.CURRENT)){
         selectedMove = previousDirection;
         chosenChemicalType = this.switchColor(chosenChemicalType);
      }

      Byte newState = this.saveState(selectedMove, chosenChemicalType);
      move.directionType = selectedMove;
      move.currentState = newState;
      return move;
   }
}
