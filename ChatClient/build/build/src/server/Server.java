package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {
	
	//Lists
	private ArrayList<ClientConnection> users = new ArrayList<ClientConnection>();
	private ArrayList<DataOutputStream> clients = new ArrayList<DataOutputStream>();
	
	//Numbers
	int clientID = 0;
	
	//Booleans
	
	//Strings
	
	//Objects
	private ServerSocket server;
	private ServerSocket DLServer;
	private ServerSocket VoiceServer;
	private DataInputStream serverInputData;
	private DataOutputStream serverOutputData;
	
	public void startServer(int port) throws IOException {
		
		try {
			this.server = new ServerSocket(port);
			this.DLServer = new ServerSocket(port + 1);
			this.VoiceServer = new ServerSocket(port + 2);
			
			while (true) {
				ClientConnection client = new ClientConnection(server.accept(), DLServer.accept(), VoiceServer.accept(), clientID++, this);
				
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

	public DataOutputStream getServerSendingData() {
		return serverOutputData;
	}
	
	public ArrayList<ClientConnection> getUsers() {
		return this.users;
	}
	
	public ArrayList<DataOutputStream> getClients() {
		return clients;
	}

	public void setClients(ArrayList<DataOutputStream> clients) {
		this.clients = clients;
	}

	public void addUser(ClientConnection user) {
		this.users.add(user);
	}

	@Override
	public void run() {
	}

	public DataInputStream getServerInputData() {
		return this.serverInputData;
	}
	
}