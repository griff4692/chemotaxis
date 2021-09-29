package chemotaxis.g8;

import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.DirectionType;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;

import java.util.Map;
import java.util.Optional;

public class Agent extends chemotaxis.sim.Agent {
	public Agent(SimPrinter simPrinter) {
		super(simPrinter);
	}

	private int decodeMode(byte x) { return x & 0b111; }
	private boolean decodeTurn(byte x) { return (x & 0b01000000) != 0; }
	private DirectionType decodeDT(byte x) { return DirectionType.values()[4 - ((x >> 3) & 0b111)]; }

	private byte encode(DirectionType dt, int mode, boolean turn) {
		return (byte)(mode + ((4 - dt.ordinal()) << 3) + (turn ? 0b01000000 : 0));
	}

	private DirectionType turnLeft(DirectionType dt) {
		switch (dt) {
			case NORTH: return DirectionType.WEST;
			case SOUTH: return DirectionType.EAST;
			case EAST: return DirectionType.NORTH;
			case WEST: return DirectionType.SOUTH;
		};
		return DirectionType.CURRENT;
	}
	private DirectionType turnRight(DirectionType dt) {
		switch (dt) {
			case NORTH: return DirectionType.EAST;
			case SOUTH: return DirectionType.WEST;
			case EAST: return DirectionType.SOUTH;
			case WEST: return DirectionType.NORTH;
		};
		return DirectionType.CURRENT;
	}

	@Override
	public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
		Move move = new Move();
		int lastMode = decodeMode(previousState);
		DirectionType lastDT = decodeDT(previousState);
		boolean lastTurn = decodeTurn(previousState);
		System.out.println("Agent: " + lastDT + "  " + lastMode);

		for (DirectionType dt: neighborMap.keySet()) {
			ChemicalCell cell = neighborMap.get(dt);
			int mode = 0;
			if (cell.getConcentration(ChemicalType.BLUE) > 0.99) mode += 1;
			if (cell.getConcentration(ChemicalType.RED) > 0.99) mode += 2;
			if (cell.getConcentration(ChemicalType.GREEN) > 0.99) mode += 4;
			if (mode != 0) {
				System.out.println("Agent Ins: " + dt + "  " + mode);
				move.directionType = dt;
				move.currentState = encode(dt, mode, false);
				return move;
			}
		}

		if (lastMode == 0) {
			move.directionType = DirectionType.CURRENT;
			move.currentState = previousState;
			return move;
		}

		if (3 <= lastMode && lastMode <= 6) {
			if (neighborMap.get(lastDT).isBlocked()) {
				System.out.println("Agent Hit Wall!");
				switch (lastMode) {
					case 3: lastDT = turnLeft(lastDT); break;
					case 4: lastDT = turnRight(lastDT); break;
					case 5: lastDT = lastTurn ? turnLeft(lastDT) : turnRight(lastDT); break;
					case 6: lastDT = !lastTurn ? turnLeft(lastDT) : turnRight(lastDT); break;
				};
			}
		} else if (lastMode == 1) {
			lastDT = turnLeft(lastDT);
			while (neighborMap.get(lastDT).isBlocked()) lastDT = turnRight(lastDT);
		} else if (lastMode == 2) {
			lastDT = turnRight(lastDT);
			while (neighborMap.get(lastDT).isBlocked()) lastDT = turnLeft(lastDT);
		}


		move.directionType = lastDT;
		lastTurn = !lastTurn;
		move.currentState = encode(lastDT, lastMode, lastTurn);
		return move;
	}
}