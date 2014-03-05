package probabilistic;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.lang.Math;

import java.awt.Point;

public class PRSolver{
    private static final double colorProb = 0.88;
    private static int actions[][] = {Maze.NORTH, Maze.EAST, Maze.SOUTH, Maze.WEST}; 

    private Maze maze;// a reference to maze
    private String colors;
    private HashMap<StateVar, Distribution<KeyPair<StateVar, StateVar>>> transitionTable;
    private HashMap<Character, Distribution<KeyPair<Character, Character>>> emissionTable;// universal emission table

    private Distribution<StateVar> initD;

    public PRSolver(Maze maze, String colors){
        this.maze = maze;
        this.colors = colors;
        initTables();
    }

    public List<StateVar> viterbi(String observations){
        List<Distribution<StateVar>> table = filtering(observations);
        List<StateVar> bestSeq = new ArrayList<StateVar>(observations.length());
        Distribution<StateVar> lastD = table.get(table.size() - 1);// last distribution
        StateVar sv = lastD.mostPossible();
        // do{
        //     bestSeq.add(0, sv);
        //     System.out.println("state: " + sv.position + " " + sv.color);
        //     sv = sv.lastState;
        // } while (sv != null);// should not include initD

        do{
            bestSeq.add(0, sv);
            // System.out.println("state: " + sv);
            sv = sv.lastState;
        } while (sv.lastState != null);// should not include initD

        return bestSeq;
    }

    // observation is a list of chars
    public List<Distribution<StateVar>> filtering(String observations){
        List<Distribution<StateVar>> ret = new LinkedList<Distribution<StateVar>>();
        Distribution<StateVar> distribution = initD;
        int i = 0;
        for (char c: observations.toCharArray()) {
            distribution = filterAt(distribution, c);
            ret.add(distribution);
            // System.out.println("time " + ++i + " " + c);
            // distribution.printDist();
        }
        return ret;
    }

    // initialize transition and emission tables
    private void initTables(){
        transitionTable = new HashMap<StateVar, Distribution<KeyPair<StateVar, StateVar>>>();
        emissionTable = new HashMap<Character, Distribution<KeyPair<Character, Character>>>();
        initD = new Distribution<StateVar>();
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

                    initD.updateSub(svs, 1.0);// each state is of equal prob at start

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

        initD.normalize();
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

        for (StateVar sv_init: initD.keySet()) {
            StateVar sv = new StateVar(sv_init);// must create a new state

            Distribution<KeyPair<Character, Character>> colorTable = emissionTable.get(new Character(sv.color));

            double obsProb, transitionProb = 0.0;
            obsProb = colorTable.getProb(new KeyPair<Character, Character>(new Character(sv.color), new Character(obs)));
            // System.out.println("obs prob: " +  obsProb);

            double viterbiProb, bestProb = Double.MIN_VALUE;

            for (StateVar svs: lastD.keySet()) {
                Distribution<KeyPair<StateVar, StateVar>> posTable = transitionTable.get(svs);
                transitionProb += (posTable.getProb(new KeyPair<StateVar, StateVar>(svs, sv)) * lastD.getProb(svs));

                // for viterbi, need to sort this?
                viterbiProb = posTable.getProb(new KeyPair<StateVar, StateVar>(svs, sv)) * lastD.getProb(svs);
                if (bestProb < viterbiProb) {
                    bestProb = viterbiProb;
                    sv.lastState = svs;
                }

            }
            // System.out.println("transition prob: " +  transitionProb);
            ret.updateSub(sv, obsProb * transitionProb);
        }
        ret.normalize();
        return ret;
    }

    // state variable instance
    public class StateVar{
        protected Point position;
        protected char color;// just use a char to represent a color

        protected StateVar lastState;// for viterbi last state

        // each state has an emission table, or may use an universal emission table
        // private Distribution<KeyPair<Character, Character>> emissionTable;

        public StateVar(int x, int y, char color){
            this.position = new Point(x, y);
            this.color = color;
            this.lastState = null;
        }

