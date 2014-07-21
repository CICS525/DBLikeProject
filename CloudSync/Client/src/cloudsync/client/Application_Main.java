package cloudsync.client;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Application_Main {

	public static Thread FXThread;
	public static Thread systemTrayThread;
	public static ClientSettings settings = null;
	public static SessionMaster masterSession = null;

	static Stage newStage;

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
		settings = ClientSettings.getInstance();

		if (settings.loadSettings()) {
			settings.saveSettings();

			masterSession = SessionMaster.getInstance();
			masterSession.setMasterServerLocation(settings.getRecentMaster());

			System.out.println("Helloooo");
			if (masterSession.connect(settings.getUsername(), settings.getPassword())) {
				System.out.println("Hellooosdadasdasdadasdasdasdso");

				createSystemTrayThread();
			} else {
				launchSwingUI();

			}
		} else {
			launchSwingUI();
		}
	}

}
