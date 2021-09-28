//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package chemotaxis.g5;

import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.DirectionType;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;
import chemotaxis.sim.ChemicalCell.ChemicalType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Agent extends chemotaxis.sim.Agent {
    private static Map<Integer, DirectionType> intToDirection = 
                                    Map.of(4, DirectionType.CURRENT, 0, DirectionType.NORTH,
                                           1, DirectionType.SOUTH, 2, DirectionType.EAST,
                                           3, DirectionType.WEST);

    // followed https://stackoverflow.com/questions/20412354/reverse-hashmap-keys-and-values-in-java to reverse
    private static Map<DirectionType, Integer> directionToInt =
                                    intToDirection.entrySet()
                                    .stream()
                                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    public Agent(SimPrinter var1) {
        super(var1);
        // for easier access to our memory mapping to direction. Baeldung referenced for Map of
        /*
        this.intToDirection = Map.of(0, DirectionType.CURRENT, 1, DirectionType.NORTH, 
                                     2, DirectionType.SOUTH, 3, DirectionType.EAST,
                                     4, DirectionType.WEST);

        // follow https://stackoverflow.com/questions/20412354/reverse-hashmap-keys-and-values-in-java to reverse
        this.directionToInt =  
            intToDirection.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        */
    }

    public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        Move move = new Move();

        // set our default behaviours to look for
        // how do we change this depending on the path to go to? First chemical is used for priming?
        // then have a switch depending on our selected chemical
		ChemicalType chosenChemicalType = ChemicalType.GREEN;

        /*
        // get the first bit (from the left) and if it's set then follow blue rather than green
        if (randomNum >> 7 & 1 == 1) {
            chosenChemicalType = ChemicalType.BLUE;
        }
        */
        ChemicalType repulseChemicalType = ChemicalType.RED;

        /* 
         * Look for a hill to climb if we haven't started, 
         * else, look for a value that's higher than the one we came from
         * default: keep going in the previous direction
        */ 
        // grab required number of bits for direction: https://stackoverflow.com/questions/15255692/grabbing-n-bits-from-a-byte
        // lower three bits represent current direction/gradient direction we're following
        int prevGradientDirection = previousState & 0x7;
        int backwardsDirection = this.getBackwardsDirection(Agent.intToDirection.get(prevGradientDirection));
        int increasingGradDirectionType = this.getIncreasingGradient(chosenChemicalType, currentCell, neighborMap);

        if (increasingGradDirectionType != 0) {
            move.directionType = Agent.intToDirection.get(increasingGradDirectionType);
            // unset then set the lowest three bits with the right direction
            // source: https://stackoverflow.com/questions/31007977/how-to-set-3-lower-bits-of-uint8-t-in-c
            // right now we're just using lowest three bits for anything

            //TODO set the pledge direction to the increasingGrad directionType
            move.currentState = (byte) increasingGradDirectionType;
            return move; //done
        }

        // can't find increasing direction => 1) keep going in previous direction 2) pick random (not previous) 3) pick backwards because stuck
        Set<Integer> possibleMoveSet = this.getPossibleMoves(neighborMap);
        /*
        // check if previous direction is available (ie: continue forward)
        if (possibleMoveSet.contains(prevGradientDirection)) {
            move.directionType = Agent.intToDirection.get(prevGradientDirection);
            // don't need to change memory since we're continuing
            return move; // done
        }

        // if only option is going backwards, size is 1 and we take it
        if (possibleMoveSet.size() == 1) {
            int newMoveDirection = possibleMoveSet.iterator().next();
            move.directionType = Agent.intToDirection.get(newMoveDirection);
            move.currentState = (byte) newMoveDirection;
            return move;
        }

        // otherwise take a random one that's not going backwards
        possibleMoveSet.remove(backwardsDirection);

        int count = 0;
        for (Integer dir : possibleMoveSet) {
            if (count == randomNum % possibleMoveSet.size()) {
                move.directionType = Agent.intToDirection.get(dir);
                move.currentState = (byte) dir.intValue();
                return move;
            }
        }
        
        // somehow got here so stay in the same place. Print error but don't die
        System.err.println("Error in finding direction!");
        move.directionType = DirectionType.CURRENT;
        return move;
        */
        // setting specific bits in a byte type: https://stackoverflow.com/questions/4674006/set-specific-bit-in-byte
        return getNavigateMove(previousState, possibleMoveSet, backwardsDirection);
   }

    private Move getNavigateMove(Byte previousState, Set<Integer> possibleMoveSet, int backwardsDirection){
        //Using second and third bit from the left as wallFollow bits
        //Using fourth and fifth bit from the left as the Pledge Direction bits
        int wallFollow = (previousState >> 5) & 3;
        Move move = new Move();
        DirectionType pledgeDirection = intToDirection.get((previousState >> 3) & 3);

        DirectionType[] relativePledge = getRelativeDirections(pledgeDirection);
        DirectionType[] relativeBack = getRelativeDirections(intToDirection.get(backwardsDirection));

        //If we are not following a wall
        if(wallFollow == 0){
            //If we cannot move in the pledge direction
            if(!possibleMoveSet.contains(pledgeDirection)){
                //Now find what way we can move, then set wall following to 1 for right turns, 2 for left turns
                if(possibleMoveSet.contains(relativePledge[1])){
                    move.currentState = (byte) ((previousState | (1 << 5)) & ~(1 << 6));
                    move.directionType= relativePledge[1];
                }
                else if(possibleMoveSet.contains(relativePledge[0])){
                    move.currentState = (byte) ((previousState | (1 << 6)) & ~(1 << 5));
                    move.directionType =  relativePledge[0];
                }
                else{
                    move.currentState = (byte) ((previousState | (1 << 5)) & ~(1 << 6));
                    move.directionType = relativePledge[2];
                }
            //We can move in the pledge direction, and do so, no wall following.
            }else{
                move.currentState = (byte) ((previousState & ~(1 << 5)) & ~(1 << 6));
                move.directionType = pledgeDirection;
            }
        //We are using right turn wall following.
        }else if(wallFollow == 1){
            if(possibleMoveSet.contains(pledgeDirection)){
                //if the Pledge direction is backwards, do not follow it, instead follow the wall.
                if(backwardsDirection == directionToInt.get(pledgeDirection)) {
                    move.currentState = (byte) ((previousState | (1 << 5)) & ~(1 << 6));
                    if (possibleMoveSet.contains(relativeBack[0])) {
                        move.directionType = relativeBack[0];
                    } else if (possibleMoveSet.contains(relativeBack[2])) {
                        move.directionType = relativeBack[2];
                    } else if (possibleMoveSet.contains(relativeBack[1])) {
                        move.directionType = relativeBack[1];
                    //This case is a dead end
                    }else{
                        move.directionType = intToDirection.get(backwardsDirection);
                    }
                }
                //Pledge direction is not backwards, and we have navigated the obstacle. break away from the wall.
                else{
                    move.currentState = (byte) ((previousState & ~(1 << 5)) & ~(1 << 6));
                    move.directionType = pledgeDirection;
                }
            //No Pledge direction, so follow the wall.
            }else{
                move.currentState = (byte) ((previousState | (1 << 5)) & ~(1 << 6));
                if (possibleMoveSet.contains(relativeBack[0])) {
                    move.directionType = relativeBack[0];
                } else if (possibleMoveSet.contains(relativeBack[2])) {
                    move.directionType = relativeBack[2];
                } else if (possibleMoveSet.contains(relativeBack[1])) {
                    move.directionType = relativeBack[1];
                }else{
                    move.directionType = intToDirection.get(backwardsDirection);
                }

            }
        //We are wall following left turns, same rules apply to right turn wall following.
        }else {
            if (possibleMoveSet.contains(pledgeDirection)) {
                if (backwardsDirection == directionToInt.get(pledgeDirection)) {
                    move.currentState = (byte) ((previousState | (1 << 6)) & ~(1 << 5));
                    if (possibleMoveSet.contains(relativeBack[1])) {
                        move.directionType = relativeBack[1];
                    } else if (possibleMoveSet.contains(relativeBack[2])) {
                        move.directionType = relativeBack[2];
                    } else if (possibleMoveSet.contains(relativeBack[0])) {
                        move.directionType = relativeBack[0];
                    }else{
                        move.directionType = intToDirection.get(backwardsDirection);
                    }
                } else {
                    move.currentState = (byte) ((previousState & ~(1 << 5)) & ~(1 << 6));
                    move.directionType = pledgeDirection;
                }
            //
            } else {
                move.currentState = (byte) ((previousState | (1 << 5)) & ~(1 << 6));
                if (possibleMoveSet.contains(relativeBack[1])) {
                    move.directionType = relativeBack[1];
                } else if (possibleMoveSet.contains(relativeBack[2])) {
                    move.directionType = relativeBack[2];
                } else if (possibleMoveSet.contains(relativeBack[0])) {
                    move.directionType = relativeBack[0];
                }else{
                    move.directionType = intToDirection.get(backwardsDirection);
                }
            }
        }
        return move;
    }
    //Helper function to get relative directions for a direction
    private DirectionType[] getRelativeDirections(DirectionType direction){
        switch(direction){
            //[LEFT, RIGHT, BACKWARDS]
            //Current is set to south since Current should never occur.
            case NORTH:
                return new DirectionType[]{DirectionType.WEST, DirectionType.EAST, DirectionType.SOUTH};
            case EAST:
                return new DirectionType[]{DirectionType.NORTH, DirectionType.SOUTH, DirectionType.WEST};
            case WEST:
                return new DirectionType[]{DirectionType.SOUTH, DirectionType.NORTH, DirectionType.EAST};
            case SOUTH:
                return new DirectionType[]{DirectionType.EAST, DirectionType.WEST, DirectionType.NORTH};
            case CURRENT:
                return new DirectionType[]{DirectionType.EAST, DirectionType.WEST, DirectionType.NORTH};
            default:
                return new DirectionType[]{DirectionType.EAST, DirectionType.WEST, DirectionType.NORTH};
        }
    }

    private int getBackwardsDirection(DirectionType directionType) {
        int retDir = 0;
        switch (directionType) {
            case NORTH: retDir = 2; // SOUTH
                break;
            case SOUTH: retDir = 1;
                break;
            case EAST: retDir = 4;
                break;
            case WEST: retDir = 3;
                break;
            default: retDir = 0;
                break;
        }
        return retDir;
    }

    private Set<Integer> getPossibleMoves(Map<DirectionType, ChemicalCell> neighborMap) {
        Set<Integer> possibleMovesSet = new HashSet<>();
        
        for (Map.Entry<DirectionType, ChemicalCell> entry : neighborMap.entrySet()) {
            if (entry.getValue().isOpen()) {
                possibleMovesSet.add(Agent.directionToInt.get(entry.getKey()));
            }
        }

        return possibleMovesSet;
    }

    // get the greatest increasing direction from our neighbouring cells
    private int getIncreasingGradient(ChemicalType chosenChemicalType, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        int increasingGradDirection = 0;
        double highestNetAttraction = 0.0;

        for (Map.Entry<DirectionType, ChemicalCell> entry : neighborMap.entrySet()) {
			// double net = neighborMap.get(directionType).getConcentration(chosenChemicalType) - neighborMap.get(directionType).getConcentration((repulseChemicalType));
            double net = entry.getValue().getConcentration(chosenChemicalType);

            if(net > highestNetAttraction) {
                highestNetAttraction = net;
                increasingGradDirection = Agent.directionToInt.get(entry.getKey());
            }
        }

        return increasingGradDirection;
    }

    /*public Move makeMove(Integer var1, Byte var2, ChemicalCell var3, Map<DirectionType, ChemicalCell> var4) {
        Move var5 = new Move();
        ChemicalType var6 = ChemicalType.BLUE;
        double var7 = var3.getConcentration(var6);
        Iterator var9 = var4.keySet().iterator();

        while(var9.hasNext()) {
            DirectionType var10 = (DirectionType)var9.next();
            if (var7 <= ((ChemicalCell)var4.get(var10)).getConcentration(var6)) {
                var7 = ((ChemicalCell)var4.get(var10)).getConcentration(var6);
                var5.directionType = var10;
            }
        }

        return var5;
    }*/
}
