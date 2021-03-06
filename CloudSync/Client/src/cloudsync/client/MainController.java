package cloudsync.client;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainController implements Initializable{

	public StackPane MainStackPane;

	@FXML
	public Menu Actions;

	@FXML
	public Menu Main;

	public static MenuItem browser;
	public static MenuItem history;
	public static MenuItem home;
	public static MenuItem newuser;
	public static MenuItem issue;
	public static MenuItem exit;


	Stage thisStage;

	Thread systemTrayThread;



	public void setStackPane(Node node) {
		System.out.println("Calling inside setStackPane");
		MainStackPane.getChildren().setAll(node);
	}

	public void ClearStackPane() {
		MainStackPane.getChildren().clear();
	}

	// Function to store the Stage
	public void setStage(Stage currentStage)
	{
		this.thisStage = currentStage;
		thisStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent t) {
				//Platform.exit();
			}
		});
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		//---------------------------- Main Menu Drop down --------------------------------

		home = new MenuItem("Home Page");
		home.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				Application_Navigator.loadVista(Application_Navigator.CREDENTIALS);

			}
		}); 

		
		newuser = new MenuItem("New User");
		newuser.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				System.out.println("Calling New User window");
				Application_Navigator.loadVista(Application_Navigator.NEWUSER);
			}
		}); 
		
		
		exit = new MenuItem("Exit Application");
		exit.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				System.out.println("quitting");
				System.exit(0);
			}
		}); 

		
		Main.getItems().add(home);
	
		Main.getItems().add(newuser);

		Main.getItems().add(exit);
		

		//---------------------------- Actions Menu Drop down --------------------------------

		history = new MenuItem("View History");
		history.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {       	
				// open the default web browser for the HTML page
				try {
					URL webURL = new URL("http://webdevelopmentdev.azurewebsites.net/");
					Desktop.getDesktop().browse(webURL.toURI());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}); 

		Actions.getItems().add(history);

		browser = new MenuItem("View Browser");
		browser.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				System.out.println("Inside the browser");

				Application_Navigator.loadVista(Application_Navigator.BROWSER);
			}
		}); 

		Actions.getItems().add(browser);
		browser.setDisable(true);
		
		issue = new MenuItem("Report Issue");
		issue.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				Application_Navigator.loadVista(Application_Navigator.ISSUE);
			}
		}); 

		Actions.getItems().add(issue);

		StartConnection();
	}


	public void StartConnection()
	{
		System.out.println("Starting the Application");
		LoggerClass.logger.info("Starting the Application");
		if(ClientMain.initClientMain())
		{
			Application_Navigator.SESSION_OK = true;
		}else
		{
			ClientMain.deinitClientMain();
			Application_Navigator.SESSION_OK = false;
		}
		
		System.out.println("Calling here");
		systemTrayThread = new Thread(new SystemTrayImplementor());
		systemTrayThread.start();
	}

}
