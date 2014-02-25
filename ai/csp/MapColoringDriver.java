package csp;

import java.util.List;
import csp.ConstraintSatisfactionProblem.*;

public class MapColoringDriver {
	public static void main(String args[]) {
	

		// interesting starting state:  
		//  8, 5, 1  (IDS slow, but uses least memory.)


		// set up the "standard" 331 problem:
		MapColoringProblem mcProblem = new MapColoringProblem();
	
		Assignment assignment = mcProblem.basicBacktrackingSearch();
		mcProblem.printResult(assignment);
	}
}