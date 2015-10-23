package server;

/**
 * User object used to store information about the remote client.
 * @author Ben Sixel
 *
 */

public class User {
	
	private String origName;
	private String displayName;
	private String ID;
	private ClientConnection CC;
	private boolean admin = false;
	
	/**
	 * Constructor for a user.
	 * @param username The desired username for the user.
	 * @param ID The unique ID provided by the remote client.
	 * @param CC The client connection established by the server.
	 */
	public User(String username, String ID, ClientConnection CC) {
		this.displayName = username;
		this.origName = username;
		this.ID = ID;
		this.CC = CC;
		this.CC.setUser(this);
	}
	
	/**
	 * Getter for the display name of the connected client.
	 * @return The display name of the user.
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

	/**
	 * Getter for the unique ID provided by the remote client.
	 * @return The unique ID string provided by the remote client.
	 */
	public String getID() {
		return ID;
	}

	/**
	 * Getter for the user's connection to the remote client.
	 * @return The connection to the remote client.
	 */
	public ClientConnection getCC() {
		return CC;
	}
	
	/**
	 * Getter for the admin boolean field. Does the user have administrative privileges on the server?
	 * @return The admin boolean: If true then the user is an admin.
	 */
	public boolean isAdmin() {
		return this.admin;
	}
	
	/**
	 * Sets whether the user has administrator privileges. True for yes, false for no.
	 * @param b A boolean representing whether or not the user should have administrative privileges.
	 */
	public void setAdmin(boolean b) {
		this.admin = b;
	}
	
	/**
	 * A getter for the original name of the user.
	 * @return The original name of the user, given when initializing the user.
	 */
	public String getFirstName() {
		return this.origName;
	}
	
	/**
	 * Returns a string representative of the user - in this case, the user's display name.
	 * @return The user's display name.
	 */
	public String toString() {
		return this.displayName;
	}
	
}