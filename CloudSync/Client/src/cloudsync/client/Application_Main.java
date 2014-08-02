package cloudsync.client;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Application_Main {

	public static Thread FXThread;
	public static Thread systemTrayThread;
	public static Thread browserThread;


	public static void main(String[] args) throws InterruptedException {
		if(LoggerClass.initializeLogger())
		{
			System.out.println("Logger Started");
		}
		LoggerClass.writeLog("Starting UI");
		launchUIThread();
	}

	public static void launchUIThread() {
		LoggerClass.writeLog("Launching UI");

		UIThread.launchUI();
	}	
}