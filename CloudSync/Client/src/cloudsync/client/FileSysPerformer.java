package cloudsync.client;

import java.util.ArrayList;

import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.Metadata.STATUS;

public class FileSysPerformer {
	//FileSysPerformer should be singleton design pattern
	private static FileSysPerformer that = null;

	private ArrayList<Metadata> metaList = null;
	private PerformThread thread = null;
	
	// Test by Sky
	private FileSysPerformer(){
		// private constructor to secure singleton
	}
	
	public static FileSysPerformer getInstance(){
		if(that==null){
			that = new FileSysPerformer();
		}
		return that;
	}
	
	private boolean prepareFolder(String filename){
		//maybe need to create directory before writing
		return false;
	}
	
	private boolean deleteFile(String filename){
		// delete empty folder when it is empty
		// file separate 
		return false;
	}

	private boolean FilePerform(Metadata metadata){
		// Add this file to the ignore list of its FileSysMonitor
		for(FileSysMonitor aMonitor : ClientMain.getAllFileMonitors()){
			aMonitor.startIgnoreFile(metadata.filename);
		}
		
		if(metadata.status==STATUS.DELETE){
			deleteFile(metadata.filename);
		}else{
			prepareFolder(metadata.filename);
			SessionBlob blobSession = new SessionBlob();
			blobSession.downloadFile(metadata);
		}
		
		// Remove this file from the ignore list of its FileSysMonitor 
		for(FileSysMonitor aMonitor : ClientMain.getAllFileMonitors()){
			aMonitor.stopIgnoreFile(metadata.filename);
		}

		return false;
	}
	
	public void addUpdateLocalTask(Metadata metadata){
		if(metaList == null){
			metaList = new ArrayList<Metadata>();
		}
		metaList.add(metadata);
		
		if(thread==null){
			thread = new PerformThread();
			thread.start();
		}
	}
	
	private class PerformThread extends Thread{

		@Override
		public void run() {
			while( !metaList.isEmpty() ){
				for( Metadata aMeta : metaList ){
					boolean suc = FilePerform(aMeta);
					if(suc){
						metaList.remove(aMeta);
					}
				}
			}
			super.run();
			thread = null;	//quit the thread
		}
	}
}
