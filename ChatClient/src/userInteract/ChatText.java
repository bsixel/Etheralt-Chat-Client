package userInteract;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class ChatText extends Text {
	
	String msg;
	
	public ChatText(String msg, String textColor, String bgColor) {
		
		this.setText(msg);
		this.minWidth(475);
		this.prefWidth(475);
		this.maxWidth(475);
		this.setWrappingWidth(475);
		this.setFill(Color.valueOf(textColor));
		this.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			if (e.getButton() == MouseButton.SECONDARY) {
				System.out.println("Copied " + "'" + getMsg() + "' to clipboard.");
				Toolkit.getDefaultToolkit ().getSystemClipboard().setContents(new StringSelection(getMsg()), null);
			}
		});
		
	}
	
	public String getMsg() {
		return super.getText();
	}
	
}