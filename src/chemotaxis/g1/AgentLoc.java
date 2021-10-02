package chemotaxis.g1;

import java.awt.*;

public class AgentLoc {
    public Point loc;
    public AgentState state;
    // Turn on which this agent spawned
    public int epoch;

    AgentLoc(Point loc, AgentState state, int epoch) {
        this.loc = new Point(loc);
        this.state = new AgentState(state);
        this.epoch = epoch;
    }

    AgentLoc(AgentLoc prior) {
        this.loc = new Point(prior.loc);
        this.state = new AgentState(prior.state);
        this.epoch = prior.epoch;
    }
}
