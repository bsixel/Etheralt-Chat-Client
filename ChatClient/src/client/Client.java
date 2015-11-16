package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

import javafx.application.Platform;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import tools.CommandParser;
import tools.FileHandler;
import userInteract.LoginScreenController;
import userInteract.Popups;

/**
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
	private DataInputStream textInData;
	private DataOutputStream textOutData;
	private Socket DLSocket;
	private DataInputStream DLInData;
	private DataOutputStream DLOutData;
	private Socket voiceSocket;
	private DataInputStream voiceInData;
	private DataOutputStream voiceOutData;
	private Socket picSocket;
	private DataInputStream picInData;
	private DataOutputStream picOutData;
	
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
	 */
	public void startClient(String IP, int port, LoginScreenController ls, String password, Object lock) throws IOException {
		
		this.textSocket = new Socket(InetAddress.getByName(IP), port);
		this.textInData = new DataInputStream(this.textSocket.getInputStream());
		this.textOutData = new DataOutputStream(this.textSocket.getOutputStream());
		this.DLSocket = new Socket(InetAddress.getByName(IP), port + 1);
		this.DLInData = new DataInputStream(this.DLSocket.getInputStream());
		this.DLOutData = new DataOutputStream(this.DLSocket.getOutputStream());
		this.voiceSocket = new Socket(InetAddress.getByName(IP), port + 2);
		this.voiceInData = new DataInputStream(this.voiceSocket.getInputStream());
		this.voiceOutData = new DataOutputStream(this.voiceSocket.getOutputStream());
		this.picSocket = new Socket(InetAddress.getByName(IP), port + 3);
		this.picInData = new DataInputStream(this.picSocket.getInputStream());
		this.picOutData = new DataOutputStream(this.picSocket.getOutputStream());
		
		String editedName = ls.getUsernameField().getText();
		int n = 1;
		while (true) {
			
			if (password.equalsIgnoreCase("")) {
				this.textOutData.writeUTF("*!givename: " + editedName.trim() + " " + FileHandler.getProperty("computer_ID") + " default");
			} else {
				this.textOutData.writeUTF("*!givename: " + editedName.trim() + " " + FileHandler.getProperty("computer_ID") + " " + password);
			}
			String input = this.textInData.readUTF().trim();
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
					lock.notifyAll();
				}
				ls.setLocked(false);
				this.clientName = editedName;
				ls.setUsername(editedName);
				break;
			} else {
				editedName = editedName + n;
				n++;
			}
		}
		ls.setLocked(false);
		
		Thread audioThread = new Thread(() -> {
			try {
				InputStream stream = this.voiceInData;
				AudioStream audioStream = new AudioStream(stream);
				AudioPlayer.player.start(audioStream);
			} catch (Exception e) {
				if (!this.running) {
					FileHandler.debugPrint("Probably closed the client, still working on graceful audio shutdown.");
				}
			}
		});
		audioThread.setDaemon(true);
		audioThread.start();
		try {
		while (this.running) {
			
			String previous = null;
			String input = this.textInData.readUTF().trim();
			
			if (input != null && !input.equalsIgnoreCase(previous)) {
				
				if (input.startsWith("*![System") || input.startsWith("*![Server")) {
					String phrase = input.trim().substring(input.trim().indexOf("!") + 1);
					ls.getMainController().addMessage(phrase, "blue", "black");

				} else if (input.startsWith("/") || input.startsWith("*!")) {
					Platform.runLater(() -> {
						CommandParser.parse(input, ls.getMainController());
					});
				} else if (input.split(" ")[0].equalsIgnoreCase("[" + this.clientName + "]")) {
					ls.getMainController().addMessage(input, "indigo", "black");
				} else {
					ls.getMainController().addMessage(input, "darkviolet", "black");
				}

			}
			previous = input;
		}
		FileHandler.debugPrint("Stopped running client.");
		this.DLInData.close();
		this.DLOutData.close();
		this.voiceInData.close();
		this.voiceOutData.close();
		this.picInData.close();
		this.picOutData.close();
		this.textInData.close();
		this.textOutData.close();
		} catch (java.io.EOFException e) {
			Platform.runLater(() -> {
				ls.getMainController().logout();
				FileHandler.debugPrint("Kicked from server! Server closed or otherwise lost connection.");
				Popups.startInfoDlg("Kicked from server!", "Kicked from server: " + System.lineSeparator() + "Server closed or lost connection.");
			});
			FileHandler.debugPrint("You probably just closed the chat client window. Eventually I'll figure out how to make it gracefully shut down.");
		}
	}

	/**
	 * Getter for the client's text sending stream.
	 * @return The DataOutputStream used for sending chat text from the client.
	 */
	public DataOutputStream getClientSendingData() {
		return textOutData;
	}

	/**
	 * Getter for the client's name.
	 * @return The client's name.
	 */
	public String getClientName() {
		return clientName;
	}

	/**
	 * Getter for the client's file transfer receiving stream.
	 * @return The DataInputStream used for receiving file transfers.
	 */
	public DataInputStream getDLInData() {
		return DLInData;
	}

	/**
	 * Getter for the client's file transfer sending stream.
	 * @return The DataInputStream used for sending file transfers.
	 */
	public DataOutputStream getDLOutData() {
		return DLOutData;
	}

	/**
	 * Getter for the client's out stream of audio data.
	 * @return The DataOutputStream associated with audio data.
	 */
	public DataOutputStream getVoiceOutData() {
		return voiceOutData;
	}

	/**
	 * Getter for the client's image input stream.
	 * @return The DataInputStream associated with the client's image data.
	 */
	public DataInputStream getPicInData() {
		return picInData;
	}

	/**
	 * Getter for the client's image output stream.
	 * @return The DataOutputStream associated with the client's image data.
	 */
	public DataOutputStream getPicOutData() {
		return picOutData;
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
	
}