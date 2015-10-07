package server;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import tools.CommandParser;

public class ServerLauncher {
	
	public static void main(String[] args) throws IOException {
		
		Server server = new Server();
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		PrintStream out = System.out;
		Thread serverThread = new Thread(() -> {
			try {
				System.out.println("Starting with password '" + args[1] + "'.");
				System.out.println("Args zero: " + args[0]);
				server.startServer(Integer.parseInt(args[0]), true, args[1], out);
			} catch (Exception e) {
				try {
					System.out.println("Starting with password 'default'.");
					server.startServer();
				} catch (Exception e1) {
					e1.printStackTrace();
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