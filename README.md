
# Project 2: Chemotaxis

## Course Summary

Course: COMS 4444 Programming and Problem Solving (Fall 2021)  
Website: http://www.cs.columbia.edu/~kar/4444f21  
University: Columbia University  
Instructor: Prof. Kenneth Ross  
TA: Griffin Adams

## Project Description

Chemotaxis involves following a gradient in chemical signal concentrations toward reaching a target. Given a set of three chemicals (red, green, and blue), a controller applies chemicals to the map, and an agent moves in response to chemical stimuli. These chemicals diffuse throughout the grid over time. Further, there may be blocked cells in the grid that agents must move around. Your objective is to move your agent through the map to reach a target location as quickly as possible.

## Implementation

This code was heavily adapted from the previous year's version of [Chemotaxis](https://github.com/adilovesgh/coms4444-chemotaxis).

Along with designing some maps for the project, you will be creating your own controller and agent that extend the simulator's abstract controller and agent, respectively. Please follow these steps to begin your implementation:
1.  Enter the `coms4444-chemotaxis/src/maps` directory, and create a folder called "g*x*" (where *x* is the number of your team). For example, if you are team "g5," please create a folder called "g5" in the `maps` directory.
2.  Create your maps inside your newly-created folder.
3.  Enter the `coms4444-chemotaxis/src/chemotaxis` directory, and create a folder called "g*x*" (where *x* is the number of your team). For example, if you are team "g5," please create a folder called "g5" in the `chemotaxis` directory.
4.  Create Java files called `Controller.java` and `Agent.java` inside your newly-created folder.
5.  Copy the following code into `Controller` (the TODOs indicate all changes you need to make):
```
package chemotaxis.gx; // TODO modify the package name to reflect your team

import java.awt.Point;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.SimPrinter;

public class Controller extends chemotaxis.sim.Controller {

   /**
    * Controller constructor
    *
    * @param start       start cell coordinates
    * @param target      target cell coordinates
    * @param size     	 grid/map size
    * @param simTime     simulation time
    * @param budget      chemical budget
    * @param seed        random seed
    * @param simPrinter  simulation printer
    *
    */
   public Controller(Point start, Point target, Integer size, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
   	super(start, target, size, simTime, budget, seed, simPrinter);
   }

   /**
    * Apply chemicals to the map
    *
    * @param currentTurn         current turn in the simulation
    * @param chemicalsRemaining  number of chemicals remaining
    * @param locations           current locations of the agents
    * @param grid                game grid/map
    * @return                    a cell location and list of chemicals to apply
    *
    */
   @Override
   public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid) {
      // TODO add your code here to apply chemicals

      return null; // TODO modify the return statement to return your chemical placement
   }
}
```
6.  Copy the following code into `Agent` (the TODOs indicate all changes you need to make):
```
package chemotaxis.gx; // TODO modify the package name to reflect your team

import java.util.Map;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;

public class Agent extends chemotaxis.sim.Agent {

   /**
    * Agent constructor
    *
    * @param simPrinter  simulation printer
    *
    */
   public Agent(SimPrinter simPrinter) {
      super(simPrinter);
   }

   /**
    * Move agent
    *
    * @param randomNum        random number available for agents
    * @param previousState    byte of previous state
    * @param currentCell      current cell
    * @param neighborMap      map of cell's neighbors
    * @return                 agent move
    *
    */
   @Override
   public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
      // TODO add your code here to move the agent

      return null; // TODO modify the return statement to return your agent move
   }
}
```


## Submission

To submit your code for each class and for the final deliverable of the project, you will create a pull request to merge your branch into the *master* branch. The TA will merge the commits from the pull request after the deliverable deadline has passed. The base repository will be updated before the start of the next class meeting.

In order to improve performance and readability of code during simulations, we would like to prevent flooding the console with print statements. Therefore, we have provided a printer called `SimPrinter` to allow for toggled printing to the console. When adding print statements for testing/debugging in your code, please make sure to use the methods in `SimPrinter` (instance available in `Player`) rather than use `System.out` statements directly. Additionally, please set the `enablePrints` default variable in `Simulator` to *true* in order to enable printing. This also allows us to not require that you comment out any print statements in your code submissions.


## Simulator

