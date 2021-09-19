package chemotaxis.g1;

public class AgentState {
    // State is represented as a single byte of memory
    private byte state;

    private static final byte NORTH_BITS = 0x0;
    private static final byte EAST_BITS = 0x01;
    private static final byte SOUTH_BITS = 0x02;
    private static final byte WEST_BITS = 0x3;

    // First two bits are used for direction
    private static final byte DIRECTION_MASK = 0x3;

    public enum Direction {
        NORTH,
        EAST,
        SOUTH,
        WEST
    }

    public AgentState() {
        this.state = 0x0;
    }

    /**
     * Initialize the agent with a prior serialized state
     *
     * @param prior_state Byte serialization of the agent's prior state
     */
    public AgentState(Byte prior_state) {
        this.state = prior_state;
    }

    public Byte serialize() {
        return this.state;
    }

    /**
     * setDirection - Set the direction the agent just moved
     *
     * @param dir Direction the agent just moved
     */
    public void setDirection(Direction dir) {
        this.state &= ~DIRECTION_MASK;
        switch (dir) {
            case NORTH:
                this.state &= NORTH_BITS;
                break;
            case EAST:
                this.state &= EAST_BITS;
                break;
            case WEST:
                this.state &= WEST_BITS;
                break;
            case SOUTH:
                this.state &= SOUTH_BITS;
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
    public Direction getDirection() {
        switch (this.state & DIRECTION_MASK) {
            case NORTH_BITS:
                return Direction.NORTH;
            case EAST_BITS:
                return Direction.EAST;
            case WEST_BITS:
                return Direction.WEST;
            case SOUTH_BITS:
                return Direction.SOUTH;
        }
        throw new RuntimeException("unreachable direction state");
    }
}
