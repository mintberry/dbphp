package assignment_robots;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.shape.Polygon;
import javafx.scene.Group;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import java.util.List;

import assignment_mazeworld.SearchProblem.*;
import assignment_robots.ArmPlanner.*;


public class ArmDriver extends Application {
	// default window size
	protected static int window_width = 600;
	protected static int window_height = 400;
	
	public void addPolygon(Group g, Double[] points) {
		Polygon p = new Polygon();
	    p.getPoints().addAll(points);
	    
	    g.getChildren().add(p);
	}
	
	// plot a ArmRobot;
	public void plotArmRobot(Group g, ArmRobot arm, double[] config, Paint stroke, Paint fill) {
		arm.set(config);
		double[][] current;
		Double[] to_add;
		Polygon p;
		for (int i = 1; i <= arm.getLinks(); i++) {
			current = arm.getLinkBox(i);
			
			
			to_add = new Double[2*current.length];
			for (int j = 0; j < current.length; j++) {
				// System.out.println(current[j][0] + ", " + current[j][1]);
				to_add[2*j] = current[j][0];
				//to_add[2*j+1] = current[j][1];
				to_add[2*j+1] = window_height - current[j][1];
			}
			p = new Polygon();
			p.getPoints().addAll(to_add);
			p.setStroke(stroke);
			p.setFill(fill);
			g.getChildren().add(p);
		}
		
	}
	
	public void plotWorld(Group g, World w) {
		int len = w.getNumOfObstacles();
		double[][] current;
		Double[] to_add;
		Polygon p;
		for (int i = 0; i < len; i++) {
			current = w.getObstacle(i);
			to_add = new Double[2*current.length];
			for (int j = 0; j < current.length; j++) {
				to_add[2*j] = current[j][0];
				//to_add[2*j+1] = current[j][1];
				to_add[2*j+1] = window_height - current[j][1];
			}
			p = new Polygon();
			p.getPoints().addAll(to_add);
			g.getChildren().add(p);
		}
	}
	
	// The start function; will call the drawing;
	// You can run your PRM or RRT to find the path; 
	// call them in start; then plot the entire path using
	// interfaces provided;
	@Override
	public void start(Stage primaryStage) {
		
		
		// setting up javafx graphics environments;
		primaryStage.setTitle("CS 76 2D world");

		Group root = new Group();
		Scene scene = new Scene(root, window_width, window_height);

		primaryStage.setScene(scene);
		
		Group g = new Group();

		// setting up the world;
		
		// creating polygon as obstacles;
		

		double a[][] = {{10, 400}, {150, 300}, {100, 210}};
		Poly obstacle1 = new Poly(a);
		
		double b[][] = {{500, 230}, {450, 400}, {580, 325}};
		Poly obstacle2 = new Poly(b);
		
		// double c[][] = {{260, 20}, {400, 180}, {470, 20}};
		double c[][] = {{200, 50}, {340, 210}, {410, 50}};
		Poly obstacle3 = new Poly(c);

		// add 4 boundaries as obstacles
		double e1[][] = {{0,0}, {window_width, 0}, {window_width, -1}};
		double e2[][] = {{0,0}, {-1, window_height}, {0, window_height}};
		double e3[][] = {{0, window_height}, {window_width, window_height + 1}, {window_width, window_height}};
		double e4[][] = {{window_width,0}, {window_width, window_height}, {window_width + 1, 0}};
		Poly edge1 = new Poly(e1);		
		Poly edge2 = new Poly(e2);
		Poly edge3 = new Poly(e3);
		Poly edge4 = new Poly(e4);
		// Declaring a world; 
		World w = new World();
		// Add obstacles to the world;
		w.addObstacle(obstacle1);
		w.addObstacle(obstacle2);
		w.addObstacle(obstacle3);

		w.addObstacle(edge1);
		w.addObstacle(edge2);
		w.addObstacle(edge3);
		w.addObstacle(edge4);
		
		plotWorld(g, w);
		
		ArmRobot arm = new ArmRobot(4);
		
		// double[] config1 = {10, 20, 80, Math.PI/4, 80, Math.PI/4};
		// double[] config2 = {320, 200, 80, .1, 80, .2};
		// double[] config1 = {10, 20, 80, Math.PI/4, 80, Math.PI/4};
		// double[] config2 = {555, 30, 80, Math.PI * 7.0 / 11.0, 80, .2};
		// double[] config1 = {10, 20, 60, Math.PI/4, 70, Math.PI/4, 30, Math.PI / 4};
		// double[] config2 = {460, 80, 60, .1, 70, .2, 30, Math.PI / 2};
		double[] config1 = {10, 20, 20, Math.PI/4, 40, Math.PI/4, 20, Math.PI / 4, 20, Math.PI / 3 * 5};
		double[] config2 = {520, 40, 20, .1, 40, .2, 20, Math.PI / 2, 20, Math.PI / 3 * 5};
		
		// arm.set(config2);
		
		// Plan path between two configurations;
		ArmLocalPlanner ap = new ArmLocalPlanner();

		// from config1 to config2
		ArmPlanner planner = new ArmPlanner(config1, config2, w);
		planner.xRoadMap(600, 15);
		List<SearchNode> astarPath = planner.astarSearch();
		
		// get the time to move from config1 to config2;
		// double time = ap.moveInParallel(config1, config2);
		// System.out.println(time);
		
		
		// boolean result;
		// result = w.armCollisionPath(arm, config1, config2);
		// System.out.println(result);
		// plot robot arm 

		// for (SearchNode node: planner.roadMap.keySet()) {
		// 	plotArmRobot(g, arm, ((ConfigNode)node).armConfig.config, Color.BLUE, Color.LIGHTBLUE);
		// }
		// after finding the path, plot each arm
		int id = 0;
		if (astarPath == null) {
			plotArmRobot(g, arm, config1, Color.BEIGE, Color.YELLOW);
			plotArmRobot(g, arm, config2, Color.BEIGE, Color.YELLOW);
			System.out.println("No path found");
		} else {
			for (SearchNode node: astarPath) {
				ConfigNode cn = (ConfigNode)node;

				System.out.println(cn.armConfig.toString());
				if (id == 0 || id == astarPath.size() - 1) {
					// plotArmRobot(g, arm, cn.armConfig.config, Color.BEIGE, Color.YELLOW);
				} else {
					plotArmRobot(g, arm, cn.armConfig.config, Color.BLUE, Color.rgb(id * 7 % 255,177,id * 7 % 255));
				}
				id++;
			}
		}  
		plotArmRobot(g, arm, config1, Color.BROWN, Color.YELLOW);
		plotArmRobot(g, arm, config2, Color.BROWN, Color.YELLOW);

	    scene.setRoot(g);
	    primaryStage.show();
		

	}
	
	
	public static void main(String[] args) {
		launch(args);
	}
}
