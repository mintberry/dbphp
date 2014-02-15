/*
 * ServerGame.java created on Dec 31, 2004 by devin.
 *
 */

package chai;

import chesspresso.position.Position;

class ServerGame {
	private Position position;
	private PlayerHandler whitePlayerHandler;
	private PlayerHandler blackPlayerHandler;

	public ServerGame(PlayerHandler wp, PlayerHandler bp) {
		position = new Position();
		whitePlayerHandler = wp;
		blackPlayerHandler = bp;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public PlayerHandler otherPlayer(PlayerHandler p) {
		if(p == whitePlayerHandler) return blackPlayerHandler;
		return whitePlayerHandler;
	}
	
	//public boolean move(int clientColor, int move) {
		//return boardState.move(clientColor, move);
	//}

}