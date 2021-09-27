package chemotaxis.g1;


import chemotaxis.sim.*;
import chemotaxis.sim.ChemicalCell.ChemicalType;


import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

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

class GameCell {
    public boolean occupied;
    public ChemicalCell cell;

    /**
     * Copy constructor
     *
     * @param priorCell
     */
    GameCell(GameCell priorCell) {
        this.occupied = priorCell.occupied;
        this.cell = GameCell.cloneChemicalCell(priorCell.cell);
    }

    /**
     * Default constructor from ChemicalCell
     *
     * @param priorCell
     */
    GameCell(ChemicalCell priorCell) {
        this.occupied = false;
        this.cell = GameCell.cloneChemicalCell(priorCell);
    }

    public boolean isBlocked() {
        return this.cell.isBlocked();
    }

    // ChemicalCell doesn't have a proper clone method
    public static ChemicalCell cloneChemicalCell(final ChemicalCell priorCell) {
        ChemicalCell newCell = new ChemicalCell(priorCell.isOpen());
        ChemicalType[] chems = {ChemicalType.BLUE, ChemicalType.RED, ChemicalType.GREEN};
        for (ChemicalType c : chems) {
            newCell.setConcentration(c, priorCell.getConcentration(c));
        }
        return newCell;
    }

    /**
     * Attenuates concentrations < 0.001 to 0
     *
     * @param priorCell
     * @return
     */
    public static ChemicalCell cloneAttenuatedChemicalCell(final ChemicalCell priorCell) {
        double MIN_DETECTABLE_CONCENTRATION = 0.001;
        ChemicalCell newCell = new ChemicalCell(priorCell.isOpen());
        ChemicalType[] chems = {ChemicalType.BLUE, ChemicalType.RED, ChemicalType.GREEN};
        for (ChemicalType c : chems) {
            double conc = priorCell.getConcentration(c);
            if (conc < MIN_DETECTABLE_CONCENTRATION) {
                conc = 0;
            }
            newCell.setConcentration(c, conc);
        }
        return newCell;
    }

    /**
     * Returns the points north, south, east, and west of `p`
     * <p>
     * NOTE: Points are not validated and may be out of bounds.
     * Separate bounds checkin is required.
     *
     * @param p
     * @return
     */
    public static Point[] pointNeighbors(final Point p) {
        int row = p.x;
        int col = p.y;
        // NOTE: Do NOT modify the order in which the points are returned.
        // Other code depends on these orders
        Point[] candidates = {
                new Point(row - 1, col), new Point(row + 1, col),
                new Point(row, col - 1), new Point(row, col + 1)
        };
        return candidates;
    }

    // Directions corresponding to the order of points returned by pointNeighbors
    // Too bad Java doesn't have tuples and pattern matching..........
    public static DirectionType[] pointNeighborsDirections() {
        return new DirectionType[]{DirectionType.NORTH, DirectionType.SOUTH, DirectionType.WEST, DirectionType.EAST};
    }
}

public class GameState {
    private int currentTurn;
    private final Point start;
    private final Point target;
    private final int agentGoal;
    private final int spawnFreq;
    private int chemicalsRemaining = 0;
    private GameCell[][] grid;
    private ArrayList<AgentLoc> agents;
    private chemotaxis.sim.Move Move;

    /**
     * Constructor to initialize a brand new game
     *
     * @param start
     * @param target
     * @param agentGoal
     * @param spawnFreq
     * @param chemicalsRemaining
     * @param grid
     */
    private GameState(final Point start, final Point target, int agentGoal, int spawnFreq,
                      int chemicalsRemaining, ChemicalCell[][] grid) {
        this.currentTurn = 1;
        this.start = new Point(start);
        this.target = new Point(target);
        this.agentGoal = agentGoal;
        this.spawnFreq = spawnFreq;
        this.chemicalsRemaining = chemicalsRemaining;
        this.grid = GameState.buildGrid(grid);
        this.agents = new ArrayList<>();
        // Epoch 0 here is not a bug. The sim always spawns an agent on turn "0"
        // and then plays turn 1, which may spawn another agent.
        this.agents.add(new AgentLoc(target, new AgentState(), 0));
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
    private GameState placeChemicalAndStep(ChemicalPlacement placement) {
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
        Point p = placement.location;
        // Not sure why the placement has a list, but ok...
        for (ChemicalType c : placement.chemicals) {
            this.grid[p.x][p.y].cell.setConcentration(c, 1.0);
        }
    }

    private void moveAgents() {
        // TODO
        // For agent in agent list (should be sorted in epoch order)
        // If agent not at target
        // Provide neighbors to agent
        // Get move
        // Update game state
        int prevEpoch = -1;
        // Agent class re-used to run every `Agent.makeMove`
        Agent agentClass = new Agent(new SimPrinter(false));
        for (AgentLoc agent : this.agents) {
            if (agent.epoch >= prevEpoch) {
                throw new RuntimeException("agents out of epoch order");
            }
            prevEpoch = agent.epoch;
            // Don't move agents that have reached the target
            if (agent.loc.equals(this.start)) {
                continue;
            }

            // Create a map of chemical cells for the agent
            Point[] candidates = GameCell.pointNeighbors(agent.loc);
            DirectionType[] candidateDirections = GameCell.pointNeighborsDirections();
            HashMap<DirectionType, ChemicalCell> chems = new HashMap<>();
            for (int i = 0; i < candidates.length; ++i) {
                Point p = candidates[i];
                if (this.pointOutOfBounds(p)) {
                    continue;
                }
                ChemicalCell cell = GameCell.cloneAttenuatedChemicalCell(this.grid[p.x][p.y].cell);
                chems.put(candidateDirections[i], cell);
            }
            // Get the agent's move
            int randNo = 0;  // lol
            ChemicalCell currentCell = GameCell.cloneAttenuatedChemicalCell(this.grid[agent.loc.x][agent.loc.y].cell);
            Move = agentClass.makeMove(randNo, agent.state.serialize(), currentCell, chems);

            // TODO(etm): Update the game state
        }

        throw new RuntimeException("not implemented");
    }

    private void diffuseGrid() {
        GameCell[][] newGrid = new GameCell[this.grid.length][this.grid[0].length];
        for (int row = 0; row < this.grid.length; ++row) {
            for (int col = 0; col < this.grid[0].length; ++col) {
                GameCell newCell = new GameCell(this.grid[row][col]);

                // Calculate the concentration of each chemical type
                ChemicalType[] chems = {ChemicalType.BLUE, ChemicalType.RED, ChemicalType.GREEN};
                for (ChemicalType chemType : chems) {
                    Point[] candidates = GameCell.pointNeighbors(new Point(row, col));
                    GameCell currentCell = this.grid[row][col];
                    double points = 1.0;
                    double chemSum = currentCell.cell.getConcentration(chemType);
                    // Check neighboring cells
                    for (Point c : candidates) {
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
                || p.x > this.grid.length
                || p.y < 0
                || p.y > this.grid[0].length;
    }
}
