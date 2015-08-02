package userInteract;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import server.Server;
import tools.FileHandler;
import tools.Popups;
import tools.SystemInfo;
import application.WindowController;



public class LoginScreenController {
	
	//Lists
	private List<Button> buttons = new ArrayList<Button>();
	
	//Buttons
	private Button loginButton;
	
	//Numbers
	
	//Booleans
	
	//Strings
	private String loginString = "Please enter your display name.";
	private String username;
	
	//Scene
	private Stage window;
	private Scene chatScreen;
	private VBox layout = new VBox(15);
	
	//Objects
	private Label usernameLabel;
	private TextField usernameField;
	private WindowController windowController;
	private MainScreenController mainController;
	private CheckBox hostBox = new CheckBox();
	private Label hostLabel = new Label("Host?");
	private HBox hostLayout = new HBox(10);
	private HBox IPLayout = new HBox(10);
	private TextField IPField;
	private Label IPLabel;
	private TextField portField;
	
	private void initHostInfo() {
		
		this.getHostBox().allowIndeterminateProperty().set(false);
		this.hostLayout.getChildren().addAll(this.hostLabel, this.getHostBox());
		this.hostLayout.setAlignment(Pos.CENTER);
		this.getHostBox().setTooltip(new Tooltip("Enable/Disable Host"));
		this.getHostBox().selectedProperty().addListener(e -> {
			
			if (this.IPLabel.getText().equals("Please enter a host IP address and port: ")) {
				this.IPLabel.setText("Port: ");
				this.IPLayout.getChildren().remove(this.getIPField());
			} else if (this.IPLabel.getText().equals("Port: ")) {
				this.IPLabel.setText("Please enter a host IP address and port: ");
				this.IPLayout.getChildren().add(0, this.getIPField());
			}
			
			this.getIPField().setVisible(!this.getIPField().isVisible());
			
		});
		
	}
	
	private void initUsernameLabel() {
		
		this.usernameLabel = new Label(loginString);
		this.usernameLabel.setPrefSize(175, 15);
		this.usernameLabel.setTextAlignment(TextAlignment.CENTER);
		this.layout.getChildren().addAll(usernameLabel, getUsernameField());
		
	}
	
