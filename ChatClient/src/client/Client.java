package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javafx.application.Platform;
import tools.CommandParser;
import tools.DataPacket;
import tools.FileHandler;
import userInteract.LoginScreenController;
import userInteract.Popups;
import static tools.FileHandler.debugPrint;

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

public class Client {

	//booleans
	private boolean running = true;
	private boolean admin = false;

	//Objects
	private Socket textSocket;
	private ObjectInputStream recevingData;
	private ObjectOutputStream sendingData;

	//Strings
	private String clientName;

	/**
	 * Starts the client running, receiving, and sending data.
	 * @param IP The remote to which we are connecting.
	 * @param port The starting port used for the connection. The application uses this port and the proceeding three.
	 * @param ls The LoginScreenController used for controlling the UI.
	 * @param password The password used to connect to the remote server.
	 * @param lock An object used to lock the UI thread until login is either successful or timed out.
	 * @throws IOException If connection is lost to the remote server and somehow not caught by the many catches within.
	 * @throws ClassNotFoundException If there is an unknown object type read from the input stream.
	 */
	public void startClient(String IP, int port, LoginScreenController ls, String password, Object lock) throws IOException, ClassNotFoundException {

		this.textSocket = new Socket(InetAddress.getByName(IP), port);
		this.sendingData = new ObjectOutputStream(this.textSocket.getOutputStream());
		this.recevingData = new ObjectInputStream(this.textSocket.getInputStream());
		System.out.println("Client 61");

		String editedName = ls.getUsernameField().getText();
		int n = 1;
		while (true) {

			if (password.equalsIgnoreCase("")) {
				this.sendingData.writeObject(new DataPacket("message", "all", "all", "*!givename: " + editedName.trim() + " " + FileHandler.getProperty("computer_ID") + " default", null));
			} else {
				this.sendingData.writeObject(new DataPacket("message", "all", "all", "*!givename: " + editedName.trim() + " " + FileHandler.getProperty("computer_ID") + " " + password, null));
			}
			DataPacket packet = (DataPacket) this.recevingData.readObject();
			String input = packet.getMessage();
			FileHandler.debugPrint("Input: " + input);
			if (input.equalsIgnoreCase("*!decline:password")) {
				FileHandler.debugPrint("Declined password: " + password);
				ls.setLocked(false);
				ls.setNameTaken(true);
				synchronized (lock) {
					lock.notifyAll();
				}
			}
			if (input.equalsIgnoreCase("*!decline:username")) {
				FileHandler.debugPrint("Declined username.");
				ls.setLocked(false);
				ls.setNameTaken(true);
				synchronized (lock) {
					lock.notifyAll();
				}
			}
			if (input.equalsIgnoreCase("*!granted")) {
				FileHandler.debugPrint("Granted.");
				synchronized (lock) {
					ls.setLocked(false);
					lock.notifyAll();
				}
				this.clientName = editedName;
				ls.setUsername(editedName);
				break;
			} else {
				editedName = editedName + n;
				n++;
			}
		}
		ls.setLocked(false);

		try {

			PacketHandler packetHandler = new PacketHandler(ls);

			while (this.running) {

				String previous = null;
				DataPacket packet;
				if ((packet = (DataPacket) this.recevingData.readObject()) == null) {
					continue;
				}
				String input = packet.getMessage();

				if (packet.getType().equals("dlpacket")) {
					packetHandler.feedDLPack(packet);
					continue;
				} else if (packet.getType().equals("imgpacket")) {
					packetHandler.feedImgPacket(packet);
					continue;
				} else if (packet.getType().equals("audiopacket")) {
					packetHandler.feedAudioPacket(packet);
					continue;
				} else if (packet.getType().equals("voicepacket")) {
					packetHandler.feedVoicePacket(packet);
					continue;
				} else if (packet.getType().equals("command")) {
					Platform.runLater(() -> {
						CommandParser.parse(input, ls.getMainController());
					});
				} else if (packet.getType().equals("message") && !input.equalsIgnoreCase(previous)) {
					if (input.startsWith("*![System") || input.startsWith("*![Server")) {
						String phrase = input.trim().substring(input.trim().indexOf("!") + 1);
						ls.getMainController().addMessage(phrase, "blue", "black");
					}else if (packet.getFrom().startsWith("[" + this.clientName + "]")) {
						ls.getMainController().addMessage(input, "indigo", "black");
					} else {
						ls.getMainController().addMessage(input, "darkviolet", "black");
					}

				}
				previous = input;
			}
			FileHandler.debugPrint("Stopped running client.");
			this.recevingData.close();
			this.sendingData.close();
		} catch (java.io.EOFException e) {
			Platform.runLater(() -> {
				ls.getMainController().logout();
				FileHandler.debugPrint("Kicked from server! Server closed or otherwise lost connection.");
				Popups.startInfoDlg("Kicked from server!", "Kicked from server: " + System.lineSeparator() + "Server closed or lost connection.");
			});
			FileHandler.debugPrint("You probably just closed the chat client window. Eventually I'll figure out how to make it gracefully shut down.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Getter for the client's text sending stream.
	 * @return The ObjectOutputStream used for sending chat text from the client.
	 */
	public ObjectOutputStream getSendingStream() {
		return sendingData;
	}

	/**
	 * Getter for the client's text receiving stream.
	 * @return The ObjectOutputStream used for sending chat text from the client.
	 */
	public ObjectInputStream getRecevingStream() {
		return recevingData;
	}

	/**
	 * Getter for the client's name.
	 * @return The client's name.
	 */
	public String getClientName() {
		return clientName;
	}

	/**
	 * The getter for the client's name.
	 * @return The client's name.
	 */
	public String getClientname() {
		return clientName;
	}

	/**
	 * Returns a string representation of the client. In this case, the client's name.
	 */
	public String toString() {
		return clientName;
	}

	/**
	 * Sets whether or not the client is running (sending/receiving data).
	 * @param b A boolean value: True if running.
	 */
	public void setRunning(boolean b) {
		this.running = b;
	}

	/**
	 * Getter for the client's admin status in relation to the currently connected server.
	 * @return A boolean indicating whether the client is admin (true) or not (false) on the server to which it is connected.
	 */
	public boolean isAdmin() {
		return this.admin;
	}

	/**
	 * Setter for whether or not the client is considered an admin on the currently connected server.
	 * @param admin Boolean value: True if the user is an admin, false otherwise.
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	/**
	 * Sends a simple text message through this client.
	 * @param message The message to send.
	 */
	public void sendMessage(String message) {
		try {
			this.sendingData.writeObject(new DataPacket("message", this.clientName, "all", message, null));
		} catch (IOException e) {
			debugPrint("Error sending message data packet!");
//			debugPrint(e.getStackTrace()[0].toString());
			e.printStackTrace();
		}
	}

	/**
	 * Sends a command message through this client.
	 * @param message The message to send.
	 */
	public void sendCommand(String message) {
		String[] args = message.split(" ");
		try {
			this.sendingData.writeObject(new DataPacket("command", this.clientName, args[1], message, null));
		} catch (IOException e) {
			debugPrint("Error sending message data packet!");
			debugPrint(e.getStackTrace()[0].toString());
		}
	}

}