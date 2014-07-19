package cloudsync.client;

import java.io.IOException;
import java.util.ArrayList;

import cloudsync.sharedInterface.AzureConnection;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.SessionBlob;

public class ClientMain {

	private static SessionMaster masterSession = null;
	private static ClientSettings settings = null;
	private static ArrayList<FileSysMonitor> allFileMonitors = null;
	
	public static ArrayList<FileSysMonitor> getAllFileMonitors(){
		return allFileMonitors;
	}

	public static void main(String[] args) {
		System.out.println("ClientMain starts ...");
		
		settings = ClientSettings.getInstance();
		settings.loadSettings();

		//SessionBlobClient sessionBlod = new SessionBlobClient();
		////sessionBlod.blobTest();
		//String filename = "C:\\Users\\Elitward\\Capture.JPG";
		//Metadata metadata = new Metadata();
		//metadata.filename = FileSysPerformer.getInstance().getBaseFilename(filename);
		//metadata.blobKey = "Capture.JPG";
		//metadata.blobServer = new AzureConnection();
		//sessionBlod.uploadFile(filename, metadata);
		//sessionBlod.downloadFile(metadata);
		//boolean suc = sessionBlod.deleteFile(metadata);
		
		/*
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
		*/
		
		// Client should do upload first & do download. 
		// This is in order to handle the file could be modified when the client is not running.
		// Here should scan all local file timestamps to compate with the one in local metadata.
		
		masterSession = SessionMaster.getInstance();
		masterSession.setMasterServerLocation(settings.getRecentMaster());
		masterSession.connect(settings.getUsername(), settings.getPassword());
		
		//--- wait here forever ---
		try {
			byte[] buff = new byte[128];
			System.in.read(buff);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
