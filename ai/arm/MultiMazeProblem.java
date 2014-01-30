package assignment_mazeworld;

import java.util.ArrayList;
import java.util.Arrays;

// Find a path for a single agent to get from a start location (xStart, yStart)
//  to a goal location (xGoal, yGoal)

public class MultiMazeProblem extends InformedSearchProblem {

	private static int actions[][] = {Maze.NORTH, Maze.EAST, Maze.SOUTH, Maze.WEST}; 
	
	private int xStart[], yStart[], xGoal[], yGoal[];

	private int k;// number of robots

	private Maze maze;
	
	public MultiMazeProblem(Maze m, int[] sx, int[] sy, int[] gx, int[] gy, int robots) {
		k = robots;

		startNode = new MultiMazeNode(sx, sy, 0);
		xStart = sx;
		yStart = sy;
		xGoal = gx;
		yGoal = gy;
		
		maze = m;		
	}
	

	
	// node class used by searches.  Searches themselves are implemented
	//  in SearchProblem.
	public class MultiMazeNode implements SearchNode {

		// location of the agent in the maze
		protected int[] state; 
		
		// how far the current node is from the start.  Not strictly required
		//  for uninformed search, but useful information for debugging, 
		//  and for comparing paths
		private double cost;  

		public MultiMazeNode(int[] x, int[] y, double c) {
			state = new int[2 * MultiMazeProblem.this.k];

			for (int i = 0; i < x.length; ++i) {
				state[i * 2] = x[i];
				state[i * 2 + 1] = y[i]; //
			}
		
			cost = c;

		}

		public MultiMazeNode(int[] stateNew, double c) {
			state = new int[2 * MultiMazeProblem.this.k];

			for (int i = 0; i < stateNew.length; ++i) {
				state[i] = stateNew[i];
			}
		
			cost = c;

		}
		
		public int getX(int t) {// robots are numbered 0..k-1
			return state[2 * t];
		}
		
		public int getY(int t) {
			return state[2 * t + 1];
		}

		public boolean hasCollision(int n, int nX, int nY){
			for (int i = 0; i < MultiMazeProblem.this.k; ++i) {
				if (n != i && (nX == getX(i) && nY == getY(i))) {
					return true;
				}
			}
			return false;
		}

		public ArrayList<SearchNode> getSuccessors() {

			ArrayList<SearchNode> successors = new ArrayList<SearchNode>();

			for (int i = 0; i < MultiMazeProblem.this.k; ++i) {
				for (int[] action: actions) {
					int xNew = getX(i) + action[0];
					int yNew = getY(i) + action[1]; 

					state[2 * i] += action[0];
					state[2 * i + 1] += action[1];
					
					//System.out.println("testing successor " + xNew + " " + yNew);
					
					if(maze.isLegal(xNew, yNew) && !hasCollision(i, xNew, yNew)) {// also test if collides with other robot
						//System.out.println("legal successor found " + " " + xNew + " " + yNew);
						SearchNode succ = new MultiMazeNode(state, getCost() + 1.0);

						if (succ.heuristic() + 1.0 < this.heuristic()) {
							System.out.println("err");
						}	

						successors.add(succ);
					}

					state[2 * i] -= action[0];
					state[2 * i + 1] -= action[1];
					
				}
			}

			return successors;

		}
		
		@Override
		public boolean goalTest() {
			for (int i = 0; i < MultiMazeProblem.this.k; ++i) {
				if (getX(i) != xGoal[i] || getY(i) != yGoal[i]) {
					return false;
				}
			}
			return true;
		}


		// an equality test is required so that visited sets in searches
		// can check for containment of states
		@Override
		public boolean equals(Object other) {
			return Arrays.equals(state, ((MultiMazeNode) other).state);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(state); 
		}

		@Override
		public String toString() {
			String strState = new String();
			for (int i = 0; i < MultiMazeProblem.this.k; ++i) {
				strState += "(" + getX(i) + "," + getY(i) + ")";
			}
			return strState;
		}

		@Override
		public double getCost() {
			return cost;
		}
		

		@Override
		public double heuristic() {
			double distance = 0.0;
			// manhattan distance metric for multirobot:
			for (int i = 0; i < MultiMazeProblem.this.k; ++i) {
				distance += Math.abs(xGoal[i] - getX(i));
				distance += Math.abs(yGoal[i] - getY(i));
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
