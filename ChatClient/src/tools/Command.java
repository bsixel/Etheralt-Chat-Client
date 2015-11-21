package tools;

import java.io.Serializable;

/*
 * 
 * @author Ben Sixel
 *   Copyright 2015 Benjamin Sixel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

/*
 * 
 * This class is not yet implemented in the chat client. At some point in time I hope to rewrite the command parser 
 * to use this command object instead of a whole bunch of nested 'if' statements.
 *
 */

public class Command implements Serializable {

	private static final long serialVersionUID = -411661664259234767L;
	private String name;
	private String description;
	private Runnable command;
	
	/**
	 * Default constructor for a Command.
	 * @param name The name to give the command.
	 * @param description The description assigned to the command, called when the (eventually implemented) /help command is called.
	 * @param command The runnable to execute when the command is called.
	 */
	public Command(String name, String description, Runnable command) {
		this.name = name;
		this.description = description;
		this.command = command;
	}
	
	/**
	 * Getter for the command's name.
	 * @return The name of the command.
	 */
	public String getName() {
		return this.name;
	}
	
	@Override
	/**
	 * 
	 * A string representation of the element: in this case, the name of the command followed by the description. Useful for the eventual help command.
	 */
	public String toString() {
		return this.name + ": " + description;
	}
	
	/**
	 * Getter for the runnable to execute when calling the command.
	 * @return
	 */
	public Runnable getCommand() {
		return this.command;
	}

}