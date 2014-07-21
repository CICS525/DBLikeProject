package cloudsync.client;

import java.io.IOException;
import java.util.ArrayList;

import cloudsync.sharedInterface.Metadata;

public class ClientMain {

	private static SessionMaster masterSession = null;
	private static ClientSettings settings = null;
	private static ArrayList<FileSysMonitor> allFileMonitors = new ArrayList<FileSysMonitor>();
	
	public static ArrayList<FileSysMonitor> getAllFileMonitors(){
		
		return allFileMonitors;
	}

	public static void main(String[] args) {
		System.out.println("ClientMain starts ...");
		
		//settings = ClientSettings.getInstance();
		//settings.loadSettings();

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
		} */
		FileSysMonitor fileMonitor = new FileSysMonitor();
		fileMonitor.StartListen(System.getProperty("user.dir"), new FileSysMonitorCallback(){

			@Override
			public void Callback(String filename, Action action) {
				// TODO Auto-generated method stub
				System.out.println("Callback fired!");
				System.out.println("This is the callback action :"+ action.toString());
				
			}
			
		});
		//allFileMonitors.add(fileMonitor);
		
		
		// Client should do upload first & do download. 
		// This is in order to handle the file could be modified when the client is not running.
		// Here should scan all local file timestamps to compate with the one in local metadata.
		
		/*
		masterSession = SessionMaster.getInstance();
		masterSession.setMasterServerLocation(settings.getRecentMaster());
		masterSession.connect(settings.getUsername(), settings.getPassword());
		
		boolean b = masterSession.rmiCheckUsernamePassword("someone", "aPassword");
		long l = masterSession.rmiGetMasterServerGlobalCounter();
		ArrayList<Metadata> a = masterSession.rmiGetCompleteMetadata(10);
		Metadata m = masterSession.rmiCommitFileUpdate(null, "info");
		
		//--- wait here forever ---
		/*
		try {
			byte[] buff = new byte[128];
			System.in.read(buff);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}

}
