package ass3.app;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Invalidkeyword implements Runnable{

	@Override
	public void run() {		
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText("Your Input is invalid !!!");
		alert.setContentText("Ooops, please input another keyword >_<");
		alert.showAndWait();		
	}}
