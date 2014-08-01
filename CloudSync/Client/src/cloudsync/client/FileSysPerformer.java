package cloudsync.client;

import java.io.File;
import java.util.ArrayList;

import cloudsync.sharedInterface.FileSysCallback;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.Metadata.STATUS;

public class FileSysPerformer {
	private static final long INTERVAL_BETWEEN_RETRY = 1000;	//milliseconds
	
	private class MetadataEx extends Metadata{

		private static final long serialVersionUID = 6819752436364996918L;
		Metadata metadata = null;
		FileSysCallback callback = null;
		
		MetadataEx(Metadata metadata, FileSysCallback callback){
			this.metadata = metadata;
			this.callback = callback;
		}
	}
	
	//FileSysPerformer should be singleton design pattern
	private static FileSysPerformer that = null;

	private ArrayList<MetadataEx> metaList = null;
	private PerformThread thread = null;
	
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
		// maybe need to create directory before writing
		// Convert base filename to absolute file name 
		String absFilename = getAbsoluteFilename(filename);
		String folder = absFilename.substring(0, absFilename.lastIndexOf(File.separator));
		File directory = new File(folder);
		if(!directory.exists()){
			System.out.println("Creating a directory " + folder);
			try {
				directory.mkdir();
				return true;
			} catch (SecurityException se) {
				System.out.println(se.getMessage());
				return false;
			}
		}
		else
			return false;
	}
	
	private boolean deleteFile(String filename){
		boolean ans = false;
		// Convert base filename to absolute file name 
		String absFilename = getAbsoluteFilename(filename);
		File file = new File(absFilename);
		if(!file.exists()){
			//the target already gone, no action to do, stop ignore
			for(FileSysMonitor aMonitor : ClientMain.getAllFileMonitors()){
				aMonitor.stopIgnoreFile(filename);
			}
			return true;
		}
		if (file.delete()){
			System.out.println(file.getName() + " is deleted.");
			ans = true;
		} else {
			System.out.println("Delete operation fails");
			ans = false;
		}
		// delete empty folder when it is empty
		String folder = absFilename.substring(0,absFilename.lastIndexOf(File.separator));
		File directory = new File(folder);
		if (directory.isDirectory()){
			if(directory.list().length>0){
				System.out.println("Directory is not empty");
			} else {
				ans = directory.delete();
			}	
		} else {
			System.out.println("This is not a folder");
		}
		return ans;
	}

	private boolean FilePerform(Metadata metadata){
		boolean ans = false;
		// Add this file to the ignore list of all FileSysMonitors
		for(FileSysMonitor aMonitor : ClientMain.getAllFileMonitors()){
			aMonitor.startIgnoreFile(metadata.basename);
		}
		
		if(metadata.status==STATUS.DELETE){
			ans = deleteFile(metadata.basename);
		}else{
			prepareFolder(metadata.basename);
			SessionBlobClient blobSession = new SessionBlobClient();
			ans = blobSession.downloadFile(metadata);
		}
		
		return ans;
	}
	
	public void addUpdateLocalTask(Metadata metadata){
		addUpdateLocalTask(metadata, null);
	}
	
	public void addUpdateLocalTask(Metadata metadata, FileSysCallback callback){
		if(metaList == null){
			metaList = new ArrayList<MetadataEx>();
		}
		synchronized (metaList) {
			//metaList.add(metadata);
			MetadataEx metadataEx = new MetadataEx(metadata, callback);
			metaList.add(metadataEx);
		}
		if(thread==null){
			thread = new PerformThread();
			thread.start();
		}
	}
	
	private class PerformThread extends Thread{

		@Override
		public void run() {
			// A list to store all the meta data that is going to be deleted 
			while( true ){
				ArrayList<MetadataEx> delList = new ArrayList<MetadataEx>();
				ArrayList<MetadataEx> cloneList = null;
				
				synchronized (metaList) {
					cloneList = (ArrayList<MetadataEx>) metaList.clone();
				}
				
				for( MetadataEx aMetaEx : cloneList ){
					boolean suc = FilePerform(aMetaEx.metadata);
					System.out.println("PerformThread@FileSysPerformer : FilePerform(" + aMetaEx.metadata.basename + " # " + aMetaEx.metadata.status + ") -> " + suc);

					if(suc){
						delList.add(aMetaEx);
					}else{
					}
				}
				
				if(delList.size()>0){
					synchronized (metaList) {
						for( MetadataEx aMetaEx : delList ){
							FileSysCallback callback = aMetaEx.callback;
							if(callback!=null){
								callback.onFinish(true, aMetaEx.metadata.basename);
								// Remove this file from the ignore list all FileSysMonitors 
								//for(FileSysMonitor aMonitor : ClientMain.getAllFileMonitors()){
								//	aMonitor.stopIgnoreFile(aMetaEx.metadata.basename);
								//}
							}
						}
						metaList.removeAll(delList);
					}
				}
				
				synchronized (metaList) {
					if( metaList.isEmpty() )
						break;  //nothing to do, break the current thread
					else
						try {
							Thread.sleep(INTERVAL_BETWEEN_RETRY);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				}
			}
			super.run();
			thread = null;	//quit the thread
		}
	}
	
	public String getAbsoluteFilename(String baseFilename){
		String rootDir = ClientSettings.getInstance().getRootDir();
		if(baseFilename.startsWith(rootDir))
			return baseFilename;
		else
			return Metadata.mixRootAndFilename(rootDir, baseFilename);
	}
	
	public String getAbsoluteFilename(Metadata metadata){
		return getAbsoluteFilename(metadata.basename);
	}
	
	public String getBaseFilename(String absoluteFilename){
		String rootDir = ClientSettings.getInstance().getRootDir();;
		if(rootDir==null || rootDir.length()==0)
			return null;	//error
		
		if(!rootDir.endsWith(File.separator)){
			rootDir += File.separator;
		}
		
		if(absoluteFilename.startsWith(rootDir)){
			String ret = absoluteFilename.substring(rootDir.length());
			return ret;
		}else{
			return null;	//the file is NOT in root directory
		}
	}
	
	public boolean addDelayPerformFile(String basename){
		return false;
	}

	public boolean removeDelayPerformFile(String basename){
		return false;
	}

}
