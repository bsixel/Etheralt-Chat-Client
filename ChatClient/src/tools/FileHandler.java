package tools;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import client.Client;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.WindowEvent;
import userInteract.ChatBox;
import userInteract.ChatText;
import userInteract.LoginScreenController;
import userInteract.MainScreenController;
import userInteract.Popups;

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

public class FileHandler {
	
	/*
	 * Some constant strings used for finding the properties file and the log files.
	 */
	public static final String chatLogPath = System.getProperty("user.home") + "/Documents/Etheralt Chat Client/chat_log.log";
	public static final String errorLogPath = System.getProperty("user.home") + "/Documents/Etheralt Chat Client/error_log.log";
	public static final String downloadsPath = System.getProperty("user.home") + "/Documents/Etheralt Chat Client/Downloads";
	public static final String picturesPath = System.getProperty("user.home") + "/Documents/Etheralt Chat Client/Pictures";
	public static final String configPath = System.getProperty("user.home") + "/Documents/Etheralt Chat Client/chat_client.properties";
	
	/**
	 * Writes a error message both to the error stream of the console and to the error log.
	 * @param msg The message to write to the error log and console.
	 */
	public static void debugPrint(String msg) {
		System.err.println(SystemInfo.getFullDate() + ": " + msg);
		writeToErrorLog(SystemInfo.getFullDate() + ": " + msg);
	}
	
	/**
	 * Writes a message both to the regular stream of the console and to the chat log.
	 * @param msg The message to write to the chat log and console.
	 */
	public static void chatPrint(String msg) {
		System.out.println(SystemInfo.getFullDate() + ": " + msg);
		writeToChatLog(SystemInfo.getFullDate() + ": " + msg);
	}
	
	/**
	 * Generates a config file just in case the file does not yet exist.
	 */
	public static void generateConfigFile() {
		
		try {
			if (new File(configPath).createNewFile()) {
				
				FileWriter writer = new FileWriter(configPath, true);
				PrintWriter printer = new PrintWriter(writer);
				writer.close();
				printer.close();
				
			}
		} catch (IOException e) {
			FileHandler.debugPrint(configPath);
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
		}
		
	}
	
	/**
	 * Gets a property from the properties file.
	 * @param property The name of the property to get.
	 * @return The property's value.
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
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
		} finally {
			try {
				fileStream.close();
			} catch (IOException e) {
				FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
			}
		}
		return res;
		
	}
	
	/**
	 * Sets a property in the properties file.
	 * @param property The property to set.
	 * @param value The value to assign to the property.
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
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
		}
		
	}
	
	/**
	 * Initializes the config file in the case that it does not yet exist.
	 * @return True if the generation was successful.
	 */
	public static boolean initUserPrefs() {
		
		try {
			File configFile = new File(configPath);
			configFile.createNewFile();
			InputStream configReader = new FileInputStream(configFile);
			Properties defaultProperties = new Properties();
			
			defaultProperties.setProperty("computer_ID", "");
			defaultProperties.setProperty("last_username", "");
			defaultProperties.setProperty("last_IP", "");
			defaultProperties.setProperty("last_port", "");
			defaultProperties.setProperty("prev_ips", "");
			
			Properties userProperties = new Properties(defaultProperties);
			userProperties.load(configReader);
			configReader.close();
			return true;
			
		} catch (IOException e) {
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
			return false;
		}
		
		
	}
	
	/**
	 * Writes a message to the chat log.
	 * @param message The message to write to the chat log.
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
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
			FileHandler.debugPrint(chatLogPath);
		}
		
	}
	
	/**
	 * Writes a message to the error log.
	 * @param message The message to write to the error log.
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
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
			FileHandler.debugPrint(errorLogPath);
		}
		
	}
	
	/**
	 * Reads from log into the chat box.
	 * @param chatBox The batbox into which to read the previous chat messages.
	 * @throws IOException If there is an error reading from the chat log: either there is no such file or no permission is given to read the file.
	 */
	public static void readLog(ChatBox chatBox) throws IOException {
		new File(chatLogPath).createNewFile();
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(chatLogPath);
		} catch (FileNotFoundException e) {
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
		}
		BufferedReader textReader = new BufferedReader(fileReader);
		
