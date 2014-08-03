package cloudsync.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class IssueController implements Initializable{

	public TextArea description;
	
	public TextField email;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
	}
	
	public void submitIssueAction(ActionEvent event)
	{
		if(EmailAttachmentSender.sendMail(email.getText(), description.getText()))
		{
			LoggerClass.writeLog("Email Sent Successfully");
			System.out.println("Email Sent Successfully");
			Application_Navigator.loadVista(Application_Navigator.CREDENTIALS);
		}
	}
	

}
