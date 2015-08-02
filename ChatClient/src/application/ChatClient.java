package application;

import java.io.File;
import java.io.FileInputStream;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tools.FileHandler;
import userInteract.LoginScreenController;
import userInteract.MainScreenController;

public class ChatClient extends Application {
	
	public LoginScreenController loginScreenController;
	public MainScreenController mainScreenController;
	public WindowController windowController;
	public Stage window;
	public Scene loginScene;
	public Scene mainScene;
	public VBox mainScreenLayout = new VBox(15);
	public GridPane secondScreenLayout = new GridPane();

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			
			setUserAgentStylesheet(STYLESHEET_CASPIAN);
			this.window = primaryStage;
			this.loginScene = new Scene(mainScreenLayout, window.getWidth(), window.getHeight());
			this.secondScreenLayout.setId("loginbackground");
			this.mainScene = new Scene(secondScreenLayout, window.getWidth(), window.getHeight());
			this.windowController = new WindowController(window);
			this.windowController.initWindow();
			this.mainScreenController = new MainScreenController(secondScreenLayout, window, mainScene, loginScene, windowController);
			this.loginScreenController = new LoginScreenController(mainScreenController, mainScreenLayout, window, mainScene, windowController);
			this.loginScreenController.initLoginScreen();
			this.mainScreenController.initMainScreen();
			this.loginScene.getStylesheets().add(getClass().getResource("cyprus.css").toExternalForm());
			this.mainScene.getStylesheets().add(getClass().getResource("cyprus.css").toExternalForm());
			primaryStage.setScene(loginScene);
			primaryStage.show();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}