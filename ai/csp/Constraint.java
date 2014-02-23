package csp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Constraint {
    private HashMap<List<Integer>, HashSet<List<Integer>>> map;
    private HashSet<Integer> variables;

    public Constraint(){
        
    }

    public boolean isSatisfied(List<Integer> assignment){
        for (Map.Entry<List<Integer>, HashSet<List<Integer>>> entry: map.entrySet()) {
            List<Integer> vars = entry.getKey();
            List<Integer> values = new ArrayList<Integer>(vars.size());
            for (int i = 0; i < vars.size(); ++i) {
                if (vars.get(i) < assignment.size()) {
                    values.set(i, new Integer(assignment.get(vars.get(i))));
                } else {
                    break;
                }
            }
            if (!entry.getValue().contains(values)) {
                return false;
            }
        }
        return true;
    }

    public boolean involves(int var){
        return variables.contains(new Integer(var));
    }


}