		for (int i =0; i < getLogLength(); i++) {
			chatBox.addText(new ChatText(textReader.readLine(), "darkred", "black"));
		}
		fileReader.close();
		textReader.close();
		
	}
	
	/**
	 * Gets the length in number of lines of the chat log.
	 * @return The number of lines in the chat log.
	 * @throws IOException If there is an issue trying to access the chat log.
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
	 * Gets the length in number of lines of the properties file.
	 * @return The number of lines in the properties file.
	 * @throws IOException If there is an issue trying to access the properties file.
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
	 * Downloads a file from the specified URL, saving it to the specified file path.
	 * @param url The string representing the URL from which to get the file.
	 * @param fileName The path to which the file will be saved.
	 * @throws IOException Thrown if there is a problem establishing a connection to the URL.
	 */
	public static void downloadFile(String url, String fileName) throws IOException {
		
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
				Platform.runLater(() -> System.out.println("Supposed file length (): " + getFileSize(link)));
				File file = new File(downloadsPath + "/" + fileName + "." + fileType);
				FileOutputStream fileStream = new FileOutputStream(file);
				fileStream.getChannel().transferFrom(stream, 0, Long.MAX_VALUE);
				fileStream.close();
				stream.close();
				Thread.currentThread().interrupt();
				System.out.println("Download of " + fileName + " complete.");
			} catch (Exception e) {
				FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
			}
		};
		dlThread = new Thread(downloadFile);
		dlThread.start();
		
	}
	
	/**
	 * Gets the size of a remote file from a URL. Currently only used for debugging.
	 * @param url The URL of the remote file.
	 * @return The size of the file given the file's header.
	 */
	private static long getFileSize(URL url) {
	    HttpURLConnection conn = null;
	    try {
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("HEAD");
	        conn.getInputStream();
	        return conn.getContentLengthLong();
	    } catch (IOException e) {
	        return -1;
	    } finally {
	        conn.disconnect();
	    }
	}
	
	/**
	 * Downloads a file from a URL. Adds a warning message popup if the user attempts to close the parent window.
	 * @param url The string representation of the URL from which to grab the file.
	 * @param sc The screen controller which contains the parent window.
	 * @throws IOException Thrown if unable to establish a connection to the URL.
	 */
	public static void downloadFile(String url, MainScreenController sc) throws IOException {
		
		Thread dlThread;
		Runnable downloadFile = () -> {
			try {
				EventHandler<WindowEvent> onWindowClose = sc.getWindow().onCloseRequestProperty().get();
				sc.getWindow().setOnCloseRequest(e -> {
					e.consume();
					if (Popups.startConfDlg("Are you sure you want to exit? There is a download in progress.")) {
						sc.getWindow().onCloseRequestProperty().set(onWindowClose);
						sc.getWindow().close();
						System.exit(0);
				}
				});
				
				URL link;
				if (url.startsWith("https://") || url.startsWith("http://")) {
					link = new URL(url);
				} else {
					link = new URL("http://" + url);
				}
				String fileType = url.substring(url.lastIndexOf(".") + 1);
				ReadableByteChannel stream = Channels.newChannel(link.openStream());
				Platform.runLater(() -> System.out.println("Supposed file length: " + getFileSize(link)));
				String fileName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
				File file = new File(FileHandler.downloadsPath + "/" + fileName + "." + fileType);
				FileOutputStream fileStream = new FileOutputStream(file);
				fileStream.getChannel().transferFrom(stream, 0, Long.MAX_VALUE);
				fileStream.close();
				stream.close();
				Platform.runLater(() -> {
					if (Popups.startConfDlg("Download complete. Open folder?")) {
						try {
							Desktop.getDesktop().open(new File(downloadsPath));
						} catch (Exception e1) {
							Popups.startInfoDlg("Error: Unable to open downloads folder.", String.format("Unable to open downloads folder! May be an OS issue. %n Please report this as a bug."));
						}
					}
				});
				Thread.currentThread().interrupt();
				System.out.println("Download of " + fileName + " complete.");
				sc.getWindow().onCloseRequestProperty().set(onWindowClose);
			} catch (Exception e) {
				FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
			}
		};
		dlThread = new Thread(downloadFile);
		dlThread.start();
		
	}
	
	/**
	 * Saves properties to disk given a login screen controller.
	 * @param ls The login screen controller whose properties we are saving.
	 */
	public static void saveProperties(LoginScreenController ls) {
		
		try {
			File configFile = new File(configPath);
			configFile.createNewFile();
			Properties properties = new Properties();
			if (FileHandler.getProperty("computer_ID") == null) {
				properties.setProperty("computer_ID", UUID.randomUUID().toString());
			} else {
				properties.setProperty("computer_ID", getProperty("computer_ID"));
			}
			
			properties.setProperty("last_username", ls.getUsernameField().getText());
			properties.setProperty("last_IP", ls.getIPChoice());
			properties.setProperty("last_port", ls.getPortField().getText());
			try {
				String previps = getProperty("prev_ips");
				System.out.println("PREVIPS: " + previps);
				if (!previps.contains(ls.getIPChoice())) {
					properties.setProperty("prev_ips", previps + "," +ls.getIPChoice());
				} else {
					properties.setProperty("prev_ips", previps);
				}
			} catch (Exception e) {
				properties.setProperty("prev_ips", ls.getIPChoice());
				debugPrint("Error saving last IP!");
			}
			
			OutputStream writer = new FileOutputStream(configFile);
			properties.store(writer, "Saved user info");
			writer.close();
		} catch (IOException e) {
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
		}
		
	}
	
	/**
	 * Downloads a file sent from another client.
	 * @param sc The screen controller containing the client used to receive data.
	 * @param file The file save destination.
	 * @param length The supposed length of the file. Mostly for debugging.
	 * @return A runnable which downloads the file.
	 */
	public static Runnable dlFile(MainScreenController sc, File file, long length) {
		Runnable run = () -> {
			FileOutputStream fileOut = null;
			int count = 1;
			try {
				long total = 0;
				byte[] fileBuffer = new byte[8192];
				fileOut = new FileOutputStream(file);
				while (total != length) {
					//count = sc.getClient().getRecevingStream().readObject(fileBuffer, 0, 8192);
					total += count;
					fileOut.write(fileBuffer, 0, count);
					fileOut.flush();
				}
				fileOut.close();
				Platform.runLater(() -> {
					if (Popups.startConfDlg(file.getName() + " finished downloading. Open folder?")) {
						try {
							Desktop.getDesktop().open(new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - file.getName().length())));
						} catch (Exception e) {
							System.err.println("Failed to open containing folder.");
							FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
						}
					}
				});
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				Platform.runLater(() -> Popups.startInfoDlg("Download Error", "Failed to download file!"));
				FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
				debugPrint("Failed send count: " + count);
			}
		};
		
		return run;
		
	}
	
	/**
	 * Returns a runnable that sends a file to a remote client or clients, as given by the input.
	 * @param target The name of the target to which the file is being sent.
	 * @param file The file to send.
	 * @param client The client being used to send the file.
	 * @return A runnable which sends the specified file.
	 */
	public static Runnable sendFile(String target, File file, Client client) {
		Runnable run = () -> {
			int count = 1;
			try {
				byte[] fileBuffer = new byte[8192];
				FileInputStream fileStream = new FileInputStream(file);
				String name = file.getName().replaceAll(" ", "_");
				long fileLength = file.length();
				long total = 0;
				client.getSendingStream().writeObject(new DataPacket("dlpacket", client.getClientName(), target, name + " start " + fileLength, null));
				long packetnum = 1;
				while (total != fileLength) {
					count = fileStream.read(fileBuffer, 0, 8192);
					client.getSendingStream().writeObject(new DataPacket("dlpacket", client.getClientName(), target, name + " transfer " + count + " " + packetnum++, Arrays.copyOf(fileBuffer, count)));
					total += count;
				}
				System.err.println("Sending finished after " + (packetnum - 1) + " packets. Total length: " + total + " Target: " + fileLength);
				client.getSendingStream().writeObject(new DataPacket("dlpacket", client.getClientName(), target, name + " end " + total, null));
				fileStream.close();
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				Platform.runLater(() -> Popups.startInfoDlg("Download Error", "Failed to send file!"));
				FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
				debugPrint("Failed send count: " + count);
			}
		};
		return run;
	}
	
	/**
	 * Returns a runnable which downloads an image from a remote client through the given local client.
	 * @param sc The screen controller used for interaction.
	 * @param file The destination file to which the image is saved.
	 * @param length The length of the specified file.
	 * @return A runnable which downloads an incoming image from a remote client.
	 */
	public static Runnable dlPic(MainScreenController sc, File file, long length) {
		Runnable run = () -> {
			FileOutputStream fileOut = null;
			int count = 1;
			try {
				long total = 0;
				byte[] fileBuffer = new byte[8192];
				fileOut = new FileOutputStream(file);
				while (total != length) {
					FileHandler.writeToErrorLog("Testing logging. Also debugging that img thing. Length: " + length + "; count: " + count + "; Total: " + total + " (this was the first go)");
					//FileHandler.writeToErrorLog("Testing logging. Also debugging that img thing. Available?: " + sc.getClient().getPicInData().available());
					//count = sc.getClient().getPicInData().read(fileBuffer, 0, 8192);
					total += count;
					FileHandler.writeToErrorLog("Testing logging. Also debugging that img thing. Length: " + length + "; count: " + count + "; Total: " + total + " (this was the second go)");
					fileOut.write(fileBuffer, 0, count);
					fileOut.flush();
				}
				fileOut.close();
				Platform.runLater(() -> {
					System.out.println("Blrgh.");
					Image img = null;
					try {
						img = new Image(new FileInputStream(file));
					} catch (Exception e) {
						FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
					}
					ImageView imgview = new ImageView(img);
					sc.getImages().getChildren().add(imgview);
				});
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				Platform.runLater(() -> Popups.startInfoDlg("Download Error", "Failed to download picture!"));
				FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
				System.out.println("Failed send count: " + count);
			}
		};
		
		return run;
		
	}
	
	/**
	 * Returns a runnable that sends an image to a remote client or clients, as given by the input.
	 * @param target The name of the target to which the image is being sent.
	 * @param file The image to send.
	 * @param client The client being used to send the image.
	 * @return A runnable which sends the specified image.
	 */
	public static Runnable sendPic(String target, File file, Client client) {
		Runnable run = () -> {
			int count = 1;
			try {
				byte[] fileBuffer = new byte[8192];
				FileInputStream fileStream = new FileInputStream(file);
				String name = file.getName().replaceAll(" ", "_");
				long fileLength = file.length();
				long total = 0;
				client.getSendingStream().writeObject(new DataPacket("imgpacket", client.getClientName(), target, name + " start " + fileLength, null));
				long packetnum = 1;
				while (total != fileLength) {
					count = fileStream.read(fileBuffer, 0, 8192);
					client.getSendingStream().writeObject(new DataPacket("imgpacket", client.getClientName(), target, name + " transfer " + count + " " + packetnum++, Arrays.copyOf(fileBuffer, count)));
					total += count;
				}
				System.err.println("Sending finished after " + (packetnum - 1) + " packets. Total length: " + total + " Target: " + fileLength);
				client.getSendingStream().writeObject(new DataPacket("imgpacket", client.getClientName(), target, name + " end " + total, null));
				fileStream.close();
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				Platform.runLater(() -> Popups.startInfoDlg("Download Error", "Failed to send file!"));
				FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
				debugPrint("Failed send count: " + count);
			}
		};
		return run;
	}
	
	/**
	 * Transmits audio from a file through the screen controller's client streams.
	 * @param sc The screen controller whose client to use for sending the file.
	 * @param file The file whose audio is being transmitted.
	 */
	public static void transmitAudio(Client client, File file) {
		int count = 1;
		try {
			byte[] fileBuffer = new byte[8192];
			FileInputStream fileStream = new FileInputStream(file);
			client.getSendingStream().writeObject(new DataPacket("audiopacket", client.getClientName(), "all", "start", fileBuffer));
			while ((count = fileStream.read(fileBuffer, 0, 8192)) > 0) {
				client.getSendingStream().writeObject(new DataPacket("audiopacket", client.getClientName(), "all", "transfer " + count, fileBuffer));
			}
			client.getSendingStream().writeObject(new DataPacket("audiopacket", client.getClientName(), "all", "end", fileBuffer));
			fileStream.close();
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			Platform.runLater(() -> Popups.startInfoDlg("Download Error", "Failed to transmit audio!"));
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
			System.out.println("Failed send count: " + count);
		}
	}
	
}