package tools;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.stream.Collectors;

import application.ChatClient;
import client.Client;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import userInteract.ChatText;
import userInteract.MainScreenController;
import userInteract.Popups;

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
				sc.addMessage("To " + System.lineSeparator() + "[" + args[1] + "] " + SystemInfo.getDate() + ": " + message.substring(message.indexOf(" ") + 1), "green", "black");
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
			sc.getImages().getChildren().clear();
			sc.getChatField().clear();
			new File(FileHandler.chatLogPath).delete();
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
			sendFile(input, sc);
		} else if (command.equalsIgnoreCase("/getfile")) {
			System.out.println("Kinda getting file from " + args[1] + ", checking target...");
			if (sc.getClient().getClientName().equalsIgnoreCase(args[2]) || args[2].equals("all")) {
			
				if (Popups.startConfDlg("Accept file from " + args[1] + "?")) {
					File file = Popups.startFileSaver("Select a download location.", args[3].substring(args[3].lastIndexOf(".") + 1), args[3]);
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
			});
		} else if (command.equalsIgnoreCase("/img")) {
			try {
				sc.addMessage("Attempted to send file to " + args[1] + ".", "blue", "black");
				File file = Popups.startFileOpener("Select an image to send.");
				Thread thread = new Thread(FileHandler.sendPic(args[1], file, sc.getClient()), "PicThread for " + file.getName());
				thread.setDaemon(true);
				sc.getDlThreads().add(thread);
				thread.start();
				
				sc.getChatField().clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("/getimg")) {
			if (sc.getClient().getClientName().equalsIgnoreCase(args[2]) || args[2].equals("all")) {
				File file = new File(FileHandler.picturesPath + "/" + args[3]);
				Thread thread = new Thread(FileHandler.dlPic(sc, file, Integer.parseInt(args[4])));
				thread.setDaemon(true);
				sc.getDlThreads().add(thread);
				thread.start();
			}
		} else if (command.equalsIgnoreCase("/declineimg")) {
			sc.getDlThreads().forEach(e -> {
				if (e.getName().equalsIgnoreCase("PicThread for " + args[1])) {
					e.interrupt();
				}
			});
		} else if (command.equalsIgnoreCase("/youtube")) {
			try {
				sc.getClient().getClientSendingData().writeUTF("*!youtube" + input.substring(input.indexOf(" ")));
			} catch (IOException e) {
				e.printStackTrace();
			}
			sc.getChatField().clear();
		} else if (command.equalsIgnoreCase("/youtubeplay")) {
			if (sc.getClient().getClientName().equalsIgnoreCase(args[1]) || args[1] .equalsIgnoreCase("all")) {
				
			}
		} else if (command.equalsIgnoreCase("/linkopen")) {

			if (input.contains("youtube")) {
				try {
					desktop.browse(new URI(input.split(" ", 3)[2]));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				WebView webview = new WebView();
				webview.setOnMouseClicked(click -> {
					click.consume();
					if (click.getClickCount() == 2) {
						try {
							desktop.browse(new URI(input.split(" ", 3)[2]));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				webview.setPrefSize(500, 500);
				WebEngine webengine = webview.getEngine();
				webengine.load(args[2]);
				sc.getImages().getChildren().add(webview);
			}
		} else if (command.equalsIgnoreCase("/clearmedia")) {
			sc.getChatField().clear();
			sc.getImages().getChildren().stream().filter(e -> e instanceof ChatText).collect(Collectors.toList());
			sc.getImages().getChildren().clear();
		} else if (command.equalsIgnoreCase("/browse")) {
			WebView webview = new WebView();
			webview.setOnMouseClicked(click -> {
				if (click.getClickCount() == 2) {
					try {
						desktop.browse(new URI("https://www.google.com"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			webview.setPrefSize(500, 500);
			WebEngine webengine = webview.getEngine();
			webengine.load("https://www.google.com");
			sc.getImages().getChildren().add(webview);
		} else if (command.equalsIgnoreCase("/audio")) {
			sc.getChatField().clear();
			Platform.runLater(() -> {
				File file = Popups.startFileOpener("Select audio file to transmit.");
				Thread audioThread = new Thread(() -> {
					FileHandler.transmitAudio(sc, file);
				});
				audioThread.setDaemon(true);
				audioThread.start();
			});
			
		} else if (command.equalsIgnoreCase("/updateusers")) {
			sc.getUsersArea().setText(input.substring("/updateusers ".length()));
		} else if (command.equalsIgnoreCase("/kick") && sc.getClient().isAdmin()) {
			try {
				sc.getClient().getClientSendingData().writeUTF("*!kick " + args[1] + " " + input.split("'")[1]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("/kicked")) {
			sc.logout();
			Popups.startInfoDlg("Kicked from server!", "Kicked from server: " + System.lineSeparator() + input.split("'")[1]);
		}
		
	}
	
public static void parseMSC(String input, MainScreenController sc, ArrayList<String> prevInput) {
		
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
				sc.addMessage("To " + System.lineSeparator() + "[" + args[1] + "] " + SystemInfo.getDate() + ": " + message.substring(message.indexOf(" ") + 1), "green", "black");
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
			sc.getImages().getChildren().clear();
			sc.getChatField().clear();
			new File(FileHandler.chatLogPath).delete();
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
			sendFile(input, sc);
		} else if (command.equalsIgnoreCase("/getfile")) {
			System.out.println("Kinda getting file from " + args[1] + ", checking target...");
			if (sc.getClient().getClientName().equalsIgnoreCase(args[2]) || args[2].equals("all")) {
			
				if (Popups.startConfDlg("Accept file from " + args[1] + "?")) {
					File file = Popups.startFileSaver("Select a download location.", args[3].substring(args[3].lastIndexOf(".") + 1), args[3]);
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
		} else if (command.equalsIgnoreCase("/img")) {
			try {
				sc.addMessage("Attempted to send file to " + args[1] + ".", "blue", "black");
				File file = Popups.startFileOpener("Select an image to send.");
				Thread thread = new Thread(FileHandler.sendPic(args[1], file, sc.getClient()), "PicThread for " + file.getName());
				thread.setDaemon(true);
				sc.getDlThreads().add(thread);
				thread.start();
				
				sc.getChatField().clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("/youtube")) {
			try {
				sc.getClient().getClientSendingData().writeUTF("*!youtube" + input.substring(input.indexOf(" ")));
			} catch (IOException e) {
				e.printStackTrace();
			}
			sc.getChatField().clear();
		} else if (command.equalsIgnoreCase("/clearmedia")) {
			sc.getChatField().clear();
			sc.getImages().getChildren().stream().filter(e -> e instanceof ChatText).collect(Collectors.toList());
			sc.getImages().getChildren().clear();
		} else if (command.equalsIgnoreCase("/browse")) {
			WebView webview = new WebView();
			webview.setOnMouseClicked(click -> {
				if (click.getClickCount() == 2) {
					try {
						desktop.browse(new URI("https://www.google.com"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			webview.setPrefSize(500, 500);
			WebEngine webengine = webview.getEngine();
			webengine.load("https://www.google.com");
			sc.getImages().getChildren().add(webview);
		} else if (command.equalsIgnoreCase("/audio")) {
			sc.getChatField().clear();
			Platform.runLater(() -> {
				File file = Popups.startFileOpener("Select audio file to transmit.");
				Thread audioThread = new Thread(() -> {
					FileHandler.transmitAudio(sc, file);
				});
				audioThread.setDaemon(true);
				audioThread.start();
			});
			
		} else if (command.equalsIgnoreCase("/kick") && sc.getClient().isAdmin()) {
			try {
				sc.getClient().getClientSendingData().writeUTF("*!kick " + args[1] + " '" + input.split("'")[1] + "'");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void sendFile(String input, MainScreenController sc) {
		String[] args = input.split(" ");
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
		} else if (command.equalsIgnoreCase("*!admind")) {
			client.setAdmin(true);
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
		}
	}
	
}