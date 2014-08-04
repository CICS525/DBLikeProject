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

import cloudsync.client.DialogFX.Type;
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
public class NewUserController implements Initializable {

	@FXML
    private TextField UserTF;
    @FXML
    private PasswordField PassTF;
    @FXML
    private PasswordField ConfirmTF;
    
    
	
    /**
     * Initializes the controller class.
     */
    public void initialize(URL url, ResourceBundle rb) {
    	
	}    
    
    public void SubmitButtonAction(ActionEvent event)
    {
    	if(SessionEntry.getInstance().createAccount(UserTF.getText(), PassTF.getText()))
    	{
	    	DialogFX dialog = new DialogFX(Type.INFO);
	        dialog.setTitleText("User Created");
	        dialog.setMessage("New User Successfully Created");
	        dialog.showDialog();
    	}else
    	{
    		DialogFX dialog = new DialogFX(Type.ERROR);
            dialog.setTitleText("User Not Created");
            dialog.setMessage("New User Creation Failed");
            dialog.showDialog();
    	}
    }
    
    public boolean checkMandatory()
    {
    	return true;
    }

    
}
