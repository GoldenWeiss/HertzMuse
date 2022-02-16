package vue;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class VueAlert
{
	public static void sendAlert(String title, String message, AlertType type)
	{
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setContentText(message);
		
		alert.show();
	}
}