	private void initIPField() {

		this.IPLabel = new Label("Please enter a host IP address and port: ");
		
		this.setIPField(new TextField());
		
		String prevIP = FileHandler.getProperty("last_IP");
		if (prevIP == null) {
			FileHandler.setProperty("last_IP", "");
		} else if (prevIP != null) {
			this.getIPField().setText(prevIP);
		}
		
		this.getIPField().setMaxSize(175, 10);
		this.getIPField().setPrefSize(175, 10);
		
		this.getIPField().addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			if (key.getCode() == KeyCode.ENTER) {
				if (this.getHostBox().isSelected()) {
					try {
						login(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (!this.getHostBox().isSelected() && (getIPField().getText().contains(" ") || getIPField().getText().equals("") || getIPField().getText().equals(null)) && (getIPField().getText().contains(" ") || getIPField().getText().equals("") || getIPField().getText().equals(null))) {
					Popups.startInfoDlg("Invalid IP", "Please enter a valid host.");
				} else if (!this.getHostBox().isSelected() && !(getIPField().getText().contains(" ") || getIPField().getText().equals("")) && (getIPField().getText().contains(" ") || getIPField().getText().equals("") || getIPField().getText().equals(null))) {
					try {
						login(false);
					} catch (Exception e) {
						Popups.startInfoDlg("Invalid IP", "Please enter a valid host.");
						e.printStackTrace();
					}
				}
			}
		});
		
		this.setPortField(new TextField());
		
		String prevPort= FileHandler.getProperty("last_port");
		if (prevPort == null) {
			FileHandler.setProperty("last_port", "");
		} else if (prevPort != null) {
			this.getPortField().setText(prevPort);
		}
		
		this.getPortField().setMaxSize(50, 10);
		this.getPortField().setPrefSize(50, 10);
		
		this.IPLayout.getChildren().addAll(this.getIPField(), this.getPortField());
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
				if (this.getHostBox().isSelected()) {
					try {
						login(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (!this.getHostBox().isSelected() && (getIPField().getText().contains(" ") || getIPField().getText().equals("") || getIPField().getText().equals(null))) {
					Popups.startInfoDlg("Invalid IP", "Please enter a valid host IP address.");
				} else if (!this.getHostBox().isSelected() && !(getIPField().getText().contains(" ") || getIPField().getText().equals(""))) {
					try {
						login(false);
					} catch (Exception e) {
						Popups.startInfoDlg("Invalid IP", "Please enter a valid host.");
						e.printStackTrace();
					}
				}
			}
		});
		
	}
	
	public LoginScreenController(MainScreenController mainController, VBox mainScreenLayout, Stage window, Scene nextScene, WindowController windowController) {
		
		this.setMainController(mainController);
		this.windowController = windowController;
		this.layout = mainScreenLayout;
		this.window = window;
		this.chatScreen = nextScene;
		
	}
	
	private void login(boolean b) throws UnknownHostException {
		if (getUsernameField().getText().contains(" ") || getUsernameField().getText().equals("")) {
			Popups.startInfoDlg("", "Please enter a name with no spaces.");
		} else if (!getUsernameField().getText().contains(" ")) {
			this.username = getUsernameField().getText();
			this.getMainController().setUsername(this.username);
			this.windowController.setUsername(this.username);
			Label userLabel = this.getMainController().getUsernameLabel();
			String str = " Logged in as " + this.username + " ";
			userLabel.setText(str);
			getMainController().setServer(new Server());
			
			if (b) {
				
				Runnable startServer = () -> {
					try {
						getMainController().getServer().startServer(Integer.parseInt(this.getPortField().getText()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
				Runnable startClient = () -> {
					try {
						getMainController().getClient().startClient("127.0.0.1", Integer.parseInt(this.getPortField().getText()), this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
				Thread serverThread = new Thread(startServer);
				serverThread.setDaemon(true);
				serverThread.start();
				
				Thread clientThread = new Thread(startClient);
				clientThread.setDaemon(true);
				clientThread.start();
				
			} else {
				
				Runnable startClient = () -> {
					try {
						getMainController().getClient().startClient(this.getIPField().getText(), Integer.parseInt(this.getPortField().getText()), this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
				
				Thread clientThread = new Thread(startClient);
				clientThread.setDaemon(true);
				clientThread.start();
				
			}
			this.mainController.scrollToBottom();
			FileHandler.saveProperties(this);
			this.window.setScene(chatScreen);
			window.setUserData(username);
			window.setWidth(1075);
			window.setHeight(750);
			this.window.setMinWidth(1075);
			this.window.setMinHeight(750);
			window.setX((WindowController.getScreenWidth() / 2) - window.getWidth() / 2);
			window.setY((WindowController.getScreenHeight() / 2) - window.getHeight() / 2);
			window.setResizable(true);
			this.IPLabel.setText("Please enter a host IP address and port: ");
			
		}
	}
	
	private void initLoginButton() {
		
		this.loginButton = new Button();
		this.loginButton.setText("Log in");
		this.loginButton.addEventHandler(ActionEvent.ACTION, e -> {
			if (this.getHostBox().isSelected()) {
				try {
					login(true);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} else if (!this.getHostBox().isSelected() && (getIPField().getText().contains(" ") || getIPField().getText().equals("") || getIPField().getText().equals(null))) {
				Popups.startInfoDlg("Invalid IP", "Please enter a valid host IP address.");
			} else if (!this.getHostBox().isSelected() && !(getIPField().getText().contains(" ") || getIPField().getText().equals(""))) {
				try {
					login(false);
				} catch (Exception e1) {
					Popups.startInfoDlg("Invalid IP", "Please enter a valid host.");
					e1.printStackTrace();
				}
			}
		});
		
	}
	
	public void initLoginScreen() {
		
		initUserField();
		initLoginButton();
		initUsernameLabel();
		initHostInfo();
		this.layout.getChildren().addAll(this.loginButton, this.hostLayout);
		initIPField();
		FileHandler.initUserPrefs();
		window.setWidth(250);
		window.setHeight(400);
		layout.setAlignment(Pos.CENTER);
		window.setOnCloseRequest(e -> {
			e.consume();
			if (Popups.startConfDlg("Are you sure you want to exit?")) {
				try {
					this.mainController.getClient().getClientSendingData().writeUTF("*![System] " + SystemInfo.getDate() + ": " + this.mainController.getClient().getClientName() + " has disconnected.");
					FileHandler.writeToChatLog("[System] " + SystemInfo.getDate() + ": " + this.mainController.getClient().getClientName() + " has disconnected.");
				} catch (Exception e1) {
					//e1.printStackTrace();
					System.err.println("You probably tried closing the window without logging in. That throws errors.");
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

	public TextField getIPField() {
		return IPField;
	}

	public void setIPField(TextField iPField) {
		IPField = iPField;
	}

	public TextField getPortField() {
		return portField;
	}

	public void setPortField(TextField portField) {
		this.portField = portField;
	}

	public CheckBox getHostBox() {
		return hostBox;
	}

	public void setHostBox(CheckBox hostBox) {
		this.hostBox = hostBox;
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
	
	public LoginScreenController getThis() {
		return this;
	}
	
}