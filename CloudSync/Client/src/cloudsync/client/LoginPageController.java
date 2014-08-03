/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cloudsync.client;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
* FXML Controller class
*
* @author Vyas
*/
public class LoginPageController implements Initializable {

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
    @FXML
    public Button OpenSession;
    @FXML 
    public Button StopSession;
    @FXML
    public Button NewUser;
    @FXML
    public Button RunInBackGround;
	
    /**
     * Initializes the controller class.
     */
    public void initialize(URL url, ResourceBundle rb) {
    	DeviceNameTF.setDisable(true);
    	System.out.println("Calling Initialize in the FXMLDocument Controller");
    	initializeComponents();
        InitializeUI();
	}    

    @SuppressWarnings("deprecation")
	@FXML
    private void OpenSessionButtonAction(ActionEvent event) throws InterruptedException {
    	ClientMain.getSettings().setUsername(UsernameTF.getText());
    	ClientMain.getSettings().setPassword(PasswordTF.getText());
    	ClientMain.getSettings().setRootDir(DirectoryTF.getText());
    	ClientMain.getSettings().setDeviceName(DeviceNameTF.getText());
    	ClientMain.getSettings().saveSettings();
    	
    	OpenSession.setDisable(true);
    	StopSession.setDisable(false);
    	RunInBackGround.setDisable(false);
    }

    @FXML // Directory Button Code
    private void ChooseDirectoryButtonAction(ActionEvent event) {
        DirectoryChooser directory = new DirectoryChooser();
        directory.setTitle("JavaFX Projects");
        File defaultDirectory = new File("c:/");
        directory.setInitialDirectory(defaultDirectory);
    }
    
    
    
    
    @FXML
    public void NewUserButtonAction(ActionEvent event)
    {
    	Application_Navigator.SESSION_OK = false;
    	UsernameTF.setDisable(false);
    	PasswordTF.setDisable(false);
    	DirectoryTF.setDisable(false);
    	OpenSession.setDisable(false);
    	
    }
     
    public void InitializeUI()
    {
    	if(Application_Navigator.SESSION_OK)
    	{
    	OpenSession.setDisable(true);
    	UsernameTF.setDisable(true);
    	PasswordTF.setDisable(true);
    	DirectoryTF.setDisable(true);
    	
    	}else
    	{
    		OpenSession.setDisable(false);
    	}
    	UsernameTF.setText(ClientMain.getSettings().getUsername());
    	PasswordTF.setText(ClientMain.getSettings().getPassword());
    	DirectoryTF.setText(ClientMain.getSettings().getRootDir());

    	
    }
    
    public void initializeComponents()
    {
    	System.out.println("Initialize components");
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
            
        }
        
        
       
        
    }

    
   

    
    
    
    
    
}