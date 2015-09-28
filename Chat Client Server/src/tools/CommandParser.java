package tools;

import java.io.IOException;
import java.util.stream.Collectors;

import server.ClientConnection;
import server.Server;

public class CommandParser {
	
	public static int nthOccurrence(String str, String c, int n) {
	    int pos = str.indexOf(c, 0);
	    while (n-- > 0 && pos != -1)
	        pos = str.indexOf(c, pos + 1);
	    return pos;
	}
	
	public static void parse(String input, Server server) {
		String[] args = input.split(" ");
		String command = args[0];
		
		if (command.equalsIgnoreCase("/addAdmin")) {
			server.getUsers().forEach(u -> {
				if (u.getDisplayName().equalsIgnoreCase(args[1])) {
					u.setAdmin(true);
				}
			});
		} else if (command.equalsIgnoreCase("/users")) {
			System.out.println("Connected users: ");
			server.getUsers().forEach(u -> {
				System.out.println(u.getDisplayName() + ":" + u.getID());
			});
		} else if (command.equalsIgnoreCase("/password")) {
			System.out.println("Password: '" + server.getPassword() + "'");
		} else if (command.equals("/stop")) {
			System.out.println("Shutting down server.");
			System.exit(0);
		}
		
	}
	
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
		}
	}
	
}