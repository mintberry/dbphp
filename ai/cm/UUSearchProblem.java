
// CLEARLY INDICATE THE AUTHOR OF THE FILE HERE (YOU),
//  AND ATTRIBUTE ANY SOURCES USED (INCLUDING THIS STUB, BY
//  DEVIN BALKCOM).

package cannibals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class UUSearchProblem {
	
	// used to store performance information about search runs.
	//  these should be updated during the process of searches

	// see methods later in this class to update these values
	protected int nodesExplored;
	protected int maxMemory;

	protected UUSearchNode startNode;
	
	protected interface UUSearchNode {
		public ArrayList<UUSearchNode> getSuccessors();
		public boolean goalTest();
		public int getDepth();
	}

	// breadthFirstSearch:  return a list of connecting Nodes, or null
	// no parameters, since start and goal descriptions are problem-dependent.
	//  therefore, constructor of specific problems should set up start
	//  and goal conditions, etc.
	
	public List<UUSearchNode> breadthFirstSearch() {
		resetStats();
		// You will write this method
		HashMap<UUSearchNode, UUSearchNode> visited = new HashMap<UUSearchNode, UUSearchNode>();
		ArrayList<UUSearchNode> toVisit = new ArrayList<UUSearchNode>();
		UUSearchNode cur = null;

		toVisit.add(startNode);
		visited.put(startNode, null);
		incrementNodeCount();

		while(!toVisit.isEmpty()){
			cur = toVisit.remove(0);
			if (cur.goalTest()) {
				break;
			}
			ArrayList<UUSearchNode> successors = cur.getSuccessors();
			for (UUSearchNode node: successors) {
				if (!visited.containsKey(node)) {// not visited
					incrementNodeCount();
					toVisit.add(node);
					visited.put(node, cur);
				}
			}	

			updateMemory(visited.size());
		}

		return backchain(cur, visited);
	}
	
	// backchain should only be used by bfs, not the recursive dfs
	private List<UUSearchNode> backchain(UUSearchNode node,
			HashMap<UUSearchNode, UUSearchNode> visited) {
		// you will write this method

		ArrayList<UUSearchNode> chain = new ArrayList<UUSearchNode>();
		// node may not be the goal, then return an empty list
		if (node.goalTest()) {
			UUSearchNode next = node;
			while(next != null) {
				chain.add(0, next);
				next = visited.get(next);
			}
		}

		return chain;
	}

	public List<UUSearchNode> depthFirstMemoizingSearch(int maxDepth) {
		resetStats(); 
		
		// You will write this method
		HashMap<UUSearchNode, Integer> visited = new HashMap<UUSearchNode, Integer>();

		return dfsrm(startNode, visited, 0, maxDepth);
	}

	// recursive memoizing dfs. Private, because it has the extra
	// parameters needed for recursion.  
	private List<UUSearchNode> dfsrm(UUSearchNode currentNode, HashMap<UUSearchNode, Integer> visited, 
			int depth, int maxDepth) {
		
		// keep track of stats; these calls charge for the current node

		List<UUSearchNode> chain = null;
	
		// you write this method.  Comments *must* clearly show the 
		//  "base case" and "recursive case" that any recursive function has.
		if (depth > maxDepth) {
			// do nothing
		} else {
			// if a visited node is visited again in a shorter path
			if (!visited.containsKey(currentNode) || visited.get(currentNode) > depth) {
				updateMemory(visited.size());
				incrementNodeCount();
				visited.put(currentNode, depth);
				if (currentNode.goalTest()) {// base
					chain = new ArrayList<UUSearchNode>();
					chain.add(currentNode);
				} else { // recursive
					ArrayList<UUSearchNode> successors = currentNode.getSuccessors();
					for (UUSearchNode node: successors) {
						chain = dfsrm(node, visited, depth + 1, maxDepth);
						if (chain != null) {// only one goal?
							chain.add(0, currentNode);
							break;
						}
					}	
				}
			}
			
		}
	

		return chain;	
	}
	
	
	// set up the iterative deepening search, and make use of dfspc
	public List<UUSearchNode> IDSearch(int maxDepth) {
		resetStats();
		// you write this method
		int depth = 0;
		List<UUSearchNode> chain = null;

		while(null == (chain = depthFirstPathCheckingSearch(depth)) && depth <= maxDepth) {
			depth++;
			// System.out.println("hahaha: " + depth + " " + this.nodesExplored);
		}

		return chain;
	}

	// set up the depth-first-search (path-checking version), 
	//  but call dfspc to do the real work
	public List<UUSearchNode> depthFirstPathCheckingSearch(int maxDepth) {
		resetStats();
		
		// I wrote this method for you.  Nothing to do.

		HashSet<UUSearchNode> currentPath = new HashSet<UUSearchNode>();

		return dfsrpc(startNode, currentPath, 0, maxDepth);

	}

	// recursive path-checking dfs. Private, because it has the extra
	// parameters needed for recursion.
	private List<UUSearchNode> dfsrpc(UUSearchNode currentNode, HashSet<UUSearchNode> currentPath,
			int depth, int maxDepth) {

		// you write this method
		// incrementNodeCount();
		// updateMemory(currentPath.size());
		List<UUSearchNode> chain = null;

		if (depth > maxDepth) {
			// do nothing
		} else {
			if (!currentPath.contains(currentNode)) {// not visited

				incrementNodeCount();
				updateMemory(currentPath.size());
				
				currentPath.add(currentNode);
				if (currentNode.goalTest()) {// base
					chain = new ArrayList<UUSearchNode>();
					chain.add(currentNode);
				} else { // recursive
					ArrayList<UUSearchNode> successors = currentNode.getSuccessors();
					for (UUSearchNode node: successors) {
						// incrementNodeCount();

						chain = dfsrpc(node, currentPath, depth + 1, maxDepth);
						if (chain != null) {
							chain.add(0, currentNode);
							break;
						}
					}	
				}
				currentPath.remove(currentNode);
			}
		}
	
		return chain;
	}

	protected void resetStats() {
		nodesExplored = 0;
		maxMemory = 0;
	}
	
	protected void printStats() {
		System.out.println("Nodes explored during last search:  " + nodesExplored);
		System.out.println("Maximum memory usage during last search " + maxMemory);
	}
	
	protected void updateMemory(int currentMemory) {
		maxMemory = Math.max(currentMemory, maxMemory);
	}
	
	protected void incrementNodeCount() {
		nodesExplored++;
	}

}
