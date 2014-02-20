package chai;

import java.util.Random;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.lang.Math;
import java.util.Arrays;
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.util.Timer;
import java.util.Collections;

import chesspresso.Chess;
import chesspresso.game.Game;
import chesspresso.pgn.*;
import chesspresso.position.Position;
import chesspresso.move.IllegalMoveException;

public class SimpleAI implements ChessAI {
    // private static final int depthLimit = 5;// 6 for pruning, otherwise 4
    private int depthLimit;
    private static final double CHESS_MAX = 100000.0;
    private static final double CHESS_MIN = -100000.0;    
    private static final double CHESS_WIN = CHESS_MAX / 3;
    private static final double CHESS_LOSE = CHESS_MIN / 3;
    private static final int TEXT_MOVE = 6;

    private short bestMove;
    private HashMap<Long, Decision> transpositionTable;// back chain with this hashmap
    private List<Short> killerMoves;// stores the path from previous search
    private int chainCounter;

    private List<Game> playBook;

    private int explored;

    protected int player; // the player always wants max

    public SimpleAI(){
        transpositionTable = new HashMap<Long, Decision>();
        playBook = new LinkedList<Game>();
        depthLimit = 5;
        try{
            readBook();
        } catch (Exception e){
            System.out.println("Read book error: " + e.toString());
            return;
        }
    }

    public SimpleAI(int depth){
        this();
        depthLimit = depth;
    }

    private void readBook() throws Exception{
        URL url = this.getClass().getResource("book.pgn");

        File f = new File(url.toURI());
        FileInputStream fis = new FileInputStream(f);            
        PGNReader pgnReader = new PGNReader(fis, "book.pgn");

        //hack: we know there are only 120 games in the opening book
        for (int i = 0; i < 120; i++)  {
          Game g = new Game(pgnReader.parseGame().getModel());
          playBook.add(g);
        }
    }

    private short applyBook(Position position){
        short move = 0;
        // System.out.println("pos: " + playBook.size());
        Collections.shuffle(playBook, new Random(System.currentTimeMillis()));
        for (Game g: playBook) {
            if (g.containsPosition(position)) {// if current position is found in this book
                g.gotoPosition(position);
                System.out.println("move from book " + g.getNextShortMove());
                move = g.getNextShortMove();
                break;
            }
        }
        // there can be no match
        return move;
    }

    public short getMove(Position position){
        // short [] moves = position.getAllMoves();
        // short move = moves[new Random().nextInt(moves.length)];

        int depth = 1;
        explored = 0;
        bestMove = 0;
        player = position.getToPlay();
        Decision dec = new Decision();

        // System.out.println("1material: " + String.valueOf(position.getMaterial() + ", domination: " + String.valueOf(position.getDomination())));

        if (position.getPlyNumber() <= TEXT_MOVE) {// use move in text instead of search
            bestMove = applyBook(position);
        } 
        if (bestMove == 0) {

            while(depth <= depthLimit) {

                // System.out.println("SimpleAI depth: " + String.valueOf(depth));
                // killerMoves = forwardchain(position);
                // transpositionTable.clear();

                try{
                    dec = maxVal_p(position, CHESS_MIN, CHESS_MAX, 0, depth);
                    // dec = maxVal(position, 0, depth);
                    bestMove = dec.move;
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
        }
        // Position pos = new Position(position);
        // try{pos.doMove(dec.move);}catch(IllegalMoveException e){}
        // System.out.println("2material: " + String.valueOf(pos.getMaterial() + ", domination: " + String.valueOf(pos.getDomination())));

        System.out.println("nodes explored: " + String.valueOf(explored));
        return bestMove;// check 0
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

        return new Decision(move, mv, 0);
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
        return new Decision(move, mv, 0);
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
                // if (curDepth < killerMoves.size() && chainCounter > 0) {
                //     moves[0] = killerMoves.get(curDepth).shortValue();
                //     chainCounter--;
                // }

                Decision temp;
                for (int i = 0; i < moves.length; ++i) {

                    if (moves[i] == moves[0] && i != 0) {
                        moves[i] = swap;
                    }

                    Position newPos = new Position(position);
                    newPos.doMove(moves[i]);
                    if (transpositionTable.containsKey(newPos.getHashCode()) && 
                        maxDepth - curDepth - 1 <= transpositionTable.get(newPos.getHashCode()).height) {
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
                if (!transpositionTable.containsKey(position.getHashCode()) || 
                    maxDepth - curDepth > transpositionTable.get(position.getHashCode()).height) {
                    transpositionTable.put(position.getHashCode(), new Decision(move, mv, maxDepth - curDepth));   
                }
            }
        }

        return new Decision(move, mv, maxDepth - curDepth);
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
                // if (curDepth < killerMoves.size() && chainCounter > 0) {
                //     moves[0] = killerMoves.get(curDepth).shortValue();  
                //     chainCounter--;
                // }

                Decision temp;
                for (int i = 0; i < moves.length; ++i) {

                    if (moves[i] == moves[0] && i != 0) {
                        moves[i] = swap;
                    }

                    Position newPos = new Position(position);
                    newPos.doMove(moves[i]);
                    if (transpositionTable.containsKey(newPos.getHashCode()) && 
                        maxDepth - curDepth - 1 <= transpositionTable.get(newPos.getHashCode()).height) {
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
                if (!transpositionTable.containsKey(position.getHashCode()) || 
                    maxDepth - curDepth > transpositionTable.get(position.getHashCode()).height) {
                    transpositionTable.put(position.getHashCode(), new Decision(move, mv, maxDepth - curDepth));   
                }
            }
        }

        return new Decision(move, mv, maxDepth - curDepth);
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
        double eval = (position.getDomination() + position.getMaterial())
            * (player == position.getToPlay()?1:-1);// + white, - black
        return eval;
    }

    private boolean isDraw(Position position){
        // just stalemate and 50-move for now
        // FIXME
        // return position.isTerminal() && !position.isMate();
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
        chainCounter = chain.size();
        return chain;
    }

    private class Decision{
        protected short move;
        protected double value;
        protected int height;// depth to cut off

        public Decision(){
            move = 0;
            value = 0;
            height = 0;
        }

        public Decision(short move, double value, int height){
            this.move = move;
            this.value = value;
            this.height = height;
        }
    }
}
