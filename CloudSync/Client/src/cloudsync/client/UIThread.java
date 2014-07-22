package cloudsync.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class UIThread extends Application implements Runnable{
	public static Stage newStage;
	public static boolean added;

	public void run() {
		launch(UIThread.class);
	}
	

	@Override
	public void start(Stage arg0) throws Exception {
		Platform.setImplicitExit(false);
		newStage = new Stage();
		FXMLLoader loader = new FXMLLoader();
		Pane mainPane;
		try {
			mainPane = (Pane) loader.load(getClass().getResourceAsStream("FXMLDocument.fxml"));
			Scene scene = new Scene(mainPane);
			FXMLDocumentController mainController = loader.getController();
			mainController.setStage(newStage);
			newStage.setScene(scene);
			added = true;
			newStage.getIcons().add(new Image("/images/logo.png"));
			newStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	@Override
	public void stop()
	{
		newStage.hide();
		System.out.println("Hiding the UI");
	}
	
	public static void openStage()
	{
		System.out.println("Showing the FX Application");
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				newStage.show();
			}});
	}
	
	public static void closeStage()
	{
		System.out.println("Hiding the UI");
		newStage.hide();
	}
}