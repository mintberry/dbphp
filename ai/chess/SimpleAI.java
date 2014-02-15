package chai;

import java.util.Random;
import java.util.Collections;
import java.lang.Math;

import chesspresso.position.Position;
import chesspresso.move.IllegalMoveException;

public class SimpleAI implements ChessAI {
    private static final int depthLimit = 3;
    private static final double CHESS_MAX = 10000.0;
    private static final double CHESS_MIN = -10000.0;    
    private static final double CHESS_WIN = CHESS_MAX / 3;
    private static final double CHESS_LOSE = CHESS_MIN / 3;

    protected int player; // the player always wants max

    public short getMove(Position position){
        // short [] moves = position.getAllMoves();
        // short move = moves[new Random().nextInt(moves.length)];

        int depth = 1;
        short bestMove;
        player = position.getToPlay();
        Decision dec = new Decision();

        while(depth <= depthLimit) {
            try{
                dec = maxVal(position, 0, depth);
            } catch (IllegalMoveException e) {
                System.out.println("SimpleAI: illegal move!");
            }
            depth++;

            if (dec.move == 0) {
                System.out.println("SimpleAI: failed to decide! " + String.valueOf(depth));
            }
        }
        
        return dec.move;// check 0
    }

    private Decision maxVal(Position position, int curDepth, int maxDepth) throws IllegalMoveException{
        double mv = CHESS_MIN;
        short move = 0;
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
                        move = temp.move;
                    }
                }
            }
        }
        return new Decision(move, mv);
    }

    private double utility(Position position){
        // return actual value for terminals
        // and random value for non-terminals
        double uv = 0.0;// default for draw
        if (!position.isTerminal()){// non-terminal
            uv = (Math.random() * 2 - 1) * CHESS_WIN;
        } else if (!isDraw(position)) {// win
            uv = (this.player == position.getToPlay()? CHESS_LOSE: CHESS_WIN);// depends on player
        }
        return uv;
    }

    private double evaluation(Position position){
        // return heuristic value for non-terminals
        return 0.0;
    }

    private boolean isDraw(Position position){
        // just stalemate and 50-move for now
        return position.isStaleMate() || position.getHalfMoveClock() >= 100;
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
