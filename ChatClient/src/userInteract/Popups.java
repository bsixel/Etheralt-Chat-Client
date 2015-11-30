package userInteract;

import java.io.File;

import application.ChatClient;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

public class Popups {
	
	/**
	 * Starts a popup which simply presents a message to the user.
	 * @param title The title of the popup.
	 * @param info The message being shown by the popup.
	 */
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
		popup.setWidth(300);
		popup.setHeight(100);
		Label infoLabel = new Label();
		infoLabel.setText(info);
		layout.getChildren().addAll(exitButton, infoLabel);
		popup.setAlwaysOnTop(true);
		popup.showAndWait();
		
	}
	
	/**
	 * Starts a dialog prompting for a yes or no answer from the user, returned after one of the corresponding buttons is pressed as a boolean.
	 * @param question The question being asked.
	 * @return A boolean: true for yes, false for no.
	 */
	public static boolean startConfDlg(String question) {
		BooleanProperty b = new SimpleBooleanProperty(false);
		Stage popup = new Stage(StageStyle.UTILITY);
		VBox layout = new VBox(5);
		layout.setAlignment(Pos.CENTER);
		HBox buttonLayout = new HBox(5);
		Scene scene = new Scene(layout, popup.getWidth(), popup.getHeight());
		popup.setScene(scene);
		scene.setFill(Color.SILVER);
		popup.setOnCloseRequest(e -> b.set(false));
		Button yesButton = new Button("Yes");
		yesButton.setOnAction(e -> {
			b.set(true);
			popup.close();
		});
		Button noButton = new Button("No");
		noButton.setOnAction(e -> {
			b.set(false);
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
		popup.setWidth(300);
		popup.setHeight(115);
		Label infoLabel = new Label();
		infoLabel.setText(question);
		buttonLayout.getChildren().addAll(yesButton, noButton);
		buttonLayout.setAlignment(Pos.CENTER);
		layout.getChildren().addAll(infoLabel, buttonLayout);
		popup.setAlwaysOnTop(true);
		popup.showAndWait();
		return b.get();
	}
	
	/**
	 * Starts a popup prompting the user for a password. Hides the password in a system-determined format.
	 * @param question The question being asked. i.e. "Please enter password:" or some such.
	 * @return The string password the user entered.
	 */
	public static String startPasswdDlg(String question) {
		BooleanProperty b = new SimpleBooleanProperty(false);
		Stage popup = new Stage(StageStyle.UNDECORATED);
		VBox layout = new VBox(5);
		layout.setAlignment(Pos.CENTER);
		Scene scene = new Scene(layout, popup.getWidth(), popup.getHeight());
		popup.setScene(scene);
		scene.setFill(Color.SILVER);
		popup.setOnCloseRequest(e -> b.set(false));
		PasswordField passField = new PasswordField();
		Button submitButton = new Button("Submit");
		submitButton.setOnAction(e -> {
			b.set(true);
			popup.close();
		});
		passField.setOnKeyPressed(e -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				submitButton.fire();
				popup.close();
			}
		});
		popup.addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			if (key.getCode().equals(KeyCode.ENTER)) {
				submitButton.fire();
			}
		});
		popup.addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			if (key.getCode().equals(KeyCode.ESCAPE)) {
				popup.close();
			}
		});
		popup.initModality(Modality.APPLICATION_MODAL);
		popup.setResizable(false);
		popup.setTitle("Confirmation Dialog");
		popup.setWidth(300);
		popup.setHeight(115);
		Label questionLabel = new Label();
		questionLabel.setText(question);
		layout.getChildren().addAll(questionLabel, passField, submitButton);
		popup.setAlwaysOnTop(true);
		popup.showAndWait();
		return passField.getText();
	}
	
	/**
	 * Starts a popup prompting the user to enter an answer in the provided text field.
	 * @param question The question being asked.
	 * @return A string representation of the answer given by the user.
	 */
	public static String startAnsDlg(String question) {
		BooleanProperty b = new SimpleBooleanProperty(false);
		Stage popup = new Stage(StageStyle.UNDECORATED);
		VBox layout = new VBox(5);
		layout.setAlignment(Pos.CENTER);
		Scene scene = new Scene(layout, popup.getWidth(), popup.getHeight());
		popup.setScene(scene);
		scene.setFill(Color.SILVER);
		popup.setOnCloseRequest(e -> b.set(false));
		TextField ansField = new TextField();
		Button submitButton = new Button("Submit");
		submitButton.setOnAction(e -> {
			b.set(true);
			popup.close();
		});
		ansField.setOnKeyPressed(e -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				submitButton.fire();
				popup.close();
			}
		});
		popup.addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			if (key.getCode().equals(KeyCode.ENTER)) {
				submitButton.fire();
			}
		});
		popup.addEventHandler(KeyEvent.KEY_PRESSED, key -> {
			if (key.getCode().equals(KeyCode.ESCAPE)) {
				popup.close();
			}
		});
		popup.initModality(Modality.APPLICATION_MODAL);
		popup.setResizable(false);
		popup.setTitle("Confirmation Dialog");
		popup.setWidth(300);
		popup.setHeight(115);
		Label questionLabel = new Label();
		questionLabel.setText(question);
		layout.getChildren().addAll(questionLabel, ansField, submitButton);
		popup.setAlwaysOnTop(true);
		popup.showAndWait();
		return ansField.getText();
	}

	/**
	 * Starts a new popup prompting the user to select a file to open.
	 * @param title The desired title of the popup.
	 * @return A File object representing the file the user chose.
	 */
	public static File startFileOpener(String title) {
		
		Stage popup = new Stage();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		return fileChooser.showOpenDialog(popup);
		
	}
	
	/**
	 * Starts a popup prompting the user to select a destination for saving a file. Starts with suggested file extension.
	 * @param title The title of the popup.
	 * @param ext The extension initially suggested.
	 * @return The File object representing where the user will be saving the file.
	 */
	public static File startFileSaver(String title, String ext) {
		
		Stage popup = new Stage();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"), new FileChooser.ExtensionFilter(ext + " Files", "*." + ext));
		return fileChooser.showSaveDialog(popup);
		
	}
	
	/**
	 * Starts a popup prompting the user to select a destination for saving a file. Starts with suggested file extension and a suggested name.
	 * @param title The title of the popup.
	 * @param ext The extension initially suggested.
	 * @param name The initially suggested name for the file.
	 * @return The File object representing where the user will be saving the file.
	 */
	public static File startFileSaver(String title, String ext, String name) {
		
		Stage popup = new Stage();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.setInitialFileName(name);
		FileChooser.ExtensionFilter prefFilter = new FileChooser.ExtensionFilter(ext + " Files", "*." + ext);
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"), prefFilter);
		fileChooser.setSelectedExtensionFilter(prefFilter);
		return fileChooser.showSaveDialog(popup);
		
	}
	
	/**
	 * Starts a popup prompting the user to select a destination for saving a file.
	 * @param title The title of the popup.
	 * @return The File object representing where the user will be saving the file.
	 */
	public static File startFileSaver(String title) {
		
		Stage popup = new Stage();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		return fileChooser.showSaveDialog(popup);
		
	}
	
	/**
	 * Starts a popup prompting the user to select and image to open.
	 * @param title The title for the popup.
	 * @return The File object representing the location of the image.
	 */
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
	
	/**
	 * Starts a popup to view the given image.
	 * @param title The title of the popup.
	 * @param img The image to display.
	 */
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