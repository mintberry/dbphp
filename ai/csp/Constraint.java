package csp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import csp.ConstraintSatisfactionProblem.*;

public class Constraint {
    private HashMap<List<Integer>, HashSet<List<Integer>>> map;
    private int varCount;// FIXME

    public Constraint(HashMap<List<Integer>, HashSet<List<Integer>>> map, int count){
        this.map = map;
        this.varCount = count;
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
        for (Map.Entry<List<Integer>, HashSet<List<Integer>>> entry: map.entrySet()) {
            List<Integer> vars = entry.getKey();
            List<Integer> values = new ArrayList<Integer>(vars);
            // System.out.println("test2: " + values.size() + ":" + vars.size());
            if (vars.contains(new Integer(var))) {// if var is in this key
                for (int i = 0; i < vars.size(); ++i) {
                    // if (var == vars.get(i).intValue()){// get index of var, appear only once
                    //     // check if there is a match
                    //     for (List<Integer> li: entry.getValue()) {
                    //         if (li.get(i).intValue() == value) {
                    //             // System.out.println("test3: " + assignment.assigned);
                    //             return true;
                    //         }
                    //     }
                    //     // if no match
                    //     return false;
                    // } 
                    // assignment.print();
                    if (assignment.isAssigned(vars.get(i))) {
                        values.set(i, new Integer(assignment.assignmentAt(vars.get(i).intValue())));
                    } else if(var == vars.get(i)){
                        values.set(i, new Integer(value));
                    } else {
                        // this key is not fully assigned
                        // keyAssigned = false;
                        // break;
                        values.set(i, new Integer(-1));
                    }
                }
                if (!partialMatch(entry.getValue(), values)) {
                    return false;
                }
            }
        }
        return true;// var may not be in the constraint
    }

    public boolean involves(int var){
        // FIXME
        return var < varCount;
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