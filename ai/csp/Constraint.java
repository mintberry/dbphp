package csp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import csp.ConstraintSatisfactionProblem.*;

public class Constraint {
    private HashMap<List<Integer>, HashSet<List<Integer>>> map;
    // a hashmap for graph
    private HashMap<Integer, HashSet<Integer>> graph;

    public Constraint(HashMap<List<Integer>, HashSet<List<Integer>>> map, int count){
        this.map = map;

        // build the map for binary constraints
        this.graph = new HashMap<Integer, HashSet<Integer>>();
        for (List<Integer> li: map.keySet()) {
            Integer key1 = li.get(0);
            Integer key2 = li.get(1);
            if (!graph.containsKey(key1)) {
                graph.put(key1, new HashSet<Integer>());
                graph.get(key1).add(key2);// add the neighbor
            } else {
                graph.get(key1).add(key2);
            }

            if (!graph.containsKey(key2)) {
                graph.put(key2, new HashSet<Integer>());
                graph.get(key2).add(key1);// add the neighbor
            } else {
                graph.get(key2).add(key1);
            }
        }
    }

    // public boolean isSatisfied(Assignment assignment){// deprecated, only valid for binary constraint
    //     for (Map.Entry<List<Integer>, HashSet<List<Integer>>> entry: map.entrySet()) {
    //         boolean keyAssigned = true;
    //         List<Integer> vars = entry.getKey();
    //         List<Integer> values = new ArrayList<Integer>(vars);//
    //         for (int i = 0; i < vars.size(); ++i) {
    //             if (assignment.isAssigned(vars.get(i))) {
    //                 values.set(i, new Integer(assignment.assignmentAt(vars.get(i))));
    //             } else {
    //                 // this key is not fully assigned
    //                 keyAssigned = false;
    //                 break;
    //             }
    //         }
    //         if (keyAssigned && !entry.getValue().contains(values)) {
    //             return false;
    //         }
    //     }
    //     return true;
    // }

    public boolean isSatisfied(Assignment assignment, int var, int value){
        if (involves(var)) {
            for (Map.Entry<List<Integer>, HashSet<List<Integer>>> entry: map.entrySet()) {
                List<Integer> vars = entry.getKey();
                List<Integer> values = new ArrayList<Integer>(vars);
                // System.out.println("test2: " + values.size() + ":" + vars.size());
                if (vars.contains(new Integer(var))) {// if var is in this key
                    for (int i = 0; i < vars.size(); ++i) {
                        if (assignment.isAssigned(vars.get(i))) {
                            values.set(i, new Integer(assignment.assignmentAt(vars.get(i).intValue())));
                        } else if(var == vars.get(i)){
                            values.set(i, new Integer(value));
                        } else {
                            values.set(i, new Integer(-1));
                        }
                    }
                    if (!partialMatch(entry.getValue(), values)) {
                        return false;
                    }
                }
            }
        }
        return true;// var may not be in the constraint
    }

    public boolean involves(int var){
        return graph.containsKey(new Integer(var));
    }

    public boolean mac3(Assignment assignment, Domain domain, int var){// FIXME domain instead of domainMap
        // List<List<Integer>> queue = new LinkedList<List<Integer>>(map.keySet());// FIXME for MAC
        List<List<Integer>> queue = macQueue(assignment, var);

        while (!queue.isEmpty()) {
            List<Integer> li = queue.remove(0);
            if (li.size() == 2) {// binary constraint only
                if (revise(domain.map, li)) {
                    if (!domain.map.get(li.get(0)).isEmpty()) {// domain of li[0] is not empty
                        for (Integer neighbor: graph.get(li.get(0))) {// add new pair to the queue, but is queue really a set?

                            if (neighbor.intValue() != li.get(1).intValue()) {// exclude li[1]
                                List<Integer> newLi = new ArrayList<Integer>(2);// newLi[neighbor, li[0]]
                                newLi.add(neighbor);
                                newLi.add(li.get(0));
                                queue.add(newLi);
                            }

                        }
                    } else {
                        return false;
                    }
                }
            }
        }

        return true;

    }