        public StateVar(StateVar sv){// not called yet
            this.position = new Point(sv.position);
            this.color = sv.color;
            this.lastState = null;
        }

        @Override
        public boolean equals(Object obj){
            return position.equals(((StateVar)obj).position);
        }

        @Override
        public int hashCode(){
            return position.hashCode();
        }

        @Override
        public String toString(){
            String str = new String();
            str += String.format("(%d,%d) ", position.x, position.y);
            str += color;
            return str;
        }
    }

    protected class Distribution<T>{
        protected HashMap<T, Double> distribution;
        protected double sum;

        public Distribution(){
            distribution = new HashMap<T, Double>();
            sum = 0.0;
        }

        public void updateSub(T key, Double prob){
            if (distribution.containsKey(key)) {
                sum -= distribution.get(key);
            }
            distribution.put(key, prob);
            sum += prob;
        }

        public void updateAdd(T key, Double prob){
            double newProb = prob;
            if (distribution.containsKey(key)) {
                newProb = distribution.get(key) + prob;
            }
            distribution.put(key, newProb);
            sum += prob;
        }

        public void normalize(){
            for (Map.Entry<T, Double> entry: distribution.entrySet()) {
                distribution.put(entry.getKey(), entry.getValue() / sum);
            }
        }

        public double getProb(T key){
            if (distribution.containsKey(key)) {
                return distribution.get(key).doubleValue();    
            }
            return 0.0;// if key is not in distribution, return 0
        }

        public List<T> keySet(){
            return new ArrayList<T>(distribution.keySet());
        }

        public T mostPossible(){
            return Collections.max(this.distribution.keySet(), new Comparator<T>(){
            public int compare(T k1, T k2) {
                //ascending order
                return getProb(k1) >= getProb(k2) ? 1 : -1;
            }
            });
        }

        public void printDist(){
            System.out.println("distribution has " + distribution.size() + " entries");
            for (T key: distribution.keySet()) {
                System.out.println(String.format("%s: %s", key, distribution.get(key)));
            }
            System.out.print("\n");
        }

        private T randomVar(double prob){
            double lb = 0.0, ub = 0.0;
            List<T> keySet = this.keySet();
            T cur = null;
            for (int i = 0; i < keySet.size(); ++i) {
                cur = keySet.get(i);
                lb = ub;
                ub += distribution.get(cur).doubleValue();
                if (prob >= lb && prob < ub) {
                    return cur;
                }
            }
            return cur;
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

    // below this are test methods
    ///////////////////////////////


    public void autotest(int limit, int len){
        double correct = 0;

        for (int i = 0; i < limit; ++i) {
            List<StateVar> actual = randomPath(len);
            String obs = randomObs(actual);
            List<StateVar> result = viterbi(obs);
            correct += pathCompare(actual, result);
        }

        System.out.println("correct rate: " + correct / limit);
    }

    private List<StateVar> randomPath(int len){
        List<StateVar> ret = new ArrayList<StateVar>(len);
        List<StateVar> states = initD.keySet();
        StateVar cur = states.get((int)(Math.random() * (states.size() - 1)));// random start state
        ret.add(cur);
        for (int i = 1; i < len; ++i) {
            double rand = Math.random();
            StateVar sv = transitionTable.get(cur).randomVar(rand).dst;
            cur = sv;
            ret.add(cur);
        }
        return ret;
    }

    private String randomObs(List<StateVar> path){
        String ret = new String();
        for (StateVar sv: path) {
            double rand = Math.random();
            Character c = emissionTable.get(new Character(sv.color)).randomVar(rand).dst;
            ret += c.charValue();
        }
        return ret;
    }

    private double pathCompare(List<StateVar> actual, List<StateVar> result){
        int len = actual.size();
        double ret = 0.0;
        for (int i = 0; i < len; ++i) {
            if (actual.get(i).equals(result.get(i))) {
                ret += 1.0;
            }
        }
        return ret / len;
    }
}