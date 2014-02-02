package assignment_robots;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.shape.Polygon;
import javafx.scene.Group;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class CarDriver extends Application {
	// default window size
	protected static int window_width = 600;
	protected static int window_height = 400;
	
	// Draw a polygon;
	public void addPolygon(Group g, Double[] points) {
		Polygon p = new Polygon();
	    p.getPoints().addAll(points);
	    
	    g.getChildren().add(p);
	}
	
	// plot a car robot
	public void plotCarRobot(Group g, CarRobot car, CarState s, Paint stroke, Paint fill) {
		//System.out.println(car);
		//System.out.println(s);
		car.set(s);
		double[][] current = car.get();
		Double[] to_add = new Double[2*current.length];
		for (int j = 0; j < current.length; j++) {
			// System.out.println(current[j][0] + ", " + current[j][1]);
			to_add[2*j] = current[j][0];
			//to_add[2*j+1] = current[j][1];
			to_add[2*j+1] = window_height - current[j][1];
		}
		Polygon p = new Polygon();
		p.getPoints().addAll(to_add);
		
		p.setStroke(stroke);
		p.setFill(fill);
		g.getChildren().add(p);
	}
		
	// plot the World with all the obstacles;
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
		
		
	
		primaryStage.setTitle("CS 76 2D world");
		Group root = new Group();
		Scene scene = new Scene(root, window_width, window_height);

		primaryStage.setScene(scene);
		
		Group g = new Group();
		
		double a[][] = {{10, 400}, {150, 300}, {100, 210}};
		Poly obstacle1 = new Poly(a);
		
		// double b[][] = {{350, 30}, {300, 200}, {430, 125}};
		double b[][] = {{400, 30}, {350, 200}, {480, 125}};

		Poly obstacle2 = new Poly(b);

		
		double c[][] = {{110, 220}, {250, 380}, {320, 220}};
		Poly obstacle3 = new Poly(c);
		
		double d[][] = {{0, 50}, {250, 50}, {250, 0}, {0, 0}};
		Poly obstacle4 = new Poly(d);
		
		double e[][] = {{300, 30}, {500, 30}, {500, 0}, {300, 0}};
		Poly obstacle5 = new Poly(e);
		
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
		w.addObstacle(obstacle4);
		w.addObstacle(obstacle5);

		w.addObstacle(edge1);
		w.addObstacle(edge2);
		w.addObstacle(edge3);
		w.addObstacle(edge4);
			
		plotWorld(g, w);
		
		CarRobot car = new CarRobot();
		
		CarState state1 = new CarState(270, 15, 0);
		CarState state2 = new CarState(570, 215, 0);
	    // Set CarState;
		// car.set(state1);
		
		// boolean collided = w.carCollisionPath(car, state1, 0, 1.2);
	 //    System.out.println(collided);
		// plotCarRobot(g, car, state1);
		CarPlanner cp = new CarPlanner(state1, state2, w);

		cp.xRRT(900, 3.0, 0.0);
		for (CarState cs: cp.rrt.keySet()) {
			plotCarRobot(g, car, cs, Color.RED, Color.PINK);
		}
		plotCarRobot(g, car, state1, Color.MAROON, Color.SALMON);
		plotCarRobot(g, car, state2, Color.MAROON, Color.SALMON);
		System.out.println(cp.rrt.keySet().size());
		
	    scene.setRoot(g);
	    primaryStage.show();
		
	}
	public static void main(String[] args) {
		launch(args);
	}
}
