package chai;

import java.util.Random;

import chesspresso.position.Position;

public class RandomAI implements ChessAI {
	public short getMove(Position position) {
		short [] moves = position.getAllMoves();
		short move = moves[new Random().nextInt(moves.length)];
        System.out.println(String.valueOf(Double.POSITIVE_INFINITY) + ": " + String.valueOf(Double.POSITIVE_INFINITY / 3));
		return move;
	}
}
