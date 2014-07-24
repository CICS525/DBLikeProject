package cloudsync.client;

import java.io.File;
import java.util.ArrayList;

import cloudsync.client.FileSysMonitorCallback.Action;
import cloudsync.sharedInterface.AzureConnection;
import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.FileSender;
import cloudsync.sharedInterface.FileSysCallback;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.Metadata.STATUS;
import cloudsync.sharedInterface.ServerLocation;
import cloudsync.sharedInterface.SessionBlob;

public class ClientMain {

	private static ClientSettings settings = null;
	private static ServerLocation masterLocation = null;
	private static SessionMaster masterSession = null;
	//private static FileSysMonitor fileMonitor = null;
	private static ArrayList<FileSysMonitor> allFileMonitors = new ArrayList<FileSysMonitor>();
	private static MetadataManager metadataManager = null;
	
	public static ArrayList<FileSysMonitor> getAllFileMonitors(){
		return allFileMonitors;
	}
	public static void main(String[] args) {
		System.out.println("ClientMain starts ...");
		
		//settings = ClientSettings.getInstance();
		//settings.loadSettings();

		// Client should do upload first & do download. 
		// This is in order to handle the file could be modified when the client is not running.
		// Here should scan all local file time stamps to compare with the one in local metadata.
		/*
		if(allFileMonitors==null){
			allFileMonitors = new ArrayList<FileSysMonitor>();
		}*/
		FileSysMonitor fileMonitor = new FileSysMonitor(System.getProperty("user.dir"));
		fileMonitor.StartListen(new FileSysMonitorCallback(){
			@Override
			public void Callback(String filename, Action action) {
				// TODO Auto-generated method stub
				System.out.println(action.toString());
			}
		});		
		
		// Client should do upload first & do download. 
		// This is in order to handle the file could be modified when the client is not running.
		// Here should scan all local file timestamps to compate with the one in local metadata.
		
		/*
		masterSession = SessionMaster.getInstance();
		masterSession.setMasterServerLocation(settings.getRecentMaster());
		masterSession.connect(settings.getUsername(), settings.getPassword());
		
		//--- [should not change, unless conflict] ---
		incomplete.filename = FileSysPerformer.getInstance().getBaseFilename(absoluteFilename);
		incomplete.parent = metadataManager.findByBasename(FileSysPerformer.getInstance().getBaseFilename(absoluteFilename)).globalCounter;
		//--- [to be over written by Master Server] ---
		incomplete.globalCounter = metadataManager.getGlobalWriteCounter();
		incomplete.status = Action.MODIFY==action ? STATUS.LAST : STATUS.DELETE ;
		//incomplete.timestamp = new Date();
		//--- [to be set by Master Server] ---
		//incomplete.blobKey
		//incomplete.blobServer
		//incomplete.blobBackup
		

		while(true){
			Metadata complete = masterSession.rmiCommitFileUpdate(incomplete, tempFileOnServer);
						
			if(complete!=null){
				if( complete.status!=STATUS.CONFLICT ){
					//should rename & try again in next FileSysMonitor callback

					int pointIdx = absoluteFilename.lastIndexOf(".");
					if(pointIdx<0)
						pointIdx = absoluteFilename.length();
					String head = absoluteFilename.substring(0, pointIdx);
					String tail = absoluteFilename.substring(pointIdx, absoluteFilename.length());
					
					String temp = head + "_" + ClientSettings.getInstance().getDeviceName() + tail;
					int retry = 0;
					while(true){
						File check = new File(temp);
						if(check.exists())
							temp = head + "_" + ClientSettings.getInstance().getDeviceName() + (retry++) + tail;
						else
							break;
					}

					File fOld = new File(absoluteFilename);
					File fNew = new File(temp);
					
					for(FileSysMonitor aMonitor : ClientMain.getAllFileMonitors()){
						aMonitor.startIgnoreFile(absoluteFilename);
					}
					
					boolean get = fOld.renameTo(fNew);

					for(FileSysMonitor aMonitor : ClientMain.getAllFileMonitors()){
						aMonitor.stopIgnoreFile(absoluteFilename);
					}
					return false;
				}else if( complete.status!=STATUS.ERROR ){
					return false;
				}
				boolean suc = metadataManager.updateLocalMetadata(complete);
				return suc;
			}
		}*/
	}

	/*
	public static void main(String[] args) {
		boolean suc = initClientMain();
		System.out.println("main@ClientMain=>" + suc);
	}*/
}
