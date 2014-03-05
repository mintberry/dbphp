package probabilistic;

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

import probabilistic.PRSolver.*;

public class PRDriver extends Application {

    Maze maze;

    // instance variables used for graphical display
    private static final int PIXELS_PER_SQUARE = 50;
    MazeView mazeView;
    List<AnimationPath> animationPathList;
    
    // some basic initialization of the graphics; needs to be done before 
    //  runSearches, so that the mazeView is available
    private void initMazeView() {
        maze = Maze.readFromFile("5x5.maz");
        
        animationPathList = new ArrayList<AnimationPath>();
        // build the board
        mazeView = new MazeView(maze, PIXELS_PER_SQUARE);
        
    }

    public static void main(String[] args) {
        launch(args);
    }

    // assumes maze and mazeView instance variables are already available
    private void reasoning() {

        // List<SearchNode> astarPath = mazeProblem.astarSearch();
        // animationPathList.add(new AnimationPath(mazeView, astarPath));
        // System.out.println("A*:  ");
        // mazeProblem.printStats();
        PRSolver solver = new PRSolver(maze, "rgby");
        // List<StateVar> path = solver.viterbi("bgyrrbggy");
        // animationPathList.add(new AnimationPath(mazeView, path));
        solver.autotest(10000, 5);
    }

    // javafx setup of main view window for mazeworld
    @Override
    public void start(Stage primaryStage) {
        
        initMazeView();
    
        primaryStage.setTitle("CS 76 probabilistic reasoning");

        // add everything to a root stackpane, and then to the main window
        StackPane root = new StackPane();
        root.getChildren().add(mazeView);
        primaryStage.setScene(new Scene(root));

        primaryStage.show();

        // do the real work of the driver; run search tests
        reasoning();

        // sets mazeworld's game loop (a javafx Timeline)
        Timeline timeline = new Timeline(0.2);
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
        private Node piece;
        private List<StateVar> searchPath;
        private int currentMove = 0;

        private int lastX;
        private int lastY;

        boolean animationDone = true;

        public AnimationPath(MazeView mazeView, List<StateVar> path) {
            searchPath = path;
            StateVar firstNode = searchPath.get(0);
            piece = mazeView.addPiece(firstNode.position.x, firstNode.position.y);
            lastX = firstNode.position.x;
            lastY = firstNode.position.y;
        }

        // try to do the next step of the animation. Do nothing if
        // the mazeView is not ready for another step.
        public void doNextMove() {

            // animationDone is an instance variable that is updated
            //  using a callback triggered when the current animation
            //  is complete
            if (currentMove < searchPath.size() && animationDone) {
                StateVar mazeNode = searchPath
                        .get(currentMove);
                int dx = mazeNode.position.x - lastX;
                int dy = mazeNode.position.y - lastY;
                // System.out.println("animating " + dx + " " + dy);
                animateMove(piece, dx, dy);
                lastX = mazeNode.position.x;
                lastY = mazeNode.position.y;

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