#### Steps to run the simulator:
1.  On your command line, clone the git repository.
2.  Enter `cd coms4444-chemotaxis/src` to enter the source folder of the repository.
3.  Run `make clean` and `make compile` to clean and compile the code.
4.  Run one of the following:
    * `make report`: report simulation results to the console/log file using command-line simulation arguments
    * `make verify`: verify that a map configuration is valid
    * `make gui`: run simulations from the GUI with live modifications to simulation arguments

#### Simulator arguments:
> **[-r | --turns]**: total number of turns (default = 100)

> **[-t | --team]**: team/player

> **[-b | --budget]**: chemical budget (default = 100)

> **[-c | --check]**: verify map validity when a map is specified

> **[-m PATH | --map PATH]**: path to the simulation map, specifying the map size and locations of blocked cells

> **[-s | --seed]**: seed value for random player (default = 10)

> **[-l PATH | --log PATH]**: enable logging and output log to both console and log file

> **[-v | --verbose]**: record verbose log when logging is enabled (default = false)

> **[-g | --gui]**: enable GUI (default = false)
 
> **[-a | --agentGoal]**: number of agents needed to reach target to "win" (default = 3)

> **[-f | --fpm]**: speed (frames per minute) of GUI when continuous GUI is enabled (default = 15)

> **[-r | --spawnFreq]**: Spawn Frequency of new Agents (default behavior is that once every 10 turns a new agent is spawned)


## Map Configuration

A map file (*.map* extension) contains a grid configuration of the simulation. Each map configuration specifies the size of the map on the first line, followed by the coordinates of the start and target locations on the second line, and the coordinates of any blocked locations on successive lines. An example of a map configuration is as follows:

```
100	      # Size of grid
20 30 80 90   # (x, y) of start cell followed by (x, y) of target cell
15 23	      # Blocked cell 1
29 84	      # Blocked cell 2
47 36	      # Blocked cell 3
...
```

Note that the game grid/map is 1-indexed, not 0-indexed. For instance, location (1, 1) on the map would be the top-left cell in the map.


## API Description

The following provides the API available for students to use:
1. `Agent`: the agent abstraction that should be extended by implemented agents.
	*	`makeMove`: specifies a move that will be made by the agent, given the chemical concentrations of the current cell and those of its neighbors.
2. `ChemicalCell`: a container of chemical concentrations in a particular cell of the grid.
	*	`applyConcentration`: applies a full concentration (1.0) of a chemical if the cell is unblocked.
	*	`getConcentration`: retrieves the concentration of a chemical at a cell.
	*	`setConcentration`: sets a concentration of a chemical if the cell is unblocked.
	*	`getConcentrations`: returns the concentrations of all chemicals at a cell.
	*	`setConcentrations`: sets the concentrations of all chemicals at a cell.
	*	`isBlocked`: checks if a cell is a blocked location.
	*	`isOpen`: checks if a cell is an open location.
	*	`toString`: returns an *(r, g, b)* vector of the chemical concentrations at a cell.
3. `ChemicalPlacement`: a structure containing the location where chemicals are applied, as well as the chemicals that will be applied.
4. `Controller`: the controller abstraction that should be extended by implemented controllers.
	*	`applyChemicals`: specifies chemicals to be applied to the map, as well as a location to apply chemicals to.
5. `DirectionType`: any of the cardinal directions and the current location.
6. `Move`: a move containing a direction to move in and a byte of state to preserve.
7. `SimPrinter`: contains methods for toggled printing.
	* `println`: prints with cursor at start of the next line.
	* `print`: prints with cursor at the end of the current line.

Classes that are used by the simulator include:
1. `AgentWrapper`: a player wrapper that enforces appropriate timeouts on agent moves.
2. `ControllerWrapper`: a player wrapper that enforces appropriate timeouts on controller decisions.
3. `HTTPServer`: a lightweight web server for the simulator.
4. `Log`: basic functionality to log results, with the option to enable verbose logging.
5. `Simulator`: the simulator and entry point for the project; manages the agent and controller, wrapper, logging, server, and GUI state.
6. `Timer`: basic functionality for imposing timeouts.

## Ed
If you have any questions about the project, please post them in [Ed](https://edstem.org/us/courses/12432/discussion/)


## Disclaimer
This project belongs to Columbia University. It may be freely used for educational purposes.
