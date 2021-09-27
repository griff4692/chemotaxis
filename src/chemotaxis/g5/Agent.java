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
import java.util.Iterator;
import java.util.Map;

public class Agent extends chemotaxis.sim.Agent {
    public Agent(SimPrinter var1) {
        super(var1);
    }

    public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        Move move = new Move();

		ChemicalType chosenChemicalType = ChemicalType.GREEN;
        ChemicalType repulseChemicalType = ChemicalType.RED;
        //DirectionType repulseDirection = DirectionType.NORTH;

		//double highestConcentration = 0;
        //double repulseConcentration = 0;
        double highestNetAttraction = 0;

        for (DirectionType directionType : neighborMap.keySet()) {
			double net = neighborMap.get(directionType).getConcentration(chosenChemicalType) - neighborMap.get(directionType).getConcentration((repulseChemicalType));
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
                     move.directionType = DirectionType.NORTH;
             }
             if(!neighborMap.keySet().contains(move.directionType)){
                 move.directionType = neighborMap.keySet().iterator().next();
             }
        }

        switch(move.directionType){
            case NORTH:
                move.currentState = 1;
            case SOUTH:
                move.currentState = 2;
            case EAST:
                move.currentState = 3;
            case WEST:
                move.currentState = 4;
            case CURRENT:
                move.currentState = 0;
        }
		return move;
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
