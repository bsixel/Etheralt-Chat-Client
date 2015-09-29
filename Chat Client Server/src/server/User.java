package server;

import server.ClientConnection;

public class User {
	
	private String displayName;
	private String ID;
	private ClientConnection CC;
	private boolean admin = false;
	
	public User(String username, String ID, ClientConnection CC) {
		this.displayName = username;
		this.ID = ID;
		this.CC = CC;
	}
	
	public String getDisplayName() {
		return displayName;
	}

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