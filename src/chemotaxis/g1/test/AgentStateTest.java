package chemotaxis.g1.test;

import chemotaxis.g1.AgentState;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import org.junit.Test;

import static org.junit.Assert.*;

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

    // The agent strategy can only be set once
    @Test(expected = RuntimeException.class)
    public void strategyCannotBeChanged() {
        AgentState agent = new AgentState();
        agent.setStrategy(AgentState.Strategy.STRONG);
        agent.setStrategy(AgentState.Strategy.WEAK);
    }

    @Test(expected = RuntimeException.class)
    public void cannotGetStrategyBeforeSetting() {
        AgentState agent = new AgentState();
        agent.getStrategy();
    }

    @Test
    public void settingStrategyWorks() {
        AgentState agent = new AgentState();
        agent.setStrategy(AgentState.Strategy.STRONG);
        assertEquals(agent.getStrategy(), AgentState.Strategy.STRONG);

        agent = new AgentState();
        agent.setStrategy(AgentState.Strategy.WEAK);
        assertEquals(agent.getStrategy(), AgentState.Strategy.WEAK);
    }
}