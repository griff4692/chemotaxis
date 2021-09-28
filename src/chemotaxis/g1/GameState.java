package chemotaxis.g1;


import chemotaxis.sim.*;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.g1.GameCell;

import javax.xml.validation.Schema;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

class AgentLoc {
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

public class GameState {
    private int currentTurn;
    private final Point start;
    private final Point target;
    private final int agentGoal;
    private final int spawnFreq;
    private int agentsOnTarget;
    private int chemicalsRemaining;
    private GameCell[][] grid;
    private ArrayList<AgentLoc> agents;
    private chemotaxis.sim.Move Move;

    /**
     * Constructor to initialize a brand-new game
     *
     * @param start
     * @param target
     * @param agentGoal
     * @param spawnFreq
     * @param chemicalsRemaining
     * @param grid
     */
    public GameState(final Point start, final Point target, int agentGoal, int spawnFreq,
                     int chemicalsRemaining, ChemicalCell[][] grid) {
        this.currentTurn = 1;
        this.start = new Point(start);
        this.target = new Point(target);
        this.agentGoal = agentGoal;
        this.spawnFreq = spawnFreq;
        this.agentsOnTarget = 0;
        this.chemicalsRemaining = chemicalsRemaining;
        this.grid = GameState.buildGrid(grid);
        this.agents = new ArrayList<>();
        // Epoch 0 here is not a bug. The sim always spawns an agent on turn "0"
        // and then plays turn 1, which may spawn another agent.
        this.agents.add(new AgentLoc(start, new AgentState(), 0));
    }

    /**
     * Copy Constructor
     *
     * @param priorState
     */
    GameState(final GameState priorState) {
        this.currentTurn = priorState.currentTurn;
        this.start = new Point(priorState.start);
        this.target = new Point(priorState.target);
        this.agentGoal = priorState.agentGoal;
        this.spawnFreq = priorState.spawnFreq;
        this.agentsOnTarget = priorState.agentsOnTarget;
        this.chemicalsRemaining = priorState.chemicalsRemaining;

        // Create a clone of the chemical cell grid
        this.grid = GameState.cloneGrid(priorState.grid);

        // Clone Agent states
        this.agents = new ArrayList<>();
        for (AgentLoc a : priorState.agents) {
            agents.add(new AgentLoc(a));
        }
    }

    /**
     * Places a chemical and runs the game 1 tick, returning a new `GameState`
     * representing the new game configuration.
     * <p>
     * This method does NOT modify the `GameState` it is invoked on.
     * <p>
     * Operations are performed in the following order, which should achieve
     * the same effect as the official simulator:
     * <p>
     * - Spawn agent (if necessary)
     * - Place chemicals
     * - Move agents (and despawn if on target)
     * - Diffuse chemicals to prepare state for next iteration
     * - Increment "current turn" counter
     * - Spawn agent if necessary
     *
     * @param placement
     * @return
     */
    public GameState placeChemicalAndStep(ChemicalPlacement placement) {
        // Copy of current state
        GameState nextState = new GameState(this);

        nextState.placeChemical(placement);
        nextState.moveAgents();
        nextState.diffuseGrid();
        // Spawn next agent if necessary
        Point start = nextState.start;
        if (nextState.currentTurn % nextState.spawnFreq == 0 && !nextState.grid[start.x][start.y].occupied) {
            nextState.grid[start.x][start.y].occupied = true;
            nextState.agents.add(new AgentLoc(start, new AgentState(), nextState.currentTurn));
        }
        // Deliberately placed after the logic to spawn new agents in order
        // to match the simulator's behavior.
        // `GameState.currentTurn` always indicates the turn that is about to be simulated.
        nextState.currentTurn += 1;

        return nextState;
    }

