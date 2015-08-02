package userInteract;

import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class ChatBox extends VBox {
	
	private SimpleBooleanProperty bottom = new SimpleBooleanProperty(false);
	
	public ChatBox() {
		
		this.setMaxWidth(500);
		this.setMinWidth(500);
		this.setPrefWidth(500);
		this.getStyleClass().add("chat-box");
		
	}
	
	public void addText(String msg, String color, String bgColor) {
		
		this.getChildren().add(new ChatText(msg, color, bgColor));
		
	}
	
	public List<Node> getMessages() {
		return this.getChildren().stream()
				.filter(e -> e.getClass().equals(ChatText.class)).collect(Collectors.toList());
	}

	public void addText(ChatText chatText) {
		this.getChildren().add(chatText);
	}
	
	public Boolean getIsAtBottom() {
		return this.bottom.getValue();
	}
	
	public void setIsAtBottom(boolean b) {
		this.bottom.set(b);
	}
	
	public SimpleBooleanProperty getBottomProperty() {
		return this.bottom;
	}
	
}