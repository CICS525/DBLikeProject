package cloudsync.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class BrowserThread extends Application implements Runnable{
	public static boolean added; 
	public static Stage browserStage;
	
	public void run() {
		launch(UIThread.class);
	}
	
	
	@Override
	public void start(Stage arg0) throws Exception {
		Platform.setImplicitExit(false);
		browserStage = new Stage();
		FXMLLoader loader = new FXMLLoader();
		Pane mainPane;
		try {
			mainPane = (Pane) loader.load(getClass().getResourceAsStream("FileBrowser.fxml"));
			Scene scene = new Scene(mainPane);
			FileBrowserController mainController = loader.getController();
			mainController.setStage(browserStage);
			browserStage.setScene(scene);
			added=true;
			browserStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	@Override
	public void stop()
	{
		browserStage.hide();
		System.out.println("Hiding the UI");
	}
	
	public static void openStage()
	{
		System.out.println("Showing the FX Application");
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				browserStage.show();
			}});
	}
}
