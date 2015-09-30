package server;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import tools.CommandParser;
import tools.FileHandler;

public class ServerLauncher {
	
	public static void main(String[] args) throws IOException {
		
		// Defaults for the server port and server password.
		Integer serverPort = 25565;
		String serverPassword = "default";
		
		Server server = new Server();
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		PrintStream out = System.out;
		Thread serverThread = new Thread(() -> {
			try {
				// It gets mad without these, for a weird reason I don't understand.
				Integer port = serverPort;
				String password = serverPassword;
				if (args.length != 0) {
					port = Integer.parseInt(args[0]);
					if (args.length > 1) {
						password = args[1];
					}
				}
				System.out.println("Starting with password '" + password + "'.");
				server.startServer(port, true, password, out);
			} catch (Exception e) {
				try {
					System.out.println("Starting with default password.");
					server.startServer(serverPort, true, serverPassword, out);
				} catch (Exception e1) {
					e1.printStackTrace();
					FileHandler.writeToErrorLog(e1.getMessage());
				}
			}
		});
		serverThread.setDaemon(true);
		serverThread.start();
		while (true) {
			CommandParser.parse(scanner.next(), server);
		}
	}

}