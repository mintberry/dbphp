package assignment_robots;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Math;
import java.util.Collections;
import java.util.Comparator;

import assignment_mazeworld.*;

// This is the local planner of the robot arms;
// Can return time between two configurations;
// can get the path between configurations;

public class ArmPlanner extends InformedSearchProblem	 {

	protected HashMap<ConfigNode, HashSet<ConfigNode>> roadMap;

	protected HashMap<ConfigNode, Integer> components;
	protected UnionFind<ConfigNode> uf;

	protected final double[] start;
	protected final double[] goal;

	protected World world;

	// initialize the planner
	public ArmPlanner(double[] s, double[] g, World world){
		roadMap = new HashMap<ConfigNode, HashSet<ConfigNode>>();
		this.world = world;

		this.start = new double[s.length];
		this.goal = new double[g.length];

		System.arraycopy(s, 0, this.start, 0, s.length);
		System.arraycopy(g, 0, this.goal, 0, g.length);

		components = new HashMap<ConfigNode, Integer>();
		// uf = new UnionFind();
	}

	// roadmap constuction, N configs, k nearest neighbour
	public void xRoadMap(int N, int k){
		// sample configs including start and goal
		int links = this.start.length / 2 - 1;
		double length = start[2];// length of first link, can vary
		startNode = new ConfigNode(this.start, links, 0); // startNode inherited
		ConfigNode goalNode = new ConfigNode(this.goal, links, 0);

		components.put((ConfigNode)startNode, 0);
		components.put(goalNode, 1);

		if (!world.armCollision(((ConfigNode)startNode).armConfig) && !world.armCollision(goalNode.armConfig)) {
			roadMap.put((ConfigNode)startNode, new HashSet<ConfigNode>());
			roadMap.put(goalNode, new HashSet<ConfigNode>());


			// 
			for (int i = 2; i < N;) {
				double[] config = randomConfig(links, ArmDriver.window_width, ArmDriver.window_height, this.start);
				ArmRobot ar = new ArmRobot(links);
				ar.set(config);
				// check if this config collide with obs
				if (!world.armCollision(ar)) {
					ConfigNode node = new ConfigNode(config, links, 0);
					roadMap.put(node, new HashSet<ConfigNode>());

					components.put(node, i);
					++i;
				}
			}	

			// init uf now!
			this.uf = new UnionFind<ConfigNode>(components);
			int i = 0;

			// connect vertices
			for (ConfigNode node: roadMap.keySet()) {
				List<ConfigNode> neighbours = kNearest(node, k);
				ConfigNode temp = new ConfigNode(node.armConfig.config, links, 0);
				for (ConfigNode neighbour: neighbours) {
					if (!uf.find(node, neighbour) &&
						!world.armCollisionPath(temp.armConfig, node.armConfig.config, neighbour.armConfig.config)) {
						// add edge, connected
						roadMap.get(node).add(neighbour);
						roadMap.get(neighbour).add(node);

						uf.unite(node, neighbour);
						i++;
					}
				}
			}
			System.out.println(i);
		} else {
			System.out.println("start or goal error");
		}

	}

	// sample a config
	private double[] randomConfig(int links, double width, double height, double[] startConfig){
		double[] config = new double[links * 2 + 2];
		config[0] = width * Math.random();
		config[1] = height * Math.random();
		for (int i = 1; i <= links; i++) {
			// need to sample arm length?
			config[2*i] = startConfig[2 * i];
			config[2*i+1] = Math.random() * (Math.PI * 2.0);
		}
		return config;
	}

	// k nearest neighbours
	private List<ConfigNode> kNearest(ConfigNode node, int k){
		ArrayList<ConfigNode> neighbours = new ArrayList<ConfigNode>(roadMap.keySet());

		// sort the collection
		Collections.sort(neighbours, new Comparator<ConfigNode>(){
			public int compare(ConfigNode config1, ConfigNode config2) {
	    	//ascending order
	      	return config1.distance(node.armConfig.config) >= config2.distance(node.armConfig.config) ? 1 : -1;

	    }
		});

		// if (neighbours.get(0).distance(node.armConfig.config) < neighbours.get(1).distance(node.armConfig.config) && 
		// 	neighbours.get(1).distance(node.armConfig.config) < neighbours.get(2).distance(node.armConfig.config)) {
		// 	System.out.println("HAHA");
		// }

		return neighbours.subList(0, k);
	}

	// node class used by searches.  Searches themselves are implemented
	//  in SearchProblem.
	public class ConfigNode implements SearchNode {

		// location of the agent in the maze
		protected ArmRobot armConfig; 

		// protected HashSet<ConfigNode> neighbours;
		
		// how far the current node is from the start.  Not strictly required
		//  for uninformed search, but useful information for debugging, 
		//  and for comparing paths
		private double cost;  

		public ConfigNode(double[] config, int links, double c) {
			this.armConfig = new ArmRobot(links);

			this.armConfig.set(config);

			this.cost = c;

		}

		// distance from this config to a neighbour
		protected double distance(double[] neighbour){
			ArmLocalPlanner alp = new ArmLocalPlanner();
			return alp.moveInParallel(this.armConfig.config, neighbour);
		}
		
		// public int getX() {
		// 	return state[0];
		// }
		
		// public int getY() {
		// 	return state[1];
		// }

		public ArrayList<SearchNode> getSuccessors() {
			return new ArrayList<SearchNode>(roadMap.get(this));
		}
		
		@Override
		public boolean goalTest() {
			return Arrays.equals(armConfig.config, ArmPlanner.this.goal);
		}


		// an equality test is required so that visited sets in searches
		// can check for containment of states
		@Override
		public boolean equals(Object other) {
			return this.armConfig.equals(other);
		}

		@Override
		public int hashCode() {
			return armConfig.hashCode(); 
		}

		@Override
		public String toString() {
			return armConfig.toString();
		}

		@Override
		public double getCost() {
			return cost;
		}
		

		@Override
		public double heuristic() {
			// just uniform cost search, or the distance to the goal?
			return this.distance(goal);
		}

		@Override
		public int compareTo(SearchNode o) {
			return (int) Math.signum(priority() - o.priority());
		}
		
		@Override
		public double priority() {
			return heuristic() + getCost();
		}

	}

	// union find class
	public class UnionFind<T> {
		private int[] id;
		private int[] sz;

		protected HashMap<T, Integer> map;

		public UnionFind(HashMap<T, Integer> m) {
			int N = m.size();
			id = new int[N];
			sz = new int[N];
			for (int i = 0; i < N; i++) {
				id[i] = i;
				sz[i] = 1;
			}

			this.map = m;
		}

		private int root(T e) {
			int i = map.get(e);
			while (i != id[i])  {
				id[i] = id[id[i]];
				i = id[i];
			}
			return i;
		}

		public boolean find(T e1, T e2) {
			return root(e1) == root(e2);
		}

		public void unite(T e1, T e2) {
			int i = root(e1), j = root(e2);
			if (sz[i] > sz[j]) {
	 			id[j] = i;	
	 			sz[i] += sz[j];
			} else {
	 			id[i] = j;
	 			sz[j] += sz[i];
			}
		}
	}
 }
