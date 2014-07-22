package cloudsync.client;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;

public class SystemTrayImplementor implements Runnable {

	public static SystemTray tray;
	public static TrayIcon trayIcon;
	public static boolean added;

	public void run() {
		if (!added) {
			// TODO Auto-generated method stub
			System.out.println("Inside creating System Tray");
			try {
				String iconfile = "/images/home_2.png";
				if (SystemTray.isSupported()) {
					System.out.println("system tray supported");
					tray = SystemTray.getSystemTray();
					System.out.println(tray.getTrayIconSize().toString());

					Image image = Toolkit.getDefaultToolkit().getImage(
							"/images/icon.png");
					ActionListener UIListener = new ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							if(UIThread.added)
							{
								UIThread.openStage();
							}else
							{
								Application_Main.launchUIThread();
							}
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

					PopupMenu popup = new PopupMenu();
					MenuItem defaultItem = new MenuItem("Open UI");
					MenuItem exitItem = new MenuItem("Exit");
					exitItem.addActionListener(exitListener);
					defaultItem.addActionListener(UIListener);
					popup.add(defaultItem);
					popup.add(exitItem);
					System.out.println(image.toString());
					trayIcon = new TrayIcon(createImage(iconfile,
							"tray icon"), "Dropbox Application", popup);
					trayIcon.setImageAutoSize(true);
					tray.add(trayIcon);
					added = true;

				} else {
					System.out.println("system tray not supported");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < 50; i++) {
			try {
				TimeUnit.SECONDS.sleep(1);
				System.out.println("Hello" + i);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected static Image createImage(String path, String description) {
		URL imageURL = SystemTrayImplementor.class.getResource(path);
		System.out.println(imageURL.toString());
		return (new ImageIcon(imageURL, description)).getImage();
	}
}