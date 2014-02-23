package csp;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

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
	private Constraint constraint;
	private HashMap<Integer, HashSet<Integer>> map;// variables and domains

	// Lists as maps for string and integer
	List<String> varNames; 
	List<String> domainNames;


	public MapColoringProblem() {


		varNames = new ArrayList<String>();
		domainNames = new ArrayList<String>();

		map = new HashMap<Integer, HashSet<Integer>>();

		constraint = new Constraint();


		assignment = this.assignmentInit();
	}

	@Override
	protected List<Integer> orderDomainValues(int var){
		return null;
	}

	@Override
	protected boolean assignmentComplete(){
		return true;
	}

	@Override
	protected List<Integer> assignmentInit(){
		// return an empry linked list
		return new LinkedList<Integer>();
	}

	@Override
	protected boolean valueConsistent(int value){// can integrate this in orderDomainValues
		return true;
	}


	// private class MapColoringConstriant implements Constraint{
	// 	@Override
	// 	public boolean isSatisfied() {
	// 		return true;
	// 	}

	// 	@Override
	// 	public boolean involves() {
	// 		return true;
	// 	}
	// }

	private static List<String> readFile(String fileName) throws IOException {
		Path path = Paths.get(fileName);
		return Files.readAllLines(path, ENCODING);
	}


	public static void readFromFile(String filename) {
		try {
			List<String> lines = readFile(filename);

			int i = 0;
			for (String line : lines) {

				i++;
			}

		} catch (IOException E) {
			return;
		}
	}
}
