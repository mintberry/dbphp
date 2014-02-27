package csp;

import java.util.List;
import csp.ConstraintSatisfactionProblem.*;

public class CSPDriver {
	public static void main(String args[]) {
	

		// interesting starting state:  
		//  8, 5, 1  (IDS slow, but uses least memory.)


		// set up the "standard" 331 problem:
		MapColoringProblem mcProblem = new MapColoringProblem("mapcoloring.csp");
	
		Assignment assignment = mcProblem.basicBacktrackingSearch();
		mcProblem.printResult(assignment);
		mcProblem.printStats();

		CircuitBoardProblem cbProblem = new CircuitBoardProblem("circuitboard.csp");
		assignment = cbProblem.basicBacktrackingSearch();
		cbProblem.printResult(assignment);
		cbProblem.printBoard(assignment);
		cbProblem.printStats();
	}
}
