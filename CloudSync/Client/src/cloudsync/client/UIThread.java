package cloudsync.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class UIThread extends Application implements Runnable{

	public void run() {
		// TODO Auto-generated method stub
		launch(UIThread.class);
		
		
		Platform.setImplicitExit(true);
	}

	@Override
	public void start(Stage arg0) throws Exception {
		// TODO Auto-generated method stub
		
		FXMLLoader loader = new FXMLLoader();
		Pane mainPane;
		try {
			mainPane = (Pane) loader.load(getClass().getResourceAsStream("FXMLDocument.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));

			Scene scene = new Scene(mainPane);

			FXMLDocumentController mainController = loader.getController();
			mainController.setStage(arg0);

			arg0.setScene(scene);
			arg0.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	@Override
	public void stop()
	{
		System.out.println("Stop is calling here");
		System.out.println("Making the thread wait");
	}
}
