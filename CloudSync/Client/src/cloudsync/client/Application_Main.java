package cloudsync.client;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Application_Main {

	public static Thread FXThread;
	public static Thread systemTrayThread;
	public static Thread browserThread;
	
	public static ClientSettings settings = null;
	public static SessionMaster masterSession = null;
	public static FileSysMonitor fileMonitor = null;


	public static void main(String[] args) throws InterruptedException {
		UIThread.added = false;
		SystemTrayImplementor.added = false;
		BrowserThread.added = false;
		initializeApplication();
	}


	public static void createSystemTrayThread() {
		System.out.println("Creating the System Tray Thread");
		if(!SystemTrayImplementor.added)
		{
		systemTrayThread = new Thread(new SystemTrayImplementor());
		systemTrayThread.start();
		}
	}

	public static void clearSystemTray() {
		systemTrayThread.destroy();
	}

	public static void clearUI() {
		FXThread = null;
	}

	public static void launchUIThread() {
		if(!UIThread.added)
		{
		FXThread = new Thread(new UIThread());
		FXThread.start();
		}
	}
	

	public static void launchBrowserThread() {
		// TODO Auto-generated method stub
		if(!BrowserThread.added)
		{Platform.runLater(new Runnable() {
			@Override
			public void run() {
			try {
				System.out.println("calling browser");
				new BrowserThread().start(new Stage());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}});
			
		}
	}

	public static void initializeApplication() throws InterruptedException {
		
		createSystemTrayThread();
	}
}