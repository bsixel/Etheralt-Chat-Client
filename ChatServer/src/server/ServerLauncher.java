package server;

import static tools.FileHandler.debugPrint;

import java.io.IOException;
import java.util.Scanner;

import tools.CommandParser;
import tools.FileHandler;
import tools.SystemInfo;

/**
 * 
 * @author Ben Sixel
 * Server launcher. Launches the server itself and sends user input from the console to the command parser, or directly to chat itself.
 *
 */

public class ServerLauncher {
	
	/**
	 * Main method, takes args from console when launched as a String array.
	 * @param args Typical launch args taken from console.
	 * @throws IOException ...Sometime. But hopefully not.
	 */
	public static void main(String[] args) throws IOException {
		
		Server server = new Server();
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		Thread serverThread = new Thread(() -> {
			try {
				debugPrint("Starting with password '" + args[1] + "' and port " + args[0] + ".");
				server.startServer(Integer.parseInt(args[0]), args[1]);
			} catch (Exception e) {
				try {
					String lastPass = FileHandler.getProperty("last_password");
					String lastPort = FileHandler.getProperty("last_port");
					if (!lastPass.equals(null) && !lastPort.equals(null)) {
						debugPrint("Starting with password '" + lastPass + "' on port " + lastPort + ".");
					}
					server.startServer();
				} catch (Exception e1) {
					try {
						debugPrint("Starting with password 'default' on port 25566");
						server.startDefaultServer();
					} catch (Exception e2) {
						debugPrint("Unable to start default server!");
						debugPrint(e2.getStackTrace()[0].toString());
						e2.printStackTrace();
						System.exit(-1);
					}
				}
			}
		});
		serverThread.setDaemon(true);
		serverThread.start();
		
		/*
		 * Takes user input from the command line for commands and chatting with the connected users.
		 */
		while (true) {
			String input = scanner.nextLine();
			if (input.startsWith("/")) {					//If the input starts with a '/' send it to the command parser.
				try {
					CommandParser.parse(input, server);
				} catch (Exception e) {
					debugPrint(e.getStackTrace()[0].toString());
				}
			} else if (!input.startsWith("*")) {			//Sends non-command input to connected clients as chat.
				server.getUsers().forEach(u -> {
					try {
						u.getCC().getSendingData().writeUTF("*![Server] " + SystemInfo.getDate() + ": " + input);
					} catch (Exception e) {
						debugPrint(e.getStackTrace()[0].toString());
					}
				});
			}
		}
	}

}
