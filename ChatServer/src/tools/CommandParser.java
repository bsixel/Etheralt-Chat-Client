package tools;

import static tools.FileHandler.debugPrint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ConcurrentModificationException;
import java.util.stream.Collectors;

import server.ClientConnection;
import server.Server;
import server.User;

/*
 * 
 * @author Ben Sixel
 * Command Parser for commands coming from both server and remote clients.
 */

public class CommandParser {

	private static final String initStr = "Connected users:";

	/**
	 * Gets the nth occurrence of a string c in another string str.
	 * @param str The string we are searching within.
	 * @param c The string for which we need the nth place of occurrence.
	 * @param n The number representing which occurrence we are looking for.
	 * @return An int representing the position at which the string occurs for the nth time.
	 */
	public static int nthOccurrence(String str, String c, int n) {
		int pos = str.indexOf(c, 0);
		while (n-- > 0 && pos != -1)
			pos = str.indexOf(c, pos + 1);
		return pos;
	}

	/**
	 * Parses commands coming from the server itself.
	 * @param input The string which we are parsing.
	 * @param server The server which we will be working with/manipulating using the commands.
	 * @throws Emergency exception. In the server launcher we provide a catch to stop this from completely breaking the server.
	 */
	public static void parse(String input, Server server) throws Exception {
		String[] args = input.split(" ");
		String command = args[0];

		if (command.equalsIgnoreCase("/admin")) {
			server.getUsers().forEach(u -> {
				if (u.getDisplayName().equalsIgnoreCase(args[1])) {
					try {
						u.sendCommand("*!admind");
						String currAdmins = FileHandler.getProperty("admins");
						if (currAdmins == null || currAdmins.equals("")) {
							currAdmins = "";
							FileHandler.setProperty("admins", u.getID());
							debugPrint("Added " + args[1] + " as admin!");
							u.setAdmin(true);
							return;
						}
						String newAdmins = String.format("%1$s,%2$s", currAdmins, u.getID());
						FileHandler.setProperty("admins", newAdmins);
						System.out.println("Added " + args[1] + " as admin!");
						u.setAdmin(true);
					} catch (Exception e) {
						debugPrint("Failed to add " + args[1] + " as admin!");
						debugPrint(e.getStackTrace()[0].toString());
					}
				}
			});
		} else if (command.equalsIgnoreCase("/users")) {
			System.out.println("Connected users: ");
			server.getUsers().forEach(u -> {
				System.out.println(u.getDisplayName() + ":" + u.getID());
			});
		} else if (command.equalsIgnoreCase("/password")) {
			System.out.println("Password: '" + server.getPassword() + "'");
		} else if (command.equalsIgnoreCase("/stop")) {
			debugPrint("Shutting down server.");
			System.exit(0);
		} else if (command.equalsIgnoreCase("/kick")) {
			try {
				server.killUser(args[1], input.split("'")[1]);
			} catch (Exception e) {
				debugPrint(e.getStackTrace()[0].toString());
				debugPrint("Error! Input = " + input);
			}
		} else if (command.equalsIgnoreCase("/IP")) {
			try {
				System.out.println(new BufferedReader(new InputStreamReader(new URL("http://agentgatech.appspot.com").openStream())).readLine());
			} catch (Exception e) {
				FileHandler.debugPrint("Unable to get current IP!");
			}
		} else if (command.equalsIgnoreCase("/updateusers")) {
			String users = buildUsers(server);
			debugPrint("Updating connected users for clients.");
			server.getUsers().forEach(u -> {
				try {
					u.sendCommand("/updateusers " + users);
				} catch (Exception e) {
					debugPrint("Error notifying of new connection: " + e.getStackTrace()[0].toString());
				}
			});
		} else if (command.equalsIgnoreCase("/port")) {
			System.out.println(server.getPortStart());
		} else if (command.equalsIgnoreCase("/admins")) {
			String admins = FileHandler.getProperty("admins");
			if (admins == null || admins.equals("")) {
				System.out.println("No registered admins.");
			} else {
				System.out.printf("Registered admins: %1$s%n", admins);
			}
		}

		System.out.print("> ");
	}

