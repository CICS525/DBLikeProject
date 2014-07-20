/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cloudsync.client;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author Vyas
 */
public class FXMLDocumentController implements Initializable {
    @FXML
    private TextField UsernameTF;
    @FXML
    private TextField PasswordTF;
    @FXML
    private TextField DirectoryTF;
    @FXML
    private TextField DeviceNameTF;
    @FXML
    private Button ChooseDirectoryButton;
    @FXML
    public CheckBox remembercheck;
    
    

	
	public String userName;
	public String passWord;
	public String RootDir;
	public String device;

    Stage thisStage;



    /**
     * Initializes the controller class.
     */
    public void initialize(URL url, ResourceBundle rb) {
        
        // Initializing the Choose Directory Button Event Handler
        ChooseDirectoryButton.setOnAction(new EventHandler<ActionEvent>() {
             public void handle(ActionEvent event) {
                  DirectoryChooser directoryChooser = new DirectoryChooser();
                  directoryChooser.setTitle("This is my file ch");
                  //Show open file dialog
                  File file = directoryChooser.showDialog(null);
                 if(file!=null){
                      DirectoryTF.setText(file.getPath());
                 }
              }
        });
        
        // Code to get the Host Name
        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            String hostname = addr.getHostName();
            DeviceNameTF.setText(hostname);
        } catch (UnknownHostException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
	}    

    @FXML
    private void OpenSessionButtonAction(ActionEvent event) throws InterruptedException {
             System.out.println(Application_Main.systemTrayThread.getName());
             SystemTrayImplementor.tray.remove(SystemTrayImplementor.trayIcon);
             SystemTrayImplementor.added = false;
             Application_Main.systemTrayThread.stop();
    }

    @FXML // Directory Button Code
    private void ChooseDirectoryButtonAction(ActionEvent event) {
        DirectoryChooser directory = new DirectoryChooser();
        directory.setTitle("JavaFX Projects");
        File defaultDirectory = new File("c:/");
        directory.setInitialDirectory(defaultDirectory);
    }
    
    
    // Function to store the Stage
    public void setStage(Stage currentStage)
    {
        this.thisStage = currentStage;
        thisStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
     	   public void handle(WindowEvent t) {
     	      System.out.println("It is here");
     	      //Platform.exit();
     	   }
     	});
    }
    
    // Validate the Username and Password and create the master Connection,Socket
    public boolean validateWithMaster()
    {
    	/*masterSession = SessionMaster.getInstance();
		masterSession.setMasterServerLocation(settings.getRecentMaster());
		masterSession.connect(settings.getUsername(), settings.getPassword());
    	return masterSession.connect(settings.getUsername(), settings.getPassword());*/
    	return true;
    }
    
    @FXML
    public void runBackGroundButtonAction(ActionEvent event)
    {
        Application_Main.createSystemTrayThread();
    }
    
   
    
}
