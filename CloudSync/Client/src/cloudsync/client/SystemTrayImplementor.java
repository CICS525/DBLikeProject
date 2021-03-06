package cloudsync.client;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;

public class SystemTrayImplementor implements Runnable {

	public static SystemTray tray;
	public static TrayIcon trayIcon;

	public void run() {

			// TODO Auto-generated method stub
			System.out.println("Inside creating System Tray");
			try {
				String iconfile = "/images/logo.png";
				if (SystemTray.isSupported()) {
					System.out.println("system tray supported");
					tray = SystemTray.getSystemTray();
					System.out.println(tray.getTrayIconSize().toString());

					Image image = Toolkit.getDefaultToolkit().getImage(
							"/images/icon.png");
					ActionListener UIListener = new ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("Inside action listener");
							
								UIThread.openStage();
							
						}
					};

					ActionListener exitListener = new ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							trayIcon.displayMessage("Dropbox Exiting....",
									"Thank you for Using Dropbox",
									MessageType.INFO);
							System.exit(0);
						}

					};
					
					
					ActionListener openFolder = new ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							try {
								Desktop.getDesktop().open(new File(ClientMain.getSettings().getRootDir()));
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}

					};

					PopupMenu popup = new PopupMenu();
					MenuItem defaultItem = new MenuItem("Applcation Control");
					MenuItem exitItem = new MenuItem("Exit");
					MenuItem openfolder = new MenuItem("Open Folder");
					exitItem.addActionListener(exitListener);
					defaultItem.addActionListener(UIListener);
					openfolder.addActionListener(openFolder);
					popup.add(openfolder);
					popup.add(defaultItem);
					popup.add(exitItem);
					
					System.out.println(image.toString());
					trayIcon = new TrayIcon(createImage(iconfile,
							"tray icon"), "Dropbox Application", popup);
					trayIcon.setImageAutoSize(true);
					tray.add(trayIcon);
					

					if(Application_Navigator.SESSION_OK)
					{
						trayIcon.displayMessage("Dropbox Started Successfully....",
								"Your Root Directory will be Syncing from Cloud",
								MessageType.INFO);
						
						setToolTip("Dropbox Application : Connected");
						
						LoggerClass.writeLog("System Tray Message - Success");

						if(Application_Navigator.SESSION_OK)
				    	{
				        	UIThread.closeStage();;
				    	}
					}else
					{
						LoggerClass.writeLog("System Tray Message - Failure");

						trayIcon.displayMessage("Dropbox Initializion Failed....",
								"Please re-enter your username and password",
								MessageType.INFO);
						
						setToolTip("Dropbox Application : Not Connected");

					}
				} else {
					LoggerClass.writeLog("system tray not supported");

					System.out.println("system tray not supported");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		

		/*for (int i = 0; i < 50; i++) {
			try {
				TimeUnit.SECONDS.sleep(1);
				System.out.println("Hello" + i);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
	}

	protected static Image createImage(String path, String description) {
		URL imageURL = SystemTrayImplementor.class.getResource(path);
		System.out.println(imageURL.toString());
		return (new ImageIcon(imageURL, description)).getImage();
	}
	
	public static void displayMessage(String trayMessage,String trayMessageBody,MessageType messageType)
	{
		if(trayIcon!=null){
			trayIcon.displayMessage(trayMessage,
					trayMessageBody,
					messageType);
		}
	}
	
	
	public static void setToolTip(String message)
	{
		trayIcon.setToolTip(message);
	}
}