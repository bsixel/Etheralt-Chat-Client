package server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
	private PrintStream out;
	
	/**
	 * Starts the server with the desired port and password. Also takes in the System.out of the main thread in case some change is made which affects the way java handles printing to the console.
	 * @param port
	 * @param password
	 * @param out
	 * @throws IOException
	 */
	public void startServer(int port, String password, PrintStream out) throws IOException {
		this.password = password;
		this.out = out;
		FileHandler.generateConfigFile();
		
		try {
			this.server = new ServerSocket(port);
			this.DLServer = new ServerSocket(port + 1);
			this.VoiceServer = new ServerSocket(port + 2);
			this.picServer = new ServerSocket(port + 3);
			out.println("Server started successfully.");
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
	 * Starts the server with the desired port and password. Also takes in the System.out of the main thread in case some change is made which affects the way java handles printing to the console.
	 * @throws IOException
	 */
	public void startServer() throws IOException {
		this.out = System.out;
		FileHandler.generateConfigFile();
		int port = Integer.parseInt(FileHandler.getProperty("last_port"));
		
		
		try {
			
			this.server = new ServerSocket(port);
			this.DLServer = new ServerSocket(port + 1);
			this.VoiceServer = new ServerSocket(port + 2);
			this.picServer = new ServerSocket(port + 3);
			out.println("Server started successfully.");
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
	 * Starts the server with the desired port and password. Also takes in the System.out of the main thread in case some change is made which affects the way java handles printing to the console.
	 * @throws IOException
	 */
	public void startDefaultServer() throws IOException {
		this.out = System.out;
		FileHandler.generateConfigFile();
		int port = 25566;
		
		
		try {
			this.server = new ServerSocket(port);
			this.DLServer = new ServerSocket(port + 1);
			this.VoiceServer = new ServerSocket(port + 2);
			this.picServer = new ServerSocket(port + 3);
			out.println("Server started successfully.");
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

	public ServerSocket getServer() {
		return server;
	}

	public ArrayList<User> getUsersSubList() {
		return usersSubList;
	}

	public void setUsersSubList(ArrayList<User> usersSubList) {
		this.usersSubList = usersSubList;
	}

	public ObservableList<User> getUsers() {
		synchronized (users) {
			return users;
		}
	}

	public void setUsers(ObservableList<User> users) {
		this.users = users;
	}
	
	public void addUser(User user) {
		this.users.add(user);
	}
	
	public void killUser(String name, String reason) {
		Iterator<User> iter = getUsers().iterator();
		while (iter.hasNext()) {
			User u;
			synchronized (u = iter.next()) {
				if (u.getDisplayName().equalsIgnoreCase(name)) {
					out.println("Killed user " + u.getDisplayName());
					try {
						u.getCC().getSendingData().writeUTF("/kicked: '" + reason + "'");
						u.getCC().getDLSocket().close();
						u.getCC().getSocket().close();
						u.getCC().getVoiceSocket().close();
					} catch (SocketException e) {
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		getUsers().removeIf(u -> u.getDisplayName().equalsIgnoreCase(name));
	}
	
	public void killUserAuto(String name, String reason) {
		Iterator<User> iter = getUsers().iterator();
		while (iter.hasNext()) {
			User u;
			synchronized (u = iter.next()) {
				if (u.getDisplayName().equalsIgnoreCase(name)) {
					out.println("Killed user " + u.getDisplayName());
					try {
						u.getCC().getSendingData().writeUTF("/kicked: '" + reason + "'");
						u.getCC().getDLSocket().close();
						u.getCC().getSocket().close();
						u.getCC().getVoiceSocket().close();
					} catch (SocketException e) {
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}

	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
	}
	
	public void addClientID() {
		this.clientID ++;
	}
	
	public void subClientID() {
		this.clientID --;
	}

	public ServerSocket getDLServer() {
		return DLServer;
	}

	public void setDLServer(ServerSocket dLServer) {
		DLServer = dLServer;
	}

	public ServerSocket getVoiceServer() {
		return VoiceServer;
	}

	public void setVoiceServer(ServerSocket voiceServer) {
		VoiceServer = voiceServer;
	}

	public ServerSocket getPicServer() {
		return picServer;
	}

	public void setPicServer(ServerSocket picServer) {
		this.picServer = picServer;
	}

	public void setServer(ServerSocket server) {
		this.server = server;
	}
	
	public String getPassword() {
		return this.password;
	}

	public int getPortStart() {
		return server.getLocalPort();
	}
	
}