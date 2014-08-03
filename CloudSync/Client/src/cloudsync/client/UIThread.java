package cloudsync.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class UIThread extends Application{
	public static Stage newStage;
	public static boolean added;

	public static void launchUI()
	{
		launch();
	}

	@Override
	public void start(Stage arg0) throws Exception {
		LoggerClass.writeLog("Opening UI Stage");
		Platform.setImplicitExit(false);
		newStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        Pane mainPane = (Pane) loader.load(getClass().getResourceAsStream(Application_Navigator.MAIN));
        newStage.setScene(createScene(mainPane));
        MainController mainController = loader.getController();
        mainController.setStage(newStage);
        Application_Navigator.setMainController(mainController);
        Application_Navigator.loadVista(Application_Navigator.CREDENTIALS);
        newStage.getIcons().add(new Image("/images/logo.png"));
        newStage.setHeight(430);
        newStage.setWidth(715);
        newStage.show();
        newStage.setResizable(false);
        newStage.show();
		LoggerClass.writeLog("Stage Successfully Opened");
	}
	
	@Override
	public void stop()
	{
		newStage.hide();
		System.out.println("Hiding the UI");
		LoggerClass.writeLog("Hiding the UI");

	}
	
	public static void openStage()
	{
		System.out.println("Showing the FX Application");
		LoggerClass.writeLog("Showing the FX Application");

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				newStage.show();
			}});
	}
	
	public static void closeStage()
	{
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				newStage.hide();
			}});
		LoggerClass.writeLog("Hiding the UI");
	}
	
	
	private Scene createScene(Pane mainPane) {
        Scene scene = new Scene(mainPane);
        return scene;
    }
}