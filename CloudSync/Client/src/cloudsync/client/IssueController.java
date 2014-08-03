package cloudsync.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class IssueController implements Initializable{

	public TextArea description;
	public Button submitbutton;
	public TextField email;
	public Label emaillabel;
	public Label desclabel;
	
	public Label successmsg;
	
	public Button backtohomepage;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
		submitbutton.setVisible(true);
		email.setVisible(true);
		description.setVisible(true);
		desclabel.setVisible(true);
		emaillabel.setVisible(true);
		successmsg.setVisible(false);
		backtohomepage.setVisible(false);
			
	}
	
	public void submitIssueAction(ActionEvent event)
	{
		if(EmailAttachmentSender.sendMail(email.getText(), description.getText()))
		{
			LoggerClass.writeLog("Email Sent Successfully");
			System.out.println("Email Sent Successfully");
			submitbutton.setVisible(false);
			desclabel.setVisible(false);
			emaillabel.setVisible(false);
			email.setVisible(false);
			description.setVisible(false);
			successmsg.setVisible(true);
			backtohomepage.setVisible(true);
			
		}
	}
	
	
	public void backtohomepageaction(ActionEvent event)
	{
		Application_Navigator.loadVista(Application_Navigator.CREDENTIALS);
	}
	

}
