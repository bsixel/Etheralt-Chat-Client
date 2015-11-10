package userInteract;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

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