    private boolean revise(HashMap<Integer, HashSet<Integer>> domainMap, List<Integer> vars){
        boolean revised = false, found = false;
        List<Integer> domain = new ArrayList<Integer>(domainMap.get(vars.get(0)));
        for (Integer x1: domain) {
            List<Integer> values = new ArrayList<Integer>(vars);
            values.set(0, x1);
            found = false;
            for (Integer x2: domainMap.get(vars.get(1))) {
                values.set(1, x2);
                if (getConstraint(vars).contains(values)) {// map may not have vars? FIXME
                    found = true;
                    break;
                }
            }

            if (!found) {// no match
                revised = true;
                if(!domainMap.get(vars.get(0)).remove(x1)){
                    System.out.println("revise domain failed");
                }
            }
        }

        return revised;
    }

    // public int mrv(Domain domain){
    //     return domain.mrv().intValue();
    // }

    // returns a value for choices ruled out
    public int valueFlexibility(Assignment assignment, Domain domain, int var, int value){
        int ret = 0;

        if (null != graph.get(new Integer(var))) {// tasmania
            for (Integer neighbor: graph.get(new Integer(var))) {// get all neighbors
                if (!assignment.isAssigned(neighbor.intValue())) {// if neighbor is not assigned
                    if (domain.map.get(new Integer(var)).contains(new Integer(value))) {// if value is in neighbors domain
                        ret++;// it gets ruled out
                    }
                }
            }
        }   
        return ret;
    }


    public Integer degreeHeuristic(Assignment assignment){
        // System.out.println("test: " + assignment.unassigned.size());
        return Collections.min(assignment.unassigned, new Comparator<Integer>(){
        public int compare(Integer v1, Integer v2) {
            //ascending order
            return varDegree(assignment, v1.intValue()) >= varDegree(assignment, v2.intValue()) ? 1 : -1;
        }
        });
    }


    // should work for general case
    private boolean partialMatch(HashSet<List<Integer>> liSet, List<Integer> value){
        for (List<Integer> li: liSet) {
            if (liMatch(value, li)) {
                return true;
            }
        }
        return false;
    }

    private boolean liMatch(List<Integer> value, List<Integer> li){
        for (int i = 0; i < li.size(); ++i) {
            if (value.get(i).intValue() != -1 && value.get(i).intValue() != li.get(i).intValue()) {
                return false;
            }
        }
        return true;
    }

    private int varDegree(Assignment assignment, int var){
        int ret = 0;
        // System.out.println("test: " + graph.get(new Integer(var)));
        if (null != graph.get(new Integer(var))) {// tasmania
            for (Integer neighbor: graph.get(new Integer(var))) {
                if (!assignment.isAssigned(neighbor.intValue())) {
                    ret++;
                }
            }   
        }
        return ret;
    }

    private List<List<Integer>> macQueue(Assignment assignment, int var){
        List<List<Integer>> ret = new LinkedList<List<Integer>>();
        if (null != graph.get(new Integer(var))) {// tasmania
            for (Integer neighbor: graph.get(new Integer(var))) {// get all neighbors
                if (!assignment.isAssigned(neighbor.intValue())) {// if neighbor is not assigned
                    List<Integer> li = new ArrayList<Integer>(2);
                    li.add(neighbor);
                    li.add(new Integer(var));
                    ret.add(li);
                }
            }
        }
        return ret;
    }

    private HashSet<List<Integer>> getConstraint(List<Integer> key){
        List<Integer> liKey = new ArrayList<Integer>(key);
        if (!map.containsKey(liKey)) {
            Integer temp = liKey.get(0);
            liKey.set(0, liKey.get(1));
            liKey.set(1, temp);
        }

        return map.get(liKey);
    }

}