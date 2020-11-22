package whork;

import javafx.application.Application;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WhorkDesktopApp extends Application {
	public static void launchApp(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		HBox hbox = new HBox(new Label("Whork desktop application"));
		primaryStage.setScene(new Scene(hbox));
		primaryStage.setTitle("Whork");
		primaryStage.show();
	}
}	

