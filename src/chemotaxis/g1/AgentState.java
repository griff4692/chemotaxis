package chemotaxis.g1;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell.ChemicalType;

public class AgentState {
    // State is represented as a single byte of memory
    private byte state;

    private static final byte NORTH_BITS = 0x0;
    private static final byte EAST_BITS = 0x01;
    private static final byte SOUTH_BITS = 0x02;
    private static final byte WEST_BITS = 0x3;

    // First two bits are used for direction
    private static final byte DIRECTION_MASK = 0x3;

    // Third and Fourth bit used for color agent is currently looking for
    private static final byte COLOR_MASK = 0x3 << 2;
    private static final byte RED_BITS = 0x1 << 2;
    private static final byte GREEN_BITS = 0x2 << 2;

    // Fifth bit is used to track whether the agent is has selected a strategy
    private static final byte INITIALIZED_BIT = 0x1 << 4;

    // Sixth bit is used to track the strategy
    private static final byte STRAT_MASK = 0x1 << 5;
    private static final byte WEAK_CHEM_BITS = 0x1 << 5;

    public enum Strategy {
        STRONG, WEAK
    }

    public AgentState() {
        this.state = 0x0;
    }

    /**
     * Initialize the agent with a prior serialized state
     *
     * @param prevState Byte serialization of the agent's prior state
     */
    public AgentState(Byte prevState) {
        this.state = prevState;
    }

    public AgentState(AgentState prevState) {
        this.state = prevState.state;
    }

    public Byte serialize() {
        return this.state;
    }

    public CardinalDirection asCardinalDir(DirectionType dir) {
        switch (dir) {
            case NORTH:
                return CardinalDirection.NORTH;
            case SOUTH:
                return CardinalDirection.SOUTH;
            case EAST:
                return CardinalDirection.EAST;
            case WEST:
                return CardinalDirection.WEST;
            case CURRENT:
                return this.getDirection();
        }
        throw new RuntimeException("unreachable");
    }

    public void setFollowColor(ChemicalType color) {
        this.state &= ~COLOR_MASK;
        switch (color) {
            case BLUE:
                // Blue is encoded as 0x0
                break;
            case RED:
                this.state |= RED_BITS;
                break;
            case GREEN:
                this.state |= GREEN_BITS;
            default:
                throw new RuntimeException("Color state cannot be 11");
        }
    }

    public ChemicalType getFollowColor() {
        if ((this.state & COLOR_MASK) == RED_BITS) {
            return ChemicalType.RED;
        }
        else if ((this.state & COLOR_MASK) == GREEN_BITS) {
            return ChemicalType.RED;
        }
        return ChemicalType.BLUE;
    }

    // Change the color bits that the agent is currently following
    public void changeFollowColor(ChemicalType color) {
        this.state &= ~COLOR_MASK;
        switch (color) {
            case BLUE:
                // If current color is BLUE, change to RED
                this.state |= RED_BITS;
                break;
            case RED:
                this.state |= GREEN_BITS;
                break;
            case GREEN:
                // BLUE is encoded as 0x0
                break;
            default:
                throw new RuntimeException("Color state cannot be 11");
        }
    }

    /**
     * setDirection - Set the direction the agent just moved
     *
     * @param dir Direction the agent just moved
     */
    public void setDirection(DirectionType dir) {
        if (dir == DirectionType.CURRENT) {
            return;
        }
        setDirection(this.asCardinalDir(dir));
    }

    public void setDirection(CardinalDirection dir) {
        this.state &= ~DIRECTION_MASK;
        switch (dir) {
            case NORTH:
                this.state |= NORTH_BITS;
                break;
            case EAST:
                this.state |= EAST_BITS;
                break;
            case WEST:
                this.state |= WEST_BITS;
                break;
            case SOUTH:
                this.state |= SOUTH_BITS;
                break;
            default:
                throw new RuntimeException("invalid Direction enum");
        }
    }

    /**
     * getDirection - Retrieve the previous direction the agent moved
     *
     * @return The previous direction the agent moved
     */
    public CardinalDirection getDirection() {
        switch (this.state & DIRECTION_MASK) {
            case NORTH_BITS:
                return CardinalDirection.NORTH;
            case EAST_BITS:
                return CardinalDirection.EAST;
            case WEST_BITS:
                return CardinalDirection.WEST;
            case SOUTH_BITS:
                return CardinalDirection.SOUTH;
        }
        throw new RuntimeException("unreachable direction state");
    }

    public void setInitialized() {
        this.state |= INITIALIZED_BIT;
    }

    public boolean isInitialized() {
        return (this.state & INITIALIZED_BIT) != 0;
    }

    public Strategy getStrategy() {
//        if (!this.isInitialized()) {
//            throw new RuntimeException("agent uninitialized");
//        }
        if ((this.state & STRAT_MASK) == WEAK_CHEM_BITS) {
            return Strategy.WEAK;
        }
        return Strategy.STRONG;
    }

    public void setStrategy(Strategy strat) {
        if (this.isInitialized()) {
            throw new RuntimeException("cannot change strategy after initialization");
        }
        if (strat == Strategy.WEAK) {
            this.state |= WEAK_CHEM_BITS;
        }
    }
}
