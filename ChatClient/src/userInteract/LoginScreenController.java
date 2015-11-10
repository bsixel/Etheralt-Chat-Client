package userInteract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import tools.FileHandler;
import tools.SystemInfo;

/**
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

public class LoginScreenController {

	// Lists
	private List<Button> buttons = new ArrayList<Button>();

	// Buttons
	private Button loginButton;

	// Numbers

	// Booleans
	private boolean locked = true;
	private boolean stopped = false;
	private boolean nameTaken = false;

	// Strings
	private String loginString = "Please enter your display name.";
	private String username;

	// Scene
	private Stage window;
	private Scene chatScreen;
	private VBox layout = new VBox(15);

	// Objects
	private Label usernameLabel;
	private TextField usernameField;
	private MainScreenController mainController;
	private HBox IPLayout = new HBox(10);
	private ComboBox<String> IPChoice = new ComboBox<String>();
	private Label IPLabel;
	private TextField portField;


	public LoginScreenController(MainScreenController mainController, VBox mainScreenLayout, Stage window, Scene nextScene) {

		this.setMainController(mainController);
		this.layout = mainScreenLayout;
		this.window = window;
		this.chatScreen = nextScene;

	}
	
	private void initUsernameLabel() {

		this.usernameLabel = new Label(loginString);
		this.usernameLabel.setPrefSize(175, 15);
		this.usernameLabel.setTextAlignment(TextAlignment.CENTER);
		this.layout.getChildren().addAll(usernameLabel, getUsernameField());

	}

	private void initIPField() {
		
		List<String> ips;
		try {
			ips = Arrays.asList(FileHandler.getProperty("prev_ips").split(","));
			Collections.reverse(ips);
			this.IPChoice.setValue(ips.get(0));
		} catch (NullPointerException e1) {
			ips = new ArrayList<String>();
			FileHandler.debugPrint("Error loading previous IPs from config - loading defaults (empty).");
		}
		this.IPChoice.getItems().addAll(ips);
		this.IPChoice.setEditable(true);
		
		this.IPLabel = new Label("Please enter a host IP address and port: ");

		this.IPChoice.setMaxSize(175, 10);
		this.IPChoice.setPrefSize(175, 10);

		this.IPChoice.addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			if (key.getCode() == KeyCode.ENTER) {
				if ((this.getIPChoice().contains(" ") || this.getIPChoice().equals("")
						|| this.getIPChoice().equals(null))
						&& (this.getIPChoice().contains(" ") || this.getIPChoice().equals("")
								|| this.getIPChoice().equals(null))) {
					Popups.startInfoDlg("Invalid IP", "Please enter a valid host.");
				} else if (!(this.getIPChoice().contains(" ") || this.getIPChoice().equals(""))
						&& (this.getIPChoice().contains(" ") || this.getIPChoice().equals("")
								|| this.getIPChoice().equals(null))) {
					try {
						login(false);
					} catch (Exception e) {
						Popups.startInfoDlg("Invalid IP", "Please enter a valid host.");
						FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
					}
				}
			}
		});

		this.setPortField(new TextField());

		String prevPort = FileHandler.getProperty("last_port");
		if (prevPort == null) {
			FileHandler.setProperty("last_port", "");
		} else if (prevPort != null) {
			this.IPChoice.getEditor().setText(ips.get(0));
			this.getPortField().setText(prevPort);
		}
		
		

		this.getPortField().setMaxSize(50, 10);
		this.getPortField().setPrefSize(50, 10);

		this.IPLayout.getChildren().addAll(this.IPChoice, this.getPortField());
		this.layout.getChildren().addAll(this.IPLabel, this.IPLayout);
		this.IPLayout.setAlignment(Pos.CENTER);

	}

	private void initUserField() {

		this.setUsernameField(new TextField());

		String prevUsername = FileHandler.getProperty("last_username");
		if (prevUsername == null) {
			FileHandler.setProperty("last_username", "");
		} else if (prevUsername != null) {
			this.getUsernameField().setText(prevUsername);
		}

		this.getUsernameField().setMaxSize(215, 15);
		this.getUsernameField().setPrefSize(215, 15);
		this.getUsernameField().addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			if (key.getCode() == KeyCode.ENTER) {
				if ((this.getIPChoice().contains(" ") || this.getIPChoice().equals("") || this.getIPChoice().equals(null))) {
					Popups.startInfoDlg("Invalid IP", "Please enter a valid host IP address.");
				} else if (!(this.getIPChoice().contains(" ") || this.getIPChoice().equals(""))) {
					try {
						login(false);
					} catch (Exception e) {
						Popups.startInfoDlg("Invalid IP", "Please enter a valid host.");
						FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
					}
				}
			}
		});

	}

	private void login(boolean b) throws NumberFormatException, IOException {
		
		if (FileHandler.getProperty("computer_ID") == null) {
			FileHandler.setProperty("computer_ID", UUID.randomUUID().toString());
		}
		if (getUsernameField().getText().contains(" ") || getUsernameField().getText().equals("")) {
			Popups.startInfoDlg("", "Please enter a name with no spaces.");
		} else if (!getUsernameField().getText().contains(" ")) {
			this.username = getUsernameField().getText();
			this.getMainController().setUsername(this.username);
			Label userLabel = this.getMainController().getUsernameLabel();
			String str = " Logged in as " + this.username + " ";
			userLabel.setText(str);
			String pass = Popups.startPasswdDlg("Enter server password:");
			Object lock = new Object();
			Runnable startClient = () -> {
				String testIP = this.getIPChoice();
				try {
					getMainController().getClient().startClient(testIP, Integer.parseInt(this.getPortField().getText()), this, pass, lock);
				} catch (Exception e) {
					Platform.runLater(() -> {FileHandler.debugPrint("Line " + e.getStackTrace()[0].toString() + ": Unable to start client; incorrect password or invalid server. Might also have been kicked from server.");});
					Platform.runLater(() -> {getMainController().logout();});
					FileHandler.debugPrint("Line " + e.getStackTrace()[0].getLineNumber() + ": Unable to start client; incorrect password or invalid server. Might also have been kicked from server.");
					setLocked(false);
					setStop(true);
				}
			};

			Thread clientThread = new Thread(startClient);
			clientThread.setDaemon(true);
			clientThread.start();
			synchronized (lock) {
				try {
					while (locked) {
						lock.wait(3000);
						if (locked) {
							Popups.startInfoDlg("Connection error!", "Unable to connect to server:" + System.lineSeparator() + "Connection timed out.");
							FileHandler.debugPrint("Connection error! Unable to connect to server: Connection timed out.");
							return;
						}
					}
				} catch (InterruptedException e) {
					FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
				}
			}
			if (this.nameTaken) {
				Popups.startInfoDlg("Connection error!", "Unable to connect to server:" + System.lineSeparator() + "Incorrect password/Username taken.");
				FileHandler.debugPrint("Connection error! Unable to connect to server: Incorrect password/Username taken.");
				return;
			}
			if (isStopped()) {
				Popups.startInfoDlg("Connection error!", "Unable to connect to server: " + System.lineSeparator() + "Connection refused or incorrect password.");
				FileHandler.debugPrint("Connection error! Unable to connect to server: Connection refused or incorrect password.");
				return;
			}
			this.mainController.scrollToBottom();
			FileHandler.saveProperties(this);
			this.window.setScene(chatScreen);
			window.setWidth(1075);
			window.setHeight(750);
			this.window.setMinWidth(1075);
			this.window.setMinHeight(750);
			window.setX((SystemInfo.getScreenWidth() / 2) - window.getWidth() / 2);
			window.setY((SystemInfo.getScreenHeight() / 2) - window.getHeight() / 2);
			window.setResizable(true);
			this.IPLabel.setText("Please enter a host IP address and port: ");

		}
	}

	private void initLoginButton() {

		this.loginButton = new Button();
		this.loginButton.setText("Log in");
		this.loginButton.addEventHandler(ActionEvent.ACTION, e -> {
			if (this.getIPChoice().equals(null) || this.getIPChoice().contains(" ") || this.getIPChoice().equals("")) {
				Popups.startInfoDlg("Invalid IP", "Please enter a valid host IP address.");
			} else if (!(this.getIPChoice().contains(" ") || this.getIPChoice().equals(""))) {
				try {
					login(false);
				} catch (Exception e1) {
					Popups.startInfoDlg("Invalid IP", "Please enter a valid host.");
					FileHandler.debugPrint("Invalid IP! Try a different host.");
					FileHandler.debugPrint(e1.getMessage() + e1.getStackTrace()[0].toString());
				}
			}
		});

	}

	public void initLoginScreen() {

		FileHandler.initUserPrefs();
		initUserField();
		initLoginButton();
		initUsernameLabel();
		this.layout.getChildren().addAll(this.loginButton);
		initIPField();
		window.setWidth(250);
		window.setHeight(400);
		layout.setAlignment(Pos.CENTER);
		window.setOnCloseRequest(e -> {
			e.consume();
			if (Popups.startConfDlg("Are you sure you want to exit?")) {
				try {
					this.mainController.getClient().getClientSendingData().writeUTF("*![System] " + SystemInfo.getDate() + ": " + this.mainController.getClient().getClientName() + " has disconnected.");
					FileHandler.writeToChatLog("[System] " + SystemInfo.getDate() + ": "
							+ this.mainController.getClient().getClientName() + " has disconnected.");
					this.mainController.getClient().setRunning(false);
				} catch (Exception e1) {
					if (window.getScene().equals(chatScreen)) {
						FileHandler.debugPrint("You probably tried closing the window without logging in. That throws errors.");
					}
				}
				window.close();
				System.exit(0);
			}
		});

	}

	public List<Button> getButtons() {
		return this.buttons;
	}

	public VBox getLayout() {
		return this.layout;
	}

	public String getUsername() {
		return this.username;
	}

	public TextField getUsernameField() {
		return usernameField;
	}

	public void setUsernameField(TextField usernameField) {
		this.usernameField = usernameField;
	}

	public TextField getPortField() {
		return portField;
	}

	public void setPortField(TextField portField) {
		this.portField = portField;
	}

	public MainScreenController getMainController() {
		return mainController;
	}

	public void setMainController(MainScreenController mainController) {
		this.mainController = mainController;
	}

	public void setUsername(String name) {
		this.username = name;
	}

	public void toggleLock() {
		this.locked = !this.locked;
	}

	public boolean isStopped() {
		return stopped;
	}

	public void setStop(boolean stop) {
		this.stopped = stop;
	}
	
	public void setLocked(boolean locked) {
		this.locked= locked;
	}
	
	public boolean getLocked() {
		return this.locked;
	}
	
	public void setNameTaken(boolean taken) {
		this.nameTaken = taken;
	}
	
	public boolean getNameTaken() {
		return this.nameTaken;
	}

	public String getIPChoice() {
		return this.IPChoice.getEditor().getText();
	}

	public void setIPChoice(ComboBox<String> iPChoice) {
		IPChoice = iPChoice;
	}

}