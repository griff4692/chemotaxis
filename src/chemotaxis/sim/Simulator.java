/*
    Project: Chemotaxis
    Course: COMS 4444 Programming & Problem Solving (Fall 2021)
    Instructor: Prof. Kenneth Ross
    URL: http://www.cs.columbia.edu/~kar/4444f21
    Authors: Aditya Sridhar, Griffin Adams
    Simulator Version: 2.0
*/

package chemotaxis.sim;

import java.awt.Desktop;
import java.awt.Point;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import chemotaxis.sim.ChemicalCell.ChemicalType;

public class Simulator {
	
	// Simulator structures
	private static String teamName, mapName;
	private static ControllerWrapper controllerWrapper;
	private static ChemicalCell[][] grid;
	private static Random random;
	private static HTTPServer server;
	
	// Simulator inputs
	private static int seed = 10;
	private static int turns = 100;
	private static int agentGoal = 3;
	private static int budget = 50;
	private static int spawnFreq = 10;
	private static double fpm = 15;
	private static boolean showGUI = false;
	private static boolean verifyMap = false;
	
	// Defaults
	private static boolean validMap = true;
	private static boolean enableControllerPrints = false;
	private static boolean enableAgentPrints = false;
	private static int chemicalsRemaining = budget;
	private static Point start, target;
	private static Map<Integer,Point> agentLocations;
	private static Map<Integer,Byte> previousStates;
	private static List<Point> blockedLocations;
	private static Map<ChemicalType, Integer> chemicalsUsed;
	private static int mapSize = 100;
	private static long timeout = 1000;
	private static int currentTurn = 0;
	private static String version = "2.0";
	private static String projectPath, sourcePath, staticsPath, guiPath;
    

	private static void setup() {
		projectPath = new File(".").getAbsolutePath().substring(0, 
				new File(".").getAbsolutePath().indexOf("chemotaxis") + "chemotaxis".length());
		sourcePath = projectPath + File.separator + "src";
		staticsPath = projectPath + File.separator + "statics";
	}
	
	private static void parseCommandLineArguments(String[] args){
		chemicalsUsed = new HashMap<>();
		for(ChemicalType chemicalType : ChemicalType.values())
			chemicalsUsed.put(chemicalType, 0);
		
		blockedLocations = new ArrayList<>();
		for(int i = 0; i < args.length; i++) {
            switch (args[i].charAt(0)) {
                case '-':
                    if(args[i].equals("-t") || args[i].equals("--team")) {
                        i++;
                    	if(i == args.length) 
                            throw new IllegalArgumentException("The team name is missing!");
                        teamName = args[i];
                    }
                    else if(args[i].equals("-g") || args[i].equals("--gui"))
                        showGUI = true;
                    else if(args[i].equals("-c") || args[i].equals("--check"))
                    	verifyMap = true;
                    else if(args[i].equals("-l") || args[i].equals("--log")) {
                        i++;
                    	if(i == args.length) 
                            throw new IllegalArgumentException("The log file path is missing!");
                        Log.setLogFile(args[i]);
                        Log.assignLoggingStatus(true);
                    }
                    else if(args[i].equals("-v") || args[i].equals("--verbose"))
                        Log.assignVerbosityStatus(true);
                    else if(args[i].equals("-f") || args[i].equals("--fpm")) {
                    	i++;
                        if(i == args.length)
                            throw new IllegalArgumentException("The GUI frames per minute is missing!");
                        fpm = Double.parseDouble(args[i]);
                    }
                    else if(args[i].equals("-b") || args[i].equals("--budget")) {
                    	i++;
                        if(i == args.length) 
                            throw new IllegalArgumentException("The chemical budget is missing!");
                        budget = Integer.parseInt(args[i]);
                        chemicalsRemaining = budget;
                    }
					else if(args[i].equals("-r") || args[i].equals("--spawnFreq")) {
						i++;
						if(i == args.length)
							throw new IllegalArgumentException("The Respawn Rate is missing!");
						spawnFreq = Integer.parseInt(args[i]);
					}
					else if(args[i].equals("-a") || args[i].equals("--agentGoal")) {
						i++;
						if(i == args.length)
							throw new IllegalArgumentException("The target agent goal is missing!");
						agentGoal = Integer.parseInt(args[i]);
					}
                    else if(args[i].equals("-m") || args[i].equals("--map")) {
                    	i++;
                        if(i == args.length) 
                            throw new IllegalArgumentException("The map is missing!");
                        mapName = args[i];
                    }
                    else if(args[i].equals("-s") || args[i].equals("--seed")) {
                    	i++;
                        if(i == args.length) 
                            throw new IllegalArgumentException("The seed number is missing!");
                        seed = Integer.parseInt(args[i]);
                        random = new Random(seed);
                    }
                    else if(args[i].equals("-u") || args[i].equals("--turns")) {
                    	i++;
                        if (i == args.length)
                            throw new IllegalArgumentException("The total number of turns is not specified!");
                        turns = Integer.parseInt(args[i]);
                    }
                    else 
                        throw new IllegalArgumentException("Unknown argument \"" + args[i] + "\"!");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument \"" + args[i] + "\"!");
            }
        }

