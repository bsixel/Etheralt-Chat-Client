package application;

import javafx.stage.Screen;
import javafx.stage.Stage;

public class WindowController {
	
	//Numbers
	
	//Strings
	private String username = "";
	
	//Window
	private Stage window;
	
	public WindowController(Stage window) {
		
		this.window = window;
		
	}
	
	public void initWindow() {
		
		this.window.setTitle("Etheralt CC");
		
	}
	
	public static double getScreenWidth() {
		return Screen.getPrimary().getVisualBounds().getWidth();
	}
	
	public static double getScreenHeight() {
		return Screen.getPrimary().getVisualBounds().getHeight();
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
}