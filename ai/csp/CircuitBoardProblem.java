package csp;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.awt.Point;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.lang.Math;


// for the first part of the assignment, you might not extend UUSearchProblem,
//  since UUSearchProblem is incomplete until you finish it.

public class CircuitBoardProblem extends ConstraintSatisfactionProblem {
	final static Charset ENCODING = StandardCharsets.UTF_8;

	// the following are the only instance variables you should need.
	//  (some others might be inherited from UUSearchProblem, but worry
	//  about that later.)

	// integer-based vars, domains and constraints
	private HashMap<List<Integer>, HashSet<List<Integer>>> constraintMap;
	private HashMap<Integer, HashSet<Integer>> domainMap;// variables and domains, optional

	// bidirectional map
	HashMap<String, Integer> var2int;
	HashMap<Point, Integer> domain2int; 

	private int varCount;

	private Point board;

	private List<String> varNames; 
	private	List<Point> varSizes;


	public CircuitBoardProblem(String cspFile) {
		List<String> lines = readFromFile(cspFile);


		// get board size from file
		board = str2point(lines.get(0));


		// get variables and sizes
		varNames = new ArrayList<String>(lines.size());
		varSizes = new ArrayList<Point>(lines.size());
		for (int i = 1; i < lines.size(); ++i) {
			if (lines.get(i).charAt(0) != '#') {
				String[] strVar = lines.get(i).split(":");
				varNames.add(strVar[0]);
				varSizes.add(str2size(strVar[1]));
			}
		}
		varCount = varNames.size();

		// build domain map and var map
		var2int = new HashMap<String, Integer>();
		domain2int = new HashMap<Point, Integer>();
		// n * m positions
		List<Point> allPoints = new ArrayList<Point>(board.x * board.y);
		for (int i = 0; i < board.x; ++i) {
			for (int j = 0; j < board.y; ++j) {
				allPoints.add(new Point(i, j));
			}
		}
		buildMap(var2int, varNames);
		buildMap(domain2int, allPoints);

		// init domain for each var
		domainMap = new HashMap<Integer, HashSet<Integer>>();
		for (int i = 0; i < varNames.size(); ++i) {
			Point size = varSizes.get(i);
			String varName = varNames.get(i);
			for (Point base: allPoints) {
				if (base.x + size.x < board.x && base.y + size.y < board.y) {// a legal position for this var
					Integer key = var2int.get(varName);
					Integer value = domain2int.get(base);
					if (!domainMap.containsKey(key)) {
						domainMap.put(key, new HashSet<Integer>());
					}
					domainMap.get(key).add(value);
				}
			}
		}
		domain = new Domain(domainMap);

		// init constraint
		constraintMap = new HashMap<List<Integer>, HashSet<List<Integer>>>();
		List<Point> posList = new ArrayList<Point>(domain2int.keySet());// a list ordered as in map
		for (int i = 0; i < varNames.size(); ++i) {
			for (int j = i + 1; j < varNames.size(); ++j) {
				Point size1 = varSizes.get(i);
				String varName1 = varNames.get(i);
				Point size2 = varSizes.get(j);
				String varName2 = varNames.get(j);

				for (Integer intPos1: domainMap.get(var2int.get(varName1))) {// get all possible base for v1
					for (Integer intPos2: domainMap.get(var2int.get(varName2))) {// get all possible base for v2
						Point base1 = posList.get(intPos1);
						Point base2 = posList.get(intPos2);
						if (!intersect(base1, base2, size1, size2)) {// add to constraintMap if no intersection
							List<Integer> constraintKey = new ArrayList<Integer>(2);
							constraintKey.add(var2int.get(varName1));
							constraintKey.add(var2int.get(varName2));
							if (!constraintMap.containsKey(constraintKey)) {
								constraintMap.put(constraintKey, new HashSet<List<Integer>>());
							}
							List<Integer> constraintVal = new ArrayList<Integer>(2);
							constraintVal.add(intPos1);
							constraintVal.add(intPos2);
							constraintMap.get(constraintKey).add(constraintVal);
						}
					}
				}
			}
		}
		// test
		// List<String> varNames2 = new ArrayList<String>(var2int.keySet()); 
		// for (Map.Entry<List<Integer>, HashSet<List<Integer>>> entry: constraintMap.entrySet()) {
		// 	List<Integer> key = entry.getKey();
		// 	HashSet<List<Integer>> value = entry.getValue();
		// 	String out = new String();
		// 	for (Integer i: key) {
		// 		out += (varNames2.get(i.intValue()) + " ");
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
	// private void atoiConstraint(String constraint){
	// 	String[] raw = constraint.split(":");
	// 	String[] strVarSet = raw[0].split(" ");// e.g. SA Q
	// 	String[] strDomSets = raw[1].split(",");// e.g. red green,red blue

	// 	List<Integer> intVarSet = new ArrayList<Integer>(Arrays.asList(map2int(var2int, strVarSet)));

	// 	HashSet<List<Integer>> intDomSets = new HashSet<List<Integer>>();
	// 	for (String strDomSet: strDomSets) {
	// 		intDomSets.add(new ArrayList<Integer>(Arrays.asList(map2int(domain2int, strDomSet.split(" ")))));
	// 	}

	// 	constraintMap.put(intVarSet, intDomSets);

	// }

	// private Integer[] map2int(HashMap<String, Integer> map, String[] keys){
	// 	Integer[] ret = new Integer[keys.length];
	// 	for (int i = 0; i < keys.length; ++i) {
	// 		ret[i] = map.get(keys[i]);
	// 	}
	// 	return ret;
	// }

	private <T> void buildMap(HashMap<T, Integer> map, List<T> names){
		for (T s: names) {
			map.put(s, 0);// just put the names
		}
		int i = 0;
		for (T key: map.keySet()) {// the order should be fixed
			map.put(key, new Integer(i));
			i++;
		}

		// test
		// for (String key: map.keySet().toArray(new String[map.keySet().size()])) {
		// 	System.out.println(key + ": ");
		// }
	}

	private Point str2point(String pair){
		String[] coords = pair.split(",");
		return new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
	}

	private Point str2size(String pair){
		String[] coords = pair.split(",");
		return new Point(Integer.parseInt(coords[0]) - 1, Integer.parseInt(coords[1]) - 1);
	}

	private boolean intersect(Point base1, Point base2, Point size1, Point size2){
		return !(base1.y + size1.y < base2.y || base1.x + size1.x < base2.x ||
			base1.y > base2.y + size2.y || base1.x > base2.x + size2.x);
	}

	public void printResult(Assignment assignment){
		if (null == assignment) {
			System.out.println("No solution for this problem");
		} else {
			List<String> varNames = new ArrayList<String>(var2int.keySet()); 
			List<Point> domainNames = new ArrayList<Point>(domain2int.keySet());
			for (int i = 0; i < varCount; ++i) {
				System.out.println(String.format("%3s: %s",varNames.get(i), domainNames.get(assignment.assignment[i]).toString()));
			}
		}
	}

	public void printBoard(Assignment assignment){
		char[][] cBoard = new char[board.y][board.x];
		List<Point> domainNames = new ArrayList<Point>(domain2int.keySet());
		for (int i = 0; i < board.y; ++i) {
			for (int j = 0; j < board.x; ++j) {
				cBoard[i][j] = '.';
			}
		}
		for (int i = 0; i < varCount; ++i) {
			String var = varNames.get(i);
			Point size = varSizes.get(i);

			Point base = domainNames.get(assignment.assignment[i]);
			for (int k = base.x; k <= base.x + size.x; ++k) {
				for (int j = base.y; j <= base.y + size.y; ++j) {
					cBoard[j][k] = var.charAt(0);// first character
				}
			}
		}

		for (int i = 0; i < board.y; ++i) {
			System.out.print(cBoard[board.y - i - 1]);
			System.out.print("\n");
		}
	}


}
