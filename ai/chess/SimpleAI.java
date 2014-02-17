package chai;

import java.util.Random;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.lang.Math;
import java.util.Arrays;

import chesspresso.Chess;
import chesspresso.position.Position;
import chesspresso.move.IllegalMoveException;

public class SimpleAI implements ChessAI {
    private static final int depthLimit = 5;// 6 for pruning, otherwise 4
    private static final double CHESS_MAX = 10000.0;
    private static final double CHESS_MIN = -10000.0;    
    private static final double CHESS_WIN = CHESS_MAX / 3;
    private static final double CHESS_LOSE = CHESS_MIN / 3;

    private HashMap<Long, Decision> transpositionTable;// back chain with this hashmap
    private List<Short> killerMoves;// stores the path from previous search
    private int explored;

    protected int player; // the player always wants max

    public SimpleAI(){
        transpositionTable = new HashMap<Long, Decision>();
    }

    public short getMove(Position position){
        // short [] moves = position.getAllMoves();
        // short move = moves[new Random().nextInt(moves.length)];

        int depth = 1;
        explored = 0;
        short bestMove;
        player = position.getToPlay();
        Decision dec = new Decision();

        while(depth <= depthLimit) {

            // System.out.println("SimpleAI depth: " + String.valueOf(depth));
            killerMoves = forwardchain(position);
            transpositionTable.clear();

            try{
                dec = maxVal_p(position, CHESS_MIN, CHESS_MAX, 0, depth);
                // dec = maxVal(position, 0, depth);

                if (dec.value == CHESS_WIN) {// if a move results in a mate
                    break;
                }
            } catch (IllegalMoveException e) {
                System.out.println("SimpleAI: illegal move!");
            }
            depth++;
            if (dec.move == 0) {
                System.out.println("SimpleAI: failed to decide! " + String.valueOf(depth));
            }
        }
        // System.out.println("material: " + String.valueOf(position.getMaterial() + ", domination: " + String.valueOf(position.getDomination())));
        System.out.println("nodes explored: " + String.valueOf(explored));
        return dec.move;// check 0
    }

    private Decision maxVal(Position position, int curDepth, int maxDepth) throws IllegalMoveException{
        double mv = CHESS_MIN;
        short move = 0;
        explored++;
        if (curDepth >= maxDepth) {// base case 1
            // return utility value of current position
            mv = utility(position);
            // System.out.println("SimpleAI: " + String.valueOf(move));
            // mv = evaluation(position);
        } else {
            if (position.isTerminal()) {// base case 2
                mv = utility(position);
            } else { // recursive case
                short [] moves = position.getAllMoves();
                Decision temp;
                for (int i = 0; i < moves.length; ++i) {
                    Position newPos = new Position(position);
                    newPos.doMove(moves[i]);
                    temp = minVal(newPos, curDepth + 1, maxDepth);
                    if (mv < temp.value) {
                        mv = temp.value;
                        move = moves[i];
                    }
                }
            }
        }

        return new Decision(move, mv);
    }

    private Decision minVal(Position position, int curDepth, int maxDepth) throws IllegalMoveException{
        double mv = CHESS_MAX;
        short move = 0;
        explored++;
        if (curDepth >= maxDepth) {// base case 1
            // return utility value of current position
            mv = utility(position);
            // mv = evaluation(position);
        } else {
            if (position.isTerminal()) {// base case 2
                mv = utility(position);
            } else { // recursive case
                short [] moves = position.getAllMoves();
                Decision temp;
                for (int i = 0; i < moves.length; ++i) {
                    Position newPos = new Position(position);
                    newPos.doMove(moves[i]);
                    temp = maxVal(newPos, curDepth + 1, maxDepth);
                    if (mv > temp.value) {
                        mv = temp.value;
                        move = moves[i];
                    }
                }
            }
        }
        return new Decision(move, mv);
    }

