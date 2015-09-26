package userInteract;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import application.ChatClient;
import application.WindowController;
import client.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tools.AudioHandler;
import tools.CommandParser;
import tools.FileHandler;
import tools.SystemInfo;

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

	//Strings
	private String username = "";

	//Scene
	private Stage window;
	private GridPane layout = new GridPane();

	//Objects
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
	private ProgressBar pb = new ProgressBar();

	public MainScreenController(GridPane layout, Stage window, Scene currentScene, Scene nextScene, WindowController windowController) throws URISyntaxException, IOException {

		new File(FileHandler.downloadsPath).mkdirs();
		new File(FileHandler.picturesPath).mkdirs();
		this.layout = layout;
		this.window = window;
		FileHandler.readLog(this.getChatBox());

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
		//this.chatView.setFitToWidth(true);

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
					FileHandler.downloadFile(this.window, this.dlField.getText(), this);
				} catch (Exception ex) {
					ex.printStackTrace();
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
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
		this.scienceButton.addEventHandler(ActionEvent.ACTION, e -> {
			try {
				File file = Popups.startPictureOpener("Select a file to do absolutely nothing with!");
				System.out.println(file.getName());
				if (file != null) {
					Image img = new Image(new FileInputStream(file));
					ImageView imgview = new ImageView(img);
					imgview.setOnMouseClicked(click -> {
						if (click.getClickCount() == 2) {
							Popups.startImageViewer(file.getName(), img);
						}
					});
					this.getImages().getChildren().add(imgview);
				}
			} catch (Exception ex) {
				System.out.println("Well, that didn't work. Stop trying to break things.");
				ex.printStackTrace();
			}
		});
		
		sendButton.addEventHandler(ActionEvent.ACTION, e -> {
			CommandParser.sendFile("/sendfile all", this);
		});
		
		this.folderButton.addEventHandler(ActionEvent.ACTION, e -> {
			try {
				Desktop.getDesktop().open(new File(System.getProperty("user.home") + "/Documents/Etheralt Chat Client"));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		
	}

	public void addMessage(String msg, String color, String bgColor) {

		FileHandler.writeToChatLog(msg);

		if (!msg.startsWith("*!") && !msg.startsWith("/")) {
			Platform.runLater(() -> {
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
				if (this.getChatField().getText().startsWith("/")) {
					Platform.runLater(() -> {
						CommandParser.parse(this.getChatField().getText(), this, prevInput);
					});
				} else {
					try {
						this.getClient().getClientSendingData().writeUTF(this.getChatField().getText().trim());
						prevInput.add(this.getChatField().getText());
						this.getChatField().clear();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (key.getCode() == KeyCode.UP) {
				if (i < prevInput.size() - 1) {
					i++;
				}
				try {
					this.chatField.setText(prevInput.get(i));
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Problem scrolling through previous input.");
				}
			}
		});

	}

	private void initLogoutButton() {

		this.logoutButton = new Button();
		this.logoutButton.setText("Log out");
		this.logoutButton.setOnAction(e -> {
			try {
				this.client.getClientSendingData().writeUTF("*![System] " + SystemInfo.getDate() + ": " + this.client.getClientName() + " has disconnected.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			window.close();
			try {
				new ChatClient().start(new Stage());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
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

}