package assignment_mazeworld;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashSet;
import java.awt.Point;

// Find a path for a single agent to get from a start location (xStart, yStart)
//  to a goal location (xGoal, yGoal)

public class PacmanMazeProblem extends InformedSearchProblem {

	private static int actions[][] = {Maze.NORTH, Maze.EAST, Maze.SOUTH, Maze.WEST}; 
	
	private int width, height, xGoal, yGoal;

	private Maze maze;
	
	public PacmanMazeProblem(Maze m, int sx, int sy, int gx, int gy) {
		width = sx;
		height = sy;
		xGoal = gx;
		yGoal = gy;
		
		maze = m;		
		startNode = new PacmanMazeNode(sx, sy, 0);
	}
	
	// node class used by searches.  Searches themselves are implemented
	//  in SearchProblem.
	public class PacmanMazeNode implements SearchNode {

		// location of the agent in the maze
		protected HashSet<Point> state;

		protected int[] lastAction = {0, 0};// last action to reach this node
		
		// how far the current node is from the start.  Not strictly required
		//  for uninformed search, but useful information for debugging, 
		//  and for comparing paths
		private double cost;  

		public PacmanMazeNode(int x, int y, double c) {
			state = new HashSet<Point>();

			for (int i = 0; i < x; ++i) {
				for (int j = 0; j < y; ++j) {
					if (maze.isLegal(i, j)) {// the robot knows it could not start on walls
						state.add(new Point(i, j));	
					}
				}
			}
		
			cost = c;

		}

		public PacmanMazeNode(HashSet<Point> stateNew, int[] action, double c) {
			this.state = new HashSet<Point>(stateNew);
		
			cost = c;

			lastAction[0] = action[0];
			lastAction[1] = action[1];

		}
		
		public int[] getX() {// no need for this
			int[] Xs = new int[state.size()];
			int i = 0;
			for (Point p : state) {
				Xs[i++] = p.x;
			}
			return Xs;
		}
		
		public int[] getY() {
			int[] Ys = new int[state.size()];
			int i = 0;
			for (Point p : state) {
				Ys[i++] = p.y;
			}
			return Ys;
		}

		public int[] getAction(){
			return lastAction;
		}

		public ArrayList<SearchNode> getSuccessors() {

			ArrayList<SearchNode> successors = new ArrayList<SearchNode>();

			for (int[] action: actions) {
				HashSet<Point> stateNew = new HashSet<Point>();// an empty set
				double cost = 0.0;
				for (Point p: state) {
					cost += 1.0;
					Point pointNew = new Point(p.x + action[0], p.y + action[1]);
					// if the robot can move
					if (maze.isLegal(pointNew.x, pointNew.y)) {
						if (stateNew.contains(pointNew)) {
							cost += (Math.abs(pointNew.x - xGoal) + Math.abs(pointNew.y - yGoal));
						}
						stateNew.add(pointNew);
					} else {// otherwise it will stand still
						if (stateNew.contains(p)) {
							cost += (Math.abs(p.x - xGoal) + Math.abs(p.y - yGoal));
						}
						stateNew.add(p);
					}
				}

				// what if two states are 'same'? action is different
				SearchNode succ = new PacmanMazeNode(stateNew, action, getCost() + 1.0);
				
				// test if the heuristic is consistent
				if (succ.heuristic() + cost < this.heuristic()) {
					System.out.println("err");
				}
				successors.add(succ);
				
			}
			return successors;

		}

		public boolean isMoving(int x, int y, int[] action){
			return maze.isLegal(x + action[0], y + action[1]);
		}
		
		@Override
		public boolean goalTest() {
			return state.size() == 1 && state.contains(new Point(xGoal, yGoal));
		}


		// an equality test is required so that visited sets in searches
		// can check for containment of states
		@Override
		public boolean equals(Object other) {
			return state.equals(((PacmanMazeNode) other).state) && Arrays.equals(this.lastAction, ((PacmanMazeNode) other).lastAction);
		}

		@Override
		public int hashCode() {
			return state.hashCode(); 
		}

		@Override
		public String toString() {
			String strState = new String();
			for (Point p: state) {
				strState += "(" + p.x + "," + p.y + ")";
			}
			return strState;
		}

		@Override
		public double getCost() {
			return cost;
		}
		

		@Override
		public double heuristic() {
			// manhattan distance metric for simple maze with one agent:
			// double dx = xGoal - state[0];
			// double dy = yGoal - state[1];
			// return Math.abs(dx) + Math.abs(dy);

			double distance = 0.0;
			for (Point p : state) {
				distance += (Math.abs(p.x - xGoal) + Math.abs(p.y - yGoal));
			}

			return distance;
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

}