    // pruning methods for minimax
    private Decision maxVal_p(Position position, double alpha, double beta, int curDepth, int maxDepth) throws IllegalMoveException{
        double mv = CHESS_MIN;
        short move = 0;
        explored++;
        
        if (curDepth >= maxDepth) {// base case 1, cut off
            // return utility value of current position
            mv = utility(position);

            // mv = evaluation(position);
        } else {
            if (position.isTerminal()) {// base case 2
                mv = utility(position);
            } else { // recursive case
                short [] moves = position.getAllMoves();

                short swap = moves[0];
                // swap the previous best move to first if there is one
                // if (curDepth < killerMoves.size() && 0 <= Arrays.binarySearch(moves, killerMoves.get(curDepth).shortValue())) {
                //     moves[0] = killerMoves.get(curDepth).shortValue();
                // }

                Decision temp;
                for (int i = 0; i < moves.length; ++i) {

                    if (moves[i] == moves[0] && i != 0) {
                        moves[i] = swap;
                    }

                    Position newPos = new Position(position);
                    newPos.doMove(moves[i]);
                    if (transpositionTable.containsKey(newPos.getHashCode())) {
                        temp = transpositionTable.get(newPos.getHashCode());
                    } else {
                        temp = minVal_p(newPos, alpha, beta, curDepth + 1, maxDepth);
                    }
                    if (mv < temp.value) {// only do this if mv is updated
                        mv = temp.value;
                        move = moves[i];

                        if (mv >= beta) {
                            break;
                        }
                        alpha = Math.max(alpha, mv);
                    }
                }
                // update transposition table before return, only for non-terminal or cutoffs
                transpositionTable.put(position.getHashCode(), new Decision(move, mv));
            }
        }

        return new Decision(move, mv);
    }

    private Decision minVal_p(Position position, double alpha, double beta, int curDepth, int maxDepth) throws IllegalMoveException{
        double mv = CHESS_MAX;
        short move = 0;
        explored++;
        if (curDepth >= maxDepth) {// base case 1, cut off
            // return utility value of current position
            mv = utility(position);

            // mv = evaluation(position);
        } else {
            if (position.isTerminal()) {// base case 2
                mv = utility(position);
            } else { // recursive case
                short [] moves = position.getAllMoves();
                
                short swap = moves[0];
                // swap the previous best move to first if there is one
                // if (curDepth < killerMoves.size() && 0 <= Arrays.binarySearch(moves, killerMoves.get(curDepth).shortValue())) {
                //     moves[0] = killerMoves.get(curDepth).shortValue();  
                // }

                Decision temp;
                for (int i = 0; i < moves.length; ++i) {

                    if (moves[i] == moves[0] && i != 0) {
                        moves[i] = swap;
                    }

                    Position newPos = new Position(position);
                    newPos.doMove(moves[i]);
                    if (transpositionTable.containsKey(newPos.getHashCode())) {
                        temp = transpositionTable.get(newPos.getHashCode());
                    } else {
                        temp = maxVal_p(newPos, alpha, beta, curDepth + 1, maxDepth);
                    }
                    if (mv > temp.value) {// only do this if mv is updated
                        mv = temp.value;
                        move = moves[i];

                        if (mv <= alpha) {
                            break;
                        }
                        beta = Math.min(beta, mv);
                    }
                }
                // update transposition table before return, only for non-terminal or cutoffs
                transpositionTable.put(position.getHashCode(), new Decision(move, mv));
            }
        }

        return new Decision(move, mv);
    }

    private double utility(Position position){
        // return actual value for terminals
        // and random value for non-terminals
        double uv = 0.0;// default for draw
        if (!position.isTerminal()){// non-terminal
            // uv = (Math.random() * 2 - 1) * CHESS_WIN;
            uv = evaluation(position);
        } else if (!isDraw(position)) {// win
            uv = (this.player == position.getToPlay()? CHESS_LOSE: CHESS_WIN);// depends on player
        }
        return uv;
    }

    private double evaluation(Position position){
        // return heuristic value for non-terminals
        // use either domination or material
        double eval = position.getDomination() * (player == position.getToPlay()?1:-1);// + white, - black
        // what's the max domination value?
        return eval;
    }

    private boolean isDraw(Position position){
        // just stalemate and 50-move for now
        // FIXME
        return position.isStaleMate() || position.getHalfMoveClock() >= 100;
    }

    private List<Short> forwardchain(Position position){
        LinkedList<Short> chain = new LinkedList<Short>();
        Position pos = new Position(position);
        while(transpositionTable.containsKey(pos.getHashCode())){
            Short move = new Short(transpositionTable.get(pos.getHashCode()).move);
            chain.add(move);

            try{
                pos.doMove(move.shortValue());
            } catch (IllegalMoveException e) {
                System.out.println("SimpleAI forwardchain: illegal move!");
            }
            // System.out.println("chaining: " + String.valueOf(chain.size()) + " " + String.valueOf(move.shortValue()));
        }
        return chain;
    }

    private class Decision{
        protected short move;
        protected double value;

        public Decision(){
            move = 0;
            value = 0;
        }

        public Decision(short move, double value){
            this.move = move;
            this.value = value;
        }
    }
}
