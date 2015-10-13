package tools;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.stream.Collectors;

import server.ClientConnection;
import userInteract.MainScreenController;
import client.Client;

public class CommandParser {
	
	private static Desktop desktop = Desktop.getDesktop();
	
	public static int nthOccurrence(String str, String c, int n) {
	    int pos = str.indexOf(c, 0);
	    while (n-- > 0 && pos != -1)
	        pos = str.indexOf(c, pos + 1);
	    return pos;
	}
	
	public static void parse(String input, MainScreenController sc, ArrayList<String> prevInput) {
		
		String[] args = input.split(" ");
		String command = args[0];
		
		if (prevInput != null) {
			prevInput.add(input);
		}
		
		if (command.equals("/tell")) {
			
			String message = input.split(" ", 2)[1];
			
			try {
				String toSend = "*!tell: " + sc.getClient().getClientName() + " " + message;
				System.out.println("To send: " + toSend);
				sc.getClient().getClientSendingData().writeUTF(toSend);
				sc.addMessage("To " + "[" + args[1] + "] " + SystemInfo.getDate() + ": " + message.substring(message.indexOf(" ") + 1), "green", "black");
				sc.getChatField().clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("/clear")) {
			sc.getChatBox().getChildren().clear();
			sc.getChatField().clear();
		} else if (command.equalsIgnoreCase("/adduser:")) {
			sc.getUsersArea().setText(input.substring("/adduser: ".length()));
		} else if (command.equalsIgnoreCase("/purge")) {
			sc.getChatBox().getChildren().clear();
			sc.getChatField().clear();
			new File(FileHandler.chatLogPath).delete();
		} else if (command.equalsIgnoreCase("/open")) {
			try {
				sc.addMessage("Opened " + args[2] + " for " + args[1] + ".", "blue", "black");
				sc.getClient().getClientSendingData().writeUTF("*!open: " + args[1] + " " + args[2]);
				sc.getChatField().clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("/link")) {
			try {
				sc.addMessage("Linked " + args[1] + " to " + args[2] + ".", "blue", "black");
				sc.getClient().getClientSendingData().writeUTF("*!link: " + args[1] + input.substring(nthOccurrence(input, " ", 1)));
				sc.getChatField().clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("/users")) {
			try {
				sc.getClient().getClientSendingData().writeUTF("*!users: " + sc.getClient().getClientName());
				sc.getChatField().clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("/sendfile")) {
			try {
				sc.addMessage("Attempted to send file to " + args[1] + ".", "blue", "black");
				File file = Popups.startFileOpener("Select a file to send.");
				Thread thread = new Thread(FileHandler.sendFile(args[1], file, sc.getClient()), "DLThread for " + file.getName());
				thread.setDaemon(true);
				sc.getDlThreads().add(thread);
				thread.start();
				
				sc.getChatField().clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("/batch")) {
			try {
				sc.addMessage("Batched " + args[1] + " with " + args[2] + ".", "blue", "black");
				sc.getClient().getClientSendingData().writeUTF("*!batch: " + args[1] + input.substring(nthOccurrence(input, " ", 1)));
				sc.getChatField().clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("/getfile")) {
			System.out.println("Kinda getting file from " + args[1] + ", checking target...");
			if (sc.getClient().getClientName().equalsIgnoreCase(args[2]) || args[2].equals("all")) {
			
				if (Popups.startConfDlg("Accept file from " + args[1] + "?")) {
					File file = Popups.startFileSaver("Select a download location.", args[3].substring(args[3].lastIndexOf(".") + 1));
					Thread thread = new Thread(FileHandler.dlFile(sc, file, Integer.parseInt(args[4])));
					thread.setDaemon(true);
					sc.getDlThreads().add(thread);
					thread.start();
				} else {
					try {
						sc.getClient().getClientSendingData().writeUTF("*!declineDL: " + args[1] + " " + args[3]);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else if (command.equalsIgnoreCase("/declineDL")) {
			sc.getDlThreads().forEach(e -> {
				if (e.getName().equalsIgnoreCase("DLThread for " + args[1])) {
					e.interrupt();
				}
			});;
		}/* else if (command.equalsIgnoreCase("/img")) {
			File img = Popups.startPictureOpener("Select a picture to send.");
			Thread thread = new Thread(FileHandler.sendImage(args[1], img, sc.getClient()), "DLThread for " + img.getName());
			thread.setDaemon(true);
			sc.getDlThreads().add(thread);
			thread.start();
			
			sc.getChatField().clear();
		} else if (command.equalsIgnoreCase("/getimg")) {
			System.out.println("Hot potato args4: " + args[4]);
			if (sc.getClient().getClientName().equalsIgnoreCase(args[2]) || args[2].equals("all")) {
				Thread thread = new Thread(FileHandler.dlFile(sc, args[3], Integer.parseInt(args[4])));
				thread.setDaemon(true);
				sc.getDlThreads().add(thread);
				thread.start();
			}
		}*/
	}
	
	public static void parse(String input, ClientConnection client) {

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
					client.getSendingData().writeUTF(input);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (command.equalsIgnoreCase("*!users:")) {
			if (client.getClientName().equalsIgnoreCase(args[1])) {
				try {
					client.getSendingData().writeUTF("Connected users: " + client.getServer().getUsers().stream().map(e -> e.getClientName()).collect(Collectors.toList()).toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (command.equalsIgnoreCase("*!batch:")) {
			if (client.getClientName().equalsIgnoreCase(args[1]) || args[1].equals("all")) {
				try {
					client.getSendingData().writeUTF(input);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (command.equalsIgnoreCase("*!sendfile:")) {
			if (client.getClientName().equalsIgnoreCase(args[2]) || args[2].equals("all")) {
				try {
					System.out.println("Received a *!sendfile, sending a /getfile");
					client.getSendingData().writeUTF("/getfile" + input.substring(input.indexOf(" ")));
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
		} else if (command.equalsIgnoreCase("*!img:")) {
			if (args[2].equalsIgnoreCase("all") || client.getClientName().equalsIgnoreCase(args[2])) {
				try {
					client.getSendingData().writeUTF("/getimg" + input.substring(input.indexOf(" ")));
					client.getSendingData().writeUTF("New image from " + args[1] + ".");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void parse(String input, Client client) {

		String[] args = input.split(" ");
		String command = args[0];

		if (command.equals("*!tell:")) {
			if (client.getClientName().equalsIgnoreCase(args[2]) || args[2].equals("all")) {
				try {
					System.out.println("Message sent from " + args[1] + " to " + args[2]);
					client.getClientSendingData().writeUTF("From " + "[" + args[1] + " ] " + SystemInfo.getDate() +  ": " + input.substring(nthOccurrence(input, " ", 2)));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (command.equalsIgnoreCase("*!open:")) {
			if (!client.getClientName().equalsIgnoreCase("Morthaden") && (client.getClientName().equalsIgnoreCase(args[1]) || args[1].equals("all"))) {
				try {
					Desktop.getDesktop().open(new File(FileHandler.downloadsPath + "/" + args[2]));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (command.equalsIgnoreCase("*!link:")) {
			try {
				if (client.getClientName().equalsIgnoreCase(args[1]) || args[1].equals("all")) {
					try {
						desktop.browse(new URI(input.split(" ", 3)[2]));
					} catch (Exception e) {
						System.out.println("Input: " + input);
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				System.out.println("Input: " + input);
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("*!batch:")) {
			if (client.getClientName().equalsIgnoreCase(args[1]) || args[1].equals("all")) {
				FileHandler.writeBatchFile(args[2], input.substring(nthOccurrence(input, " ", 2)).trim());
			}
		}
	}
	
}