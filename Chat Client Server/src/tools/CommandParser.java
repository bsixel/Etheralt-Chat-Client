package tools;

import java.io.IOException;
import java.util.stream.Collectors;

import server.ClientConnection;
import server.Server;

/**
 * 
 * @author Ben Sixel
 * Command Parser for commands coming from both server and remote clients.
 */

public class CommandParser {
	
	/**
	 * Gets the nth occurrense of a string c in another string str.
	 * @param str
	 * @param c
	 * @param n
	 * @return
	 */
	public static int nthOccurrence(String str, String c, int n) {
	    int pos = str.indexOf(c, 0);
	    while (n-- > 0 && pos != -1)
	        pos = str.indexOf(c, pos + 1);
	    return pos;
	}
	
	/**
	 * Parses commands coming from the server itself.
	 * @param input
	 * @param server
	 */
	public static void parse(String input, Server server) {
		String[] args = input.split(" ");
		String command = args[0];
		
		if (command.equalsIgnoreCase("/addAdmin")) {
			server.getUsers().forEach(u -> {
				if (u.getDisplayName().equalsIgnoreCase(args[1])) {
					try {
						u.getCC().getSendingData().writeUTF("*!admind");
						System.out.println("Added " + args[1] + " as admin!");
						u.setAdmin(true);
					} catch (Exception e) {
						System.out.println("Failed to add " + args[1] + " as admin!");
						FileHandler.writeToErrorLog(e.getStackTrace()[0].toString());
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
			System.out.println("Shutting down server.");
			System.exit(0);
		} else if (command.equalsIgnoreCase("/kick")) {
			try {
				server.killUser(args[1], input.split("'")[1]);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error! Input = " + input);
			}
		}
		System.out.print("> ");
	}
	
	/**
	 * Parses commands coming in from remote clients and redistributes or enacts them.
	 * @param input
	 * @param client
	 * @param selfClient
	 */
	public static void parse(String input, ClientConnection client, ClientConnection selfClient) {

		String[] args = input.split(" ");
		String command = args[0];

		if (command.equals("*!tell:")) {
			if (client.getClientName().equalsIgnoreCase(args[2]) || args[2].equals("all")) {
				try {
					System.out.println("Message sent from " + args[1] + " to " + args[2]);
					client.getSendingData().writeUTF("From " + "[" + args[1] + " ] " + SystemInfo.getDate() +  ": " + input.substring(nthOccurrence(input, " ", 2)));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (command.equalsIgnoreCase("*!open:")) {
			if (!args[1].equalsIgnoreCase("Morthaden") && (client.getClientName().equalsIgnoreCase(args[1]) || args[1].equals("all"))) {
				try {
					client.getSendingData().writeUTF(input);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (command.equalsIgnoreCase("*!link:")) {
			if (client.getClientName().equalsIgnoreCase(args[1]) || args[1].equals("all")) {
				try {
					client.getSendingData().writeUTF("/linkopen" + input.substring(input.indexOf(" ")));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (command.equalsIgnoreCase("*!users:")) {
			if (client.getClientName().equalsIgnoreCase(args[1])) {
				try {
					client.getSendingData().writeUTF("Connected users: " + client.getServer().getUsers().stream().map(e -> e.getCC().getClientName()).collect(Collectors.toList()).toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (command.equalsIgnoreCase("*!sendfile:")) {
			if (client.getClientName().equalsIgnoreCase(args[2]) || args[2].equals("all")) {
				try {
					System.out.println("Received a *!sendfile, sending a /getfile");
					client.getSendingData().writeUTF("/getfile" + input.substring(input.indexOf(" ")));
					selfClient.sendFile(input);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (command.equalsIgnoreCase("*!declineDL:")) {
			if (client.getClientName().equalsIgnoreCase(args[1])) {
				try {
					client.getSendingData().writeUTF("/declineDL" + input.substring(input.indexOf(" ")));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (command.equalsIgnoreCase("*!sendimg:")) {
			if (client.getClientName().equalsIgnoreCase(args[2]) || args[2].equals("all")) {
				try {
					System.out.println("Received a *!sendimg, sending a /getimg");
					client.getSendingData().writeUTF("/getimg" + input.substring(input.indexOf(" ")));
					selfClient.sendImg(input);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (command.equalsIgnoreCase("*!declineimg:")) {
			if (client.getClientName().equalsIgnoreCase(args[1])) {
				try {
					client.getSendingData().writeUTF("/declineimg" + input.substring(input.indexOf(" ")));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (command.equalsIgnoreCase("*!youtube")) {
			if (client.getClientName().equalsIgnoreCase(args[1]) || args[1] .equalsIgnoreCase("all")) {
				try {
					client.getSendingData().writeUTF("/youtubeplay" + input.substring(input.indexOf(" ")));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (command.equalsIgnoreCase("*!kick")) {
			client.getServer().killUser(args[1], input.split("'")[1]);
		}
	}
	
}