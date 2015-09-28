package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

import javafx.application.Platform;
import javafx.scene.control.PasswordField;
import sun.audio.AudioData;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import tools.CommandParser;
import tools.FileHandler;
import userInteract.LoginScreenController;

public class Client {
	
	//booleans
	private boolean running = true;
	private boolean milTime = true;
	
	//Objects
	private Socket textSocket;
	private DataInputStream textInData;
	private DataOutputStream textOutData;
	private Socket DLSocket;
	private DataInputStream DLInData;
	private DataOutputStream DLOutData;
	private Socket voiceSocket;
	private DataInputStream voiceInData;
	private DataOutputStream voiceOutData;
	private Socket picSocket;
	private DataInputStream picInData;
	private DataOutputStream picOutData;
	private LoginScreenController ls;
	private AudioData ad;
	private byte[] voiceInput = new byte[8192];
	private PrintStream out;
	
	//Strings
	private String clientname;

	
	public void startClient(String IP, int port, LoginScreenController ls, String password, PrintStream out, Object lock) throws IOException {
		this.out = out;
		
		this.textSocket = new Socket(InetAddress.getByName(IP), port);
		this.textInData = new DataInputStream(this.textSocket.getInputStream());
		this.textOutData = new DataOutputStream(this.textSocket.getOutputStream());
		this.DLSocket = new Socket(InetAddress.getByName(IP), port + 1);
		this.DLInData = new DataInputStream(this.DLSocket.getInputStream());
		this.DLOutData = new DataOutputStream(this.DLSocket.getOutputStream());
		this.voiceSocket = new Socket(InetAddress.getByName(IP), port + 2);
		this.voiceInData = new DataInputStream(this.voiceSocket.getInputStream());
		this.voiceOutData = new DataOutputStream(this.voiceSocket.getOutputStream());
		this.picSocket = new Socket(InetAddress.getByName(IP), port + 3);
		this.picInData = new DataInputStream(this.picSocket.getInputStream());
		this.picOutData = new DataOutputStream(this.picSocket.getOutputStream());
		
		String editedName = ls.getUsernameField().getText();
		int n = 1;
		while (true) {
			
			if (password.equalsIgnoreCase("")) {
				this.textOutData.writeUTF("*!givename: " + editedName.trim() + " " + FileHandler.getProperty("computer_ID") + " default");
			} else {
				this.textOutData.writeUTF("*!givename: " + editedName.trim() + " " + FileHandler.getProperty("computer_ID") + " " + password);
			}
			String input = this.textInData.readUTF().trim();
			out.println("Input: " + input);
			if (input.equalsIgnoreCase("*!decline:password")) {
				out.println("Declined password: " + password);
				ls.setLocked(false);
				ls.setNameTaken(true);
				synchronized (lock) {
					lock.notifyAll();
				}
			}
			if (input.equalsIgnoreCase("*!decline:username")) {
				out.println("Declined username.");
				ls.setLocked(false);
				ls.setNameTaken(true);
				synchronized (lock) {
					lock.notifyAll();
				}
			}
			if (input.equalsIgnoreCase("*!granted")) {
				out.println("Granted.");
				synchronized (lock) {
					lock.notifyAll();
				}
				ls.setLocked(false);
				this.setClientname(editedName);
				ls.setUsername(editedName);
				break;
			} else {
				editedName = editedName + n;
				n++;
			}
		}
		ls.setLocked(false);
		
		/*synchronized (lock) {
			ls.toggleLock();
			Platform.runLater(() -> {
				System.out.println("Unlocked from client.");
			});
		}*/
		
		Thread audioThread = new Thread(() -> {
			try {
				InputStream stream = this.getVoiceInData();
				AudioStream audioStream = new AudioStream(stream);
				AudioPlayer.player.start(audioStream);
			} catch (Exception e) {
				if (!this.running) {
					e.printStackTrace();
				}
			}
		});
		audioThread.setDaemon(true);
		audioThread.start();
		try {
		while (this.running) {
			
			String previous = null;
			String input = this.textInData.readUTF().trim();
			
			if (input != null && !input.equalsIgnoreCase(previous)) {
				
				if (input.startsWith("*![System")) {
					String phrase = input.trim().substring(input.trim().indexOf("!") + 1);
					ls.getMainController().addMessage(phrase, "blue", "black");

				} else if (input.startsWith("*!")) {
					CommandParser.parse(input, this);
				} else if (input.startsWith("/")) {
					Platform.runLater(() -> {
						CommandParser.parse(input, ls.getMainController(), null);
					});
				} else if (input.split(" ")[0].equalsIgnoreCase("[" + this.clientname + "]")) {
					ls.getMainController().addMessage(input, "indigo", "black");
				} else {
					ls.getMainController().addMessage(input, "darkviolet", "black");
				}

			}
			previous = input;
		}
		} catch (java.io.EOFException e) {
			System.err.println("You probably just closed the chat client window. Eventually I'll figure out how to make it gracefully shut down.");
		}
	}

