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
import javafx.scene.control.PasswordField;
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
    private PasswordField PasswordTF;
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
    
    
    boolean newUser;
	
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
    	
		System.out.println("Calling the Login function");

    	if(!UsernameTF.getText().equals(ClientMain.getSettings().getUsername()))
    	{
    		System.out.println("Deleting the Old setting files to Configure for new user");
    		ClientMain.getSettings().deleteSettingFile();
    		MetadataManager.getInstance().deleteMetaData();
    	}
    	
    	Application_Navigator.SESSION_OK = false;
    	ClientMain.getSettings().setUsername(UsernameTF.getText());
    	ClientMain.getSettings().setPassword(PasswordTF.getText());
    	ClientMain.getSettings().setRootDir(DirectoryTF.getText());
    	ClientMain.getSettings().setDeviceName(DeviceNameTF.getText());
		ClientMain.getSettings().saveSettings();

    	
    	/*if(newUser)
    	{
    		if(SessionEntry.getInstance().createAccount(UsernameTF.getText(), PasswordTF.getText()))
    		{
    			System.out.println("User successfully created");
    		}
    	}*/
    	
    	if(ClientMain.initClientMain())
    	{
    		Application_Navigator.SESSION_OK = true;
    	}
    	
    	InitializeUI();
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
    	ClientMain.deinitClientMain();
    	SystemTrayImplementor.setToolTip("Dropbox Application : Not Connected");
    	Application_Navigator.SESSION_OK = false;
    	UsernameTF.setDisable(false);
    	PasswordTF.setDisable(false);
    	DirectoryTF.setDisable(false);
    	OpenSession.setDisable(false);
    	ChooseDirectoryButton.setDisable(false);
    	NewUser.setDisable(true);	//disable Logout button
    	
    	/*newUser = true;
    	Application_Navigator.SESSION_OK = false;
    	ClientMain.deinitClientMain();
    	UsernameTF.setDisable(false);
    	PasswordTF.setDisable(false);
    	DirectoryTF.setDisable(false);
    	OpenSession.setDisable(false);
    	ChooseDirectoryButton.setDisable(false);
    	OpenSession.setText("Create");*/
    	
    }
     
    public void InitializeUI()
    {
    	if(Application_Navigator.SESSION_OK)
    	{
	    	OpenSession.setDisable(true);
	    	UsernameTF.setDisable(true);
	    	PasswordTF.setDisable(true);
	    	DirectoryTF.setDisable(true);
	    	ChooseDirectoryButton.setDisable(true);
	    	NewUser.setDisable(false);	//enable Logout button
    	}else{
        	UsernameTF.setDisable(false);
        	PasswordTF.setDisable(false);
        	DirectoryTF.setDisable(false);
    		OpenSession.setDisable(false);
        	ChooseDirectoryButton.setDisable(false);
        	NewUser.setDisable(true);	//still disable Logout button
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
