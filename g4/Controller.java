package chemotaxis.g4; 

import java.awt.Point;
import java.util.PriorityQueue;

import javax.swing.text.DefaultStyledDocument.ElementSpec;

import java.io.*;
import java.util.HashMap;
import java.lang.Math; 
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;
import chemotaxis.sim.Timer;


public class Controller extends chemotaxis.sim.Controller {
    HashMap<Point, Point> came_from;
    HashMap<Point, Integer> cost_so_far;
	List<Point> path;
	
	private Node[][] bestPath;
	private PriorityQueue<Node> frontier;

	private int startX;
	private int startY;
	private int targetX;
	private int targetY;

	private int counter = 0;

	private ArrayList<DirectionChange> directionChanges;

	boolean previouslyTimedOut = false;
	boolean currentlyTimedOut = false;

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
		this.frontier = new PriorityQueue<>();
		this.directionChanges = new ArrayList<>(0);
		this.startX = (int)start.getX() - 1;
		this.startY = (int)start.getY() - 1;
		this.targetY = (int)target.getY() - 1;
		this.targetX = (int)target.getX() - 1;
   }

   /**
    * Apply chemicals to the map
    *
    * @param currentTurn         current turn in the simulation
    * @param chemicalsRemaining  number of chemicals remaining
    * @param currentLocation     current location of the agent
    * @param grid                game grid/map
    * @return                    a cell location and list of chemicals to apply
    *
    */
   @Override
	public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, Point currentLocation, ChemicalCell[][] grid) {
		//find path in 1st round OR when no valid path was found previously
		if (currentTurn == 1 || bestPath[this.targetX][this.targetY].getParent() == null) {
			//System.out.println("calculating path...");

			previouslyTimedOut = currentlyTimedOut;
			currentlyTimedOut = true;
			currentlyTimedOut = !findPath(currentTurn, chemicalsRemaining, currentLocation, grid, !this.previouslyTimedOut);

		}

		if (bestPath[this.targetX][this.targetY].getParent() != null) {

			ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
			List<ChemicalType> chemicals = new ArrayList<>();
			//chemicalPlacement.location = new Point(1, 1);

			if (directionChanges.size() > 0 && directionChanges.get(directionChanges.size() - 1).atPostion(currentLocation)) {
				Direction direction = directionChanges.get(directionChanges.size() - 1).getDirection();
				
				if (direction == Direction.NORTH) {
					chemicals.add(ChemicalType.BLUE);
					chemicalPlacement.location = new Point((int)currentLocation.getX() - 1, (int)currentLocation.getY());	
				}
				else if (direction == Direction.SOUTH) {
					chemicals.add(ChemicalType.BLUE);
					chemicalPlacement.location = new Point((int)currentLocation.getX() + 1, (int)currentLocation.getY());	
				}
				else if (direction == Direction.WEST) {
					chemicals.add(ChemicalType.BLUE);
					chemicalPlacement.location = new Point((int)currentLocation.getX(), (int)currentLocation.getY() - 1);	
				}
				else if (direction == Direction.EAST) {
					chemicals.add(ChemicalType.BLUE);
					chemicalPlacement.location = new Point((int)currentLocation.getX(), (int)currentLocation.getY() + 1);	
				}
				else if (direction == Direction.NE) {
					chemicals.add(ChemicalType.RED);
					chemicalPlacement.location = new Point((int)currentLocation.getX() - 1, (int)currentLocation.getY());
				}
				else if (direction == Direction.SE) {
					chemicals.add(ChemicalType.RED);
					chemicalPlacement.location = new Point((int)currentLocation.getX() + 1, (int)currentLocation.getY());
				}
				else if (direction == Direction.NW) {
					chemicals.add(ChemicalType.GREEN);
					chemicalPlacement.location = new Point((int)currentLocation.getX() - 1, (int)currentLocation.getY());
				}
				else if (direction == Direction.SW) {
					chemicals.add(ChemicalType.GREEN);
					chemicalPlacement.location = new Point((int)currentLocation.getX() + 1, (int)currentLocation.getY());
				}
				directionChanges.remove(directionChanges.size() - 1);
			}
			chemicalPlacement.chemicals = chemicals;
			
			if (chemicalPlacement.location != null) {
				printAppliedChemicals(chemicalPlacement, currentTurn);
			}
			return chemicalPlacement;
		}

		//if path has more turning points than chemicals, do nothing
	
		ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
		List<ChemicalType> chemicals = new ArrayList<>();
		//chemicals.add(ChemicalType.BLUE);
		//chemicalPlacement.location = new Point(5, 5);
		chemicalPlacement.chemicals = chemicals;
		//System.out.println("no valid");
		return chemicalPlacement;
   }

   private void printAppliedChemicals(ChemicalPlacement placement, int turn) {
	   //System.out.println("Applied at position " + placement.location + "at turn " + turn + "\n=================================================================\n");
   }


   private boolean findPath(Integer currentTurn, Integer chemicalsRemaining, Point currentLocation, ChemicalCell[][] grid, boolean searchDiagonals){
		this.bestPath = new Node[this.size][this.size];

		for (int i = 0; i < this.size; i++) {
			for (int j = 0; j < this.size; j++) {
				bestPath[i][j] = new Node();
			}
		}

		int startX = (int)currentLocation.getX() - 1;
		int startY = (int)currentLocation.getY() - 1;
		bestPath[startX][startY] = new Node(startX, startY); // A node with 0 length and turns
		frontier.add(new Node(startX, startY));
		
		// Create nodes with all directions and add them to frontier
		// Node N = new Node(null, startX, startY, Direction.NORTH, Direction.NORTH, 0);
		// Node S = new Node(null, startX, startY, Direction.SOUTH, Direction.SOUTH, 0);
		// Node E = new Node(null, startX, startY, Direction.EAST, Direction.EAST, 0);
		// Node W = new Node(null, startX, startY, Direction.WEST, Direction.WEST, 0);

		// Node NE1 = new Node(null, startX, startY, Direction.NE, Direction.NORTH, 0);
		// Node NE2 = new Node(null, startX, startY, Direction.NE, Direction.EAST, 0);

		// Node SE1 = new Node(null, startX, startY, Direction.SE, Direction.SOUTH, 0);
		// Node SE2 = new Node(null, startX, startY, Direction.SE, Direction.EAST, 0);

		// Node NW1 = new Node(null, startX, startY, Direction.NW, Direction.NORTH, 0);
		// Node NW2 = new Node(null, startX, startY, Direction.NW, Direction.WEST, 0);

		// Node SW1 = new Node(null, startX, startY, Direction.SW, Direction.SOUTH, 0);
		// Node SW2 = new Node(null, startX, startY, Direction.SW, Direction.WEST, 0);

		// frontier.add(N);
		// frontier.add(S);
		// frontier.add(E);
		// frontier.add(W);
		// frontier.add(NE1);
		// frontier.add(NE2);
		// frontier.add(SE1);
		// frontier.add(SE2);
		// frontier.add(NW1);
		// frontier.add(NW2);
		// frontier.add(SW1);
		// frontier.add(SW2);		

		boolean pathFound = false;
		while (!frontier.isEmpty() && !pathFound) {
			// System.out.println(counter++ + ",  " + frontier.size());
			Node currentNode = frontier.remove();
			
			boolean addAllPaths = true;



			// Search path north
			if (currentNode.getX() > 0) {
				if (grid[currentNode.getX() - 1][currentNode.getY()].isOpen()) {
					Node newPath = new Node(currentNode, currentNode.getX() - 1, currentNode.getY());
					if (newPath.getTurns() <= chemicalsRemaining) {
						int newTurns = newPath.getTurns();
						int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
						int newLength = newPath.getLength();
						int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


						if ((newTurns <= oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;

							if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
								pathFound = true;
							}
						}
						else if (newTurns > oldTurns && newLength < oldLength) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;
						}
						else if (newTurns < oldTurns && newLength > oldLength) {
							frontier.add(newPath);
						}

					}
				}
			}

			// Search path south
			if (currentNode.getX() < this.size - 1) {
				if (grid[currentNode.getX() + 1][currentNode.getY()].isOpen()) {
					Node newPath = new Node(currentNode, currentNode.getX() + 1, currentNode.getY());
					if (newPath.getTurns() <= chemicalsRemaining) {
						int newTurns = newPath.getTurns();
						int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
						int newLength = newPath.getLength();
						int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


						if ((newTurns <= oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;

							if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
								pathFound = true;
							}
						}
						else if (newTurns > oldTurns && newLength < oldLength) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;
						}
						else if (newTurns < oldTurns && newLength > oldLength) {
							frontier.add(newPath);
						}

					}
				}
			}

			// Search path east
			if (currentNode.getY() < this.size - 1) {
				if (grid[currentNode.getX()][currentNode.getY() + 1].isOpen()) {
					Node newPath = new Node(currentNode, currentNode.getX(), currentNode.getY() + 1);
					if (newPath.getTurns() <= chemicalsRemaining) {
						int newTurns = newPath.getTurns();
						int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
						int newLength = newPath.getLength();
						int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


						if ((newTurns <= oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;

							if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
								pathFound = true;
							}
						}
						else if (newTurns > oldTurns && newLength < oldLength) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;
						}
						else if (newTurns < oldTurns && newLength > oldLength) {
							frontier.add(newPath);
						}

					}
				}
			}

			// Search path west
			if (currentNode.getY() > 0) {
				if (grid[currentNode.getX()][currentNode.getY() - 1].isOpen()) {
					Node newPath = new Node(currentNode, currentNode.getX(), currentNode.getY() - 1);
					if (newPath.getTurns() <= chemicalsRemaining) {
						int newTurns = newPath.getTurns();
						int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
						int newLength = newPath.getLength();
						int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


						if ((newTurns <= oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;

							if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
								pathFound = true;
							}
						}
						else if (newTurns > oldTurns && newLength < oldLength) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;
						}
						else if (newTurns < oldTurns && newLength > oldLength) {
							frontier.add(newPath);
						}

					}
				}
			}

			if (searchDiagonals) {
					// Search path NE going north
				if (currentNode.getX() > 0) {
					if (grid[currentNode.getX() - 1][currentNode.getY()].isOpen()) {
						
						int newTurns = currentNode.getTurns();

						if (currentNode.getDirection() != Direction.NE || currentNode.getNextDirection() != Direction.NORTH) {
							newTurns += 1;
						}

						Node newPath = new Node(currentNode, currentNode.getX() - 1, currentNode.getY(), Direction.NE, Direction.EAST, newTurns);
						if (newPath.getTurns() <= chemicalsRemaining) {
							newTurns = newPath.getTurns();
							int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
							int newLength = newPath.getLength();
							int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


							if ((newTurns <= oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;

								if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
									pathFound = true;
								}
							}
							else if (newTurns > oldTurns && newLength < oldLength) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;
							}
							else if (newTurns < oldTurns && newLength > oldLength) {
								frontier.add(newPath);
							}

						}
					}
				}


				// Search path NE going east
				if (currentNode.getY() < size - 1 && currentNode.getDirection() == Direction.NE) {
					if (grid[currentNode.getX()][currentNode.getY() + 1].isOpen()) {
						
						int newTurns = currentNode.getTurns();

						if (currentNode.getDirection() != Direction.NE || currentNode.getNextDirection() != Direction.EAST) {
							newTurns += 1;
						}

						Node newPath = new Node(currentNode, currentNode.getX(), currentNode.getY() + 1, Direction.NE, Direction.NORTH, newTurns);
						if (newPath.getTurns() <= chemicalsRemaining) {
							newTurns = newPath.getTurns();
							int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
							int newLength = newPath.getLength();
							int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


							if ((newTurns <= oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;

								if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
									pathFound = true;
								}
							}
							else if (newTurns > oldTurns && newLength < oldLength) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;
							}
							else if (newTurns < oldTurns && newLength > oldLength) {
								frontier.add(newPath);
							}

						}
					}
				}


				// Search path SE going south
				if (currentNode.getX() < size - 1) {
					if (grid[currentNode.getX() + 1][currentNode.getY()].isOpen()) {
						
						int newTurns = currentNode.getTurns();

						if (currentNode.getDirection() != Direction.SE || currentNode.getNextDirection() != Direction.SOUTH) {
							newTurns += 1;
						}

						Node newPath = new Node(currentNode, currentNode.getX() + 1, currentNode.getY(), Direction.SE, Direction.EAST, newTurns);
						if (newPath.getTurns() <= chemicalsRemaining) {
							newTurns = newPath.getTurns();
							int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
							int newLength = newPath.getLength();
							int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


							if ((newTurns <= oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;

								if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
									pathFound = true;
								}
							}
							else if (newTurns > oldTurns && newLength < oldLength) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;
							}
							else if (newTurns < oldTurns && newLength > oldLength) {
								frontier.add(newPath);
							}

						}
					}
				}


				// Search path SE going east
				if (currentNode.getY() < size - 1 && currentNode.getDirection() == Direction.SE) {
					if (grid[currentNode.getX()][currentNode.getY() + 1].isOpen()) {
						
						int newTurns = currentNode.getTurns();

						if (currentNode.getDirection() != Direction.SE || currentNode.getNextDirection() != Direction.EAST) {
							newTurns += 1;
						}

						Node newPath = new Node(currentNode, currentNode.getX(), currentNode.getY() + 1, Direction.SE, Direction.SOUTH, newTurns);
						if (newPath.getTurns() <= chemicalsRemaining) {
							newTurns = newPath.getTurns();
							int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
							int newLength = newPath.getLength();
							int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


							if ((newTurns <= oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;

								if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
									pathFound = true;
								}
							}
							else if (newTurns > oldTurns && newLength < oldLength) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;
							}
							else if (newTurns < oldTurns && newLength > oldLength) {
								frontier.add(newPath);
							}

						}
					}
				}


				// Search path NW going north
				if (currentNode.getX() > 0) {
					if (grid[currentNode.getX() - 1][currentNode.getY()].isOpen()) {
						
						int newTurns = currentNode.getTurns();

						if (currentNode.getDirection() != Direction.NW || currentNode.getNextDirection() != Direction.NORTH) {
							newTurns += 1;
						}

						Node newPath = new Node(currentNode, currentNode.getX() - 1, currentNode.getY(), Direction.NW, Direction.WEST, newTurns);
						if (newPath.getTurns() <= chemicalsRemaining) {
							newTurns = newPath.getTurns();
							int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
							int newLength = newPath.getLength();
							int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


							if ((newTurns <= oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;

								if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
									pathFound = true;
								}
							}
							else if (newTurns > oldTurns && newLength < oldLength) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;
							}
							else if (newTurns < oldTurns && newLength > oldLength) {
								frontier.add(newPath);
							}

						}
					}
				}


				// Search path NW going west
				if (currentNode.getY() > 0 && currentNode.getDirection() == Direction.NW) {
					if (grid[currentNode.getX()][currentNode.getY() - 1].isOpen()) {
						
						int newTurns = currentNode.getTurns();

						if (currentNode.getDirection() != Direction.NW || currentNode.getNextDirection() != Direction.WEST) {
							newTurns += 1;
						}

						Node newPath = new Node(currentNode, currentNode.getX(), currentNode.getY() - 1, Direction.NW, Direction.NORTH, newTurns);
						if (newPath.getTurns() <= chemicalsRemaining) {
							newTurns = newPath.getTurns();
							int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
							int newLength = newPath.getLength();
							int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


							if ((newTurns <= oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;

								if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
									pathFound = true;
								}
							}
							else if (newTurns > oldTurns && newLength < oldLength) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;
							}
							else if (newTurns < oldTurns && newLength > oldLength) {
								frontier.add(newPath);
							}

						}
					}
				}


				// Search path SW going south
				if (currentNode.getX() < size - 1) {
					if (grid[currentNode.getX() + 1][currentNode.getY()].isOpen()) {
						
						int newTurns = currentNode.getTurns();

						if (currentNode.getDirection() != Direction.SW || currentNode.getNextDirection() != Direction.SOUTH) {
							newTurns += 1;
						}

						Node newPath = new Node(currentNode, currentNode.getX() + 1, currentNode.getY(), Direction.SW, Direction.WEST, newTurns);
						if (newPath.getTurns() <= chemicalsRemaining) {
							newTurns = newPath.getTurns();
							int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
							int newLength = newPath.getLength();
							int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


							if ((newTurns <= oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;

								if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
									pathFound = true;
								}
							}
							else if (newTurns > oldTurns && newLength < oldLength) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;
							}
							else if (newTurns < oldTurns && newLength > oldLength) {
								frontier.add(newPath);
							}

						}
					}
				}


				// Search path SW going west
				if (currentNode.getY() > 0 && currentNode.getDirection() == Direction.SW) {
					if (grid[currentNode.getX()][currentNode.getY() - 1].isOpen()) {
						
						int newTurns = currentNode.getTurns();

						if (currentNode.getDirection() != Direction.SW || currentNode.getNextDirection() != Direction.WEST) {
							newTurns += 1;
						}

						Node newPath = new Node(currentNode, currentNode.getX(), currentNode.getY() - 1, Direction.SW, Direction.SOUTH, newTurns);
						if (newPath.getTurns() <= chemicalsRemaining) {
							newTurns = newPath.getTurns();
							int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
							int newLength = newPath.getLength();
							int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


							if ((newTurns <= oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;

								if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
									pathFound = true;
								}
							}
							else if (newTurns > oldTurns && newLength < oldLength) {
								frontier.add(newPath);
								bestPath[newPath.getX()][newPath.getY()] = newPath;
							}
							else if (newTurns < oldTurns && newLength > oldLength) {
								frontier.add(newPath);
							}

						}
					}
				}
			}

			


			
			
		}

		// Reconstruct path
		if (bestPath[this.targetX][this.targetY].getParent() != null) {
			Node currentNode = bestPath[this.targetX][this.targetY];

			while (currentNode.getParent() != null) {
				Node parent = currentNode.getParent();

				if (parent.getDirection() != currentNode.getDirection()) {
					directionChanges.add(new DirectionChange(parent.getX(), parent.getY(), currentNode.getDirection()));
				}

				currentNode = parent;
			}

			for (DirectionChange d : directionChanges) {
			//System.out.println(d.getX() + ", " + d.getY() + ", " + d.getDirection());
			}
			// try {
			// 	wait(10000);
			// }
			// catch(InterruptedException e) {

			// }
		}

		return true;

   }

}


