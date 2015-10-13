package server;

import server.ClientConnection;

/**
 * User object used to store information about the remote client.
 * @author Ben Sixel
 *
 */

public class User {
	
	private String displayName;
	private String ID;
	private ClientConnection CC;
	private boolean admin = false;
	
	/**
	 * Constructor for a user.
	 * @param username
	 * @param ID
	 * @param CC
	 */
	public User(String username, String ID, ClientConnection CC) {
		this.displayName = username;
		this.ID = ID;
		this.CC = CC;
	}
	
	/**
	 * Getter for the display name of the connected client.
	 * @return
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Setter for the display name of the connected client.
	 * @param displayName
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public ClientConnection getCC() {
		return CC;
	}

	public void setCC(ClientConnection cC) {
		CC = cC;
	}
	
	public boolean isAdmin() {
		return this.admin;
	}
	
	public void setAdmin(boolean b) {
		this.admin = b;
	}
	
	public String toString() {
		return this.displayName;
	}
	
}