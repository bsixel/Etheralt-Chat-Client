package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
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
	 * @param msg
	 */
	public static void debugPrint(String msg) {
		System.out.println(msg);
		writeToErrorLog(msg);
	}
	
	/**
	 * Static method to print a message to both the console and the chat log.
	 * @param msg
	 */
	public static void chatPrint(String msg) {
		System.out.println(msg);
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
			System.out.println(configPath);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Gets a property from the server properties file.
	 * @param property
	 * @return
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
			e.printStackTrace();
		} finally {
			try {
				fileStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
		
	}
	
	/**
	 * Sets a property in the server properties file.
	 * @param property
	 * @param value
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
			e.printStackTrace();
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
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Used for writing messages to the server's chat log.
	 * @param message
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
			System.out.println(chatLogPath);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Used for writing messages to the server's error/debug log.
	 * @param message
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
			System.out.println(chatLogPath);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public static int getLogLength() throws IOException {
		int n = 0;
		
		FileReader file = new FileReader(chatLogPath);
		BufferedReader reader = new BufferedReader(file);
		
		while (reader.readLine() != null) {
			n++;
		}
		
		file.close();
		reader.close();
		
		return n;
		
	}
	
	/**
	 * Returns the length of the config file. Exists in case it is needed later.
	 * @return
	 * @throws IOException
	 */
	public static int getConfigLength() throws IOException {
		int n = 0;
		
		FileReader file = new FileReader(configPath);
		BufferedReader reader = new BufferedReader(file);
		
		while (reader.readLine() != null) {
			n++;
		}
		
		file.close();
		reader.close();
		
		return n;
		
	}
	
	/**
	 * Stores the properties for a given server object.
	 * @param server
	 */
	public static void saveProperties(Server server) {
		
		try {
			File configFile = new File(configPath);
			configFile.createNewFile();
			Properties properties = new Properties();
			properties.setProperty("last_port", ((Integer) server.getPortStart()).toString());
			properties.setProperty("last_password", server.getPassword());
			
			OutputStream writer = new FileOutputStream(configFile);
			properties.store(writer, "Saved user info");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}