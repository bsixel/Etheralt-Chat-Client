package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import javafx.application.Platform;
import tools.CommandParser;
import tools.FileHandler;
import tools.Popups;
import tools.SystemInfo;

public class ClientConnection {
	
	//Lists
	
	//Objects
	private Socket textSocket;
	private DataInputStream acceptedData;
	private DataOutputStream sendingData;
	private Socket DLSocket;
	private DataInputStream DLAcceptedData;
	private DataOutputStream DLSendingData;
	private Socket voiceSocket;
	private DataInputStream voiceAcceptedData;
	private DataOutputStream voiceSendingData;
	private Server server;
	
	//Numbers
	int clientID;
	//int count;
	
	//Strings
	private String clientName;
	
	public ClientConnection(Socket socket, Socket DLSocket, Socket voiceSocket, int clientID, Server server) {
		this.textSocket = socket;
		this.voiceSocket = voiceSocket;
		this.DLSocket = DLSocket;
		this.clientID = clientID;
		this.setServer(server);
	}
	
	public void startConnection() {
		
		try {
			this.acceptedData = new DataInputStream(this.textSocket.getInputStream());
			this.sendingData = new DataOutputStream(this.textSocket.getOutputStream());
			this.DLAcceptedData = new DataInputStream(this.DLSocket.getInputStream());
			this.DLSendingData = new DataOutputStream(this.DLSocket.getOutputStream());
			this.voiceAcceptedData = new DataInputStream(this.voiceSocket.getInputStream());
			this.voiceSendingData = new DataOutputStream(this.voiceSocket.getOutputStream());
			
			while (true) {
				
				String input = this.acceptedData.readUTF().trim();
				if (input.startsWith("*!givename:")) {
					this.setClientName(input.substring(input.indexOf(" ") + 1));

					synchronized (this.getServer().getUsers()) {
						if (this.getClientName() == null || this.getClientName() == "") {
							return;
						}
						
						if (!this.getServer().getUsers().stream().anyMatch(e -> e.equals(this.getClientName()))) {
							this.getServer().getUsers().add(this);
							this.getSendingData().writeUTF("*!granted");
							break;
						}
					}
				}
			}
			
			this.getServer().getClients().add(this.getSendingData());
			
			this.getServer().getUsers().forEach(e -> {
				try {
					e.getCC().getSendingData().writeUTF("*![System] " + SystemInfo.getDate() + ": " + this.getClientName() + " has connected.");
					System.out.println("*![System] " + SystemInfo.getDate() + ": " + this.getClientName() + " has connected.");
					String users = "Connected users: ";
					for (int i = 0; i < this.getServer().getUsers().size(); i++) {
						if (i == 0) {
							users = users + this.getServer().getUsers().get(i).clientName;
						} else if (!users.contains(this.getServer().getUsers().get(i).clientName)) {
							users = users + ", " + this.getServer().getUsers().get(i).clientName;
						}
					}
					
					e.getSendingData().writeUTF("/adduser: " + users);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			});
			
			Thread dlThread = genDLThread();
			dlThread.setDaemon(true);
			dlThread.start();
			
			while (true) {
				
				String received = this.acceptedData.readUTF().trim();
				if (received == null || received == "") {
					return;
				}
				
				getServer().getUsers().forEach(e -> {
					
					if (received.startsWith("*!")) {
						System.out.println("Received: " + received);
						if (received.startsWith("*![System]")) {
							e.getSendingData().writeUTF(received.substring(received.indexOf("[")));
						} else {
							try {
								CommandParser.parse(received, e);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					} else {
						try {
							e.getSendingData().writeUTF("[" + this.getClientName() + "] " + SystemInfo.getDate() +  ": " + received);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						
					}
					
				});
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			getServer().getUsers().remove(this);
			getServer().getUsers().forEach(e -> {
				try {
					getSendingData().writeUTF("*![System] " + SystemInfo.getDate() + ": " + this.clientName + " has disconnected.");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});
			try {
				this.textSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
	}
	
	public Thread genDLThread() {
		Thread dlThread = new Thread(() -> {
			
			while (true) {
				try {
					getServer().getUsers().forEach(e -> {
						int count;
						byte[] buffer = new byte[8192];
						try {
							while((count = this.DLAcceptedData.read(buffer, 0, 8192)) >= 0) {
								e.DLSendingData.write(buffer, 0, count);
								System.out.println("CC wrote " + count + " bytes.");
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						} finally {
							try {
								e.DLSendingData.flush();
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
		
		return dlThread;
		
	}
	
	public void setClientName(String name) {
		this.clientName = name;
	}

	public String getClientName() {
		return clientName;
	}

	public DataOutputStream getSendingData() {
		return sendingData;
	}

	public void setSendingData(DataOutputStream sendingData) {
		this.sendingData = sendingData;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Socket getSocket() {
		return textSocket;
	}

	public void setSocket(Socket socket) {
		this.textSocket = socket;
	}

	public DataInputStream getAcceptedData() {
		return acceptedData;
	}

	public void setAcceptedData(DataInputStream acceptedData) {
		this.acceptedData = acceptedData;
	}

	public Socket getDLSocket() {
		return DLSocket;
	}

	public void setDLSocket(Socket dLSocket) {
		DLSocket = dLSocket;
	}

	public DataInputStream getDLAcceptedData() {
		return DLAcceptedData;
	}

	public void setDLAcceptedData(DataInputStream dLAcceptedData) {
		DLAcceptedData = dLAcceptedData;
	}

	public DataOutputStream getDLSendingData() {
		return DLSendingData;
	}

	public void setDLSendingData(DataOutputStream dLSendingData) {
		DLSendingData = dLSendingData;
	}

	public Socket getVoiceSocket() {
		return this.voiceSocket;
	}

	public void setVoiceSocket(Socket voiceSocket) {
		this.voiceSocket = voiceSocket;
	}

	public DataInputStream getVoiceAcceptedData() {
		return voiceAcceptedData;
	}

	public void setVoiceAcceptedData(DataInputStream voiceAcceptedData) {
		this.voiceAcceptedData = voiceAcceptedData;
	}

	public DataOutputStream getVoiceSendingData() {
		return voiceSendingData;
	}

	public void setVoiceSendingData(DataOutputStream voiceSendingData) {
		this.voiceSendingData = voiceSendingData;
	}

	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
	}
	
}