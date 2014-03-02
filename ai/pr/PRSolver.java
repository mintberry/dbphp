package probabilistic;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;

import java.awt.Point;

public class PRSolver{
    private static final double colorProb = 0.88;
    private static int actions[][] = {Maze.NORTH, Maze.EAST, Maze.SOUTH, Maze.WEST}; 

    private Maze maze;// a reference to maze
    private String colors;
    private HashMap<StateVar, Distribution<KeyPair<StateVar, StateVar>>> transitionTable;
    private HashMap<Character, Distribution<KeyPair<Character, Character>>> emissionTable;// universal emission table

    public PRSolver(Maze maze, String colors){
        this.maze = maze;
        this.colors = colors;
        initTables();
    }


    // observation is a list of chars
    public List<Distribution<StateVar>> filtering(String observations){
        List<Distribution<StateVar>> ret = new LinkedList<Distribution<StateVar>>();
        Distribution<StateVar> distribution = null;// should not be null
        for (char c: observations.toCharArray()) {
            distribution = filterAt(distribution, c);
            ret.add(distribution);
        }
        return ret;
    }

    // initialize transition and emission tables
    private void initTables(){
        transitionTable = new HashMap<StateVar, Distribution<KeyPair<StateVar, StateVar>>>();
        emissionTable = new HashMap<Character, Distribution<KeyPair<Character, Character>>>();
        // build the emission table
        int colorCount = this.colors.length();
        for (char cs: this.colors.toCharArray()) {
            Distribution<KeyPair<Character, Character>> colorTable = new Distribution<KeyPair<Character, Character>>();
            for (char cd: this.colors.toCharArray()) {
                if(cs == cd){
                    colorTable.updateSub(new KeyPair<Character, Character>(new Character(cs), new Character(cd)), colorProb);
                } else {
                    double prob = (1.0 - colorProb) / (colorCount - 1);
                    colorTable.updateSub(new KeyPair<Character, Character>(new Character(cs), new Character(cd)), prob);
                } // already normalized
            }
            emissionTable.put(new Character(cs), colorTable);
        }

        // build transition table
        char[] asciiMaze = maze.asciiMaze().toCharArray();
        for (int y = 0; y < maze.height; y++) {
            for (int x = 0; x < maze.width; x++) {
                StateVar svs = getStateVar(x, y, asciiMaze);
                // list possible transitions
                if (svs != null) {// sv is not wall
                    Distribution<KeyPair<StateVar, StateVar>> posTable = new Distribution<KeyPair<StateVar, StateVar>>();

                    for (int[] action: actions) {
                        int xNew = x + action[0];
                        int yNew = y + action[1]; 
                        
                        if(maze.isLegal(xNew, yNew)) {
                            StateVar svd = getStateVar(xNew, yNew, asciiMaze);
                            posTable.updateAdd(new KeyPair<StateVar, StateVar>(svs, svd), 1.0);
                        } else {
                            posTable.updateAdd(new KeyPair<StateVar, StateVar>(svs, svs), 1.0);
                        }
                        
                    } 
                    posTable.normalize();
                    transitionTable.put(svs, posTable);
                }

            }
        }
    }

    private StateVar getStateVar(int x, int y, char[] asciiMaze){
        int n = y * maze.width + x;
        char color = asciiMaze[n];
        if (maze.isLegal(x, y)) {
            StateVar sv = new StateVar(x, y, color);
            return sv;
        }
        return null;
    }

    // do filtering with last distribution and next observation
    private Distribution<StateVar> filterAt(Distribution<StateVar> lastD, char obs){
        // new a distribution
        Distribution<StateVar> ret = new Distribution<StateVar>();

        return ret;
    }

    // state variable instance
    protected class StateVar{
        protected Point position;
        protected char color;// just use a char to represent a color

        // each state has an emission table, or may use an universal emission table
        // private Distribution<KeyPair<Character, Character>> emissionTable;

        public StateVar(int x, int y, char color){
            this.position = new Point(x, y);
            this.color = color;
        }

        public StateVar(StateVar sv){
            this.position = new Point(sv.position);
            this.color = sv.color;
        }

        @Override
        public boolean equals(Object obj){
            return position.equals(((StateVar)obj).position);
        }

        @Override
        public int hashCode(){
            return position.hashCode();
        }
    }

    protected class Distribution<T>{
        protected HashMap<T, Double> distribution;
        protected double sum;

        public Distribution(){
            distribution = new HashMap<T, Double>();
            sum = 0.0;
        }

        public void updateSub(T key, Double value){
            if (distribution.containsKey(key)) {
                sum -= distribution.get(key);
            }
            distribution.put(key, value);
            sum += value;
        }

        public void updateAdd(T key, Double value){
            double newVal = value;
            if (distribution.containsKey(key)) {
                newVal = distribution.get(key) + value;
            }
            distribution.put(key, newVal);
            sum += value;
        }

        public void normalize(){
            for (Map.Entry<T, Double> entry: distribution.entrySet()) {
                distribution.put(entry.getKey(), entry.getValue() / sum);
            }
        }

        public double getProb(T key){
            return distribution.get(key).doubleValue();
        }
    }

    // a pair, src to dst, used for trasition and emission
    protected class KeyPair<S, D>{
        protected S src;
        protected D dst;// can be null

        public KeyPair(S s, D d){
            this.src = s;
            this.dst = d;
        }

        @Override
        public boolean equals(Object obj){
            @SuppressWarnings("unchecked")
            KeyPair<S, D> kp = (KeyPair<S, D>)obj;
            return src.equals(kp.src) && (dst == null || dst.equals(kp.dst));
        }

        @Override
        public int hashCode(){
            return src.hashCode() * 1000 + dst.hashCode();
        }
    }
}