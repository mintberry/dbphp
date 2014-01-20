package assignment_mazeworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import java.util.HashSet;// no need for this?

public class InformedSearchProblem extends SearchProblem {
	
	public List<SearchNode> astarSearch() {
		
		resetStats();

        PriorityQueue<SearchNode> frontier = new PriorityQueue<SearchNode>();
        HashMap<SearchNode, Double> queued = new HashMap<SearchNode, Double>();
        HashSet<SearchNode> explored = new HashSet<SearchNode>();

        HashMap<SearchNode, SearchNode> reachedFrom = new HashMap<SearchNode, SearchNode>();



        // add the start node
        frontier.add(startNode);
        queued.put(startNode, startNode.priority());
        reachedFrom.put(startNode, null);

        while(!frontier.isEmpty()) {
            SearchNode cur = frontier.poll(); // the highest priority state
            if (!explored.contains(cur)) { // if the node polled is not explored before
                incrementNodeCount();
                updateMemory(frontier.size() + reachedFrom.size() + queued.size() + explored.size());
                if (cur.goalTest()) {
                    return backchain(cur, reachedFrom);
                }
                explored.add(cur);//

                ArrayList<SearchNode> successors = cur.getSuccessors();
                for (SearchNode node : successors) {
                    if (!explored.contains(node)) {
                        if (!queued.containsKey(node) || queued.get(node) > node.priority()) {
                            queued.put(node, node.priority());
                            reachedFrom.put(node, cur);
                            frontier.add(node);
                        }
                    }
                }
            }
            
        }

		return null;
	}
}
