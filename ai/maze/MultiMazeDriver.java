package assignment_mazeworld;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import assignment_mazeworld.SearchProblem.SearchNode;
import assignment_mazeworld.MultiMazeProblem.MultiMazeNode;

public class MultiMazeDriver extends Application {

	Maze maze;
	
	// instance variables used for graphical display
	private static final int PIXELS_PER_SQUARE = 10;
	MazeView mazeView;
	List<AnimationPath> animationPathList;
	
	// some basic initialization of the graphics; needs to be done before 
	//  runSearches, so that the mazeView is available
	private void initMazeView() {
		maze = Maze.readFromFile("complex.maz");
		
		animationPathList = new ArrayList<AnimationPath>();
		// build the board
		mazeView = new MazeView(maze, PIXELS_PER_SQUARE);
		
	}
	
	// assumes maze and mazeView instance variables are already available
	private void runSearches() {
		
		int[] sx = {0};
		int[] sy = {0};
		int[] gx = {39};
		int[] gy = {39};

		// int[] sx = {0,0,0,0,0};
		// int[] sy = {0,1,2,3,4};
		// int[] gx = {8,7,6,7,6};
		// int[] gy = {0,0,0,1,1};

		// int[] sx = {2,1,1,2,0,2,0,0};
		// int[] sy = {0,2,0,2,1,1,2,0};
		// int[] gx = {1,2,0,1,2,0,1,2};
		// int[] gy = {2,2,1,1,1,0,0,0};

		MultiMazeProblem mazeProblem = new MultiMazeProblem(maze, sx, sy, gx,
				gy, sx.length);

		// List<SearchNode> bfsPath = mazeProblem.breadthFirstSearch();
		// animationPathList.add(new AnimationPath(mazeView, bfsPath));
		// System.out.println("DFS:  ");
		// mazeProblem.printStats();

		// List<SearchNode> dfsPath = mazeProblem
		// 		.depthFirstPathCheckingSearch(5000);
		// animationPathList.add(new AnimationPath(mazeView, dfsPath));
		// System.out.println("BFS:  ");
		// mazeProblem.printStats();

		List<SearchNode> astarPath = mazeProblem.astarSearch2();
		animationPathList.add(new AnimationPath(mazeView, astarPath, sx.length));
		System.out.println("A*:  ");
		mazeProblem.printStats();

	}


	public static void main(String[] args) {
		launch(args);
	}

	// javafx setup of main view window for mazeworld
	@Override
	public void start(Stage primaryStage) {
		
		initMazeView();
	
		primaryStage.setTitle("CS 76 Mazeworld");

		// add everything to a root stackpane, and then to the main window
		StackPane root = new StackPane();
		root.getChildren().add(mazeView);
		primaryStage.setScene(new Scene(root));

		primaryStage.show();

		// do the real work of the driver; run search tests
		runSearches();

		// sets mazeworld's game loop (a javafx Timeline)
		Timeline timeline = new Timeline(1.0);
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(
				new KeyFrame(Duration.seconds(.05), new GameHandler()));
		timeline.playFromStart();

	}

	// every frame, this method gets called and tries to do the next move
	//  for each animationPath.
	private class GameHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent e) {
			// System.out.println("timer fired");
			for (AnimationPath animationPath : animationPathList) {
				// note:  animationPath.doNextMove() does nothing if the
				//  previous animation is not complete.  If previous is complete,
				//  then a new animation of a piece is started.
				animationPath.doNextMove();
			}
		}
	}

	// each animation path needs to keep track of some information:
	// the underlying search path, the "piece" object used for animation,
	// etc.
	private class AnimationPath {
		private Node[] pieces;
		private List<SearchNode> searchPath;
		private int currentMove = 0;

		private int lastX[];
		private int lastY[];

		// paths for multirobot
		private int k;// number of robots

		boolean animationDone = true;

		public AnimationPath(MazeView mazeView, List<SearchNode> path, int robots) {
			k = robots;
			pieces = new Node[k];// k robots
			lastX = new int[k];
			lastY = new int[k];

			searchPath = path;
			MultiMazeNode firstNode = (MultiMazeNode) searchPath.get(0);

			for (int i = 0; i < k; ++i) {
				// System.out.println(firstNode.getX(i) + ", " + firstNode.getY(i));
				pieces[i] = mazeView.addPiece(firstNode.getX(i), firstNode.getY(i));
				lastX[i] = firstNode.getX(i);
				lastY[i] = firstNode.getY(i);
			}
		}

		// try to do the next step of the animation. Do nothing if
		// the mazeView is not ready for another step.
		public void doNextMove() {

			// animationDone is an instance variable that is updated
			//  using a callback triggered when the current animation
			//  is complete
			if (currentMove < searchPath.size() && animationDone) {
				MultiMazeNode mazeNode = (MultiMazeNode) searchPath
						.get(currentMove);
				// find out which robot moves this time
				for (int i = 0; i < k; ++i) {
					int dx = mazeNode.getX(i) - lastX[i];
					int dy = mazeNode.getY(i) - lastY[i];
					if (dx != dy) {
						// run animation
						animateMove(pieces[i], dx, dy);
						lastX[i] = mazeNode.getX(i);
						lastY[i] = mazeNode.getY(i);
						break;
					}
				}

				currentMove++;
			}

		}

		// move the piece n by dx, dy cells
		public void animateMove(Node n, int dx, int dy) {
			animationDone = false;
			TranslateTransition tt = new TranslateTransition(
					Duration.millis(300), n);
			tt.setByX(PIXELS_PER_SQUARE * dx);
			tt.setByY(-PIXELS_PER_SQUARE * dy);
			// set a callback to trigger when animation is finished
			tt.setOnFinished(new AnimationFinished());

			tt.play();

		}

		// when the animation is finished, set an instance variable flag
		//  that is used to see if the path is ready for the next step in the
		//  animation
		private class AnimationFinished implements EventHandler<ActionEvent> {
			@Override
			public void handle(ActionEvent event) {
				animationDone = true;
			}
		}
	}
}