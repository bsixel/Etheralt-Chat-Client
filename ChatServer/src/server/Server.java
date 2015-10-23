package server;

import static tools.FileHandler.debugPrint;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tools.CommandParser;
import tools.FileHandler;

/**
 * 
 * @author Ben Sixel
 * The main server object. Controls general server maintenance, although it is not in charge of taking user import server-side.
 * Handles the list of connected users and their respective server-side connections.
 * Can be started either with no arguments and defaulting to no password on port 25566 or with a desired password and port.
 *
 */

public class Server {
	
	//Lists
	private ArrayList<User> usersSubList = new ArrayList<User>();
	private ObservableList<User> users = FXCollections.observableArrayList(usersSubList);
	
	//Numbers
	int clientID = 0;
	
	//Booleans
	
	//Strings
    String initStr = "Connected users:";
    String str;
    private String password = "default";
	
	//Objects
	private ServerSocket server;
	private ServerSocket DLServer;
	private ServerSocket VoiceServer;
	private ServerSocket picServer;
	
	/**
	 * Starts the server with the desired port and password.
	 * @param port The initial port with which we should attempt to start the server.
	 * @param password The password we will use to secure the server.
	 * @throws IOException Thrown if the server loses a network connection.
	 */
	public void startServer(int port, String password) throws IOException {
		this.password = password;
		FileHandler.generateConfigFile();
		
		try {
			this.server = new ServerSocket(port);
			this.DLServer = new ServerSocket(port + 1);
			this.VoiceServer = new ServerSocket(port + 2);
			debugPrint("Server started successfully.");
			FileHandler.saveProperties(this);
			System.out.println("Saved server preferences.");
			System.out.print("> ");
			while (true) {
				ClientConnection client = new ClientConnection(server.accept(), DLServer.accept(), VoiceServer.accept(), picServer.accept(), clientID++, this);
				Thread clientThread = new Thread(() -> {
					System.out.println("Adding new client!");
					client.startConnection();
				});
				clientThread.setDaemon(true);
				clientThread.start();
			}
			
		} finally {
			this.server.close();
			this.DLServer.close();
			this.VoiceServer.close();
		}
		
	}
	
	/**
	 * Starts the server with the last port and the last password used, stored in the properties file.
	 * @throws IOException Thrown if the server loses a network connection.
	 */
	public void startServer() throws IOException {
		FileHandler.generateConfigFile();
		int port = Integer.parseInt(FileHandler.getProperty("last_port"));
		this.password = FileHandler.getProperty("last_password");
		
		try {
			
			this.server = new ServerSocket(port);
			this.DLServer = new ServerSocket(port + 1);
			this.VoiceServer = new ServerSocket(port + 2);
			this.picServer = new ServerSocket(port + 3);
			debugPrint("Server started successfully.");
			FileHandler.saveProperties(this);
			System.out.println("Saved server preferences.");
			System.out.print("> ");
			while (true) {
				ClientConnection client = new ClientConnection(server.accept(), DLServer.accept(), VoiceServer.accept(), picServer.accept(), clientID++, this);
				Thread clientThread = new Thread(() -> {
					System.out.println("Adding new client!");
					client.startConnection();
				});
				clientThread.setDaemon(true);
				clientThread.start();
				
			}
			
		} finally {
			this.server.close();
			this.DLServer.close();
			this.VoiceServer.close();
		}
		
	}
	
	/**
	 * Starts the server with the default port (25566) and password ('default' or none).
	 * @throws IOException Thrown if the server loses a network connection.
	 */
	public void startDefaultServer() throws IOException {
		FileHandler.generateConfigFile();
		int port = 25566;
		
		
		try {
			this.server = new ServerSocket(port);
			this.DLServer = new ServerSocket(port + 1);
			this.VoiceServer = new ServerSocket(port + 2);
			this.picServer = new ServerSocket(port + 3);
			debugPrint("Server started successfully.");
			FileHandler.saveProperties(this);
			System.out.println("Saved server preferences.");
			System.out.print("> ");
			while (true) {
				ClientConnection client = new ClientConnection(server.accept(), DLServer.accept(), VoiceServer.accept(), picServer.accept(), clientID++, this);
				Thread clientThread = new Thread(() -> {
					System.out.println("Adding new client!");
					client.startConnection();
				});
				clientThread.setDaemon(true);
				clientThread.start();
				
			}
			
		} finally {
			this.server.close();
			this.DLServer.close();
			this.VoiceServer.close();
		}
		
	}
	
	/**
	 * Method for removing users forcefully from the server (kicks, client-side disconnects, etc).
	 * @param name The user being removed.
	 * @param reason The reason for which the user is being removed.
	 */
	public void killUser(String name, String reason) {
		Iterator<User> iter = getUsers().iterator();
		while (iter.hasNext()) {
			User u;
			synchronized (u = iter.next()) {
				if (u.getDisplayName().equalsIgnoreCase(name)) {
					debugPrint("Killed user " + u.getDisplayName());
					try {
						CommandParser.parse("/updateusers", this);
						u.getCC().getSendingData().writeUTF("/kicked: '" + reason + "'");
						u.getCC().getDLSocket().close();
						u.getCC().getSocket().close();
						u.getCC().getVoiceSocket().close();
					} catch (Exception e) {
						debugPrint(e.getStackTrace()[0].toString());
					}
				}
			}
		}
		getUsers().removeIf(u -> u.getDisplayName().equalsIgnoreCase(name));
	}
	
	/*
	 * Some getters and setters.
	 */
	
	/**
	 * Getter for this server.
	 * @return This server.
	 */
	public ServerSocket getServer() {
		return server;
	}

	/**
	 * Getter for the observable connected users list. Because it is an ObservableList, it can have change notifiers.
	 * @return An observable list of connected users.
	 */
	public ObservableList<User> getUsers() {
		synchronized (users) {
			return users;
		}
	}
	
	/**
	 * Adds a user to the list of connected users.
	 * @param user The user we are adding.
	 */
	public void addUser(User user) {
		this.getUsers().add(user);
	}
	
	/**
	 * Essentially a counter for the number of users that have ever to the server.
	 * @return The clientID  field value.
	 */
	public int getClientID() {
		return this.clientID;
	}
	
	/**
	 * Adds one to the {@link clientID} field.
	 */
	public void addClientID() {
		this.clientID ++;
	}
	
	/**
	 * Subtracts one from the {@link clientID} field.
	 */
	public void subClientID() {
		this.clientID --;
	}

	/**
	 * Getter for the standard server socket for file transfers.
	 * @return The standard server socket for file transfer data.
	 */
	public ServerSocket getDLServer() {
		return DLServer;
	}

	/**
	 * Getter for the standard voice server socket.
	 * @return The standard voice server socket.
	 */
	public ServerSocket getVoiceServer() {
		return VoiceServer;
	}

	/**
	 * Getter for the server socket for image data.
	 * @return The server socket for image data.
	 */
	public ServerSocket getPicServer() {
		return picServer;
	}
	
	/**
	 * Getter for the server's password.
	 * @return The server's password.
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * Getter for the starting, local port.
	 * @return The standard local port for the server.
	 */
	public int getPortStart() {
		return server.getLocalPort();
	}
	
}