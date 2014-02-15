/* Chess game server by djb, based on KKTMultiServer.java from "The Java Tutorials" */

package chai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import java.net.*;
import java.io.*;

public class ChessServerOld {


	private static final int PORT_NUMBER = 8000;
	
	// weak passwords to keep out annoyance-level scans
	private static final String PASSWORD = "cs76student";
	private static final String ADMIN_PASSWORD = "cs76staff";
	
	
	private int portNumber;
	
	public static void main(String[] args) throws IOException {
		ChessServerOld server = new ChessServerOld(PORT_NUMBER);
		server.listen();

	}
	

	public ChessServerOld(int portNumber) {
		this.portNumber = portNumber;

	}

	public void listen() {
		boolean listening = true;

		try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
			while (listening) {
				new ChessServerThread(serverSocket.accept()).start();
			}
		} catch (IOException e) {
			System.err.println("Could not listen on port " + portNumber);
			System.exit(-1);
		}

	}

	private class ChessServerThread extends Thread {
		private Socket socket = null;

		public ChessServerThread(Socket socket) {
			super("ChessServerThread");
			this.socket = socket;
		}

		public void run() {

			try (PrintWriter out = new PrintWriter(socket.getOutputStream(),
					true);
					BufferedReader in = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));) {
				String inputLine, outputLine;
				// KnockKnockProtocol kkp = new KnockKnockProtocol();
				// outputLine = kkp.processInput(null);
				// out.println(outputLine);

				while ((inputLine = in.readLine()) != null) {
				// outputLine = kkp.processInput(inputLine);
				System.out.println(inputLine);
				// if (outputLine.equals("Bye"))
				// break;
				}
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}