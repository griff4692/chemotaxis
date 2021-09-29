package chemotaxis.g10; // TODO modify the package name to reflect your team

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.lang.Math;

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
      if (direction.equals(DirectionType.CURRENT)) {
         return 0;
      }
      else if (direction.equals(DirectionType.EAST)) {
         return 1;
      }
      else if (direction.equals(DirectionType.WEST)){
         return 2;
      }
      else if (direction.equals(DirectionType.NORTH)){
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
      intermediateDirectionByte = (byte) (intermediateDirectionByte<<2);
      /*Combine the two Bytes now using the bitwise OR operator*/
      byte intermediateByte =  (byte) (intermediateColorByte|intermediateDirectionByte);

      Byte returnByte = new Byte(intermediateByte);
      return returnByte;
   }

   private byte color2Byte(ChemicalCell.ChemicalType currentColor){
      if((currentColor.equals(ChemicalCell.ChemicalType.RED))){
         return 0;
      } else if (currentColor.equals(ChemicalCell.ChemicalType.GREEN)){
         return 1;
      } else {
         return 2;
      }
   }


   private static ChemicalCell.ChemicalType switchColor(ChemicalCell.ChemicalType currentColor){
      if((currentColor.equals(ChemicalCell.ChemicalType.RED))){
         return ChemicalCell.ChemicalType.GREEN;
      }
      else if (currentColor.equals(ChemicalCell.ChemicalType.GREEN)){
         return ChemicalCell.ChemicalType.BLUE;
      } else {
         return ChemicalCell.ChemicalType.RED;
      }
   }


   private ChemicalCell.ChemicalType getCurrentColor(Byte previousState){
      byte prevState = previousState.byteValue();
      int chemicalCellChoice = prevState&3;
      if(chemicalCellChoice == 0){
         return ChemicalCell.ChemicalType.RED;
      }
      else if (chemicalCellChoice == 1){
         return ChemicalCell.ChemicalType.GREEN;
      } else {
         return ChemicalCell.ChemicalType.BLUE;
      }
   }

   private DirectionType getDirectionFromState(Byte previousState){
      byte prevState = previousState.byteValue();
      int direction = prevState>>2;
      if (direction == 0){
         return DirectionType.CURRENT; //this means stay there
      }
      if (direction == 1){
         return DirectionType.EAST;
      }
      else if (direction == 2){
         return DirectionType.WEST;
      }
      else if (direction == 3){
         return DirectionType.NORTH;
      }
      else {
         return DirectionType.SOUTH;
      }
   }

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

   private static DirectionType[] getOrthogonalDirections(DirectionType previousDirection){
      if (previousDirection == null || previousDirection == DirectionType.CURRENT) {
         return new DirectionType[] { DirectionType.NORTH, DirectionType.EAST, DirectionType.SOUTH, DirectionType.WEST };
      }
      if (previousDirection == DirectionType.EAST || previousDirection == DirectionType.WEST){
         return new DirectionType[] {DirectionType.NORTH, DirectionType.SOUTH};
      }
      else {
         return new DirectionType[]{DirectionType.EAST, DirectionType.WEST};
      }
   }


   private static DirectionType turnRight(DirectionType previousDirection){
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

   private static DirectionType turnLeft(DirectionType previousDirection){
      if(previousDirection.equals(DirectionType.EAST)){
         return DirectionType.NORTH;
      }
      else if(previousDirection.equals(DirectionType.WEST)){
         return DirectionType.SOUTH;
      }
      else if(previousDirection.equals(DirectionType.NORTH)){
         return DirectionType.WEST;
      }
      else{
         return DirectionType.EAST;
      }
   }

   public static DirectionType turnBackwards(DirectionType previousDirection) {
      if (previousDirection.equals(DirectionType.NORTH)) return DirectionType.SOUTH;
      if (previousDirection.equals(DirectionType.EAST)) return DirectionType.WEST;
      if (previousDirection.equals(DirectionType.SOUTH)) return DirectionType.NORTH;
      else return DirectionType.EAST;
   }

   private static double roundToDecimal(double num, int decimalPlaces){
      return (double) (Math.round(num * (10*decimalPlaces)) / (10*decimalPlaces));
   }

   //returns null if all directions are 0 or if there are multiple directions of max concentration
   private static boolean ifDirectionIsAbsoluteMax(DirectionType proposedDirection, ChemicalCell.ChemicalType chosenChemicalType, Map<DirectionType, ChemicalCell> neighborMap) {
      double maxConcentration = neighborMap.get(proposedDirection).getConcentration(chosenChemicalType);
      if (maxConcentration == 0) return false;

      for (DirectionType direction : neighborMap.keySet()) {
         if (direction == proposedDirection){
            continue;
         }
         ChemicalCell candidateCell = neighborMap.get(direction);//if blocked, move to next
         if (candidateCell.isBlocked()) {
            continue;
         }
         if (candidateCell.getConcentration(chosenChemicalType) - maxConcentration > -0.001) {
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

   private static ArrayList<DirectionType> findAvailableMoves(Map<DirectionType, ChemicalCell> neighborMap){
      ArrayList<DirectionType> availableMoves = new ArrayList<DirectionType>();
      for (DirectionType direction: neighborMap.keySet()){
         if (direction != DirectionType.CURRENT && !neighborMap.get(direction).isBlocked()){
            availableMoves.add(direction);
         }
      }
      return availableMoves;
   }

   public static Object[] findOptimalMove(DirectionType previousDirection, ChemicalCell.ChemicalType chosenChemicalType, Map<DirectionType, ChemicalCell> neighborMap){
      ArrayList<DirectionType> availableMoves = findAvailableMoves(neighborMap);
      if (availableMoves.size() == 1){
         return new Object[] {availableMoves.get(0), chosenChemicalType};
      }
      DirectionType[] orthogonalDirections = getOrthogonalDirections(previousDirection);

      for (DirectionType orthogonalDirection: orthogonalDirections) {
         if (!neighborMap.get(orthogonalDirection).isBlocked() && ifDirectionIsAbsoluteMax(orthogonalDirection, chosenChemicalType, neighborMap)){
            return new Object[] {orthogonalDirection, switchColor(chosenChemicalType)};
         }
      }

      // if gets to this point, resort to default functionality -- still need to implement turn-right strategy
      if (previousDirection != null && !previousDirection.equals(DirectionType.CURRENT) && neighborMap.get(previousDirection).isBlocked()) {
         DirectionType directionToRight = turnRight(previousDirection);
         DirectionType directionToLeft = turnLeft(previousDirection);
         DirectionType directionBackwards = turnBackwards(previousDirection);
         if (!neighborMap.get(directionToRight).isBlocked()) {
            return new Object[] {directionToRight, chosenChemicalType};
         } else if (!neighborMap.get(directionToLeft).isBlocked()){
            return new Object[] {directionToLeft, chosenChemicalType};
         } else{
            return new Object[] {directionBackwards, chosenChemicalType};
         }
      }
      return new Object[] {previousDirection, chosenChemicalType};
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
      ChemicalCell.ChemicalType chosenChemicalType = this.getCurrentColor(previousState);
      DirectionType previousDirection = getDirectionFromState(previousState);
      Object[] res = this.findOptimalMove(previousDirection,chosenChemicalType,neighborMap);
      DirectionType selectedMove = (DirectionType) res[0];
      chosenChemicalType = (ChemicalCell.ChemicalType) res[1];

      Byte newState = this.saveState(selectedMove, chosenChemicalType);
      move.directionType = selectedMove;
      move.currentState = newState;
      return move;
   }
}