package chai;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import chesspresso.Chess;

/*
 * PlayerHandlerThread.java created on Dec 29, 2004 by devin.
 *  Updated for AI Chess 2013.
 */


public class PlayerHandler extends Thread {
	
	private Socket socket;
	private ChessServer server;
	
	//private PrintStream out;
	//private BufferedReader in;
	private ObjectOutputStream objectOut;
	private ObjectInputStream objectIn;
	
	ServerGame game = null;
	
	static String[] COLOR_STRING = {"white", "black"};
	
	private int clientColor = Chess.WHITE;
	
	public PlayerHandler(Socket socket, ChessServer server) {
        super();
        this.socket = socket;
        this.server = server;
	}
	
	public void run() {
		boolean connected = true;
		
		try {
			
			objectOut = new ObjectOutputStream(socket.getOutputStream());
			objectIn = new ObjectInputStream(socket.getInputStream());
	
			
			while(connected) {

				String clientInput;
				
				
				clientInput = (String) objectIn.readObject();
				
				System.out.println("received: " + clientInput);
				//out.println(clientInput);
				//out.flush();
				
				// protocol
				
				//  'who':  list other players
				
				String[] input = clientInput.split("\\s");
				
				for(int i = 0; i < input.length; i++) {
					//System.out.println("token:  _" + input[i] + "_");
				}
				
				if(input.length == 0) {
					//no-op
				}
				else if (input[0].equals("who")) {
					server.who(this);
				} else if(input[0].equals("name")) {
					server.changeName(getName(), input[1]);
					setName(input[1]);
				} else if(input[0].equals("challenge")) {
					server.challenge(this, input[1]);
				} else if(input[0].equals("accept")) {
					server.accept(this, input[1]);
				}
				
				else if(input[0].equals("move")) {
	
					game.otherPlayer(this).send("move " + input[1]);
					
				
					
				}
			}
			
			try {
					sleep((long)(10));   // wait at least 10 milliseconds before
					// checking up on this thread
				} catch (InterruptedException e) {}
			
			
		} catch(IOException e) {} catch (ClassNotFoundException e) {
			
		}
		
	}  	
	
	// woohoo, someone is challenging us to a game!
	public void challenge(String challenger) {
		send(challenger + " has challenged you.  " +
				"Type 'accept " + challenger + "' to play.");
	}
	
	void startGame(ServerGame g, int colr) {
		game = g;
		clientColor = colr;
		
		send("start " + COLOR_STRING[clientColor]);
		sendState();
		
	}
	
	public void send(Object s) {
		try {
			objectOut.writeObject(s);
			objectOut.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void sendState() {
	
		send("state");
		send(game.getPosition());
	
	}
	
	
	
}
