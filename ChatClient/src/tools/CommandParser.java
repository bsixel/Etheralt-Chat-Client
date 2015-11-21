package tools;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import userInteract.ChatText;
import userInteract.MainScreenController;
import userInteract.Popups;

/*
 * 
 * @author Ben Sixel
 *   Copyright 2015 Benjamin Sixel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

public class CommandParser {

	/**
	 * Gets the position of the nth occurrence of a string within another string.
	 * @param str The string we are searching within.
	 * @param c The string to search for.
	 * @param n The number representing which occurrence we are searching for.
	 * @return An integer representing the index at which the string occurs for the nth time.
	 */
	public static int nthOccurrence(String str, String c, int n) {
		int pos = str.indexOf(c, 0);
		while (n-- > 0 && pos != -1)
			pos = str.indexOf(c, pos + 1);
		return pos;
	}

	/**
	 * Parses commands coming from the client's streams.
	 * @param input The command to parse, with arguments.
	 * @param sc The windows controller being affected by commands.
	 */
	public static void parse(String input, MainScreenController sc) {
		Desktop desktop = Desktop.getDesktop();
		String[] args = input.split(" ");
		String command = args[0];

		if (command.equalsIgnoreCase("/declineDL")) {
			sc.getDlThreads().forEach(e -> {
				if (e.getName().equalsIgnoreCase("DLThread for " + args[1])) {
					e.interrupt();
				}
			});
		} else if (command.equalsIgnoreCase("/declineimg")) {
			sc.getDlThreads().forEach(e -> {
				if (e.getName().equalsIgnoreCase("PicThread for " + args[1])) {
					e.interrupt();
				}
			});
		} else if (command.equalsIgnoreCase("/youtubeplay")) {
			if (sc.getClient().getClientName().equalsIgnoreCase(args[1]) || args[1] .equalsIgnoreCase("all")) {

			}
		} else if (command.equalsIgnoreCase("/linkopen")) {

			if (input.contains("youtube")) {
				try {
					desktop.browse(new URI(input.split(" ", 3)[2]));
				} catch (Exception e) {
					FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
				}
			} else {
				WebView webview = new WebView();
				webview.setOnMouseClicked(click -> {
					click.consume();
					if (click.getClickCount() == 2) {
						try {
							desktop.browse(new URI(input.split(" ", 3)[2]));
						} catch (Exception e) {
							FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
						}
					}
				});
				webview.setPrefSize(500, 500);
				WebEngine webengine = webview.getEngine();
				webengine.load(args[2]);
				sc.getImages().getChildren().add(webview);
			}
		} else if (command.equalsIgnoreCase("/updateusers")) {
			sc.getUsersArea().setText(input.substring("/updateusers ".length()));
		} else if (command.equalsIgnoreCase("/kicked")) {
			sc.logout();
			Popups.startInfoDlg("Kicked from server!", "Kicked from server: " + System.lineSeparator() + input.split("'")[1]);
		} else if (command.equals("*!tell:")) {
			if (sc.getClient().getClientName().equalsIgnoreCase(args[2]) || args[2].equals("all")) {
				System.out.println("Message sent from " + args[1] + " to " + args[2]);
				sc.getClient().sendMessage("From " + "[" + args[1] + " ] " + SystemInfo.getDate() +  ": " + input.substring(nthOccurrence(input, " ", 2)));
			}
		} else if (command.equalsIgnoreCase("*!admind")) {
			sc.getClient().setAdmin(true);
		} else if (command.equalsIgnoreCase("*!link:")) {
			try {
				if (sc.getClient().getClientName().equalsIgnoreCase(args[1]) || args[1].equals("all")) {
					try {
						desktop.browse(new URI(input.split(" ", 3)[2]));
					} catch (Exception e) {
						FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
						FileHandler.debugPrint("Input: " + input);
					}
				}
			} catch (Exception e) {
				FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
				FileHandler.debugPrint("Input: " + input);
			}
		}

	}

	/**
	 * Parses commands coming from the chat window.
	 * @param input The command to parse, with arguments.
	 * @param sc The windows controller sending the commands.
	 */
	public static void parseMSC(String input, MainScreenController sc) {
		Desktop desktop = Desktop.getDesktop();
		String[] args = input.split(" ");
		String command = args[0];

		if (command.equals("/tell")) {

			String message = input.split(" ", 2)[1];

			try {
				String toSend = "*!tell: " + sc.getClient().getClientName() + " " + message;
				System.out.println("To send: " + toSend);
				sc.getClient().sendCommand(toSend);
				sc.addMessage("To " + System.lineSeparator() + "[" + args[1] + "] " + SystemInfo.getDate() + ": " + message.substring(message.indexOf(" ") + 1), "green", "black");
				sc.getChatField().clear();
			} catch (Exception e) {
				FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
			}
		} else if (command.equalsIgnoreCase("/clear")) {
			sc.getChatBox().getChildren().clear();
			sc.getChatField().clear();
		} else if (command.equalsIgnoreCase("/purge")) {
			sc.getChatBox().getChildren().clear();
			sc.getImages().getChildren().clear();
			sc.getChatField().clear();
			new File(FileHandler.chatLogPath).delete();
		} else if (command.equalsIgnoreCase("/link")) {
			sc.addMessage("Linked " + args[1] + " to " + args[2] + ".", "blue", "black");
			sc.getClient().sendCommand("*!link: " + args[1] + input.substring(nthOccurrence(input, " ", 1)));
			sc.getChatField().clear();
		} else if (command.equalsIgnoreCase("/users")) {
			sc.getClient().sendCommand("*!users: " + sc.getClient().getClientName());
			sc.getChatField().clear();
		} else if (command.equalsIgnoreCase("/sendfile")) {
			sendFile(input, sc);
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
				FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
			}
		} else if (command.equalsIgnoreCase("/youtube")) {
			sc.getClient().sendCommand("*!youtube" + input.substring(input.indexOf(" ")));
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
						FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
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
					FileHandler.transmitAudio(sc.getClient(), file);
				});
				audioThread.setDaemon(true);
				audioThread.start();
			});

		} else if (command.equalsIgnoreCase("/kick") && sc.getClient().isAdmin()) {
			sc.getClient().sendCommand("*!kick " + args[1] + " '" + input.split("'")[1] + "'");
		}

	}

	/**
	 * Sends a file through the controller's client to the remote clients specified by the arguments given by the user.
	 * @param input THe input used to determine the file's target.
	 * @param sc The screen controller whose client we are using.
	 */
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
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
		}
	}

}