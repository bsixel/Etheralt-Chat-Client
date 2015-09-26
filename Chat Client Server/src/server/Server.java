package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import tools.SystemInfo;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import server.User;

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
    private String password = "";
	
	//Objects
	private ServerSocket server;
	private ServerSocket DLServer;
	private ServerSocket VoiceServer;
	private ServerSocket picServer;
	
	public void startServer(int port, boolean standalone, String password) throws IOException {
		
		this.users.addListener(new ListChangeListener<User>() {
			 
            @SuppressWarnings("rawtypes")
			@Override
            public void onChanged(ListChangeListener.Change change) {
            	str = initStr;
                getUsers().forEach(u -> {
                	if (str.split(" ").length < 3) {
                		str = str + " " + u.getDisplayName();
                	} else {
                		str = str + ", " + u.getDisplayName();
                	}
                });
                getUsers().forEach(u -> {
                	try {
                		change.next();
                		u.getCC().getSendingData().writeUTF("[System] " + SystemInfo.getDate() + ": " + ((User) change.getAddedSubList().get(0)).getCC().getClientName() + " has connected.");
                		System.out.println("[System] " + SystemInfo.getDate() + ": " + ((User) change.getAddedSubList().get(0)).getCC().getClientName() + " has connected.");
						u.getCC().getSendingData().writeUTF("/updateusers " + str);
					} catch (Exception e) {
						e.printStackTrace();
					}
                });
            }
        });
		
		try {
			this.server = new ServerSocket(port);
			this.DLServer = new ServerSocket(port + 1);
			this.VoiceServer = new ServerSocket(port + 2);
			this.picServer = new ServerSocket(port + 3);
			
			while (true) {
				ClientConnection client = new ClientConnection(server.accept(), DLServer.accept(), VoiceServer.accept(), picServer.accept(), clientID++, this);
				
				Thread clientThread = new Thread(() -> {
					client.startConnection();
					Thread.currentThread().interrupt();
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
		return users;
	}

	public void setUsers(ObservableList<User> users) {
		this.users = users;
	}
	
	public void addUser(User user) {
		this.users.add(user);
	}
	
	public void killUser(String name) {
		getUsers().forEach(u -> {
			if (u.getDisplayName().equalsIgnoreCase(name)) {
				getUsers().remove(u);
				try {
					u.getCC().getDLSocket().close();
					u.getCC().getSocket().close();
					u.getCC().getVoiceSocket().close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
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