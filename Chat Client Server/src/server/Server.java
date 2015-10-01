package server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import tools.FileHandler;
import tools.SystemInfo;

public class Server implements Runnable {
	
	//Lists
	private ArrayList<User> usersSubList = new ArrayList<User>();
	private ObservableList<User> users = FXCollections.observableArrayList(usersSubList);
	
	//Numbers
	int clientID = 0;
	
	//Booleans
	private boolean standalone = false;
	
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
	
	public void startServer(int port, boolean standalone, String password, PrintStream out) throws IOException {
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
			while (true) {
				ClientConnection client = new ClientConnection(server.accept(), DLServer.accept(), VoiceServer.accept(), picServer.accept(), clientID++, this);
				Thread clientThread = new Thread(() -> {
					System.out.println("Adding new client!");
					client.startConnection(out);
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

	public boolean isStandalone() {
		return standalone;
	}

	public void setStandalone(boolean standalone) {
		this.standalone = standalone;
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
	
	@Override
	public void run() {
	}

	public int getPortStart() {
		return server.getLocalPort();
	}
	
}