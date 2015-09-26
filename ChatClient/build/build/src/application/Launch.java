package application;
	
import javafx.application.Application;
import javafx.stage.Stage;


public class Launch extends Application {
	
	
	@Override
	public void start(Stage primaryStage) {
		
		try {
			new ChatClient().start(new Stage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}