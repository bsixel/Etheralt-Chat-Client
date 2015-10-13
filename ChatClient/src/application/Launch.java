package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import tools.FileHandler;


public class Launch extends Application {
	
	
	@Override
	public void start(Stage primaryStage) {
		
		try {
			FileHandler.initUserPrefs();
			new ChatClient().launch(new Stage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}