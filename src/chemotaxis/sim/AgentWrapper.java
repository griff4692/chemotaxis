package chemotaxis.sim;

import java.util.Map;

public class AgentWrapper {

	private Timer timer;
    private Agent agent;
    private String agentName;
    private long timeout;

    public AgentWrapper(Agent agent, String agentName, long timeout) {
        this.agent = agent;
        this.agentName = agentName;
        this.timeout = timeout;
        this.timer = new Timer();
    }

    public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
    	Log.writeToVerboseLogFile("Team " + this.agentName + "'s agent making a move...");
        
    	Move move = new Move();

        try {
            if(!timer.isAlive())
            	timer.start();
            timer.callStart(() -> { return agent.makeMove(randomNum, previousState, currentCell, neighborMap); });
            move = timer.callWait(timeout);
        }
        catch(Exception e) {
            Log.writeToVerboseLogFile("Team " + this.agentName + "'s agent has possibly timed out.");
            Log.writeToVerboseLogFile("Exception for team " + this.agentName + "'s agent: " + e);
        }

        return move;
    }
    
    public Agent getAgent() {
    	return agent;
    }

    public String getAgentName() {
        return agentName;
    }

    public void terminateThread() {
        this.timer.terminate();
    }
}