    private void placeChemical(ChemicalPlacement placement) {
        if (this.chemicalsRemaining == 0 || placement.location == null) {
            return;
        }
        if (this.chemicalsRemaining < placement.chemicals.size()) {
            return;
        }
        Point p = new Point(placement.location.x - 1, placement.location.y - 1);
        // Not sure why the placement has a list, but ok...
        for (ChemicalType c : placement.chemicals) {
            chemicalsRemaining -= 1;
            this.grid[p.x][p.y].cell.setConcentration(c, 1.0);
        }
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public Point getStart() {
        return start;
    }

    public Point getTarget() {
        return target;
    }

    public int getAgentGoal() {
        return agentGoal;
    }

    public int getSpawnFreq() {
        return spawnFreq;
    }

    public int getAgentsOnTarget() {
        return agentsOnTarget;
    }

    public int getChemicalsRemaining() {
        return chemicalsRemaining;
    }

    /**
     * Be careful! This function returns a reference to the grid to avoid
     * making a copy. Do not modify the state of this board directly.
     *
     * @return
     */
    public GameCell[][] getGrid() {
        return grid;
    }

    /**
     * Be careful! Do not modify the agent state directly.
     *
     * @return
     */
    public ArrayList<AgentLoc> getAgents() {
        return agents;
    }

    public void validateEquivalence(int currentTurn, int chemicalsRemaining, ArrayList<Point> agentLocations,
                                    ChemicalCell[][] grid) {
        if (this.currentTurn != currentTurn) {
            throw new RuntimeException("current turn " + this.currentTurn + " does not match" +
                    " expected: " + currentTurn);
        }

        if (this.chemicalsRemaining != chemicalsRemaining) {
            throw new RuntimeException("chemicals remaining: " + this.currentTurn + " does not match" +
                    " expected: " + currentTurn);
        }

        // Validate the grid
        if (this.grid.length != grid.length) {
            throw new RuntimeException("grid row count mismatch");
        }
        for (int row = 0; row < grid.length; ++row) {
            if (this.grid[row].length != grid[row].length) {
                throw new RuntimeException("column size mismatch for row " + row);
            }
            for (int col = 0; col < grid[row].length; ++col) {
                ChemicalCell ourCell = this.grid[row][col].cell;
                ChemicalCell otherCell = grid[row][col];
                if (!GameCell.chemCellEquals(ourCell, otherCell)) {
                    throw new RuntimeException("chemical cell concentration mismatch: " + ourCell + " vs expected " + otherCell
                            + " for cell " + row + ", " + col);
                }
            }
        }

        // This isn't a perfect test, but it should catch most mismatches
        for (Point agentLoc : agentLocations) {
            Point adjLoc = new Point(agentLoc.x - 1, agentLoc.y - 1);
            boolean found = false;
            for (AgentLoc actualLoc : this.agents) {
                if (adjLoc.equals(actualLoc.loc)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                continue;
            }
            throw new RuntimeException("agent location mismatch");
        }
    }


    /**
     * This function is impure and updates the `GameState` object.
     * <p>
     * Overview
     * for agent in agent list
     * if agent not at target
     * give agent a map of neighbors
     * get agent move
     * update game state (vacate old cell, occupy new cell, update agent state)
     */
    private void moveAgents() {
        int prevEpoch = -1;
        // Agent class re-used to run every `Agent.makeMove`
        Agent agentClass = new Agent(new SimPrinter(false));
        for (AgentLoc agent : this.agents) {
            if (agent.epoch <= prevEpoch) {
                throw new RuntimeException("agents out of epoch order");
            }
            prevEpoch = agent.epoch;
            // Don't move agents that have reached the target
            if (agent.loc.equals(this.target)) {
                continue;
            }

            // Create a map of chemical cells for the agent
            DirectionType[] dirs = {DirectionType.NORTH, DirectionType.SOUTH, DirectionType.WEST, DirectionType.EAST};
            HashMap<DirectionType, ChemicalCell> chems = new HashMap<>();
            for (DirectionType dir : dirs) {
                Point p = GameCell.pointInDirection(agent.loc, dir);
                if (this.pointOutOfBounds(p)) {
                    continue;
                }
                ChemicalCell cell = GameCell.cloneAttenuatedChemicalCell(this.grid[p.x][p.y].cell);
                chems.put(dir, cell);
            }

            // Get the agent's move
            int randNo = 0;  // lol
            ChemicalCell currentCell = GameCell.cloneAttenuatedChemicalCell(this.grid[agent.loc.x][agent.loc.y].cell);
            Move move = agentClass.makeMove(randNo, agent.state.serialize(), currentCell, chems);
            Point movePoint = GameCell.pointInDirection(agent.loc, move.directionType);

            // Agent internal state is always updated
            agent.state = new AgentState(move.currentState);

            // Validate Move
            if (this.pointOutOfBounds(movePoint)) {
                continue;
            }

            // Move is valid, check if agent can move there
            GameCell moveCell = this.grid[movePoint.x][movePoint.y];
            // Check to see if the cell is blocked (wall) or occupied by an agent
            if (moveCell.isBlocked() || moveCell.occupied) {
                continue;
            }

            // Vacate current cell, occupy new cell, update agent
            this.grid[agent.loc.x][agent.loc.y].occupied = false;
            if (movePoint.equals(this.target)) {
                // Agent de-spawning is implemented by not marking the target cell as occupied
                this.agentsOnTarget += 1;
            } else {
                this.grid[movePoint.x][movePoint.y].occupied = true;
            }
            // Update agent location
            agent.loc = movePoint;
        }
    }

    private void diffuseGrid() {
        GameCell[][] newGrid = new GameCell[this.grid.length][this.grid[0].length];
        for (int row = 0; row < this.grid.length; ++row) {
            for (int col = 0; col < this.grid[0].length; ++col) {
                GameCell newCell = new GameCell(this.grid[row][col]);

                // Calculate the concentration of each chemical type
                ChemicalType[] chems = {ChemicalType.BLUE, ChemicalType.RED, ChemicalType.GREEN};
                for (ChemicalType chemType : chems) {
                    GameCell currentCell = this.grid[row][col];
                    double points = 1.0;
                    double chemSum = currentCell.cell.getConcentration(chemType);
                    // Check neighboring cells
                    DirectionType[] dirs = {DirectionType.NORTH, DirectionType.SOUTH, DirectionType.WEST, DirectionType.EAST};
                    for (DirectionType d : dirs) {
                        // Candidate point
                        Point c = GameCell.pointInDirection(new Point(row, col), d);
                        // Check to see if the point is out of bounds or blocked
                        if (this.pointOutOfBounds(c) || this.grid[c.x][c.y].isBlocked()) {
                            continue;
                        }
                        points += 1.0;
                        chemSum += this.grid[c.x][c.y].cell.getConcentration(chemType);
                    }
                    // Chemical concentration is average of non-blocked surrounding points
                    newCell.cell.setConcentration(chemType, chemSum / points);
                }
                newGrid[row][col] = newCell;
            }
        }
        this.grid = newGrid;
    }

    // NB (etm): Does'nt seem possible to try out these methods unfortunately
    // Or maybe I just don't know the Java type system well enough
    private static GameCell[][] buildGrid(final ChemicalCell[][] chemCells) {
        GameCell[][] newGrid = new GameCell[chemCells.length][chemCells[0].length];
        for (int row = 0; row < newGrid.length; ++row) {
            for (int col = 0; col < newGrid[row].length; ++col) {
                newGrid[row][col] = new GameCell(chemCells[row][col]);
            }
        }
        return newGrid;
    }

    private static GameCell[][] cloneGrid(final GameCell[][] priorGrid) {
        GameCell[][] newGrid = new GameCell[priorGrid.length][priorGrid[0].length];
        for (int row = 0; row < newGrid.length; ++row) {
            for (int col = 0; col < newGrid[row].length; ++col) {
                newGrid[row][col] = new GameCell(priorGrid[row][col]);
            }
        }
        return newGrid;
    }

    private boolean pointOutOfBounds(final Point p) {
        return p.x < 0
                || p.x >= this.grid.length
                || p.y < 0
                || p.y >= this.grid[0].length;
    }
}
