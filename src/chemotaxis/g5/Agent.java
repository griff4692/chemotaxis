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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Agent extends chemotaxis.sim.Agent {
    private Map<Integer, DirectionType> intToDirection;
    private Map<DirectionType, Integer> directionToInt;
    
    public Agent(SimPrinter var1) {
        super(var1);
        // for easier access to our memory mapping to direction. Baeldung referenced for Map of
        this.intToDirection = Map.of(0, DirectionType.CURRENT, 1, DirectionType.NORTH, 
                                     2, DirectionType.SOUTH, 3, DirectionType.EAST,
                                     4, DirectionType.WEST);
        System.out.println(this.intToDirection);
        // follow https://stackoverflow.com/questions/20412354/reverse-hashmap-keys-and-values-in-java to reverse
        this.directionToInt =  
            intToDirection.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        Move move = new Move();

        // set our default behaviours to look for
        // how do we change this depending on the path to go to? First chemical is used for priming?
        // then have a swith depending on our selected chemical
		ChemicalType chosenChemicalType = ChemicalType.GREEN;
        ChemicalType repulseChemicalType = ChemicalType.RED;

        double highestNetAttraction = 0;

        /* 
         * Look for a hill to climb if we haven't started, 
         * else, look for a value that's higher than the one we came from
         * default: keep going in the previous direction
        */ 
        // grab required number of bits for direction: https://stackoverflow.com/questions/15255692/grabbing-n-bits-from-a-byte
        // lower three bits represent current direction/gradient direction we're following
        int prevGradientDirection = previousState & 0x7;
        int backwardsDirection = this.getBackwardsDirection(this.intToDirection.get(prevGradientDirection));
        int increasingGradDirectionType = this.getIncreasingGradient(currentCell, neighborMap);

        if (increasingGradDirectionType != 0) {
            move.directionType = this.intToDirection.get(increasingGradDirectionType);
            // unset then set the lowest three bits with the right direction
            // source: https://stackoverflow.com/questions/31007977/how-to-set-3-lower-bits-of-uint8-t-in-c
            // right now we're just using lowest three bits for anything
            move.currentState = (byte) increasingGradDirectionType;
            return move; //done
        }
        // can't find increasing direction => 1) keep going in previous direction 2) pick random (not previous) 3) pick backwards because stuck
        Set<Integer> possibleMoveSet = this.getPossibleMoves(neighborMap);

        // check if previous direction is available (ie: continue forward)
        if (possibleMoveSet.contains(prevGradientDirection)) {
            move.directionType = this.intToDirection.get(prevGradientDirection);
            // don't need to change memory since we're continuing
            return move; // done
        }

        // if only option is going backwards, size is 1 and we take it
        if (possibleMoveSet.size() == 1) {
            int newMoveDirection = possibleMoveSet.iterator().next();
            move.directionType = this.intToDirection.get(newMoveDirection);
            move.currentState = (byte) newMoveDirection;
            return move;
        }

        // otherwise take a random one that's not going backwards
        possibleMoveSet.remove(backwardsDirection);

        int count = 0;
        for (Integer dir : possibleMoveSet) {
            if (count == randomNum % possibleMoveSet.size()) {
                move.directionType = this.intToDirection.get(dir);
                move.currentState = (byte) dir.intValue();
                return move;
            }
        }
        
        // somehow got here so stay in the same place. Print error but don't die
        System.err.println("Error in finding direction!");
        move.directionType = DirectionType.CURRENT;
        return move;

        // iterate over possible directions and get the net concentration 
        /*
        for (DirectionType directionType : neighborMap.keySet()) {
			// double net = neighborMap.get(directionType).getConcentration(chosenChemicalType) - neighborMap.get(directionType).getConcentration((repulseChemicalType));
            double net = neighborMap.get(directionType).getConcentration(chosenChemicalType);

            if(net > highestNetAttraction) {
                highestNetAttraction = net;
                move.directionType = directionType;
            }
		}
        if (highestNetAttraction == 0){
             switch(previousState){
                 case 1:
                     move.directionType = DirectionType.NORTH;
                 case 2:
                     move.directionType = DirectionType.SOUTH;
                 case 3:
                     move.directionType = DirectionType.EAST;
                 case 4:
                     move.directionType = DirectionType.WEST;
                 case 0:
                     move.directionType = DirectionType.CURRENT; // stay in the same location if we haven't set previous direction
             }
             if(!neighborMap.keySet().contains(move.directionType)){
                 move.directionType = neighborMap.keySet().iterator().next();
             }
        }
        */
        // setting specific bits in a byte type: https://stackoverflow.com/questions/4674006/set-specific-bit-in-byte
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
                possibleMovesSet.add(this.directionToInt.get(entry.getKey()));
            }
        }

        return possibleMovesSet;
    }

    private int getIncreasingGradient(ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        return 0;
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