	/**
	 * Build a string containing connected users.
	 * @param server The server whose users are being organized.
	 * @return A string composed of 'Connected users:' plus the connected users with commas in between.
	 */
	public static String buildUsers(Server server) {
		String str = initStr;
		for (User u:server.getUsers()) {
			if (str.split(" ").length < 3) {
				str += " " + u.getDisplayName();
			} else {
				str += ", " + u.getDisplayName();
			}
		};
		return str;
	}

	/**
	 * Parses commands coming in from remote clients and redistributes or enacts them.
	 * @param input The command and arguments for the command which are being parsed.
	 * @param client The client which is receiving the command's effects.
	 * @param selfClient The client sending the command.
	 */
	public static void parse(String input, ClientConnection client, ClientConnection selfClient) {

		String[] args = input.split(" ");
		String command = args[0];

		if (command.equals("*!tell:")) {
			if (client.getClientName().equalsIgnoreCase(args[2]) || args[2].equals("all")) {
				try {
					System.out.println("Message sent from " + args[1] + " to " + args[2]);
					client.getSendingData().writeObject(new DataPacket("message", selfClient.getClientName(), client.getClientName(), "From " + "[" + args[1] + " ] " + SystemInfo.getDate() +  ": " + input.substring(nthOccurrence(input, " ", 2)), null));
				} catch (IOException e) {
					debugPrint(e.getStackTrace()[0].toString());
				}
			}
		} else if (command.equalsIgnoreCase("*!link:")) {
			if (client.getClientName().equalsIgnoreCase(args[1]) || args[1].equals("all")) {
				try {
					client.getSendingData().writeObject(new DataPacket("command", selfClient.getClientName(), client.getClientName(), "/linkopen" + input.substring(input.indexOf(" ")), null));
				} catch (IOException e) {
					debugPrint(e.getStackTrace()[0].toString());
				}
			}
		} else if (command.equalsIgnoreCase("*!users:")) {
			if (client.getClientName().equalsIgnoreCase(args[1])) {
				try {
					client.getSendingData().writeObject(new DataPacket("message", selfClient.getClientName(), client.getClientName(), "Connected users: " + client.getServer().getUsers().stream().map(e -> e.getCC().getClientName()).collect(Collectors.toList()).toString(), null));
				} catch (IOException e) {
					debugPrint(e.getStackTrace()[0].toString());
				}
			}
		} else if (command.equalsIgnoreCase("*!declineDL:")) {
			if (client.getClientName().equalsIgnoreCase(args[1])) {
				try {
					client.getSendingData().writeObject(new DataPacket("command", selfClient.getClientName(), client.getClientName(), "/declineDL" + input.substring(input.indexOf(" ")), null));
				} catch (IOException e) {
					debugPrint(e.getStackTrace()[0].toString());
				}
			}
		} else if (command.equalsIgnoreCase("*!declineimg:")) {
			if (client.getClientName().equalsIgnoreCase(args[1])) {
				try {
					client.getSendingData().writeObject(new DataPacket("command", selfClient.getClientName(), client.getClientName(), "/declineimg" + input.substring(input.indexOf(" ")), null));
				} catch (IOException e) {
					debugPrint(e.getStackTrace()[0].toString());
				}
			}
		} else if (command.equalsIgnoreCase("*!youtube")) {
			if (client.getClientName().equalsIgnoreCase(args[1]) || args[1] .equalsIgnoreCase("all")) {
				try {
					client.getSendingData().writeObject(new DataPacket("command", selfClient.getClientName(), client.getClientName(), "/youtubeplay" + input.substring(input.indexOf(" ")), null));
				} catch (IOException e) {
					debugPrint(e.getStackTrace()[0].toString());
				}
			}
		} else if (command.equalsIgnoreCase("*!kick") && (selfClient.getUser().isAdmin() || FileHandler.getProperty("admins").contains(selfClient.getUser().getID()))) {
			try {
				client.getServer().killUser(args[1], input.split("'")[1]);
			} catch (ConcurrentModificationException e) {
				debugPrint(e.getStackTrace()[0].toString());
				debugPrint("Unable to kill user - another thread had already done so!");
			}
		} else if (command.equalsIgnoreCase("*!disconnect")) {
			client.getServer().killUser(args[1], input.split("'")[1]);
		}
	}

}