	public Socket getClientSocket() {
		return textSocket;
	}

	public DataInputStream getClientReceivingData() {
		return textInData;
	}

	public DataOutputStream getClientSendingData() {
		return textOutData;
	}

	public String getClientName() {
		return clientname;
	}

	public void setClientname(String clientname) {
		this.clientname = clientname;
	}

	public Socket getTextSocket() {
		return textSocket;
	}

	public void setTextSocket(Socket textSocket) {
		this.textSocket = textSocket;
	}

	public DataInputStream getTextInData() {
		return textInData;
	}

	public void setTextInData(DataInputStream textInData) {
		this.textInData = textInData;
	}

	public DataOutputStream getTextOutData() {
		return textOutData;
	}

	public void setTextOutData(DataOutputStream textOutData) {
		this.textOutData = textOutData;
	}

	public Socket getDLSocket() {
		return DLSocket;
	}

	public void setDLSocket(Socket dLSocket) {
		DLSocket = dLSocket;
	}

	public DataInputStream getDLInData() {
		return DLInData;
	}

	public void setDLInData(DataInputStream dLInData) {
		DLInData = dLInData;
	}

	public DataOutputStream getDLOutData() {
		return DLOutData;
	}

	public void setDLOutData(DataOutputStream dLOutData) {
		DLOutData = dLOutData;
	}

	public Socket getVoiceSocket() {
		return voiceSocket;
	}

	public void setVoiceSocket(Socket voiceSocket) {
		this.voiceSocket = voiceSocket;
	}

	public DataInputStream getVoiceInData() {
		return voiceInData;
	}

	public void setVoiceInData(DataInputStream voiceInData) {
		this.voiceInData = voiceInData;
	}

	public DataOutputStream getVoiceOutData() {
		return voiceOutData;
	}

	public void setVoiceOutData(DataOutputStream voiceOutData) {
		this.voiceOutData = voiceOutData;
	}

	public LoginScreenController getLs() {
		return ls;
	}

	public void setLs(LoginScreenController ls) {
		this.ls = ls;
	}

	public AudioData getAd() {
		return ad;
	}

	public void setAd(AudioData ad) {
		this.ad = ad;
	}

	public byte[] getVoiceInput() {
		return voiceInput;
	}

	public void setVoiceInput(byte[] voiceInput) {
		this.voiceInput = voiceInput;
	}

	public Socket getPicSocket() {
		return picSocket;
	}

	public void setPicSocket(Socket picSocket) {
		this.picSocket = picSocket;
	}

	public DataInputStream getPicInData() {
		return picInData;
	}

	public void setPicInData(DataInputStream picInData) {
		this.picInData = picInData;
	}

	public DataOutputStream getPicOutData() {
		return picOutData;
	}

	public void setPicOutData(DataOutputStream picOutData) {
		this.picOutData = picOutData;
	}

	public String getClientname() {
		return clientname;
	}
	
	public void setRunning(boolean b) {
		this.running = b;
	}
	
	public void setMilTime(boolean b) {
		this.milTime = b;
	}
	
	public boolean getMilTime() {
		return this.milTime;
	}
	
}