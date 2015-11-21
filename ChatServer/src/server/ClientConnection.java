package server;

import static tools.FileHandler.chatPrint;
import static tools.FileHandler.debugPrint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import tools.CommandParser;
import tools.DataPacket;
import tools.SystemInfo;

/*
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
	private ObjectInputStream acceptedData;
	private ObjectOutputStream sendingData;
	private Server server;
	private User user;

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
	 * @param clientID The ID given to the client on connecting.
	 * @param server The server which is hosting the connection.
	 */
	public ClientConnection(Socket socket, int clientID, Server server) {
		this.textSocket = socket;
		this.clientID = clientID;
		this.server = server;
	}

	/**
	 * Starts the actual connection. Manages send/receive of data of multiple kinds, including
	 * chat, file transfer, and eventually audio.
	 */
	public void startConnection() {

		/*
		 * Establishing a connection with the remote client.
		 */
		try {
			this.sendingData = new ObjectOutputStream(this.textSocket.getOutputStream());
			this.acceptedData = new ObjectInputStream(this.textSocket.getInputStream());


			/*
			 * Verifying that the desired username is not taken,
			 *  and the user is providing the correct password to access the server.
			 */
			while (true) {

				DataPacket packet = (DataPacket) this.acceptedData.readObject();
				String input = packet.getMessage();
				String[] args = input.split(" ");
				if (input.startsWith("*!givename:")) {
					this.setClientName(args[1]);
					debugPrint("Assigning name '" + args[1] + "'");
					synchronized (this.getServer().getUsers()) {
						if (this.getClientName() == null || this.getClientName() == "") {
							continue;
						}
						if (!args[3].equals(this.getServer().getPassword())) {
							this.getSendingData().writeObject(new DataPacket("message", "all", "all", "*!decline:password", null));
							//Writing to console and error log of the unsuccessful password match.
							debugPrint("User " + this.clientName + " failed to connect with password: '" + args[3] +"' from IP " + textSocket.getInetAddress());
							debugPrint("Desired password: " + this.getServer().getPassword());
							return;
						} else if (this.getServer().getUsers().stream().anyMatch(e -> e.getDisplayName().equalsIgnoreCase(this.getClientName()))) {
							this.getSendingData().writeObject(new DataPacket("message", "all", "all", "*!decline:username", null));
							return;
						}
						if (!this.getServer().getUsers().stream().anyMatch(e -> e.getDisplayName().equalsIgnoreCase(this.getClientName())) && (args[3].equals(this.getServer().getPassword()) || this.server.getPassword().equals("default"))) {
							this.getSendingData().writeObject(new DataPacket("message", "all", "all", "*!granted", null));
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

			/*
			 * Notifying other clients of the new user.
			 */
			getServer().getUsers().forEach(u -> {
				try {
					this.getSendingData().writeObject(new DataPacket("message", "all", "all", "*![System] " + SystemInfo.getDate() + ": " + getClientName() + " has connected.", null));
					this.getSendingData().writeObject(new DataPacket("command", "all", "all", "/updateusers " + str, null));
				} catch (Exception e) {
					debugPrint("Error notifying of new connection: " + e.getStackTrace()[0]);
				}
			});


			while (this.running) {

				DataPacket packet = (DataPacket) this.acceptedData.readObject();
				String received = packet.getMessage();

				if (received.equalsIgnoreCase("*![System] " + SystemInfo.getDate() + ": " + this.clientName + " has disconnected.") || received.equalsIgnoreCase("*![System] " + SystemInfo.getDate() + ": " + this.clientName + " has disconnected.")) {
					this.getSocket().close();
					this.acceptedData.close();
					this.sendingData.close();

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

				if (packet.getType().equals("dlpacket") || packet.getType().equals("audiopacket") || packet.getType().equals("voicepacket")) {
					getServer().getUsers().forEach(u -> {
						if (packet.getTarget().equalsIgnoreCase("all") || packet.getTarget().equalsIgnoreCase(u.getDisplayName())) {
							try {
								u.getCC().sendingData.writeObject(packet);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				} else if (packet.getType().equals("command")) {
					getServer().getUsers().forEach(u -> {
						if (packet.getTarget().equalsIgnoreCase("all") || packet.getTarget().equalsIgnoreCase(u.getDisplayName())) {
							try {
								CommandParser.parse(received, u.getCC(), this);
							} catch (Exception ex) {
								debugPrint("Error while parsing command: " + received);
								debugPrint(ex.getStackTrace()[0].toString());
							}
						}
					});
				} else if (packet.getType().equals("message")) {
					if (received == null || received == "") {
						continue;
					}
					/*
					 * Printing normal chat input to server console and chat log.
					 */
					if (!received.startsWith("*!")) {
						chatPrint("[" + this.getClientName() + "] " + SystemInfo.getDate() +  ": " + received);
					}
					getServer().getUsers().forEach(u -> {
						try {
							u.sendMessage("[" + this.getClientName() + "] " + SystemInfo.getDate() +  ": " + received);
						} catch (Exception ex) {
							debugPrint("Error while sending message to clients!");
							debugPrint(ex.getStackTrace()[0].toString());
						}
					});
				}

			}
		} catch (SocketException e) {
			/*
			 * This should come into effect when the user disconnects on their own, 
			 * whether by closing their client window or losing their connection.
			 */
			try {
				getServer().killUser(getClientName(), "User disconnected.");
			} catch (ConcurrentModificationException e2) {
				debugPrint(e.getStackTrace()[0].toString());
				debugPrint("Unable to kill user - another thread had already done so!");
			}
			str = initStr;
			getServer().getUsers().forEach(u -> {
				if (str.split(" ").length < 3) {
					str = str + " " + u.getDisplayName();
				} else {
					str = str + ", " + u.getDisplayName();
				}
				if (!u.getDisplayName().equals(clientName)) {
					try {
						u.getCC().getSendingData().writeObject("/updateusers " + str);
						u.sendMessage("*![System] " + SystemInfo.getDate() + ": " + getClientName() + " has disconnected.");
					} catch (Exception e1) {
						debugPrint("Error sending disconnect message for " + this.clientName + ".");
						debugPrint(e1.getStackTrace()[0].toString());
					}
				}
			});
		} catch (IOException e1) {
			debugPrint(e1.getStackTrace()[0].toString());
			try {
				this.server.killUser(clientName, "Lost connection to client.");
			} catch (ConcurrentModificationException e2) {
				debugPrint(e2.getStackTrace()[0].toString());
				debugPrint("Unable to kill user - another thread had already done so!");
			}
		} catch (ClassNotFoundException e1) {
			debugPrint("Unknown object read from stream!");
			e1.printStackTrace();
		} finally {
			/*
			 * In the unforseeable case that somehow the client expires server-side.
			 * Actually this is probably done in this case of kicks.
			 */
			getServer().getUsers().remove(this);
			try {
				this.textSocket.close();
				getServer().subClientID();
				Thread.currentThread().interrupt();
			} catch (IOException e1) {
				debugPrint("System exception: ClientConnection line "  + this.clientName + " was terminated unexpectedly. May have been kicked?");
				try {
					this.server.killUser(clientName, "Lost connection to client.");
				} catch (ConcurrentModificationException e2) {
					debugPrint(e2.getStackTrace()[0].toString());
					debugPrint("Unable to kill user - another thread had already done so!");
				}
			}
		}

	}

	/**
	 * Thread started when a client requests a download/file transfer.
	 * @param input The string input from the remote client containing destination and file length.
	 * @return A thread used for receiving/sending data for file transfers.
	 * @throws Exception if there is a connection problem between the sending and receiving clients.
	 */
	public Thread genDLThread(String input) throws Exception {
		Thread dlThread = new Thread(() -> {
			byte[] buffer = new byte[8192];
			String[] args = input.split(" ");
			long length = Long.parseLong(args[4]);
			try {
				long total = 0;
				while (total != length) {
					int dlcount = 0;
					//dlcount = this.DLAcceptedData.read(buffer, 0, 8192);
					total += dlcount;
					getServer().getUsers().stream().map(u -> u.getCC()).forEach(e -> {
						if (e.clientName.equalsIgnoreCase(args[2]) || args[2].equalsIgnoreCase("all")) {
							try {
								//e.DLSendingData.write(buffer, 0, dlcount);
							} catch (Exception e1) {
								debugPrint("Error sending file data from " + this.clientName + " to " + args[2] + ".");
								debugPrint(e1.getStackTrace()[0].toString());
							}
						}
					});
				}

			} catch (Exception e1) {
				debugPrint("Error sending file data from " + this.clientName + " to " + args[2] + ".");
				debugPrint(e1.getStackTrace()[0].toString());
				Thread.currentThread().interrupt();
			}
			Thread.currentThread().interrupt();
		});
		return dlThread;
	}

	/**
	 * Thread started when a client requests an image download/transfer.
	 * @param input The string input from the remote client containing destination and file length.
	 * @return A thread used for receiving/sending data for image transfers.
	 * @throws Exception if there is a connection problem between the sending and receiving clients.
	 */
	public Thread genPicThread(String input) throws Exception {
		Thread picThread = new Thread(() -> {
			byte[] buffer = new byte[8192];
			String[] args = input.split(" ");
			long length = Long.parseLong(args[4]);
			try {
				long total = 0;
				while (total != length) {
					int piccount = 0;
					//piccount = this.picAcceptedData.read(buffer, 0, 8192);
					total += piccount;
					getServer().getUsers().stream().map(u -> u.getCC()).forEach(e -> {
						if (e.clientName.equalsIgnoreCase(args[2]) || args[2].equalsIgnoreCase("all")) {
							try {
								//e.picSendingData.write(buffer, 0, piccount);
							} catch (Exception e1) {
								debugPrint("Error sending image data from " + this.clientName + " to " + args[2] + ".");
								debugPrint(e1.getStackTrace()[0].toString());
							}
						}
					});
				}

			} catch (Exception e1) {
				debugPrint("Error sending image data from " + this.clientName + " to " + args[2] + ".");
				debugPrint(e1.getStackTrace()[0].toString());
				Thread.currentThread().interrupt();
			}
			Thread.currentThread().interrupt();
		});
		return picThread;
	}

	/** Method used to initiate file sending.
	 * Uses the input from the remote client to determine file size and destination.
	 * @param input The input from the remote client containing destination and file length.
	 */
	public void sendFile(String input) {

		Thread dlThread = null;
		try {
			dlThread = genDLThread(input);
		} catch (Exception e1) {
			debugPrint(e1.getStackTrace()[0].toString());
		}
		dlThread.setDaemon(true);
		dlThread.start();

	}

	/** Method used to initiate image sending.
	 * Uses the input from the remote client to determine file size and destination.
	 * @param input The input from the remote client containing destination and file length.
	 */
	public void sendImg(String input) {

		Thread picThread = null;
		try {
			picThread = genPicThread(input);
		} catch (Exception e1) {
			debugPrint(e1.getStackTrace()[0].toString());
		}
		picThread.setDaemon(true);
		picThread.start();

	}

	/*
	 * A whole bunch of getters and setters.
	 * Could probably cull some unused ones.
	 */

	/**
	 * Setter for the client name.
	 * @param name New name for the client.
	 */
	public void setClientName(String name) {
		this.clientName = name;
	}

	/**
	 * Getter for the client's name.
	 * @return The name of the client.
	 */
	public String getClientName() {
		return clientName;
	}

	/**
	 * Getter for the standard sending data of the client.
	 * @return The standard sending data of the client.
	 */
	public ObjectOutputStream getSendingData() {
		return sendingData;
	}

	/**
	 * Getter for the server itself.
	 * @return The server to which this client is connected.
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * Getter for the standard socket.
	 * @return The standard text socket.
	 */
	public Socket getSocket() {
		return textSocket;
	}

	/**
	 * Getter for the standard receiving text data.
	 * @return The standard stream for received text data.
	 */
	public ObjectInputStream getAcceptedData() {
		return acceptedData;
	}

	/**
	 * Getter for the unique client ID associated with this client.
	 * @return A unique string of numbers and letter generated by Java's standard random UUID generator.
	 */
	public int getClientID() {
		return clientID;
	}

	/**
	 * Setter for this client's unique ID.
	 * @param clientID The new ID to which this client's ID will be set.
	 */
	public void setClientID(int clientID) {
		this.clientID = clientID;
	}

	/**
	 * Sets whether this client is currently running and maintaining a connection.
	 * @param b Boolean value: true for running, false for not.
	 */
	public void setRunning(boolean b) {
		this.running = b;
	}

	/**
	 * Gets the User object representative of this connection's remote user.
	 * @return A user object representing the remote user.
	 */
	public User getUser() {
		return this.user;
	}

	/**
	 * Sets the user for this connection.
	 * @param user The user being set.
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Returns a string representing the client connection - in this case, it is simply the client's name.
	 * @return The name of the client.
	 * @Override
	 */
	@Override
	public String toString() {
		return this.clientName;
	}

}