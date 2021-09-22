package chemotaxis.g1.test;

import static org.junit.Assert.assertEquals;

import chemotaxis.g1.AgentState;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import org.junit.Test;

public class AgentStateTest {
    @Test
    public void agentFollowsRedAndBlue() {
        AgentState agent = new AgentState();
        // Default color
        assertEquals(agent.getFollowColor(), ChemicalType.BLUE);

        agent.setFollowColor(ChemicalType.RED);
        assertEquals(agent.getFollowColor(), ChemicalType.RED);
    }

    @Test(expected = RuntimeException.class)
    public void agentCannotFollowGreen() {
        AgentState agent = new AgentState();
        agent.setFollowColor(ChemicalType.GREEN);
    }
}