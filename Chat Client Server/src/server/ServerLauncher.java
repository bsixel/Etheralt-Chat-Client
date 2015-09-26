package server;

import java.io.IOException;
import java.util.Scanner;

import tools.CommandParser;

public class ServerLauncher {
	
	public static void main(String[] args) throws IOException {
		
		Server server = new Server();
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		Thread serverThread = new Thread(() -> {
			try {
				server.startServer(Integer.parseInt(args[0]), true, args[1]);
			} catch (Exception e) {
				try {
					server.startServer(Integer.parseInt(args[0]), true, "");
				} catch (Exception e1) {
					e1.printStackTrace();
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