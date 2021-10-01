package chemotaxis.g8;

import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.SimPrinter;

import java.awt.*;
import java.util.*;

public class Controller extends chemotaxis.sim.Controller {
	static int[][] DIR = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}}; // down right up left   +1: turn left; -1: turn right
	int size, spawnFreq, budget;
	Map<Integer, Status> solution;
	ChemicalCell[][] G;
	int alpha;

	private boolean checkValid(Point p) {
		return p.x >= 1 && p.y >= 1 && p.x <= size && p.y <= size;
	}

	private boolean isBlocked(Point p) {
		return !checkValid(p) || G[p.x - 1][p.y - 1].isBlocked();
	}

	private Point moveByDT(Point p, int dt) {
		return new Point(p.x + DIR[dt][0], p.y + DIR[dt][1]);
	}

	private ArrayList<Point> generatePath(Point start, int mode, int dt) {
		int[][][] vis = new int[size + 1][size + 1][4];
		ArrayList<Point> res = new ArrayList<>();
		int turn = 0;
		Point cur = moveByDT(start, dt);
		if (isBlocked(cur)) return null;
		if (mode > 2) { // turn left right l&r r&l
			while (true) {
				if (vis[cur.x][cur.y][dt] > 0) return res;
				for (int i = 0; i < 4; ++i) if (vis[cur.x][cur.y][i] != 0 && vis[cur.x][cur.y][i] <= turn - spawnFreq + 1) return res;
				turn += 1;
				vis[cur.x][cur.y][dt] = turn;
				res.add(cur);
				Point nxt = moveByDT(cur, dt);
				while (isBlocked(nxt)) {
					if (mode == 3) {
						dt = (dt + 1) % 4;
					} else if (mode == 4) {
						dt = (dt + 3) % 4;
					} else if (mode == 5) {
						dt = (dt + (turn % 2 == 0 ? 1 : 3)) % 4;
					} else {
						dt = (dt + (turn % 2 == 0 ? 3 : 1)) % 4;
					}
					nxt = moveByDT(cur, dt);
				}
				cur = nxt;
			}
		} else {
			while (true) {
				if (vis[cur.x][cur.y][dt] > 0) return res;
				for (int i = 0; i < 4; ++i) if (vis[cur.x][cur.y][i] != 0 && vis[cur.x][cur.y][i] <= turn - spawnFreq + 1) return res;
				turn += 1;
				vis[cur.x][cur.y][dt] = turn;
				res.add(cur);
				Point nxt = null;
				if (mode == 1) {
					for (int i = 5; i >= 2; --i) {
						nxt = moveByDT(cur, (dt + i) % 4);
						if (!isBlocked(nxt)) { dt = (dt + i) % 4; break; }
					}
				} else if (mode == 2) {
					for (int i = 3; i <= 6; ++i) {
						nxt = moveByDT(cur, (dt + i) % 4);
						if (!isBlocked(nxt)) { dt = (dt + i) % 4; break; }
					}
				}
				assert nxt != null;
				cur = nxt;
			}
		}
	}

	static class Status {
		int time, cost;
		Point from;
		int mode, dt;
		public Status(int cost, int time, Point from) {
			this.cost = cost; this.time = time; this.from = from;
		}

		public Status(Status s) {
			this.cost = s.cost; this.time = s.time; this.from = s.from; this.mode = s.mode; this.dt = s.dt;
		}

		@Override
		public String toString() {
			return "Status{" + "time=" + time + ", cost=" + cost + ", from=" + from + ", mode=" + mode + ", dt=" + dt + '}';
		}

		public int getV(int alpha) {
			return this.cost * alpha + this.time;
		}
	}

	private int modeCost(int x) { return (x & 1) + ((x >> 1) & 1) + ((x >> 2) & 1); }

	private int precompute(Point start, Point target) {
		Status[][] f = new Status[size + 1][size + 1];
		f[start.x][start.y] = new Status(0, 0, null);
		PriorityQueue<Status> pq = new PriorityQueue<>(Comparator.comparing(s -> s.getV(alpha)));
		pq.add(new Status(0, 0, start));

		int res = -1;
		while (!pq.isEmpty()) {
			Status uu = pq.poll(), u = f[uu.from.x][uu.from.y];
			if (uu.cost != u.cost || uu.time != u.time) continue;
			Point cur = uu.from;
			if (target.equals(cur)) {
				res = u.cost;
				break;
			}
			for (int mode = 1; mode <= 6; ++mode) {
				for (int dt = 0; dt < 4; ++dt) {
					ArrayList<Point> path = generatePath(cur, mode, dt);
					if (path == null) continue;
					for (int i = 0; i < path.size(); ++i) {
						Point nxt = path.get(i);
						Status s = new Status(u.cost + modeCost(mode), u.time + i + 1, cur);
						s.mode = mode; s.dt = dt;
						if (f[nxt.x][nxt.y] == null || f[nxt.x][nxt.y].getV(alpha) > s.getV(alpha)) {
							f[nxt.x][nxt.y] = s;
							Status ts = new Status(s); ts.from = nxt;
							pq.add(ts);
						}
					}
				}
			}
		}
		while (true) {
			Status s = f[target.x][target.y];
			System.out.println("     " + s);
//			Status ss = new Status(s); ss.from = target;
			if (target.equals(start)) return res;
			solution.put(f[s.from.x][s.from.y].time, s);
			target = s.from;
		}
	}

	private Status solve(int alpha) {
		this.alpha = alpha;
		System.out.println("Precalculating...   alpha = " + alpha);
		this.solution.clear();

		Status res = new Status(-1, -1, null);

		int costForOne = precompute(start, target);
		int timeForOne = solution.keySet().stream().max(Integer::compare).get();
		int delay = 0;
		Map<Integer, Status> oSolution = new HashMap<>(solution);
		for (int agentNumber = 1; agentNumber < Math.min(agentGoal, budget / costForOne); ++agentNumber) {
			boolean timeLimitExceed = false;
			while (true) {
				int shift = spawnFreq * agentNumber + delay + 1;
				boolean flag = true;
				if (shift + timeForOne > simTime) {
					timeLimitExceed = true;
					break;
				}
				for (int t: oSolution.keySet()) {
					if (solution.containsKey(t + shift)) { flag = false; break; }
				}
				if (flag) {
					for (Map.Entry<Integer, Status> kv: oSolution.entrySet()) {
						solution.put(kv.getKey() + shift, kv.getValue());
					}
					System.out.println(shift);
					break;
				}
				delay += 1;
			}
			delay = (delay + spawnFreq - 1) / spawnFreq * spawnFreq;
			if (timeLimitExceed) break;
			res.mode = agentNumber;
		}

		res.time = solution.keySet().stream().max(Integer::compare).get();
		res.cost = costForOne * agentGoal;
		return res;
	}


	public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter, Integer agentGoal, Integer spawnFreq) {
		super(start, target, size, grid, simTime, budget, seed, simPrinter, agentGoal, spawnFreq);

		this.size = size;
		this.G = grid;
		this.solution = new HashMap<>();
		this.budget = budget;
		spawnFreq = Math.max(spawnFreq, 3);
		this.spawnFreq = spawnFreq;

		boolean solved = false;
		Map<Integer, Status> best = null;
		int reached = -1;

		for (int a: new int[]{1000, 100, 50, 25, 20, 15, 12, 10, 7, 5, 2}) {
			Status s = solve(a);
			System.out.println(s);
			if (s.time < simTime && s.cost <= budget && s.mode == agentGoal - 1) {
				solved = true;
				break;
			}
			if (s.mode > reached) {
				reached = s.mode;
				best = Map.copyOf(solution);
			}
		}

		System.out.println("Solved: " + solved + " Reached: " + reached);

		if (!solved) {
			solution = best;
		}
	}

	@Override
	public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid) {
		ChemicalPlacement res = new ChemicalPlacement();
		currentTurn -= 1;
		System.out.println(currentTurn + ": " + solution.getOrDefault(currentTurn, null));
		if (!solution.containsKey(currentTurn)) return res;
		Status s = solution.get(currentTurn);
		res.location = moveByDT(s.from, s.dt);
		if ((s.mode & 1) > 0) res.chemicals.add(ChemicalType.BLUE);
		if ((s.mode & 2) > 0) res.chemicals.add(ChemicalType.RED);
		if ((s.mode & 4) > 0) res.chemicals.add(ChemicalType.GREEN);
		return res;
	}
}
