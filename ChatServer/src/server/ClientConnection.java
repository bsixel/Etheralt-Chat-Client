package server;

import static tools.FileHandler.chatPrint;
import static tools.FileHandler.debugPrint;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import tools.CommandParser;
import tools.SystemInfo;

/**
 * 
 * @author Ben Sixel
 * ClientConnection: The object controlling the connection between the server and a client.
 * Controls Input and output streams for chat, file sharing, and (in the future) audio.
 *
 */

public class ClientConnection {

	//Booleans
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
    String initStr = "Connected users:";
    String str;

	//Objects

	/**
	 * Initiates the server-side connection to a remote client.
	 * @param socket The socket for text chat data.
	 * @param DLSocket The socket for the file transfer system.
	 * @param voiceSocket The socket for the voice data system; Not fully implemented.
	 * @param picSocket The socket for image/media sharing.
	 * @param clientID The ID given to the client on connecting.
	 * @param server The server which is hosting the connection.
	 */
	public ClientConnection(Socket socket, Socket DLSocket, Socket voiceSocket, Socket picSocket, int clientID, Server server) {
		this.textSocket = socket;
		this.voiceSocket = voiceSocket;
		this.DLSocket = DLSocket;
		this.picSocket = picSocket;
		this.clientID = clientID;
		this.server = server;
	}