		if(random == null) {
			random = new Random(seed);
		}
	}
	
	private static void readMap() throws FileNotFoundException, IOException {
		if(mapName != null) {
			File mapFile;
			Scanner scanner;
			try {
				mapFile = new File(sourcePath + File.separator + "maps" + File.separator + teamName + File.separator + mapName);				
				scanner = new Scanner(mapFile);
			} catch(FileNotFoundException e) {
                throw new FileNotFoundException("Map file was not found!");
			}

			try {
				mapSize = Integer.parseInt(scanner.nextLine().strip());
				grid = new ChemicalCell[mapSize][mapSize];
				for(int i = 0; i < grid.length; i++)
					for(int j = 0; j < grid[0].length; j++)
						grid[i][j] = new ChemicalCell(true);
			} catch(Exception e) {
				scanner.close();
                throw new IOException("Unable to determine map size!");
			}

			try {
				String[] startAndTargetElements = scanner.nextLine().strip().split(" ");
				int startX = Integer.parseInt(startAndTargetElements[0]);
				int startY = Integer.parseInt(startAndTargetElements[1]);
				int targetX = Integer.parseInt(startAndTargetElements[2]);
				int targetY = Integer.parseInt(startAndTargetElements[3]);	
				start = new Point(startX, startY);
				agentLocations = new HashMap<Integer, Point>();
				agentLocations.put(0, new Point(startX, startY));
				previousStates = new HashMap<Integer, Byte>();
				previousStates.put(0, (byte) 0);
				target = new Point(targetX, targetY);
			} catch(Exception e) {
				scanner.close();
                throw new IOException("Unable to identify start and target locations!");
			}

			try {
				while(scanner.hasNextLine()) {
					String[] blockedLocationElements = scanner.nextLine().strip().split(" ");
					int blockedX = Integer.parseInt(blockedLocationElements[0]);
					int blockedY = Integer.parseInt(blockedLocationElements[1]);
					blockedLocations.add(new Point(blockedX, blockedY));
					grid[blockedX - 1][blockedY - 1] = new ChemicalCell(false);
				}
			} catch(Exception e) {
				scanner.close();
                throw new IOException("Cannot interpret one or more blocked cells!");
			}
			
			scanner.close();
			
			try {
	        	controllerWrapper = loadControllerWrapper();
			} catch (Exception e) {
				Log.writeToLogFile("Unable to load controller: " + e.getMessage());
			}
		}
	}
	
	private static boolean checkMap() throws IOException {

		if(mapSize < 1)
			throw new IOException("The map size must be a positive integer!");
		
		if(start.x < 1 || start.y > mapSize)
			throw new IOException("Start location (" + start.x + ", " + start.y + ") is out of bounds!");

		if(target.x < 1 || target.y > mapSize)
			throw new IOException("Target location (" + target.x + ", " + target.y + ") is out of bounds!");
		
		for(Point location : blockedLocations)
			if(location.x < 1 || location.y > mapSize)
				throw new IOException("Blocked location (" + location.x + ", " + location.y + ") is out of bounds!");
		
		List<Point> unvisitedLocations = new ArrayList<>();
		for(int i = 1; i <= mapSize; i++) {
			for(int j = 1; j <= mapSize; j++) {
				Point location = new Point(i, j);
				if(!blockedLocations.contains(location)) {
					unvisitedLocations.add(location);
				}
			}
		}
		if(unvisitedLocations.isEmpty())
			return true;
		
		if(mapSize <= 50) {
			Point firstUnvisitedLocation = unvisitedLocations.get(0);
			visitLocations(unvisitedLocations, new ArrayList<>(), 
					1, mapSize, 1, mapSize, firstUnvisitedLocation.x, firstUnvisitedLocation.y, 0, 0);
		}
		else
			return true;
		
		return unvisitedLocations.isEmpty();
	}
	
	private static void visitLocations(List<Point> unvisitedLocations, List<Point> visitedLocations, 
			int rowLowerBound, int rowUpperBound, int columnLowerBound, int columnUpperBound, int row, int column, int prevRow, int prevColumn) {
		if(row < rowLowerBound || row > rowUpperBound || column < columnLowerBound || column > columnUpperBound)
			return;
		
		Point location = new Point(row, column);
		if(visitedLocations.contains(location) || !unvisitedLocations.contains(location))
			return;
		if(unvisitedLocations.contains(location)) {
			visitedLocations.add(location);
			unvisitedLocations.remove(location);
		}
		
		if(unvisitedLocations.contains(new Point(row + 1, column)) && (prevRow != row || prevColumn != column))
			visitLocations(unvisitedLocations, visitedLocations, rowLowerBound, rowUpperBound, columnLowerBound, columnUpperBound, row + 1, column, row, column);
		if(unvisitedLocations.contains(new Point(row, column + 1)) && (prevRow != row || prevColumn != column))
			visitLocations(unvisitedLocations, visitedLocations, rowLowerBound, rowUpperBound, columnLowerBound, columnUpperBound, row, column + 1, row, column);
		if(unvisitedLocations.contains(new Point(row - 1, column)) && (prevRow != row || prevColumn != column))
			visitLocations(unvisitedLocations, visitedLocations, rowLowerBound, rowUpperBound, columnLowerBound, columnUpperBound, row - 1, column, row, column);
		if(unvisitedLocations.contains(new Point(row, column - 1)) && (prevRow != row || prevColumn != column))
			visitLocations(unvisitedLocations, visitedLocations, rowLowerBound, rowUpperBound, columnLowerBound, columnUpperBound, row, column - 1, row, column);
	}
		
	private static void placeChemicals(ChemicalPlacement chemicalPlacement) {
		Point location = chemicalPlacement.location;
		List<ChemicalType> chemicals = chemicalPlacement.chemicals;
		
		if(location == null || (location != null && (location.x < 1 || location.y > mapSize))) {
			if(location != null)
				Log.writeToLogFile("Warning: location for chemical placement is invalid. No chemicals placed.");
			return;
		}
		
		if(chemicals.size() > chemicalsRemaining) {
			Log.writeToLogFile("Warning: not enough chemicals remaining (" + chemicalsRemaining + 
					" chemicals) to complete placement request (" + chemicals.size() + " chemicals). No chemicals placed.");
			return;
		}
		
		for(ChemicalType chemical : chemicals) {
			grid[location.x - 1][location.y - 1].applyConcentration(chemical);
			chemicalsUsed.put(chemical, chemicalsUsed.get(chemical) + 1);
			chemicalsRemaining--;
		}
	}

	private static void moveAgent(DirectionType directionType, Point agentLocation) {
		Point targetLocation = new Point(agentLocation.x, agentLocation.y);
		switch(directionType) {
		case NORTH:
			if(agentLocation.x > 1 && !blockedLocations.contains(new Point(agentLocation.x - 1, agentLocation.y)))
				targetLocation = new Point(agentLocation.x - 1, agentLocation.y);
			break;
		case SOUTH:
			if(agentLocation.x < mapSize && !blockedLocations.contains(new Point(agentLocation.x + 1, agentLocation.y)))
				targetLocation = new Point(agentLocation.x + 1, agentLocation.y);
			break;
		case EAST:
			if(agentLocation.y < mapSize && !blockedLocations.contains(new Point(agentLocation.x, agentLocation.y + 1)))
				targetLocation = new Point(agentLocation.x, agentLocation.y + 1);
			break;
		case WEST:
			if(agentLocation.y > 1 && !blockedLocations.contains(new Point(agentLocation.x, agentLocation.y - 1)))
				targetLocation = new Point(agentLocation.x, agentLocation.y - 1);
			break;
		case CURRENT: break;
		default: break;
		}

		for (Point location: agentLocations.values()) {
			if (location.equals(targetLocation) && ! location.equals(target)) {
				return;
			}
		}
		agentLocation.setLocation(targetLocation.x, targetLocation.y);
	}

	private static boolean agentAtTarget(Point agentLocation) {
		return agentLocation.equals(target);
	}

	private static void diffuseCells() {
		ChemicalCell[][] newGrid = deepClone(grid);
		for(int i = 0; i < newGrid.length; i++) {
			for(int j = 0; j < newGrid[0].length; j++) {
				ChemicalCell cell = newGrid[i][j];

				if(cell.isBlocked())
					continue;
				
				for(ChemicalType chemicalType : ChemicalType.values()) {
					double concentrationSum = cell.getConcentration(chemicalType);
					int numUnblockedCells = 1;
					
					if(i > 0 && !grid[i - 1][j].isBlocked()) {
						concentrationSum += grid[i - 1][j].getConcentration(chemicalType);
						numUnblockedCells++;
					}
					if(i < mapSize - 1 && !grid[i + 1][j].isBlocked()) {
						concentrationSum += grid[i + 1][j].getConcentration(chemicalType);
						numUnblockedCells++;
					}
					if(j > 0 && !grid[i][j - 1].isBlocked()) {
						concentrationSum += grid[i][j - 1].getConcentration(chemicalType);
						numUnblockedCells++;
					}
					if(j < mapSize - 1 && !grid[i][j + 1].isBlocked()) {
						concentrationSum += grid[i][j + 1].getConcentration(chemicalType);
						numUnblockedCells++;
					}
					
					double averageConcentration = concentrationSum / numUnblockedCells;									
					cell.setConcentration(chemicalType, averageConcentration);
				}				
			}
		}		
		grid = newGrid;
	}
	
	private static void createSimulation() throws IOException, JSONException {
		
		server = null;
		
		Log.writeToLogFile("\n");
        Log.writeToLogFile("Project: Chemotaxis");
        Log.writeToLogFile("Simulator Version: " + version);
        Log.writeToLogFile("Team: " + teamName);
        Log.writeToLogFile("GUI: " + (showGUI ? "enabled" : "disabled"));
        Log.writeToLogFile("\n");
		
		if(showGUI) {
            server = new HTTPServer();
            Log.writeToLogFile("Hosting the HTTP Server on " + server.addr());
            if(!Desktop.isDesktopSupported())
                Log.writeToLogFile("Desktop operations not supported!");
            else if(!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                Log.writeToLogFile("Desktop browse operation not supported!");
            else {
                try {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + server.port()));
                } catch(URISyntaxException e) {}
            }
            updateGUI(server, getGUIState(0, false));
        }
		else {
			runSimulation();
		}
	}
	
	/**
	 * @throws IOException
	 * @throws JSONException
	 */
	private static void runSimulation() throws IOException, JSONException {
		boolean mapIsValid = checkMap();
		if(mapIsValid) {
			if(verifyMap) {
				if(mapSize <= 50)
					Log.writeToLogFile("The map is valid!");
				else
					Log.writeToLogFile("The map is too large to verify.");
				System.exit(1);
			}
		}
		else {
			validMap = false;
			Log.writeToLogFile("The map is not valid!");
			if(!showGUI)
				System.exit(1);
			updateGUI(server, getGUIState(currentTurn, false));
			return;
		}

		currentTurn = 0;
		int numReached = 0;
	
		updateGUI(server, getGUIState(currentTurn, true));
		
		for(int i = 1; i <= turns; i++) {
			currentTurn++;

			ArrayList<Point> locations = new ArrayList<>(deepClone(agentLocations.values().stream().collect(Collectors.toList())));
			ChemicalPlacement chemicalPlacement = controllerWrapper.applyChemicals(currentTurn, chemicalsRemaining, locations, deepClone(grid));
			placeChemicals(chemicalPlacement);
			
			try {
				// Concentrations less than 0.001 are undetected by the agent
				ChemicalCell[][] adjustedGrid = deepClone(grid);
				for(int j = 0; j < adjustedGrid.length; j++) {
					for(int k = 0; k < adjustedGrid[0].length; k++) {
						ChemicalCell cell = adjustedGrid[j][k];
						for(ChemicalType chemicalType : ChemicalType.values()) {
							if(cell.getConcentration(chemicalType) < 0.001)
								cell.setConcentration(chemicalType, 0.0);
						}
					}
				}

				AgentWrapper agentWrapper = loadAgentWrapper();

				for(Integer agentId : agentLocations.keySet()) {
					Point agentLocation = agentLocations.get(agentId);
					if (agentAtTarget(agentLocation)) {continue;}

					Map<DirectionType, ChemicalCell> neighborMap = new HashMap<>();

					if(agentLocation.x == 1)
						neighborMap.put(DirectionType.NORTH, new ChemicalCell(false));
					else
						neighborMap.put(DirectionType.NORTH, adjustedGrid[agentLocation.x - 2][agentLocation.y - 1]);

					if(agentLocation.x == mapSize)
						neighborMap.put(DirectionType.SOUTH, new ChemicalCell(false));
					else
						neighborMap.put(DirectionType.SOUTH, adjustedGrid[agentLocation.x][agentLocation.y - 1]);

					if(agentLocation.y == mapSize)
						neighborMap.put(DirectionType.EAST, new ChemicalCell(false));
					else
						neighborMap.put(DirectionType.EAST, adjustedGrid[agentLocation.x - 1][agentLocation.y]);

					if(agentLocation.y == 1)
						neighborMap.put(DirectionType.WEST, new ChemicalCell(false));
					else
						neighborMap.put(DirectionType.WEST, adjustedGrid[agentLocation.x - 1][agentLocation.y - 2]);

					Move move = agentWrapper.makeMove(
							random.nextInt(), previousStates.get(agentId),
							deepClone(adjustedGrid[agentLocation.x - 1][agentLocation.y - 1]), deepClone(neighborMap));

					moveAgent(move.directionType, agentLocation);
					previousStates.put(agentId, move.currentState);
					int firstX = agentLocation.x;
					int firstY = agentLocation.y;
					Log.writeToLogFile("Agent " + agentId + " moved " + move.directionType + " -> " + firstX
							+ "-" + firstY + " -> " + agentLocations.get(agentId).x
							+ "-" + agentLocations.get(agentId).y);
					if (agentAtTarget(agentLocations.get(agentId))) {
						Log.writeToLogFile("Agent " + agentId + " reached the target");
						numReached ++;
					}
				}
			
			} catch (Exception e) {
				Log.writeToLogFile("Unable to load or run agent: " + e.getMessage());
			}

			updateGUI(server, getGUIState(currentTurn, true));
			
			diffuseCells();

			if(numReached >= agentGoal) {
				Log.writeToLogFile(numReached + " agents have reached.  You needed to get to " + agentGoal + " . Congrats!");
				break;
			}

			if(currentTurn % spawnFreq == 0) {
				boolean targetOccupied = false;
				for(Point location : agentLocations.values()) {
					if(location.equals(start)) {
						targetOccupied = true;
						break;
					}
				}

				if(! targetOccupied) {
					Log.writeToLogFile("Spawning another agent after Turn " + currentTurn);
					agentLocations.put(Collections.max(agentLocations.keySet()) + 1, deepClone(start));
				} else {
					Log.writeToLogFile("Can\'t spawn another agent after Turn " + currentTurn + " because start cell is occupied by another agent");
				}
			}

		}

		updateGUI(server, getGUIState(currentTurn, false));

		Log.writeToLogFile("Experiment Information...");
		Log.writeToLogFile("Budget: " + budget);
		Log.writeToLogFile("Spawn Frequency: " + spawnFreq);
		Log.writeToLogFile("Seed: " + seed);
		Log.writeToLogFile("Map: " + mapName);
		Log.writeToLogFile("Results...");
		int chemsUsed = budget - chemicalsRemaining;
		Log.writeToLogFile("Chemicals Used: " + chemsUsed + " / " + budget);
		Log.writeToLogFile("Spawned " + agentLocations.size() + " agents --> " + numReached + " / " + agentGoal + " reached the target.");
		Log.writeToLogFile("Final time: " + currentTurn + "/" + turns);
		
		if(!showGUI)
			System.exit(1);
	}
	
	private static <T extends Object> T deepClone(T obj) {
        if(obj == null)
            return null;

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            ByteArrayInputStream bais = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(bais);
            
            return (T) objectInputStream.readObject();
        }
        catch(Exception e) {
            return null;
        }
	}
	
	private static ControllerWrapper loadControllerWrapper() throws Exception {
		Log.writeToLogFile("Loading team " + teamName + "'s controller...");

		Controller controller = loadController();
        if(controller == null) {
            Log.writeToLogFile("Cannot load team " + teamName + "'s controller!");
            System.exit(1);
        }

        return new ControllerWrapper(controller, teamName, timeout);
    }
	
	private static Controller loadController() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String controllerPackagePath = sourcePath + File.separator + "chemotaxis" + File.separator + teamName;
        Set<File> controllerFiles = getFilesInDirectory(controllerPackagePath, ".java");
		String simPath = sourcePath + File.separator + "chemotaxis" + File.separator + "sim";
        Set<File> simFiles = getFilesInDirectory(simPath, ".java");

        File classFile = new File(controllerPackagePath + File.separator + "Controller.class");

        long classModified = classFile.exists() ? classFile.lastModified() : -1;
        if(classModified < 0 || classModified < lastModified(controllerFiles) || classModified < lastModified(simFiles)) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if(compiler == null)
                throw new IOException("Cannot find the Java compiler!");

            StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
            Log.writeToLogFile("Compiling for team " + teamName + "'s controller...");

            if(!compiler.getTask(null, manager, null, null, null, manager.getJavaFileObjectsFromFiles(controllerFiles)).call())
                throw new IOException("The compilation failed!");
            
            classFile = new File(controllerPackagePath + File.separator + "Controller.class");
            if(!classFile.exists())
                throw new FileNotFoundException("The class file is missing!");
        }

        ClassLoader loader = Simulator.class.getClassLoader();
        if(loader == null)
            throw new IOException("Cannot find the Java class loader!");

        @SuppressWarnings("rawtypes")
        Class rawClass = loader.loadClass("chemotaxis." + teamName + ".Controller");
        Class[] classArgs = new Class[]{Point.class, Point.class, Integer.class, ChemicalCell[][].class, Integer.class, Integer.class, Integer.class, SimPrinter.class, Integer.class, Integer.class};

        return (Controller) rawClass.getDeclaredConstructor(classArgs).newInstance(start, target, mapSize, grid, turns, budget, seed, new SimPrinter(enableControllerPrints), agentGoal, spawnFreq);
    }

	private static AgentWrapper loadAgentWrapper() throws Exception {

		Agent agent = loadAgent();
        if(agent == null) {
            Log.writeToLogFile("Cannot load team " + teamName + "'s agent!");
            System.exit(1);
        }

        return new AgentWrapper(agent, teamName, timeout);
    }
	
	private static Agent loadAgent() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String agentPackagePath = sourcePath + File.separator + "chemotaxis" + File.separator + teamName;
        Set<File> agentFiles = getFilesInDirectory(agentPackagePath, ".java");
		String simPath = sourcePath + File.separator + "chemotaxis" + File.separator + "sim";
        Set<File> simFiles = getFilesInDirectory(simPath, ".java");

        File classFile = new File(agentPackagePath + File.separator + "Agent.class");

        long classModified = classFile.exists() ? classFile.lastModified() : -1;
        if(classModified < 0 || classModified < lastModified(agentFiles) || classModified < lastModified(simFiles)) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if(compiler == null)
                throw new IOException("Cannot find the Java compiler!");

            StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
            Log.writeToLogFile("Compiling for team " + teamName + "'s agent...");

            if(!compiler.getTask(null, manager, null, null, null, manager.getJavaFileObjectsFromFiles(agentFiles)).call())
                throw new IOException("The compilation failed!");
            
            classFile = new File(agentPackagePath + File.separator + "Agent.class");
            if(!classFile.exists())
                throw new FileNotFoundException("The class file is missing!");
        }

        ClassLoader loader = Simulator.class.getClassLoader();
        if(loader == null)
            throw new IOException("Cannot find the Java class loader!");

        @SuppressWarnings("rawtypes")
        Class rawClass = loader.loadClass("chemotaxis." + teamName + ".Agent");
        Class[] classArgs = new Class[]{SimPrinter.class};

        return (Agent) rawClass.getDeclaredConstructor(classArgs).newInstance(new SimPrinter(enableAgentPrints));
    }
	
	private static long lastModified(Iterable<File> files) {
        long lastDate = 0;
        for(File file : files) {
            long date = file.lastModified();
            if(lastDate < date)
                lastDate = date;
        }
        return lastDate;
    }
	
	private static Set<File> getFilesInDirectory(String path, String extension) {
		Set<File> files = new HashSet<File>();
        Set<File> previousDirectories = new HashSet<File>();
        previousDirectories.add(new File(path));
        do {
        	Set<File> nextDirectories = new HashSet<File>();
            for(File previousDirectory : previousDirectories)
                for(File file : previousDirectory.listFiles()) {
                    if(!file.canRead())
                    	continue;
                    
                    if(file.isDirectory())
                        nextDirectories.add(file);
                    else if(file.getPath().endsWith(extension))
                        files.add(file);
                }
            previousDirectories = nextDirectories;
        } while(!previousDirectories.isEmpty());
        
        return files;
	}
	
	private static void updateGUI(HTTPServer server, String content) {
		if(server == null)
			return;
        while(true) {
        	boolean replied = false;
        	
            while(true) {
                try {
                	guiPath = server.request();
                    break;
                } catch(Exception e) {
                    Log.writeToVerboseLogFile("HTTP request error: " + e.getMessage());
                }
            }        		
        	
            if(guiPath.equals("start.txt")) {
                try {
                    server.reply(content);
                    replied = true;
                } catch(Exception e) {
                    Log.writeToVerboseLogFile("HTTP dynamic reply error for starting GUI: " + e.getMessage());
                }
            }

			if(guiPath.equals("pause.txt")) {
				try {
					server.reply("pause");
					replied = true;
				} catch(Exception e) {
					Log.writeToVerboseLogFile("HTTP dynamic reply error for starting GUI: " + e.getMessage());
				}
			}

			if(guiPath.startsWith("parameters.txt")) {
                try {
                    server.reply("");
                    replied = true;
                    
                    String[] argsToParse = new String[14];
                    String[] params = guiPath.split("\\?")[1].split("&");
                    int index = 0;
                    for(String param : params) {
                    	String[] paramElements = param.split("=");
                    	argsToParse[index++] = "--" + paramElements[0];
                    	argsToParse[index++] = paramElements[1];
                    }
                    parseCommandLineArguments(argsToParse);
                    readMap();
                    runSimulation();
                    guiPath = "";
                } catch(Exception e) {
                    Log.writeToVerboseLogFile("HTTP dynamic reply error for receiving simulation data: " + e.getMessage());
                }
            }

			if(guiPath.equals("restart.txt")) {
				try {
					server.reply(content);
				} catch(Exception e) {
					Log.writeToVerboseLogFile("HTTP dynamic reply error for running simulation: " + e.getMessage());
				}
				return;
			}

			if(guiPath.equals("data.txt")) {
                try {
                    server.reply(content);
                } catch(Exception e) {
                    Log.writeToVerboseLogFile("HTTP dynamic reply error for running simulation: " + e.getMessage());
                }
              	return;
            }

            if(guiPath.equals(""))
            	guiPath = "webpage.html";
            else if(!Character.isLetter(guiPath.charAt(0))) {
                Log.writeToVerboseLogFile("Potentially malicious HTTP request: \"" + guiPath + "\"");
                break;
            }
            
            if(!replied) {
                try {
                    File file = new File(staticsPath + File.separator + guiPath);
                    server.reply(file);
                } catch(Exception e) {
                    Log.writeToVerboseLogFile("HTTP static reply error: " + e.getMessage());
                }
            }
        }
	}
	
	private static String getGUIState(int turn, boolean simulation) throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("refresh", 60000.0 / fpm);
		jsonObj.put("totalTurns", turns);
		jsonObj.put("currentTurn", turn);
		jsonObj.put("chemicalsRemaining", chemicalsRemaining);		
		jsonObj.put("turnsRemaining", turns - turn);
		jsonObj.put("spawnFreq", spawnFreq);
		jsonObj.put("agentGoal", agentGoal);
		jsonObj.put("size", mapSize);
		jsonObj.put("seed", seed);
		jsonObj.put("budget", budget);
		jsonObj.put("fpm", fpm);
		jsonObj.put("teamName", teamName);
		jsonObj.put("mapName", mapName);
		jsonObj.put("simulation", simulation);
		jsonObj.put("validMap", validMap);
		
		JSONArray gridArray = new JSONArray();
		if(grid == null)
			return jsonObj.toString();

		DecimalFormat concentrationFormat = new DecimalFormat("###.####");
		
		for(int i = 0; i < grid.length; i++) {
			JSONArray nestedGridArray = new JSONArray();
			for(int j = 0; j < grid[0].length; j++) {
				ChemicalCell cell = grid[i][j];
				
				JSONObject nestedGridObject = new JSONObject();
				
				JSONObject chemicalsObject = new JSONObject();
				for(ChemicalType chemicalType : ChemicalType.values())
					chemicalsObject.put(chemicalType.name(), concentrationFormat.format(cell.getConcentration(chemicalType)));
				
				nestedGridObject.put("chemicals", chemicalsObject);
				nestedGridObject.put("open", cell.isOpen());
				nestedGridObject.put("blocked", cell.isBlocked());
				
				nestedGridArray.put(nestedGridObject);
			}
			gridArray.put(nestedGridArray);
		}
		jsonObj.put("grid", gridArray);
		
		JSONObject chemicalsUsedObject = new JSONObject();
		for(ChemicalType chemical : chemicalsUsed.keySet())
			chemicalsUsedObject.put(chemical.name(), chemicalsUsed.get(chemical));
		jsonObj.put("chemicalsUsed", chemicalsUsedObject);
		
		JSONArray blockedLocationsArray = new JSONArray();
		for(Point blockedLocation : blockedLocations) {
			JSONObject blockedLocationObj = new JSONObject();
			blockedLocationObj.put("row", blockedLocation.x);
			blockedLocationObj.put("column", blockedLocation.y);
			blockedLocationsArray.put(blockedLocationObj);
		}
		
		JSONObject startLocationObj = new JSONObject();
		startLocationObj.put("row", start.x);
		startLocationObj.put("column", start.y);
		jsonObj.put("startLocation", startLocationObj);

		JSONObject targetLocationObj = new JSONObject();
		targetLocationObj.put("row", target.x);
		targetLocationObj.put("column", target.y);
		jsonObj.put("targetLocation", targetLocationObj);

		JSONArray agentLocationsArray = new JSONArray();
		for(int agentId: agentLocations.keySet()) {
			JSONObject agentLocationObj = new JSONObject();
			agentLocationObj.put("id", agentId);
			agentLocationObj.put("row", agentLocations.get(agentId).x);
			agentLocationObj.put("column", agentLocations.get(agentId).y);
			agentLocationsArray.put(agentLocationObj);
		}

		jsonObj.put("agentLocations", agentLocationsArray);
        return jsonObj.toString();
	}
	
	public static void main(String[] args) throws IOException, JSONException {
		setup();
		parseCommandLineArguments(args);
		if(!showGUI) {
			readMap();
		}
		createSimulation();
	}
}
