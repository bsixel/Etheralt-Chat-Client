package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import server.Server;

/**
 * 
 * @author Ben Sixel
 * FileHandler class. Deals with system files: 
 * writing to error logs, chat logs, creating directories and properties, etc.
 */

public class FileHandler {
	
	public static final String chatLogPath = "server_chat_log.log";
	public static final String errorLogPath = "error_log.log";
	public static final String configPath = "chat_server.properties";
	
	/**
	 * Static method to print a message to both the console and the error/debug log.
	 * @param msg The message being printed/saved.
	 */
	public static void debugPrint(String msg) {
		String fnlMsg = SystemInfo.getFullDate() + ": " + msg;
		System.out.println(fnlMsg);
		System.out.print("> ");
		writeToErrorLog(fnlMsg);
	}
	
	/**
	 * Static method to print a message to both the console and the chat log.
	 * @param msg The message being printed/saved.
	 */
	public static void chatPrint(String msg) {
		System.out.println(msg);
		System.out.print("> ");
		writeToChatLog(msg);
	}
	
	/**
	 * Initiates a config file.
	 */
	public static void generateConfigFile() {
		
		try {
			File file = new File(configPath);
			if (file.createNewFile()) {
				
				FileWriter writer = new FileWriter(configPath, true);
				PrintWriter printer = new PrintWriter(writer);
				writer.close();
				printer.close();
				
			}
		} catch (IOException e) {
			debugPrint("Error generating config file: " + configPath);
			debugPrint(e.getStackTrace()[0].toString());
		}
		
	}
	
	/**
	 * Gets a property from the server properties file.
	 * @param property The name of the desired property.
	 * @return The valuen of the desired property from the properties file.
	 */
	public static String getProperty(String property) {
		String res = null;
		InputStream fileStream = null;
		try {
			new File(configPath).createNewFile();
			Properties properties = new Properties();
			fileStream = new FileInputStream(new File(configPath));
			properties.load(fileStream);
			res = properties.getProperty(property);
		} catch (IOException e) {
			debugPrint(e.getStackTrace()[0].toString());
		} finally {
			try {
				fileStream.close();
			} catch (IOException e) {
				debugPrint(e.getStackTrace()[0].toString());
			}
		}
		return res;
		
	}
	
	/**
	 * Sets a property in the server properties file.
	 * @param property The property being set.
	 * @param value The value (as a string) we are giving the property.
	 */
	public static void setProperty(String property, String value) {
		try {
			File configFile = new File(configPath);
			configFile.createNewFile();
			Properties properties = new Properties();
			properties.setProperty(property, value);
			OutputStream writer = new FileOutputStream(configFile);
			properties.store(writer, "Saved user info");
			writer.close();
		} catch (IOException e) {
			debugPrint(e.getStackTrace()[0].toString());
		}
		
	}
	
	/**
	 * @deprecated Use {@link #saveProperties(Server)} instead.
	 */
	@Deprecated
	public static void initUserPrefs() {
		
		try {
			File configFile = new File(configPath);
			configFile.createNewFile();
			InputStream configReader = new FileInputStream(configFile);
			Properties defaultProperties = new Properties();
			defaultProperties.setProperty("last_port", "");
			defaultProperties.setProperty("last_password", "");
			
			Properties userProperties = new Properties(defaultProperties);
			userProperties.load(configReader);
			configReader.close();
			
		} catch (IOException e) {
			debugPrint(e.getStackTrace()[0].toString());
		}
		
		
	}
	
	/**
	 * Used for writing messages to the server's chat log.
	 * @param message The message to be saved to the chat log.
	 */
	public static void writeToChatLog(String message){
		
		try {
			new File(chatLogPath).createNewFile();
			FileWriter writer = new FileWriter(chatLogPath, true);
			PrintWriter printer = new PrintWriter(writer);
			printer.printf("%s" + "%n", message.trim());
			writer.close();
			printer.close();
		} catch (IOException e) {
			debugPrint("Error writing to chat log: " + chatLogPath);
			debugPrint(e.getStackTrace()[0].toString());
		}
		
	}
	
	/**
	 * Used for writing messages to the server's error/debug log.
	 * @param message The message to be saved to the error log.
	 */
	public static void writeToErrorLog(String message){
		
		try {
			new File(chatLogPath).createNewFile();
			FileWriter writer = new FileWriter(errorLogPath, true);
			PrintWriter printer = new PrintWriter(writer);
			printer.printf("%s" + "%n", message.trim());
			writer.close();
			printer.close();
		} catch (IOException e) {
			debugPrint("Error writing to chat log: " + errorLogPath);
			debugPrint(e.getStackTrace()[0].toString());
		}
		
	}
	
	/**
	 * Stores the properties for a given server object in the properties file.
	 * @param server The server whose properties we are saving to file.
	 */
	public static void saveProperties(Server server) {
		
		try {
			File configFile = new File(configPath);
			configFile.createNewFile();
			Properties properties = new Properties();
			properties.setProperty("last_port", ((Integer) server.getPortStart()).toString());
			properties.setProperty("last_password", server.getPassword());
			String prevAdmins = getProperty("admins");
			if (prevAdmins != null) {
				properties.setProperty("admins", prevAdmins);
			} else {
				properties.setProperty("admins", "");
			}
			OutputStream writer = new FileOutputStream(configFile);
			properties.store(writer, "Saved user info");
			writer.close();
		} catch (IOException e) {
			debugPrint("Error writing to config file: " + configPath);
			debugPrint(e.getStackTrace()[0].toString());
		}
		
	}
	
}