package assignment_robots;

import java.lang.Math;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;

// This is the local planner of the robot arms;
// Can return time between two configurations;
// can get the path between configurations;

public class CarPlanner {
	CarState start, goal;
	protected World world;

	HashMap<CarState, HashSet<CarState>> rrt;

	public CarPlanner(CarState css, CarState csg, World w){
		start = new CarState(css);
		goal = new CarState(csg);
		rrt = new HashMap<CarState, HashSet<CarState>>();

		world = w;
	}

	// rrt as car planner
	public void xRRT(int k, double dt, double diff){
		double time = 0.0;//1 + Math.random() * 10.0;
		int ctrl = 0;// (int)(Math.random() * 6)
		SteeredCar sc = new SteeredCar();

		rrt.put(start, new HashSet<CarState>());

		CarState rand = null, q = null, pick = null;
		CarRobot cr = new CarRobot();
		for (int i = 1; i < k;) {
			// each time randomly pick a state or use the goal
			if (diff == 0.0) {
				do {
					rand = randomState(CarDriver.window_width, CarDriver.window_height);
					cr.set(rand);
				} while (world.carCollision(cr));	
			} else {
				rand = goal;
			}
			
			// pick the nearest tree node to rand
			q = nearest(rand);
			// random choose a time
			time = Math.random() * dt;
			for (int j = 0; j < 6; j++) {// find nearest pick
				if (!world.carCollisionPath(cr, q, j, time)) {// if no collision
					CarState temp = sc.move(q, j, time);
					if (pick == null) {
						pick = new CarState(temp);
					} else if (distance(pick, rand) > distance(temp, rand)) {
						pick.set(temp);
					}
				}
			}

			if (null != pick) {
				rrt.put(pick, new HashSet<CarState>());
				rrt.get(q).add(pick);
				// System.out.println(distance(pick, goal));
				if(diff == 0.0) {// if the target is random
					i++;
				} else if (diff > distance(pick, goal)) { // util it get to the goal
					break;
				}
				// i++;	
				pick = null;
			}

		}
	} 


	private CarState randomState(double width, double height){
		return new CarState(width * Math.random(), height * Math.random(), Math.random() * (Math.PI * 2.0));
	}

	// can be imporved, currently euclidean distance
	private double distance(CarState cs1, CarState cs2){
		return Math.sqrt(Math.pow((cs1.get())[0]-(cs2.get())[0], 2)+Math.pow((cs1.get())[1]-(cs2.get())[1], 2) + 
			Math.pow((cs1.get())[2]-(cs2.get())[2], 2));
	}

	private CarState nearest(CarState target){
		ArrayList<CarState> neighbours = new ArrayList<CarState>(rrt.keySet());
		return Collections.min(neighbours, new Comparator<CarState>(){
			public int compare(CarState s1, CarState s2) {
	    	//ascending order
	      	return distance(s1, target) >= distance(s2, target) ? 1 : -1;
	    }
		});
	}


	
	
 }
