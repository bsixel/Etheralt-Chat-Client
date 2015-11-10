package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tools.FileHandler;
import userInteract.LoginScreenController;
import userInteract.MainScreenController;

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

public class ChatClient extends Application {

	public LoginScreenController loginScreenController;
	public MainScreenController mainScreenController;
	public Stage window;
	public Scene loginScene;
	public Scene mainScene;
	public VBox mainScreenLayout = new VBox(15);
	public GridPane secondScreenLayout = new GridPane();

	/**
	 * Inherited method as an Application. Starts the application process, initiates scene configuration and chat things.
	 * @param primaryStage A new stage given by Main.
	 * @throws Exception Throws an exception if something goes wrong initiating graphical properties.
	 */
	public void launch(Stage primaryStage) throws Exception {
		try {
			setUserAgentStylesheet(STYLESHEET_CASPIAN);
			this.window = primaryStage;
			this.window.setTitle("Etheralt CC");
			this.loginScene = new Scene(mainScreenLayout, window.getWidth(), window.getHeight());
			this.secondScreenLayout.setId("loginbackground");
			this.mainScene = new Scene(secondScreenLayout, window.getWidth(), window.getHeight());
			this.mainScreenController = new MainScreenController(secondScreenLayout, window, mainScene, loginScene);
			this.loginScreenController = new LoginScreenController(mainScreenController, mainScreenLayout, window, mainScene);
			this.loginScreenController.initLoginScreen();
			this.mainScreenController.initMainScreen();
			this.loginScene.getStylesheets().add(getClass().getResource("cyprus.css").toExternalForm());
			this.mainScene.getStylesheets().add(getClass().getResource("cyprus.css").toExternalForm());
			primaryStage.setScene(loginScene);
			primaryStage.show();
	} catch(Exception e) {
		FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

	}

}