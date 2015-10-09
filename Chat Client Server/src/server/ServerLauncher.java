package server;

import static tools.FileHandler.debugPrint;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import tools.CommandParser;
import tools.FileHandler;

/**
 * 
 * @author Ben Sixel
 * Server launcher. Launches the server itself and sends user input from the console to the command parser.
 *
 */

public class ServerLauncher {
	
	/**
	 * Main method.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		Server server = new Server();
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		PrintStream out = System.out;
		Thread serverThread = new Thread(() -> {
			try {
				debugPrint("Starting with password '" + args[1] + "'.");
				debugPrint("Args zero: " + args[0]);
				server.startServer(Integer.parseInt(args[0]), args[1], out);
			} catch (Exception e) {
				try {
					debugPrint("Starting with password 'default' on port " + FileHandler.getProperty("last_port"));
					server.startServer();
				} catch (Exception e1) {
					try {
						debugPrint("Starting with password 'default' on port 25566");
						server.startDefaultServer();
					} catch (Exception e2) {
						debugPrint("Unable to start default server!");
						e2.printStackTrace();
						System.exit(-1);
					}
				}
			}
		});
		serverThread.setDaemon(true);
		serverThread.start();
		while (true) {
			CommandParser.parse(scanner.nextLine(), server);
		}
	}

}