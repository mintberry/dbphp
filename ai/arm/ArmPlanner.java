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
		double length = start[2];// length of first link
		ConfigNode startNode = new ConfigNode(this.start, links, 0);
		ConfigNode goalNode = new ConfigNode(this.goal, links, 0);

		components.put(startNode, 0);
		components.put(goalNode, 1);

		if (!world.armCollision(startNode.armConfig) && !world.armCollision(goalNode.armConfig)) {
			roadMap.put(startNode, new HashSet<ConfigNode>());
			roadMap.put(goalNode, new HashSet<ConfigNode>());


			// 
			for (int i = 2; i < N;) {
				double[] config = randomConfig(links, ArmDriver.window_width, ArmDriver.window_height, length);
				ConfigNode node = new ConfigNode(config, links, 0);
				// check if this config collide with obs
				if (!world.armCollision(node.armConfig)) {

					roadMap.put(node, new HashSet<ConfigNode>());

					components.put(node, i);
					++i;
				}
			}	

			// init uf now!
			this.uf = new UnionFind<ConfigNode>(components);

			// connect vertices
			for (ConfigNode node: roadMap.keySet()) {
				List<ConfigNode> neighbours = kNearest(node, k);
				for (ConfigNode neighbour: neighbours) {
					if (!world.armCollisionPath(node.armConfig, node.armConfig.config, neighbour.armConfig.config)) {
						// add edge, connected
						roadMap.get(node).add(neighbour);
						roadMap.get(neighbour).add(node);

					}
				}
			}
		} else {
			System.out.println("start or goal error");
		}

	}

	// sample a config
	private double[] randomConfig(int links, double width, double height, double length){
		double[] config = new double[links * 2 + 2];
		config[0] = width * Math.random();
		config[1] = height * Math.random();
		for (int i = 1; i <= links; i++) {
			// need to sample arm length?
			config[2*i] = length;
			config[2*i+1] = Math.random() * (Math.PI * 2.0);
		}
		return config;
	}

	// k nearest neighbours
	private List<ConfigNode> kNearest(ConfigNode node, int k){
		List<ConfigNode> nearests = new LinkedList<ConfigNode>();
		ArrayList<ConfigNode> neighbours = new ArrayList<ConfigNode>();

		// sort the collection
		Collections.sort(neighbours, new Comparator<ConfigNode>(){
			public int compare(ConfigNode config1, ConfigNode config2) {
	    	//ascending order
	      	return config1.distance(node.armConfig.config) >= config2.distance(node.armConfig.config) ? 1 : -1;

	    }
		});

		return neighbours;
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
			// just uniform cost search
			return 0;
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

	// Get the time to move from configuration 1 to configuration 2;
	// two configurations must be valid configurations for the arm; 
	public double moveInParallel(double[] config1, double[] config2) {
		if (config1.length != config2.length) {
			System.exit(1);
		}
		if (config1.length % 2 != 0) {
			System.exit(1);
		}
		
		double d = 0;
		double maxt = 0;
		
		for (int i = 0; i < (config1.length/2); i++) {
			if (i == 0) {
				d = Math.sqrt(Math.pow(config1[0]-config2[0], 2)+Math.pow(config1[1]-config2[1], 2));
				
			}
			else {
				d = Math.abs(config1[2*i+1]-config2[2*i+1]);
			}
			if (d > maxt) {
				maxt = d;
			}
			
		}
		
		
		return maxt;
	}
	
	// Given two configurations, get the "path" between configurations;
	// return is a double array with the same length as configurations;
	// path[i] is the velocity of component config[i];
	// basically, given certain time duration: step, path[i]*step 
	// is the movement of component config[i] during step;
	public double[] getPath (double[] config1, double[] config2) {
		double time = moveInParallel(config1, config2);
		double[] path = new double[config1.length];
		
		for (int i = 0; i < config1.length; i++) {
			path[i] = (config2[i] - config1[i]) / time;
		}
		
		return path;
	}
	
	
 }
