package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
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
	private ServerSocket picServer;
	
	public void startServer(int port) throws IOException {
		
		try {
			this.server = new ServerSocket(port);
			this.DLServer = new ServerSocket(port + 1);
			this.VoiceServer = new ServerSocket(port + 2);
			this.picServer = new ServerSocket(port + 3);

			
			Thread mehThread = new Thread (() -> {
				while (true) {
					users.forEach(e -> {
						try {
							e.getSendingData().writeUTF("This message SHOULD appear every five seconds.");
							Thread.sleep(5000);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					});
					System.out.println("This message SHOULD appear every five seconds.");
				}
			});
			mehThread.setDaemon(true);
			//mehThread.start();
			
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
	
}