package userInteract;

import java.io.File;

import application.ChatClient;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Popups {
	
	private static boolean b = false;
	private static File file = null;
	
	public static void startInfoDlg(String title, String info) {
		
		Stage popup = new Stage(StageStyle.UTILITY);
		VBox layout = new VBox(5);
		layout.setAlignment(Pos.CENTER);
		Scene scene = new Scene(layout, popup.getWidth(), popup.getHeight());
		popup.setScene(scene);
		scene.setFill(Color.SILVER);
		Button exitButton = new Button("Dismiss");
		popup.addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			if (key.getCode() == KeyCode.ENTER || key.getCode() == KeyCode.ESCAPE) {
				popup.close();
			}
		});
		exitButton.setOnAction(e -> popup.close());
		popup.initModality(Modality.APPLICATION_MODAL);
		popup.setResizable(false);
		popup.setTitle(title);
		popup.setWidth(250);
		popup.setHeight(100);
		Label infoLabel = new Label();
		infoLabel.setText(info);
		layout.getChildren().addAll(exitButton, infoLabel);
		popup.setAlwaysOnTop(true);
		popup.showAndWait();
		
	}
	
	public static boolean startConfDlg(String question) {
		Stage popup = new Stage(StageStyle.UTILITY);
		VBox layout = new VBox(5);
		layout.setAlignment(Pos.CENTER);
		HBox buttonLayout = new HBox(5);
		Scene scene = new Scene(layout, popup.getWidth(), popup.getHeight());
		popup.setScene(scene);
		scene.setFill(Color.SILVER);
		popup.setOnCloseRequest(e -> b = false);
		Button yesButton = new Button("Yes");
		yesButton.setOnAction(e -> {
			b = true;
			popup.close();
		});
		Button noButton = new Button("No");
		noButton.setOnAction(e -> {
			b = false;
			popup.close();
		});
		yesButton.addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			if (key.getCode() == KeyCode.ENTER) {
				yesButton.fire();
			}
		});
		noButton.addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			if (key.getCode() == KeyCode.ENTER) {
				noButton.fire();
			}
		});
		popup.addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			if (key.getCode() == KeyCode.ESCAPE) {
				popup.close();
			}
		});
		popup.initModality(Modality.APPLICATION_MODAL);
		popup.setResizable(false);
		popup.setTitle("Confirmation Dialog");
		popup.setWidth(250);
		popup.setHeight(115);
		Label infoLabel = new Label();
		infoLabel.setText(question);
		buttonLayout.getChildren().addAll(yesButton, noButton);
		buttonLayout.setAlignment(Pos.CENTER);
		layout.getChildren().addAll(infoLabel, buttonLayout);
		popup.setAlwaysOnTop(true);
		popup.showAndWait();
		return b;
	}
	
	public static String startAnsDlg(String question) {
		Stage popup = new Stage(StageStyle.UNDECORATED);
		VBox layout = new VBox(5);
		layout.setAlignment(Pos.CENTER);
		Scene scene = new Scene(layout, popup.getWidth(), popup.getHeight());
		popup.setScene(scene);
		scene.setFill(Color.SILVER);
		popup.setOnCloseRequest(e -> b = false);
		PasswordField passField = new PasswordField();
		Button submitButton = new Button("Submit");
		submitButton.setOnAction(e -> {
			b = true;
			popup.close();
		});
		submitButton.addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			if (key.getCode() == KeyCode.ENTER) {
				submitButton.fire();
			}
		});
		popup.addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			if (key.getCode() == KeyCode.ESCAPE) {
				popup.close();
			}
		});
		popup.initModality(Modality.APPLICATION_MODAL);
		popup.setResizable(false);
		popup.setTitle("Confirmation Dialog");
		popup.setWidth(250);
		popup.setHeight(115);
		Label questionLabel = new Label();
		questionLabel.setText(question);
		layout.getChildren().addAll(questionLabel, passField, submitButton);
		popup.setAlwaysOnTop(true);
		popup.showAndWait();
		return passField.getText();
	}

	public static File startFileOpener(String title) {
		
		Stage popup = new Stage();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		return fileChooser.showOpenDialog(popup);
		
	}
	
	public static File startFileSaver(String title, String ext) {
		
		Stage popup = new Stage();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"), new FileChooser.ExtensionFilter(ext + " Files", "*." + ext));
		file = fileChooser.showSaveDialog(popup);
		
		return file;
		
	}
	
	public static File startFileSaver(String title, String ext, String name) {
		
		Stage popup = new Stage();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.setInitialFileName(name);
		FileChooser.ExtensionFilter prefFilter = new FileChooser.ExtensionFilter(ext + " Files", "*." + ext);
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"), prefFilter);
		fileChooser.setSelectedExtensionFilter(prefFilter);
		file = fileChooser.showSaveDialog(popup);
		
		return file;
		
	}
	
	public static File startFileSaver(String title) {
		
		Stage popup = new Stage();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		return fileChooser.showSaveDialog(popup);
		
	}
	
	public static File startPictureOpener(String title) {
		
		Stage popup = new Stage(StageStyle.UTILITY);
		popup.initModality(Modality.APPLICATION_MODAL);
		popup.setAlwaysOnTop(true);
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"), new FileChooser.ExtensionFilter("JPG", "*.jpg"), new FileChooser.ExtensionFilter("PNG Master Race", "*.png"), new FileChooser.ExtensionFilter("GIFs", "*.gif"), new FileChooser.ExtensionFilter("Bitmaps", "*.bmp"), new FileChooser.ExtensionFilter("TIFFS - Because nobody uses them!", "*.tiff"));
		
		return fileChooser.showOpenDialog(popup);
		
	}
	
	public static void startImageViewer(String title, Image img) {
		
		Stage popup = new Stage(StageStyle.UNIFIED);
		VBox layout = new VBox(10);
		layout.setAlignment(Pos.CENTER);
		layout.setStyle("-fx-background-color: black");
		popup.setResizable(true);
		popup.setScene(new Scene(layout));
		popup.getScene().getStylesheets().add(ChatClient.class.getResource("cyprus.css").toExternalForm());
		ImageView imgview = new ImageView(img);
		layout.getChildren().add(imgview);
		popup.setWidth(img.getWidth() + 15);
		popup.setHeight(img.getHeight() + 50);
		popup.show();
		
	}
	
}