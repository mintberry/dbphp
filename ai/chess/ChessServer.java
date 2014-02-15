package chai;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import chesspresso.Chess;

class ChessServer extends Thread {

	private Hashtable<String, PlayerHandler> playerHandlerHash;  // the active connections
	
	private HashSet<String> challengeSet;  // The open challenges

	private static final int PORT = 4444;
	
	public static void main(String[] args)  {
		ChessServer ls = new ChessServer();
		ls.start();
	}

	public ChessServer() {
		super();
		playerHandlerHash = new Hashtable<String, PlayerHandler>(20);
		challengeSet = new HashSet<String>(20);
		//gameVector = new Vector<ServerGame>(20);
	}
	
	public void run()  {
		
		ServerSocket serverSocket = null;
		boolean listening = true;	

		
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.err.println("Could not listen on port:  " + PORT);
		}
		
		Socket clientSocket = null;
		
		while (listening) {
			
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// To do:  the client should immediately declare the player
			//  name, so that the thread can be stored correctly in the
			//  hashtable, and so that the 'who' command doesn't return
			//  names like "Thread 1"
			
			PlayerHandler pht = new PlayerHandler(clientSocket, this);		
			pht.start();
			
			
			playerHandlerHash.put(pht.getName(), pht);
			
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void changeName(String oldName, String newName) {
		PlayerHandler pht = (PlayerHandler) playerHandlerHash.remove(oldName);
		playerHandlerHash.put(newName, pht);
		
	}
	
	// utility functions that the client can use to request information
	//  about other players, statistics about past games, and other 
	//  'global' (non-game-specific) information.  
	
	public void who(PlayerHandler asker) {
		
		Enumeration<PlayerHandler> e = playerHandlerHash.elements();
		
		while(e.hasMoreElements() ) {
			PlayerHandler handler = e.nextElement();
			String statusString = " (waiting for challenges)";
			if(asker.getName().equals(handler.getName())) {
				statusString = "*" + statusString;
			}
			
			asker.send(handler.getName() + statusString);
		}
	
		
	}
	
	// Utility function to start a new game.
	
	public boolean challenge(PlayerHandler challenger, 
				String opponentName) {

		PlayerHandler opponent;

		opponent = (PlayerHandler) playerHandlerHash.get(opponentName);
		
		// Does the opponent exist?
		if(opponent == null) {
			challenger.send("Player " + opponentName + " is not logged in.");
			return false;
			
		}

		challengeSet.add(challenger.getName() + " " + opponentName);
	
		challenger.send("Challenge made.  You will be notified if " + opponentName +" accepts.");
		opponent.send(challenger.getName() + " has challenged you to a game.  Type accept " + challenger.getName() +" to begin play.");
		// challenge has been succesfully made, but game will not 
		// start until accepted.  (return value of true does not 
		// indicate that challenge has been accepted!)
		return true;
	}

	void accept(PlayerHandler acceptor, 
			String challengerName) {

		PlayerHandler challenger = playerHandlerHash.get(challengerName);
		
		// Does the opponent exist?
		if(challenger == null) {
			acceptor.send("Player " + challengerName + " is not logged in.");
			return;
			
		}
			
		if(!challengeSet.contains(challenger.getName() + " " + acceptor.getName())) {
			acceptor.send("There is no open challenge to accept from " + challengerName );
			return;
			
		}
		
		// create a new ServerGame to hold game data
	
		ServerGame game = new ServerGame(challenger, acceptor);
		
		//gameVector.add(game);
		
		// challenger to play black
		// notify the two players that the game has begun  

		challenger.startGame(game, Chess.WHITE);
		acceptor.startGame(game, Chess.BLACK);
	
	}
	

	
}
