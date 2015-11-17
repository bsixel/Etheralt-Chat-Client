package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import tools.FileHandler;

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

public class Launch extends Application {
	
	@Override
	/**
	 * Inherited method as an Application. Starts the application process, initiates scene 
	 * configuration and chat things Has some strange redundancy with ChatClient's launch/start method..
	 * @param primaryStage A new stage given by Main.
	 * @throws Exception Throws an exception if something goes wrong initiating graphical properties.
	 */
	public void start(Stage primaryStage) {
		
		try {
			FileHandler.initUserPrefs();
			new ChatClient().launch(new Stage());
		} catch (Exception e) {
			FileHandler.debugPrint(e.getMessage() + e.getStackTrace()[0].toString());
		}
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}