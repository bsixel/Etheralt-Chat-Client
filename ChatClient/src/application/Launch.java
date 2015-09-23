package application;
	
import java.util.List;

import javafx.application.Application;
import javafx.stage.Stage;


public class Launch extends Application {
	
	
	@Override
	public void start(Stage primaryStage) {
		
		try {
			Parameters params = getParameters();
			List<String> args = params.getRaw();
			
			new ChatClient().launch(new Stage(), args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}