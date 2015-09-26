package tools;

public class Command {

	private String name;
	private String description;
	private Runnable command;
	
	public Command(String name, String description, Runnable command) {
		this.name = name;
		this.description = description;
		this.command = command;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String toString() {
		return this.name + ": " + description;
	}

}