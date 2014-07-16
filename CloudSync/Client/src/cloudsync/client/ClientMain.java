package cloudsync.client;

import java.io.IOException;
import java.util.ArrayList;

import cloudsync.sharedInterface.SessionBlob;

public class ClientMain {

	private static SessionMaster masterSession = null;
	private static ClientSettings settings = null;
	private static ArrayList<FileSysMonitor> allFileMonitors = null;
	
	public static ArrayList<FileSysMonitor> getAllFileMonitors(){
		return allFileMonitors;
	}

	public static void main(String[] args) {
		
		//SessionBlob sessionBlod = new SessionBlob();
		//sessionBlod.blobTest();

		settings = ClientSettings.getInstance();
		settings.loadSettings();
		
		if(allFileMonitors==null){
			allFileMonitors = new ArrayList<FileSysMonitor>();
		}
		FileSysMonitor fileMonitor = new FileSysMonitor();
		fileMonitor.StartListen(settings.getRootDir(), new FileSysMonitorCallback(){

			@Override
			public void Callback(String filename) {
				SessionMaster masterSession = SessionMaster.getInstance();
				masterSession.uploadFile(filename);
			}
			
		});
		allFileMonitors.add(fileMonitor);
		
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
