package cloudsync.client;

import java.util.ArrayList;

import cloudsync.sharedInterface.AzureConnection;
import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.SessionBlob;

public class ClientMain {

	private static SessionMaster masterSession = null;
	private static ClientSettings settings = null;
	//private static FileSysMonitor fileMonitor = null;
	private static ArrayList<FileSysMonitor> allFileMonitors = new ArrayList<FileSysMonitor>();
	
	public static ArrayList<FileSysMonitor> getAllFileMonitors(){
		return allFileMonitors;
	}
	
	public static boolean initClientMain() {
		System.out.println("ClientMain starts ...");
		
		settings = ClientSettings.getInstance();
		settings.loadSettings();

		// Client should do upload first & do download. 
		// This is in order to handle the file could be modified when the client is not running.
		// Here should scan all local file time stamps to compare with the one in local metadata.

		masterSession = SessionMaster.getInstance();
		masterSession.setMasterServerLocation(settings.getRecentMaster());
		
		FileSysMonitor fileMonitor = new FileSysMonitor();
		boolean bMnt = fileMonitor.StartListen(settings.getRootDir(), new FileSysMonitorCallback(){
			@Override
			public void Callback(String filename, Action action) {
				System.out.println(filename + " " + action);
				String absoluteFilename = FileSysPerformer.getInstance().getAbsoluteFilename(filename);
				SessionBlob sessionBlob = new SessionBlob();
				Metadata metadata = new Metadata();
				metadata.filename = filename;
				metadata.blobKey = filename;
				metadata.blobServer = new AzureConnection(DefaultSetting.eli_storageConnectionString);
				metadata.blobBackup = new AzureConnection(DefaultSetting.chris_storageConnectionString);
				boolean suc = false;
				if ( FileSysMonitorCallback.Action.MODIFY == action ) {
					//suc = sessionBlob.uploadFile(absoluteFilename, metadata);
					System.out.println("Upload File:" + absoluteFilename + "->" + suc);
				} else if ( FileSysMonitorCallback.Action.DELETE == action) {
					//suc = sessionBlob.deleteFile(metadata);
					System.out.println("Delete File:" + absoluteFilename + "->" + suc);
				}
			}
		});
		if(bMnt){
			System.out.println("initClientMain@ClientMain: fileMonitor.StartListen#" + settings.getRootDir() + "->" + bMnt);
			allFileMonitors.add(fileMonitor);
		}

		System.out.println("initClientMain@ClientMain: Connecint to Master Server: " + settings.getUsername() + "#" + settings.getPassword());
		boolean bCnt = masterSession.connect(settings.getUsername(), settings.getPassword());
		return bCnt;
	}

	public static void main(String[] args) {
		boolean suc = initClientMain();
		System.out.println("main@ClientMain=>" + suc);
	}
}
