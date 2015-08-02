package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javafx.application.Platform;
import sun.audio.AudioData;
import sun.audio.AudioDataStream;
import sun.audio.AudioPlayer;
import tools.CommandParser;
import userInteract.LoginScreenController;

public class Client {
	
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
	private LoginScreenController ls;
	private AudioData ad;
	private byte[] voiceInput = new byte[10000];
	
	//Strings
	private String clientname;
	
	public void startClient(String IP, int port, LoginScreenController ls) throws IOException {
		
		this.textSocket = new Socket(InetAddress.getByName(IP), port);
		this.textInData = new DataInputStream(this.textSocket.getInputStream());
		this.textOutData = new DataOutputStream(this.textSocket.getOutputStream());
		this.DLSocket = new Socket(InetAddress.getByName(IP), port + 1);
		this.DLInData = new DataInputStream(this.DLSocket.getInputStream());
		this.DLOutData = new DataOutputStream(this.DLSocket.getOutputStream());
		this.voiceSocket = new Socket(InetAddress.getByName(IP), port + 2);
		this.voiceInData = new DataInputStream(this.voiceSocket.getInputStream());
		this.voiceOutData = new DataOutputStream(this.voiceSocket.getOutputStream());
		
		this.ls = ls;
		String editedName = ls.getUsernameField().getText();
		while (true) {
			
			this.textOutData.writeUTF("*!givename: " + editedName.trim());
			
			String input = this.textInData.readUTF().trim();
			int n = 1;
			
			if (input.equals("*!granted")) {
				this.setClientname(editedName);
				ls.setUsername(editedName);
				break;
			} else {
				editedName = editedName + n;
				n++;
			}
		}
		
		while (true) {
			
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
				} else {
					
					if (input.split(" ")[0].equalsIgnoreCase("[" + this.clientname + "]")) {
						ls.getMainController().addMessage(input, "indigo", "black");
					} else {
						ls.getMainController().addMessage(input, "darkviolet", "black");
					}
				}

			}
			previous = input;
		}
		
	}
	
	public void sendServerMessage(String msg) {
		
		try {
			this.textOutData.writeUTF(msg.trim());
		} catch (IOException e) {
			System.out.println("Some sort of networking problem occured." + "\n" + "I don't really understand java networking yet, so I don't know what.");
			e.printStackTrace();
		}
		
	}
	
	public void closeClient() {
		
		try {
			this.textInData.close();
			this.textOutData.close();
			this.textSocket.close();
		} catch (IOException e) {
			System.out.println("Some sort of networking problem occured." + "\n" + "I don't really understand java networking yet, so I don't know what.");
			e.printStackTrace();
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
	
}