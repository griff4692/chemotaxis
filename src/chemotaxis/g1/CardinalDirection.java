package chemotaxis.g1;

import chemotaxis.sim.DirectionType;

/**
 * Direction enum that does NOT include "current" as a valid direction.
 * This makes operations on directions such as "turn right" type safe.
 */
public enum CardinalDirection {
    NORTH,
    SOUTH,
    EAST,
    WEST;

    public DirectionType asDirectionType() {
        switch (this) {
            case NORTH:
                return DirectionType.NORTH;
            case SOUTH:
                return DirectionType.SOUTH;
            case EAST:
                return DirectionType.EAST;
            case WEST:
                return DirectionType.WEST;
        }
        throw new RuntimeException("unreachable");
    }

    public CardinalDirection reverseOf() {
        switch (this) {
            case NORTH:
                return CardinalDirection.SOUTH;
            case SOUTH:
                return CardinalDirection.NORTH;
            case EAST:
                return CardinalDirection.WEST;
            case WEST:
                return CardinalDirection.EAST;
        }
        throw new RuntimeException("unreachable");
    }

    public CardinalDirection rightOf() {
        switch (this) {
            case NORTH:
                return CardinalDirection.EAST;
            case EAST:
                return CardinalDirection.SOUTH;
            case SOUTH:
                return CardinalDirection.WEST;
            case WEST:
                return CardinalDirection.NORTH;
        }
        throw new RuntimeException("unreachable");
    }

    public CardinalDirection leftOf() {
        return this.rightOf().reverseOf();
    }
}
