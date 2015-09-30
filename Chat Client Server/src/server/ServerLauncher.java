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
				System.out.println("Starting with password '" + args[1] + "'.");
				server.startServer(Integer.parseInt(args[0]), true, args[1], out);
			} catch (Exception e){
				try {
					System.out.println("Starting with password '" + FileHandler.getProperty("last_password") + "'.");
					server.startServer(Integer.parseInt(FileHandler.getProperty("last_port")), true, FileHandler.getProperty("last_password"), out);
				} catch (Exception e1) {
					try {
						System.out.println("Starting with default password.");
						server.startServer(Integer.parseInt(args[0]), true, "default", out);
					} catch (Exception e2) {
						e1.printStackTrace();
						FileHandler.writeToErrorLog(e1.getMessage());
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