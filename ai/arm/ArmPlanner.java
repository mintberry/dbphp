package assignment_robots;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;

import assignment_mazeworld.*;

// This is the local planner of the robot arms;
// Can return time between two configurations;
// can get the path between configurations;

public class ArmPlanner extends InformedSearchProblem	 {

	protected HashMap<ConfigNode, HashSet<ConfigNode>> roadMap;

	protected final double[] start;
	protected final double[] goal;

	// initialize the planner
	public ArmPlanner(double[] s, double[] g){
		roadMap = new HashMap<ConfigNode, HashSet<ConfigNode>>();

		this.start = new double[s.length];
		this.goal = new double[g.length];

		System.arraycopy(s, 0, this.start, 0, s.length);
		System.arraycopy(g, 0, this.goal, 0, g.length);
	}

	// roadmap constuction
	public void xRoadMap(){
		int links = this.start.length / 2 - 1;
		ConfigNode startNode = new ConfigNode(this.start, links, 0);


	}

	// k nearest neighbours
	private List<ConfigNode> kNearest(int k){
		List<ConfigNode> neighbours = new LinkedList<ConfigNode>();
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
			return 0.0;
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
