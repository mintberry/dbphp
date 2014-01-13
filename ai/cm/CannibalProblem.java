package cannibals;

import java.util.ArrayList;
import java.util.Arrays;

import java.lang.Math;


// for the first part of the assignment, you might not extend UUSearchProblem,
//  since UUSearchProblem is incomplete until you finish it.

public class CannibalProblem extends UUSearchProblem {

	// the following are the only instance variables you should need.
	//  (some others might be inherited from UUSearchProblem, but worry
	//  about that later.)

	private int goalm, goalc, goalb;
	private int totalMissionaries, totalCannibals; 

	public CannibalProblem(int sm, int sc, int sb, int gm, int gc, int gb) {
		// I (djb) wrote the constructor; nothing for you to do here.

		startNode = new CannibalNode(sm, sc, 1, 0);
		goalm = gm;
		goalc = gc;
		goalb = gb;
		totalMissionaries = sm;
		totalCannibals = sc;

		//test getsuccessor
		// CannibalNode test_node1 = new CannibalNode(3, 2, 1, 0);

		// CannibalNode test_node2 = new CannibalNode(2, 2, 0, 1);

		// CannibalNode test_node3 = new CannibalNode(2, 0, 1, 2);

		// CannibalNode test_node4 = new CannibalNode(0, 1, 0, 3);

		// ArrayList<UUSearchNode> test_list = test_node2.getSuccessors();
		// for (UUSearchNode node:test_list) {
		// 	System.out.println(node);
		// }
		
		
	}
	
	// node class used by searches.  Searches themselves are implemented
	//  in UUSearchProblem.
	private class CannibalNode implements UUSearchNode {

		// do not change BOAT_SIZE without considering how it affect
		// getSuccessors. 
		
		private final static int BOAT_SIZE = 2;
	
		// how many missionaries, cannibals, and boats
		// are on the starting shore
		private int[] state; 
		
		// how far the current node is from the start.  Not strictly required
		//  for search, but useful information for debugging, and for comparing paths
		private int depth;  

		public CannibalNode(int m, int c, int b, int d) {
			state = new int[3];
			this.state[0] = m;
			this.state[1] = c;
			this.state[2] = b;
			
			depth = d;

		}

		public ArrayList<UUSearchNode> getSuccessors() {

			// add actions (denoted by how many missionaries and cannibals to put
			// in the boat) to current state. 

			// You write this method.  Factoring is usually worthwhile.  In my
			//  implementation, I wrote an additional private method 'isSafeState',
			//  that I made use of in getSuccessors.  You may write any method
			//  you like in support of getSuccessors.
			int bank_m = 0, bank_c = 0;

			ArrayList<UUSearchNode> successors = new ArrayList<UUSearchNode>();
			if (0 == state[2]) {// boat is at dest
				bank_m = CannibalProblem.this.totalMissionaries - this.state[0];
				bank_c = CannibalProblem.this.totalCannibals - this.state[1];
			} else {// boat is at src
				bank_m = this.state[0];
				bank_c = this.state[1];
			}
			int boat_m = 0, boat_c = 0;

			// no missionary on boat
			for (int j = 1; j <= bank_c; ++j) {
				boat_c = j;
				if (boat_m + boat_c <= BOAT_SIZE) {
					CannibalNode new_node = new CannibalNode(state[0], state[1] - boat_c * (state[2] == 1?1:-1), state[2] ^ 1, this.depth + 1);
					if (this.valid(new_node)) {
						successors.add(new_node);
					}
				}
			}

			for (int i = 1; i <= bank_m; ++i) {// at least one missionaries on boat
				for (int j = 0; j <= bank_c; ++j) {
					boat_m = i;
					boat_c = j;
					if (boat_m + boat_c <= BOAT_SIZE && boat_m >= boat_c) {
						// validate the successor
						CannibalNode new_node = new CannibalNode(state[0] - boat_m * (state[2] == 1?1:-1), 
							state[1] - boat_c * (state[2] == 1?1:-1), state[2] ^ 1, this.depth + 1);
						if (this.valid(new_node)) {
							successors.add(new_node);
						}
					}
				}
			}

			return successors;
		}
		
		@Override
		public boolean goalTest() {
			// you write this method.  (It should be only one line long.)
			return (state[0] == CannibalProblem.this.goalm) && (state[1] == CannibalProblem.this.goalc) && 
			(state[2] == CannibalProblem.this.goalb);
		}

		

		// an equality test is required so that visited lists in searches
		// can check for containment of states
		@Override
		public boolean equals(Object other) {
			return Arrays.equals(state, ((CannibalNode) other).state);
		}

		@Override
		public int hashCode() {
			return state[0] * 100 + state[1] * 10 + state[2];
		}

		@Override
		public String toString() {
			// you write this method
			return "(" + Integer.toString(state[0]) + ", " + Integer.toString(state[1]) + ", " + Integer.toString(state[2]) + ")";
		}

		
        /* You might need this method when you start writing 
        (and debugging) UUSearchProblem.*/
        
		@Override
		public int getDepth() {
			return depth;
		}
		

		public boolean valid(CannibalNode node){
			int dest_m = CannibalProblem.this.totalMissionaries - node.state[0];
			int dest_c = CannibalProblem.this.totalCannibals - node.state[1];

			return (dest_m >= dest_c || dest_m == 0) && (node.state[0] >= node.state[1] || node.state[0] == 0);
		}

	}
	

}
