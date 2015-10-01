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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

import server.Server;

public class FileHandler {
	
	public static final String chatLogPath = System.getProperty("user.home") + "/Documents/Etheralt Chat Client/server_chat_log.log";
	public static final String errorLogPath = System.getProperty("user.home") + "/Documents/Etheralt Chat Client/error_log.log";
	public static final String downloadsPath = System.getProperty("user.home") + "/Documents/Etheralt Chat Client/Downloads";
	public static final String picturesPath = System.getProperty("user.home") + "/Documents/Etheralt Chat Client/Pictures";
	public static final String configPath = System.getProperty("user.home") + "/Documents/Etheralt Chat Client/chat_server.properties";
	
	public static void generateConfigFile() {
		
		try {
			File file = new File(configPath);
			file.getParentFile().mkdirs();
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
	
	private static int getFileSize(URL url) {
	    HttpURLConnection conn = null;
	    try {
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("HEAD");
	        conn.getInputStream();
	        return conn.getContentLength();
	    } catch (IOException e) {
	        return -1;
	    } finally {
	        conn.disconnect();
	    }
	}
	
	public static void downloadFile(String url) throws IOException {
		
		Thread dlThread;
		Runnable downloadFile = () -> {
			try {
				
				URL link;
				if (url.startsWith("https://") || url.startsWith("http://")) {
					link = new URL(url);
				} else {
					link = new URL("http://" + url);
				}
				String fileType = url.substring(url.lastIndexOf(".") + 1);
				ReadableByteChannel stream = Channels.newChannel(link.openStream());
				String fileName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
				File file = new File(FileHandler.downloadsPath + "/" + fileName + "." + fileType);
				FileOutputStream fileStream = new FileOutputStream(file);
				int total = 0;
				int count;
				fileStream.getChannel().transferFrom(stream, 0, Long.MAX_VALUE);
				fileStream.close();
				stream.close();
				Thread.currentThread().interrupt();
				System.out.println("Download of " + fileName + " complete.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		dlThread = new Thread(downloadFile);
		dlThread.start();
		
	}
	
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