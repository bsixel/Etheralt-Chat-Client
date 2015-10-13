package tools;

import java.io.BufferedInputStream;
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
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;
import java.util.UUID;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import userInteract.ChatBox;
import userInteract.ChatText;
import userInteract.LoginScreenController;
import userInteract.MainScreenController;
import client.Client;

public class FileHandler {
	
	public static final String chatLogPath = System.getProperty("user.home") + "/Documents/Etheralt Chat Client/chat_log.txt";
	public static final String downloadsPath = System.getProperty("user.home") + "/Documents/Etheralt Chat Client/Downloads";
	public static final String configPath = System.getProperty("user.home") + "/Documents/Etheralt Chat Client/chat_client.properties";
	
	public static void generateConfigFile() {
		
		try {
			if (new File(configPath).createNewFile()) {
				
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
			
			defaultProperties.setProperty("computer_ID", "");
			defaultProperties.setProperty("last_username", "");
			defaultProperties.setProperty("last_IP", "");
			defaultProperties.setProperty("last_port", "");
			
			Properties userProperties = new Properties(defaultProperties);
			userProperties.load(configReader);
			configReader.close();
			
		}catch (IOException e) {
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
	
	public static void writeBatchFile(String name, String commands){
		
		try {
			String[] cmds = commands.split("#");
			new File(chatLogPath).createNewFile();
			FileWriter writer = new FileWriter(downloadsPath + "/" + name + ".bat", false);
			PrintWriter printer = new PrintWriter(writer);
			for (String str:cmds) {
				printer.println(str);
			}
			writer.close();
			printer.close();
		} catch (IOException e) {
			System.out.println(chatLogPath);
			e.printStackTrace();
		}
	}
	
	public static void readLog(ChatBox chatBox) throws IOException {
		new File(chatLogPath).createNewFile();
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(chatLogPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader textReader = new BufferedReader(fileReader);
		
		for (int i =0; i < getLogLength(); i++) {
			chatBox.addText(new ChatText(textReader.readLine(), "darkred", "black"));
		}
		fileReader.close();
		textReader.close();
		
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
	

	
	public static void downloadFile(Stage window, String url, String fileName) throws IOException {
		
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
				File file = new File(downloadsPath + "/" + fileName + "." + fileType);
				FileOutputStream fileStream = new FileOutputStream(file);
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
	
	public static void downloadFile(Stage window, String url) throws IOException {
		
		Thread dlThread;
		Runnable downloadFile = () -> {
			try {
				EventHandler<WindowEvent> onWindowClose = window.onCloseRequestProperty().get();
				window.setOnCloseRequest(e -> {
					e.consume();
					if (Popups.startConfDlg("Are you sure you want to exit? There is a download in progress.")) {
						window.onCloseRequestProperty().set(onWindowClose);
						window.close();
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
				String fileName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
				File file = new File(FileHandler.downloadsPath + "/" + fileName + "." + fileType);
				FileOutputStream fileStream = new FileOutputStream(file);
				fileStream.getChannel().transferFrom(stream, 0, Long.MAX_VALUE);
				fileStream.close();
				stream.close();
				Platform.runLater(() -> {
					Popups.startInfoDlg("Download complete.", "Download of " + fileName + "." + fileType + " comeplete.");
				});
				Thread.currentThread().interrupt();
				System.out.println("Download of " + fileName + " complete.");
				window.onCloseRequestProperty().set(onWindowClose);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		dlThread = new Thread(downloadFile);
		dlThread.start();
		
	}
	
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
			if (!ls.getHostBox().isSelected()) {
				properties.setProperty("last_IP", ls.getIPField().getText());
			} else if (ls.getHostBox().isSelected() && getProperty("last_IP") != null) {
				properties.setProperty("last_IP", getProperty("last_IP"));
			} else {
				properties.setProperty("last_IP", "");
			}
			properties.setProperty("last_port", ls.getPortField().getText());
			
			OutputStream writer = new FileOutputStream(configFile);
			properties.store(writer, "Saved user info");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static Runnable dlFile(MainScreenController sc, File file, long length) {
		Runnable run = () -> {
			FileOutputStream fileOut = null;
			int count = 1;
			try {
				long total = 0;
				byte[] fileBuffer = new byte[8192];
				fileOut = new FileOutputStream(file);
				System.out.println("DL length: " + length);
				while (total != length) {
					count = sc.getClient().getDLInData().read(fileBuffer, 0, 8192);
					total += count;
					fileOut.write(fileBuffer, 0, count);
					System.out.println("FH wrote " + count + " bytes.");
					fileOut.flush();
				}
				System.out.println("DL Total: " + total);
				fileOut.close();
				System.out.println("Download complete.");
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				Platform.runLater(() ->Popups.startInfoDlg("Download Error", "Failed to download file!"));
				System.out.println("Failed send count: " + count);
				e.printStackTrace();
			}
		};
		
		return run;
		
	}
	
	public static Runnable sendFile(String target, File file, Client client) {
		Runnable run = () -> {
			int count = 1;
			try {
				int total = 0;
				byte[] fileBuffer = new byte[8192];
				System.out.println("FH File size: " + file.length());
				FileInputStream fileStream = new FileInputStream(file);
				client.getTextOutData().writeUTF("*!sendfile: " + client.getClientName() + " " + target + " " + file.getName().replaceAll(" ", "_") + " " + file.length());
				//client.getDLOutData().writeUTF("*!sendfile: " + client.getClientName() + " " + target + " " + file.getName().replaceAll(" ", "_") + " " + file.length());
				while ((count = fileStream.read(fileBuffer, 0, 8192)) > 0) {
					client.getDLOutData().write(fileBuffer, 0, count);
					System.out.println("FH sent " + count + " bytes.");
					client.getDLOutData().flush();
				}
				System.out.println("Finished sending.");
				client.getDLOutData().flush();
				fileStream.close();
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				Platform.runLater(() -> Popups.startInfoDlg("Download Error", "Failed to send file!"));
				System.out.println("Failed send count: " + count);
				e.printStackTrace();
			}
		};
		return run;
	}
	
}