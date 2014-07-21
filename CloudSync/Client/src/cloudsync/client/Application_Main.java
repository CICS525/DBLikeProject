package cloudsync.client;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import cloudsync.sharedInterface.AzureConnection;
import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.SessionBlob;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Application_Main {

	public static Thread FXThread;
	public static Thread systemTrayThread;
	private static Stage newStage;
	
	public static ClientSettings settings = null;
	public static SessionMaster masterSession = null;
	public static FileSysMonitor fileMonitor = null;


	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		// launchSwingUI();
		// createSystemTrayThread();
		// launchUIThread();
		initializeApplication();
	}

	public static void createSystemTrayThread() {
		System.out.println("Creating the System Tray Thread");
		systemTrayThread = new Thread(new SystemTrayImplementor());
		systemTrayThread.start();
	}

	public static void clearSystemTray() {
		systemTrayThread.destroy();
	}

	public static void clearUI() {
		FXThread = null;
	}

	public static void launchUIThread() {
		FXThread = new Thread(new UIThread());
		FXThread.start();
	}

	public static void launchSwingUI() throws InterruptedException {
		FXThread = new Thread(new UISwingFrame());
		FXThread.start();
	}

	public static void initializeApplication() throws InterruptedException {
		//if (settings.loadSettings()) {
		//} else {
		//	launchSwingUI();
		//}
		//settings.saveSettings();

		settings = ClientSettings.getInstance();
		settings.loadSettings();

		masterSession = SessionMaster.getInstance();
		masterSession.setMasterServerLocation(settings.getRecentMaster());
		
		fileMonitor = new FileSysMonitor();
		fileMonitor.StartListen(settings.getRootDir(), new FileSysMonitorCallback(){
			@Override
			public void Callback(String filename, Action action) {
				System.out.println(filename + " " + action);
				//SessionMaster masterSession = SessionMaster.getInstance();
				//masterSession.uploadFile(filename);
				
				String absoluteFilename = FileSysPerformer.getInstance().getAbsoluteFilename(filename);
				SessionBlob sessionBlob = new SessionBlob();
				Metadata metadata = new Metadata();
				metadata.filename = filename;
				metadata.blobKey = filename;
				metadata.blobServer = new AzureConnection(DefaultSetting.eli_storageConnectionString);
				metadata.blobBackup = new AzureConnection(DefaultSetting.chris_storageConnectionString);
				boolean suc = false;
				if ( FileSysMonitorCallback.Action.MODIFY == action ) {
					suc = sessionBlob.uploadFile(absoluteFilename, metadata);
					System.out.println("Upload File:" + absoluteFilename + "->" + suc);
				} else if ( FileSysMonitorCallback.Action.DELETE == action) {
					suc = sessionBlob.deleteFile(metadata);
					System.out.println("Delete File:" + absoluteFilename + "->" + suc);
				}
			}
		});

		System.out.println("Connecint to Master Server: " + settings.getUsername() + "#" + settings.getPassword());
		if (masterSession.connect(settings.getUsername(), settings.getPassword())) {
			System.out.println("ApplicationMain: masterSession connect success! create SystemTray icon.");
			createSystemTrayThread();
		} else {
			launchSwingUI();
		}
	}

}