class Node implements Comparable<Node> {
	private Node parent;
	private int x;
	private int y;
	private Direction direction;	// current direction of movement (North, North-East etc.)
	private Direction nextDirection = null; // If moving on diagonal, next orthogonal direction needed
	private int turns;
	private int length;

	// Constructor for initial nodes
	public Node() {
		this.parent = null;
		this.x = 0;
		this.y = 0;
		this.direction = Direction.NULL;
		this.nextDirection = Direction.NULL;
		this.turns = 9999999;
		this.length = 9999999;
	}

	// Constructor for new node
	public Node(Node parent, int x, int y, Direction desiredDirection, Direction nextDirection, int newTurns) {
		this.parent = parent;
		this.x = x;
		this.y = y;
		this.direction = desiredDirection;
		this.nextDirection = nextDirection;
		
		// Means we're creating paths in the START cell
		if (this.parent == null) {
			this.turns = 0;
			this.length = 0;
		}
		else {
			this.length = parent.length + 1;
			this.turns = newTurns;
		}
	}

	// Constructor for start node
	public Node(int x, int y) {
		this.parent = null;
		this.x = x;
		this.y = y;
		this.direction = Direction.NULL;
		this.turns = 0;
		this.length = 0;
	}

	// Constructor for possible paths
	public Node(Node parent, int x, int y) {
		this.parent = parent;
		this.x = x;
		this.y = y;
		this.length = parent.getLength() + 1;
		// Get direction
		if (this.x == parent.getX()) {
			if (this.y > parent.getY()) {
				this.direction = Direction.EAST;
			}
			else {
				this.direction = Direction.WEST;
			}
		}
		else {
			if (this.x < parent.getX()) {
				this.direction = Direction.NORTH;
			}
			else {
				this.direction = Direction.SOUTH;
			}
		}
		if (this.direction == parent.getDirection()) {
			this.turns = parent.getTurns();
		}
		else {
			this.turns = parent.getTurns() + 1;
		}
	}

	@Override
    public int compareTo(Node other) {
		if (this.length != other.getLength()) {
			return this.length - other.getLength();
		}
		return this.turns - other.getTurns();	
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public Node getParent() {
		return this.parent;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public Direction getNextDirection() {
		return this.nextDirection;
	}

	public int getTurns() {
		return this.turns;
	}

	public int getLength() {
		return this.length;
	}
}

class DirectionChange {
	// At coordinate (x, y) we need to start moving in a new direction
	private int x;
	private int y;
	private Direction direction;

	public DirectionChange(int x, int y, Direction direction) {
		this.x = x;
		this.y = y;
		this.direction = direction;
	}
	
	public boolean atPostion(Point p) {
		return p.getX() == this.x + 1 && p.getY() == this.y + 1;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public Direction getDirection() {
		return this.direction;
	}
}


enum Direction {
    NORTH,
    SOUTH,
	EAST,
	WEST,
	NE,
	NW,
	SE,
	SW,
	NULL
}