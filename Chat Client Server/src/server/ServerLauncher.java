package server;

import java.io.IOException;
import java.util.Scanner;

import tools.CommandParser;

public class ServerLauncher {
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		
		Server server = new Server();
		Scanner scanner = new Scanner(System.in);
		try {
			server.startServer(Integer.parseInt(args[0]), true, args[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			server.startServer(Integer.parseInt(args[0]), true, "");
		}
		while (true) {
			CommandParser.parse(scanner.next(), server);
		}
	}

}