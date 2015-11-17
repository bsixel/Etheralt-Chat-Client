package userInteract;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import application.ChatClient;
import client.Client;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tools.AudioHandler;
import tools.CommandParser;
import tools.FileHandler;
import tools.SystemInfo;

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

public class MainScreenController implements EventHandler<KeyEvent> {

	//Lists
	private ArrayList<Thread> dlThreads = new ArrayList<Thread>();
	
	//Buttons
	private Button logoutButton;
	private Button scienceButton = new Button("Science Button!");
	private Button scrollButton = new Button("Scroll Button");
	private ToggleButton tb = new ToggleButton("Call");
	private Button folderButton = new Button("Open Data Folder");
	private Button sendButton = new Button("Send File");

	//Numbers
	int i = 0;

	//Booleans
	private boolean isHosting;
	private boolean isSpinning = false;

	//Strings
	private String username = "";

	//Scene
	private Stage window;
	private GridPane layout = new GridPane();

	//Objects
	private Thread clientThread;
	private TextArea chatField = new TextArea();
	private Label usernameLabel;
	private TextArea usersArea = new TextArea("Connected users: ");
	private VBox firstColumn = new VBox(10);
	private VBox secondColumn = new VBox(10);
	private Client client = new Client();
	private TextField dlField = new TextField();
	private ChatBox chatBox = new ChatBox();
	private ScrollPane chatView = new ScrollPane(this.chatBox);
	private HBox buttonBox = new HBox(10);
	private AudioHandler audioHandler = new AudioHandler(this.client);
	private CheckBox saveAudio = new CheckBox();
	private VBox images = new VBox(10);
	private ScrollPane imageScroll = new ScrollPane(getImages());
	private HBox columnsContainer = new HBox();
	private Scene currScene;

	public MainScreenController(GridPane layout, Stage window, Scene currentScene, Scene nextScene) throws URISyntaxException, IOException {

		new File(FileHandler.downloadsPath).mkdirs();
		new File(FileHandler.picturesPath).mkdirs();
		this.layout = layout;
		this.window = window;
		FileHandler.readLog(this.getChatBox());
		this.currScene = currentScene;

	}

	private void initChatView() {
		
		this.firstColumn.setPrefSize(500, 675);
		this.firstColumn.setMinSize(500, 675);
		this.firstColumn.setMaxSize(500, 675);
		this.firstColumn.getStyleClass().add("vbox");
		
		this.chatView.setMinSize(500, 500);
		this.chatView.setPrefSize(500, 500);
		this.chatView.setMaxSize(500, 500);
		this.chatView.hbarPolicyProperty().set(ScrollBarPolicy.NEVER);
		this.chatView.vbarPolicyProperty().set(ScrollBarPolicy.ALWAYS);

		this.scrollButton.setOnAction(e -> {
			scrollToBottom();
		});

	}

	private void initSecondColumn() {
		
		this.secondColumn.setPadding(new Insets(10, 10, 10, 10));
		this.secondColumn.setPrefSize(520, 600);
		this.secondColumn.setMinSize(520, 600);
		this.secondColumn.setMaxSize(520, 600);
		this.secondColumn.getStyleClass().add("vbox");

		Button dlButton = new Button("Download link:");
		dlButton.setOnAction(e -> {
			if (!this.dlField.getText().equals(null) || !this.dlField.getText().equals(null)) {
				try {
					FileHandler.downloadFile(this.dlField.getText(), this);
				} catch (Exception ex) {
					FileHandler.debugPrint(ex.getMessage() + ex.getStackTrace()[0].toString());
				}
				this.dlField.clear();
			}
		});
		this.dlField.setPrefSize(450, 20);
		this.dlField.setMinSize(450, 20);
		this.dlField.setMaxSize(450, 20);
		this.imageScroll.setMinSize(500, 565);
		this.imageScroll.setMaxSize(500, 565);
		this.imageScroll.setPrefSize(500, 565);
		this.imageScroll.setId("imageScroll");
		this.secondColumn.getChildren().addAll(dlButton, this.dlField, this.imageScroll);

	}
	
