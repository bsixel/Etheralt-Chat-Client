package server;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import tools.CommandParser;
import tools.FileHandler;

public class ServerLauncher {
	
	public static void main(String[] args) throws IOException {
		
		Server server = new Server();
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		PrintStream out = System.out;
		Thread serverThread = new Thread(() -> {
			try {
				server.startServer(Integer.parseInt(args[0]), true, args[1], out);
			} catch (Exception e) {
				try {
					server.startServer(Integer.parseInt(args[0]), true, "default", out);
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