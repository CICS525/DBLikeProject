package cloudsync.client;

import java.io.IOException;

public class ClientMain {

	private static SessionMaster masterSession;

	public static void main(String[] args) {

		ClientSettings settings = ClientSettings.getInstance();
		settings.loadSettings();
		
		FileSysMonitor fileMonitor = new FileSysMonitor();
		fileMonitor.StartListen(settings.getRootDir(), new FileSysMonitorCallback(){

			@Override
			public void Callback(String filename) {
				SessionMaster masterSession = SessionMaster.getInstance();
				masterSession.uploadFile(filename);
			}
			
		});
		
		// Client should do upload first & do download. 
		// This is in order to handle the file could be modified when the client is not running.
		// Here should scan all local file timestamps to compate with the one in local metadata.
		
		masterSession = SessionMaster.getInstance();
		masterSession.setMasterServerLocation(settings.getRecentMaster());
		masterSession.connect(settings.getUsername(), settings.getUsername());
		
		//--- wait here forever ---
		try {
			byte[] buff = new byte[128];
			System.in.read(buff);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