	private void initUsersArea() {
		
		this.usersArea.setWrapText(true);
		this.usersArea.setPrefSize(500, 50);
		this.usersArea.setMinSize(500, 50);
		this.usersArea.setMaxSize(500, 75);
		this.usersArea.setEditable(false);

	}

	public void scrollToBottom() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
		}
		Platform.runLater(() -> {
			this.chatView.setVvalue(1.0);
		});
	}

	private void initUsernameLabel() {

		this.usernameLabel = new Label();
		this.usernameLabel.setId("username-label");
		this.usernameLabel.setText(" Logged in as ");

	}

	private void initLayout() {

		this.layout.setPadding(new Insets(10, 10, 10, 10));
		this.layout.setVgap(10);
		this.layout.setHgap(10);
		this.layout.getChildren().addAll(this.buttonBox, this.columnsContainer);
		GridPane.setConstraints(this.buttonBox, 0, 0);
		GridPane.setConstraints(this.columnsContainer, 0, 1);
		this.columnsContainer.getChildren().addAll(this.firstColumn, this.secondColumn);
		this.buttonBox.getChildren().addAll(this.logoutButton, this.scrollButton, this.folderButton, this.scienceButton, this.tb, this.saveAudio, this.sendButton);
		this.firstColumn.getChildren().addAll(this.usernameLabel, this.usersArea, this.chatView, this.chatField);
		
		this.currScene.setOnKeyPressed(k -> {
			if (k.isShiftDown() && k.getCode().equals(KeyCode.ESCAPE)) {
				this.isSpinning = !this.isSpinning;
			}
		});
		
	}
	
	private void initToggle() {
		
		this.tb.setId("voice-toggle");
		this.tb.selectedProperty().addListener(e -> {
			Thread voice = new Thread(() -> {
				this.audioHandler.start(this.saveAudio.isSelected());
			});
			voice.setDaemon(true);
			if (this.tb.isSelected()) {
				voice.start();
			} else {
				this.audioHandler.stop();
				voice.interrupt();
			}
		});
		
	}
	
	private void initExtraButtons() {
		this.scienceButton.setId("science");
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), a -> {
			if (this.isSpinning) {
				this.layout.setRotate(this.layout.getRotate() + 10);
			} else {
				this.layout.setRotate(0);
			}
		}));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
		this.scienceButton.addEventHandler(ActionEvent.ACTION, e -> {
			try {
				this.isSpinning = !this.isSpinning;
			} catch (Exception ex) {
				System.out.println("Well, that didn't work. Stop trying to break things.");
				FileHandler.debugPrint(ex.getMessage() + ex.getStackTrace()[0].toString());
			}
		});
		
		sendButton.addEventHandler(ActionEvent.ACTION, e -> {
			CommandParser.sendFile("/sendfile all", this);
		});
		
		this.folderButton.addEventHandler(ActionEvent.ACTION, e -> {
			try {
				Desktop.getDesktop().open(new File(System.getProperty("user.home") + "/Documents/Etheralt Chat Client"));
			} catch (Exception e1) {
				FileHandler.debugPrint(e1.getMessage() + e1.getStackTrace()[0].toString());
			}
		});
		
	}

	public void addMessage(String msg, String color, String bgColor) {

		FileHandler.writeToChatLog(msg);

		if (!msg.startsWith("*!") && !msg.startsWith("/")) {
			Platform.runLater(() -> {
				this.window.toFront();
				this.getChatBox().addText(new ChatText(msg, color, bgColor));
				scrollToBottom();
			});
		}

	}

	private void initChatField() {

		ArrayList<String> prevInput = new ArrayList<String>();
		this.getChatField().setPrefSize(500, 10);
		this.getChatField().setMaxHeight(10);
		this.getChatField().autosize();
		this.getChatField().setWrapText(true);
		this.getChatField().addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			if (key.getCode() == KeyCode.ENTER) {
				key.consume();
				if (this.getChatField().getText().startsWith("*!")) {
					
				} else if (this.getChatField().getText().startsWith("/")) {
					Platform.runLater(() -> {
						CommandParser.parseMSC(this.getChatField().getText(), this, prevInput);
					});
				} else {
					try {
						this.getClient().getClientSendingData().writeUTF(this.getChatField().getText().trim());
						prevInput.add(this.getChatField().getText());
						this.getChatField().clear();
					} catch (Exception e) {
						FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
					}
				}
			} else if (key.getCode() == KeyCode.UP) {
				if (i < prevInput.size() - 1) {
					i++;
				}
				try {
					this.chatField.setText(prevInput.get(i));
				} catch (Exception e) {
					FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
					FileHandler.debugPrint("Problem scrolling through previous input.");
				}
			}
		});

	}

	private void initLogoutButton() {

		this.logoutButton = new Button();
		this.logoutButton.setText("Log out");
		this.logoutButton.setOnAction(e -> {
			logout();
		});

	}

	public void initMainScreen() {

		initChatView();
		initUsernameLabel();
		initLogoutButton();
		initChatField();
		initLayout();
		initUsersArea();
		initExtraButtons();
		initSecondColumn();
		initToggle();

	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return this.username;
	}

	public Label getUsernameLabel() {
		return this.usernameLabel;
	}

	public GridPane getLayout() {
		return this.layout;
	}

	public void setIsHosting(boolean b) {
		this.isHosting = b;
	}

	public boolean getIsHosting() {
		return this.isHosting;
	}

	public Client getClient() {
		return this.client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public ScrollPane getChatView() {
		return chatView;
	}

	public void setChatView(ScrollPane chatView) {
		this.chatView = chatView;
	}

	public TextArea getChatField() {
		return chatField;
	}

	public void setChatField(TextArea chatField) {
		this.chatField = chatField;
	}

	public TextArea getUsersArea() {
		return this.usersArea;
	}

	public ChatBox getChatBox() {
		return chatBox;
	}

	public void setChatBox(ChatBox chatBox) {
		this.chatBox = chatBox;
	}

	public int getI() {
		return this.i;
	}

	public ArrayList<Thread> getDlThreads() {
		return dlThreads;
	}

	public void setDlThreads(ArrayList<Thread> dlThreads) {
		this.dlThreads = dlThreads;
	}

	public Stage getWindow() {
		return window;
	}

	public void setWindow(Stage window) {
		this.window = window;
	}

	@Override
	public void handle(KeyEvent key) {
		if (key.getCode().equals(KeyCode.PAGE_DOWN)) {
			this.scrollButton.fire();
		}
	}

	public VBox getImages() {
		return images;
	}

	public void setImages(VBox images) {
		this.images = images;
	}
	
	public void logout() {
		try {
			this.getClient().getClientSendingData().writeUTF("*![System] " + SystemInfo.getDate() + ": " + this.getClient().getClientName() + " has disconnected.");
			FileHandler.writeToChatLog("[System] " + SystemInfo.getDate() + ": " + this.getClient().getClientName() + " has disconnected.");
			this.getClient().getClientSendingData().writeUTF("*!disconnect " + this.client.getClientName() + " 'Client disconnected.'");
			System.out.println("Yeah here.");
			getClient().setRunning(false);
			this.getClientThread().interrupt();
		} catch (Exception e) {
			
		}
		try {
			window.close();
			new ChatClient().launch(new Stage());
		} catch (Exception e) {
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());;
		}
	}

	public Thread getClientThread() {
		return clientThread;
	}

	public void setClientThread(Thread clientThread) {
		this.clientThread = clientThread;
	}

}