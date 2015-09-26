package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;

import tools.CommandParser;
import tools.SystemInfo;

public class ClientConnection {
	
	//booleans
	private boolean running = true;
	
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
	private Socket picSocket;
	private DataInputStream picAcceptedData;
	private DataOutputStream picSendingData;
	private Server server;
	
	//Numbers
	int clientID;
	//int count;
	
	//Strings
	private String clientName;
	
	public ClientConnection(Socket socket, Socket DLSocket, Socket voiceSocket, Socket picSocket, int clientID, Server server) {
		this.textSocket = socket;
		this.voiceSocket = voiceSocket;
		this.DLSocket = DLSocket;
		this.picSocket = picSocket;
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
			this.picAcceptedData = new DataInputStream(this.picSocket.getInputStream());
			this.picSendingData = new DataOutputStream(this.picSocket.getOutputStream());
			
			while (true) {
				
				String input = this.acceptedData.readUTF().trim();
				String[] args = input.split(" ");
				if (input.startsWith("*!givename:")) {
					this.setClientName(args[1]);

					synchronized (this.getServer().getUsers()) {
						if (this.getClientName() == null || this.getClientName() == "") {
							continue;
						}
						
						if (!this.getServer().getUsers().stream().anyMatch(e -> e.equals(this.getClientName())) && args[2] == this.getServer().getPassword()) {
							this.getSendingData().writeUTF("*!granted");
							this.getServer().getUsers().add(new User(this.getClientName(), args[2], this));
							break;
						}
					}
				}
			}
			
			/*this.getServer().getUsers().forEach(e -> {
				try {
					e.getCC().getSendingData().writeUTF("[System] " + SystemInfo.getDate() + ": " + this.getClientName() + " has connected.");
					System.out.println("*![System] " + SystemInfo.getDate() + ": " + this.getClientName() + " has connected.");
					String users = "Connected users: ";
					for (int i = 0; i < this.getServer().getUsers().size(); i++) {
						if (i == 0) {
							users = users + this.getServer().getUsers().get(i).getCC().clientName;
						} else if (!users.contains(this.getServer().getUsers().get(i).getCC().clientName)) {
							users = users + ", " + this.getServer().getUsers().get(i).getCC().clientName;
						}
					}
					
					e.getCC().getSendingData().writeUTF("/adduser: " + users);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			});*/
			
			Thread audioThread = new Thread(() -> {
				byte[] buffer = new byte[8192];
				int count = 0;
				while (true) {
					try {
						count = this.voiceAcceptedData.read(buffer);
						this.voiceSendingData.write(buffer, 0, count);
					} catch (Exception e1) {
						if (!this.running) {
						e1.printStackTrace();
						}
						break;
					}
				}
			});
			audioThread.setDaemon(true);
			audioThread.start();
			
			while (this.running) {
				
				String received = this.acceptedData.readUTF().trim();
				if (received == null || received == "") {
					return;
				}
				
				if (received.equalsIgnoreCase("*![System] " + SystemInfo.getDate() + ": " + this.clientName + " has disconnected.") || received.equalsIgnoreCase("*![System] " + SystemInfo.getDate() + ": " + this.clientName + " has disconnected.")) {
					this.getDLSocket().close();
					this.getSocket().close();
					this.getVoiceSocket().close();
					this.acceptedData.close();
					this.sendingData.close();
					this.DLAcceptedData.close();
					this.DLSendingData.close();
					this.voiceAcceptedData.close();
					this.voiceSendingData.close();
					this.picAcceptedData.close();
					this.picSendingData.close();
					
					Iterator<User> iter = this.getServer().getUsers().iterator();
					/*this.getServer().getUsers().forEach(u -> {
						if (u.getCC().getClientName().equalsIgnoreCase(getClientName())) {
							this.getServer().getUsers().remove(u);
						}
					});*/
					iter.forEachRemaining(u -> {
						if (u.getCC().getClientName().equalsIgnoreCase(getClientName())) {
							iter.remove();
						}
					});
					this.setRunning(false);
					Thread.currentThread().interrupt();
					break;
				}
				
				getServer().getUsers().forEach(e -> {
					
					if (received.startsWith("*!")) {
						System.out.println("Received: " + received);
						try {
							CommandParser.parse(received, e.getCC(), this);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					} else {
						try {
							e.getCC().getSendingData().writeUTF("[" + this.getClientName() + "] " + SystemInfo.getDate() +  ": " + received);
							System.out.println("[" + this.getClientName() + "] " + SystemInfo.getDate() +  ": " + received);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						
					}
					
				});
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				//getSendingData().writeUTF("*![System] " + SystemInfo.getDate() + ": " + this.clientName + " has disconnected.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			getServer().getUsers().remove(this);
			try {
				this.textSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
	}

	int dlcount;
	
	public Thread genDLThread(String input) throws Exception {
		Thread dlThread = new Thread(() -> {
			byte[] buffer = new byte[8192];
			String[] args = input.split(" ");
			long length = Long.parseLong(args[4]);
			try {
				long total = 0;
				int n = 0;
				while (total != length) {
					dlcount = this.DLAcceptedData.read(buffer, 0, 8192);
					total += dlcount;
					n += 1;
					getServer().getUsers().stream().map(u -> u.getCC()).forEach(e -> {
						if (e.clientName.equalsIgnoreCase(args[2]) || args[2].equalsIgnoreCase("all")) {
							try {
								e.DLSendingData.write(buffer, 0, dlcount);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					});
				}
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			Thread.currentThread().interrupt();
		});
		return dlThread;
	}
	
	int piccount;
	public Thread genPicThread(String input) throws Exception {
		Thread picThread = new Thread(() -> {
			byte[] buffer = new byte[8192];
			String[] args = input.split(" ");
			long length = Long.parseLong(args[4]);
			try {
				long total = 0;
				int n = 0;
				while (total != length) {
					piccount = this.picAcceptedData.read(buffer, 0, 8192);
					total += piccount;
					n += 1;
					getServer().getUsers().stream().map(u -> u.getCC()).forEach(e -> {
						if (e.clientName.equalsIgnoreCase(args[2]) || args[2].equalsIgnoreCase("all")) {
							try {
								//FileHandler.writeToErrorLog("CC wrote " + );
								e.picSendingData.write(buffer, 0, piccount);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					});
				}
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			Thread.currentThread().interrupt();
		});
		return picThread;
	}

	public void sendFile(String input) {
		
		Thread dlThread = null;
		try {
			dlThread = genDLThread(input);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		dlThread.setDaemon(true);
		dlThread.start();
		
	}
	
	public void sendImg(String input) {
		
		Thread picThread = null;
		try {
			picThread = genPicThread(input);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		picThread.setDaemon(true);
		picThread.start();
		
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
	
	public void setRunning(boolean b) {
		this.running = b;
	}
	
}