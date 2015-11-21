package tools;

import java.io.Serializable;

public class DataPacket implements Serializable {

	//Numbers
	private static final long serialVersionUID = -7904588374320593571L;
	
	//Strings
	private String type;
	private String from;
	private String message;
	private String target;
	
	//Arrays
	private byte[] data;
	
	/**
	 * Constructs a new DataPacket with the given type, name, message, and data.
	 * @param type The type of data being sent: message, dlpacket, imgpacket, voicepacket, or command.
	 * @param from The name of the client sending the packet.
	 * @param target The name of the target client.
	 * @param message The message to include within the packet
	 * @param data The byte array to use as data within the packet. Only necessary for dlpackets, voicepackets, or imgpackets.
	 */
	public DataPacket(String type, String from, String target, String message, byte[] data) {
		this.type = type;
		this.from = from;
		this.message = message;
		this.data = data;
		this.target = target;
	}

	/**
	 * Gets the type of packet.
	 * @return A string representing the type of packet: message, dlpacket, imgpacket, voicepacket, or command.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets the name of the client sending the packet.
	 * @return A string with the name.
	 */
	public String getFrom() {
		return from;
	}
	
	/**
	 * Gets the name of the packet target.
	 * @return A string with the name.
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * Gets the message in the packet. Typically contains info such as target, data length, etc.
	 * @return A string with the message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Gets the data for this packet. Used for dlpackets, voicepackets, and imgpackets for transfering bytes.
	 * @return The byte array containing the data.
	 */
	public byte[] getData() {
		return data;
	}
	
}