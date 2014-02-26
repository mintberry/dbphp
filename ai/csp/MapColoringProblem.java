package csp;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.lang.Math;


// for the first part of the assignment, you might not extend UUSearchProblem,
//  since UUSearchProblem is incomplete until you finish it.

public class MapColoringProblem extends ConstraintSatisfactionProblem {
	final static Charset ENCODING = StandardCharsets.UTF_8;

	// the following are the only instance variables you should need.
	//  (some others might be inherited from UUSearchProblem, but worry
	//  about that later.)

	// integer-based vars, domains and constraints
	private HashMap<List<Integer>, HashSet<List<Integer>>> constraintMap;
	private HashMap<Integer, HashSet<Integer>> domainMap;// variables and domains, optional

	// bidirectional map
	HashMap<String, Integer> var2int;
	HashMap<String, Integer> domain2int; 

	private int varCount;


	public MapColoringProblem() {
		List<String> lines = readFromFile("mapcoloring.csp");

		List<String> varNames; 
		List<String> domainNames;

		// get the domains and vars from file
		varNames = new ArrayList<String>(Arrays.asList(lines.get(0).split(" ")));
		domainNames = new ArrayList<String>(Arrays.asList(lines.get(1).split(" ")));

		varCount = varNames.size();
		// System.out.println("test: " + varCount);

		var2int = new HashMap<String, Integer>();
		domain2int = new HashMap<String, Integer>();

		buildMap(var2int, varNames);
		buildMap(domain2int, domainNames);

		// init domain for each var
		domainMap = new HashMap<Integer, HashSet<Integer>>();
		for (int i = 0; i < varCount; ++i) {
			HashSet<Integer> domainSet = new HashSet<Integer>();
			for (int j = 0; j < domainNames.size(); ++j) {
				domainSet.add(new Integer(j));
			}

			domainMap.put(new Integer(i), domainSet);
		}
		domain = new Domain(domainMap);

		// init constraint
		constraintMap = new HashMap<List<Integer>, HashSet<List<Integer>>>();
		for (int i = 2; i < lines.size(); ++i) {
			if (lines.get(i).charAt(0) != '#') {
				atoiConstraint(lines.get(i));
			}
		}
		// test
		// for (Map.Entry<List<Integer>, HashSet<List<Integer>>> entry: constraintMap.entrySet()) {
		// 	List<Integer> key = entry.getKey();
		// 	HashSet<List<Integer>> value = entry.getValue();
		// 	String out = new String();
		// 	for (Integer i: key) {
		// 		out += (i + " ");
		// 	}
		// 	out += ":";
		// 	for (List<Integer> li: value) {
		// 		for (Integer j: li) {
		// 			out += (j + " ");	
		// 		}
		// 		out += ",";
		// 	}
		// 	System.out.println(out);
		// }
		constraint = new Constraint(constraintMap, varCount);

		this.assignmentInit();
	}

	// @Override
	// protected List<Integer> orderDomainValues(HashMap<Integer, HashSet<Integer>> domainMap, int var){
	// 	// or just use a int list in map
	// 	// reorder by lcv
	// 	return new ArrayList<Integer>(domainMap.get(new Integer(var)));
	// }

	// @Override
	// protected int unassignedVar(Domain domain){
	// 	// order on index
	// 	// add mrv
	// 	int ret = -1;
	// 	if (mrvON) {
	// 		ret = constraint.mrv(domain);
	// 	} else {
	// 		ret = assignment.assigned;
	// 	}

	// 	return ret;
	// }

	// @Override
	// protected boolean inference(Domain domain){// mac
	// 	return macON?constraint.mac3(domain.map):!macON;
	// }

	// @Override
	// protected boolean assignmentComplete(){
	// 	return assignment.allAssigned();// && constraint.isSatisfied(assignment);
	// }

	@Override
	protected void assignmentInit(){
		// return an empry linked list
		assignment = new Assignment(varCount);
	}

	// @Override
	// protected boolean valueConsistent(int var, int value){// can integrate this in orderDomainValues
	// 	return constraint.isSatisfied(assignment, var, value);
	// }

	private static List<String> readFile(String fileName) throws IOException {
		Path path = Paths.get(fileName);
		return Files.readAllLines(path, ENCODING);
	}

	public static List<String> readFromFile(String filename) {
		try {
			List<String> lines = readFile(filename);
			return lines;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// convert a string of constraint to int
	private void atoiConstraint(String constraint){
		String[] raw = constraint.split(":");
		String[] strVarSet = raw[0].split(" ");// e.g. SA Q
		String[] strDomSets = raw[1].split(",");// e.g. red green,red blue

		List<Integer> intVarSet = new ArrayList<Integer>(Arrays.asList(map2int(var2int, strVarSet)));

		HashSet<List<Integer>> intDomSets = new HashSet<List<Integer>>();
		for (String strDomSet: strDomSets) {
			intDomSets.add(new ArrayList<Integer>(Arrays.asList(map2int(domain2int, strDomSet.split(" ")))));
		}

		constraintMap.put(intVarSet, intDomSets);

	}

	private Integer[] map2int(HashMap<String, Integer> map, String[] keys){
		Integer[] ret = new Integer[keys.length];
		for (int i = 0; i < keys.length; ++i) {
			ret[i] = map.get(keys[i]);
		}
		return ret;
	}

	private void buildMap(HashMap<String, Integer> map, List<String> names){
		for (String s: names) {
			map.put(s, 0);// just put the names
		}
		int i = 0;
		for (String key: map.keySet()) {// the order should be fixed
			map.put(key, new Integer(i));
			i++;
		}

		// test
		// for (String key: map.keySet().toArray(new String[map.keySet().size()])) {
		// 	System.out.println(key + ": ");
		// }
	}

	public void printResult(Assignment assignment){
		if (null == assignment) {
			System.out.println("No solution for this problem");
		} else {
			List<String> varNames = new ArrayList<String>(var2int.keySet()); 
			List<String> domainNames = new ArrayList<String>(domain2int.keySet());
			for (int i = 0; i < varCount; ++i) {
				System.out.println(String.format("%3s: %s",varNames.get(i),domainNames.get(assignment.assignment[i])));
			}
		}
	}
}