	/**
	 * Starts the actual connection. Manages send/receive of data of multilpe kinds, including
	 * chat, file transfer, and eventually audio.
	 * @param printOut
	 */
	public void startConnection() {

		/**
		 * Establishing a connection with the remote client.
		 */
		try {
			this.acceptedData = new DataInputStream(this.textSocket.getInputStream());
			this.sendingData = new DataOutputStream(this.textSocket.getOutputStream());
			this.DLAcceptedData = new DataInputStream(this.DLSocket.getInputStream());
			this.DLSendingData = new DataOutputStream(this.DLSocket.getOutputStream());
			this.voiceAcceptedData = new DataInputStream(this.voiceSocket.getInputStream());
			this.voiceSendingData = new DataOutputStream(this.voiceSocket.getOutputStream());
			this.picAcceptedData = new DataInputStream(this.picSocket.getInputStream());
			this.picSendingData = new DataOutputStream(this.picSocket.getOutputStream());

			
			/**
			 * Verifying that the desired username is not taken,
			 *  and the user is providing the correct password to access the server.
			 */
			while (true) {

				String input = this.acceptedData.readUTF().trim();
				String[] args = input.split(" ");
				if (input.startsWith("*!givename:")) {
					this.setClientName(args[1]);
					System.out.println("Assigning name '" + args[1] + "'");
					synchronized (this.getServer().getUsers()) {
						if (this.getClientName() == null || this.getClientName() == "") {
							continue;
						}
						if (!args[3].equals(this.getServer().getPassword())) {
							this.getSendingData().writeUTF("*!decline:password");
							//Writing to console and error log of the unsuccessful password match.
							debugPrint("User " + this.clientName + " failed to connect with password: '" + args[3] +"' from IP " + textSocket.getInetAddress());
							debugPrint("Desired password: " + this.getServer().getPassword());
							return;
						} else if (this.getServer().getUsers().stream().anyMatch(e -> e.getDisplayName().equalsIgnoreCase(this.getClientName()))) {
							this.getSendingData().writeUTF("*!decline:username");
							return;
						}
						if (!this.getServer().getUsers().stream().anyMatch(e -> e.getDisplayName().equalsIgnoreCase(this.getClientName())) && (args[3].equals(this.getServer().getPassword()) || this.server.getPassword().equals("default"))) {
							this.getSendingData().writeUTF("*!granted");
							this.getServer().getUsers().add(new User(this.getClientName(), args[2], this));
							break;
						}
					}
				}
			}

			str = initStr;
	        getServer().getUsers().forEach(u -> {
	        	if (str.split(" ").length < 3) {
	        		str = str + " " + u.getDisplayName();
	        	} else {
	        		str = str + ", " + u.getDisplayName();
	        	}
	        });
	        
	        /**
	         * Notifying other clients of the new user.
	         */
	        getServer().getUsers().forEach(u -> {
	        	try {
	        		u.getCC().getSendingData().writeUTF("[System] " + SystemInfo.getDate() + ": " + getClientName() + " has connected.");
					u.getCC().getSendingData().writeUTF("/updateusers " + str);
				} catch (Exception e) {
					debugPrint("Error notifying of new connection: " + e.getStackTrace()[0]);
				}
	        });

	        /**
	         * This audio thread is currently not really doing anything.
	         * Will eventually channel audio data from this client to the others.
	         */
			Thread audioThread = new Thread(() -> {
				byte[] buffer = new byte[8192];
				int count = 0;
				while (true) {
					try {
						count = this.voiceAcceptedData.read(buffer);
						this.voiceSendingData.write(buffer, 0, count);
					} catch (Exception e1) {
						if (!this.running) {
							System.err.println("Lost audio connection to client!");
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
					iter.forEachRemaining(u -> {
						if (u.getCC().getClientName().equalsIgnoreCase(getClientName())) {
							iter.remove();
						}
					});
					this.setRunning(false);
					Thread.currentThread().interrupt();
					break;
				}
				
				/**
				 * Printing normal chat input to server console and chat log.
				 */
				if (!received.startsWith("*!")) {
					chatPrint("[" + this.getClientName() + "] " + SystemInfo.getDate() +  ": " + received);
				}
				
				/**
				 * Distributing received input to either the command parser or the other connected clients.
				 */
				try {
					getServer().getUsers().forEach(e -> {

						if (received.startsWith("*!")) {
							try {
								CommandParser.parse(received, e.getCC(), this);
							} catch (Exception ex) {
								debugPrint("Error while parsing command: " + received);
								debugPrint(ex.getStackTrace()[2].toString());
							}
						} else {
							try {
								e.getCC().getSendingData().writeUTF("[" + this.getClientName() + "] " + SystemInfo.getDate() +  ": " + received);
							} catch (Exception ex) {
								debugPrint("Error while sending message to clients!");
								debugPrint(ex.getStackTrace()[2].toString());
							}

						}

					});
				} catch (ConcurrentModificationException e) {
					debugPrint("Tried removing a user twice!");
				}

			}
		} catch (SocketException e) {
			/**
			 * This should come into effect when the user disconnects on their own, 
			 * whether by closing their client window or losing their connection.
			 */
			getServer().killUser(getClientName(), "User disconnected.");
			str = initStr;
            getServer().getUsers().forEach(u -> {
            	if (str.split(" ").length < 3) {
            		str = str + " " + u.getDisplayName();
            	} else {
            		str = str + ", " + u.getDisplayName();
            	}
            	if (!u.getDisplayName().equals(clientName)) {
            		try {
            			u.getCC().getSendingData().writeUTF("/updateusers " + str);
            			u.getCC().getSendingData().writeUTF("*![System] " + SystemInfo.getDate() + ": " + getClientName() + " has disconnected.");
            		} catch (Exception e1) {
            			debugPrint("Error sending disconnect message for " + this.clientName + ".");
            			debugPrint(e1.getStackTrace()[2].toString());
            		}
            	}
            });
		} catch (IOException e1) {
			debugPrint(e1.getStackTrace()[1].toString());
			this.server.killUser(clientName, "Lost connection to client.");
		} finally {
			/**
			 * In the unforseeable case that somehow the client expires server-side.
			 * Actually this is probably done in this case of kicks.
			 */
			getServer().getUsers().remove(this);
			try {
				this.textSocket.close();
				this.DLSocket.close();
				this.picSocket.close();
				this.voiceSocket.close();
				getServer().subClientID();
				Thread.currentThread().interrupt();
			} catch (IOException e1) {
				debugPrint("System exception: ClientConnection line "  + this.clientName + " was terminated unexpectedly. May have been kicked?");
				this.server.killUser(clientName, "Lost connection to client.");
			}
		}

	}

	int dlcount;

	/**
	 * Thread started when a client requests a download/file transfer.
	 * @param input The input from the remote client containing destination and file length.
	 * @return A thread used for receiving/sending data for file transfers.
	 * @throws Exception
	 */
	public Thread genDLThread(String input) throws Exception {
		Thread dlThread = new Thread(() -> {
			byte[] buffer = new byte[8192];
			String[] args = input.split(" ");
			long length = Long.parseLong(args[4]);
			try {
				long total = 0;
				while (total != length) {
					dlcount = this.DLAcceptedData.read(buffer, 0, 8192);
					total += dlcount;
					getServer().getUsers().stream().map(u -> u.getCC()).forEach(e -> {
						if (e.clientName.equalsIgnoreCase(args[2]) || args[2].equalsIgnoreCase("all")) {
							try {
								e.DLSendingData.write(buffer, 0, dlcount);
							} catch (Exception e1) {
								debugPrint("Error sending file data from " + this.clientName + " to " + args[2] + ".");
								debugPrint(e1.getStackTrace()[2].toString());
							}
						}
					});
				}

			} catch (Exception e1) {
				debugPrint("Error sending file data from " + this.clientName + " to " + args[2] + ".");
				debugPrint(e1.getStackTrace()[2].toString());
			}
			Thread.currentThread().interrupt();
		});
		return dlThread;
	}

	int piccount;
	/**
	 * Thread started when a client requests an image download/transfer.
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public Thread genPicThread(String input) throws Exception {
		Thread picThread = new Thread(() -> {
			byte[] buffer = new byte[8192];
			String[] args = input.split(" ");
			long length = Long.parseLong(args[4]);
			try {
				long total = 0;
				while (total != length) {
					piccount = this.picAcceptedData.read(buffer, 0, 8192);
					total += piccount;
					getServer().getUsers().stream().map(u -> u.getCC()).forEach(e -> {
						if (e.clientName.equalsIgnoreCase(args[2]) || args[2].equalsIgnoreCase("all")) {
							try {
								e.picSendingData.write(buffer, 0, piccount);
							} catch (Exception e1) {
								debugPrint("Error sending image data from " + this.clientName + " to " + args[2] + ".");
								debugPrint(e1.getStackTrace()[2].toString());
							}
						}
					});
				}

			} catch (Exception e1) {
				debugPrint("Error sending image data from " + this.clientName + " to " + args[2] + ".");
				debugPrint(e1.getStackTrace()[2].toString());
			}
			Thread.currentThread().interrupt();
		});
		return picThread;
	}

	/** Method used to initiate file sending.
	 * Uses the input from the remote client to determine file size and destination.
	 * @param input
	 */
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

	/** Method used to initiate image sending.
	 * Uses the input from the remote client to determine file size and destination.
	 * @param input
	 */
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

	/**
	 * A whole bunch of getters and setters.
	 * Could probably cull some unused ones.
	 */
	public void setClientName(String name) {
		this.clientName = name;
	}

	public String getClientName() {
		return clientName;
	}

	public DataOutputStream getSendingData() {
		return sendingData;
	}

	public Server getServer() {
		return server;
	}

	public Socket getSocket() {
		return textSocket;
	}

	public DataInputStream getAcceptedData() {
		return acceptedData;
	}

	public Socket getDLSocket() {
		return DLSocket;
	}

	public DataInputStream getDLAcceptedData() {
		return DLAcceptedData;
	}

	public DataOutputStream getDLSendingData() {
		return DLSendingData;
	}

	public Socket getVoiceSocket() {
		return this.voiceSocket;
	}

	public DataInputStream getVoiceAcceptedData() {
		return voiceAcceptedData;
	}

	public DataOutputStream getVoiceSendingData() {
		return voiceSendingData;
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