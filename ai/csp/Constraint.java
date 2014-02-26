package csp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import csp.ConstraintSatisfactionProblem.*;

public class Constraint {
    private HashMap<List<Integer>, HashSet<List<Integer>>> map;
    // a hashmap for graph
    private HashMap<Integer, HashSet<Integer>> vars;

    public Constraint(HashMap<List<Integer>, HashSet<List<Integer>>> map, int count){
        this.map = map;

        // build the map for binary constraints
        this.vars = new HashMap<Integer, HashSet<Integer>>();
        for (List<Integer> li: map.keySet()) {
            Integer key1 = li.get(0);
            Integer key2 = li.get(0);
            if (!vars.containsKey(key1)) {
                vars.put(key1, new HashSet<Integer>());
                vars.get(key1).add(key2);// add the neighbor
            } else {
                vars.get(key1).add(key2);
            }

            if (!vars.containsKey(key2)) {
                vars.put(key2, new HashSet<Integer>());
                vars.get(key2).add(key1);// add the neighbor
            } else {
                vars.get(key2).add(key1);
            }
        }
    }

    public boolean isSatisfied(Assignment assignment){// FIXME, only valid for binary constraint
        for (Map.Entry<List<Integer>, HashSet<List<Integer>>> entry: map.entrySet()) {
            boolean keyAssigned = true;
            List<Integer> vars = entry.getKey();
            List<Integer> values = new ArrayList<Integer>(vars);//
            for (int i = 0; i < vars.size(); ++i) {
                if (assignment.isAssigned(vars.get(i))) {
                    values.set(i, new Integer(assignment.assignmentAt(vars.get(i))));
                } else {
                    // this key is not fully assigned
                    keyAssigned = false;
                    break;
                }
            }
            if (keyAssigned && !entry.getValue().contains(values)) {
                return false;
            }
        }
        return true;
    }

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
        return vars.containsKey(new Integer(var));
    }

    public boolean mac3(HashMap<Integer, HashSet<Integer>> domainMap){
        List<List<Integer>> queue = new LinkedList<List<Integer>>(map.keySet());// FIXME for MAC

        while (!queue.isEmpty()) {
            List<Integer> li = queue.remove(0);
            if (li.size() == 2) {// binary constraint only
                if (revise(domainMap, li)) {
                    if (!domainMap.get(li.get(0)).isEmpty()) {// domain of li[0] is not empty
                        
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
                if (map.get(vars).contains(values)) {
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


}