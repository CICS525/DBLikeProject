package cloudsync.client;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;

import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.Metadata.STATUS;

public class FileSysPerformer {
	//FileSysPerformer should be singleton design pattern
	private static FileSysPerformer that = null;

	private ArrayList<Metadata> metaList = null;
	private PerformThread thread = null;
	public static Lock lock;
	
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
		String folder = filename.substring(0, filename.lastIndexOf(File.separator));
		File directory = new File(folder);
		if(!directory.exists()){
			System.out.println("Creating a directory " + folder);
			boolean isCreated = false;
			try {
				directory.mkdir();
				isCreated = true;
			} catch (SecurityException se) {
				System.out.println(se.getMessage());
			}
			if (isCreated) {
				System.out.println("Directory " + folder + " is created");
			}
		}
		return false;
	}
	
	private boolean deleteFile(String filename){
		File file = new File(filename);
		if (file.delete()){
			System.out.println(file.getName() + " is deleted.");
		} else {
			System.out.println("Delete operation fails");
		}
		// delete empty folder when it is empty
		String folder = filename.substring(0,filename.lastIndexOf(File.separator));
		File directory = new File(folder);
		if (directory.isDirectory()){
			if(directory.list().length>0){
				System.out.println("Directory is not empty");
			} else {
				directory.delete();
			}	
		} else {
			System.out.println("This is not a folder");
		}
		return false;
	}

	private boolean FilePerform(Metadata metadata){
		// Add this file to the ignore list of all FileSysMonitors
		lock.lock();
		for(FileSysMonitor aMonitor : ClientMain.getAllFileMonitors()){
			aMonitor.startIgnoreFile(metadata.filename);
		}
		
		if(metadata.status==STATUS.DELETE){
			deleteFile(metadata.filename);
		}else{
			prepareFolder(metadata.filename);
			SessionBlobClient blobSession = new SessionBlobClient();
			blobSession.downloadFile(metadata);
		}
		
		// Remove this file from the ignore list all FileSysMonitors 
		for(FileSysMonitor aMonitor : ClientMain.getAllFileMonitors()){
			aMonitor.stopIgnoreFile(metadata.filename);
		}
		lock.unlock();
		return false;
	}
	
	public void addUpdateLocalTask(Metadata metadata){
		if(metaList == null){
			metaList = new ArrayList<Metadata>();
		}
		lock.lock();
		metaList.add(metadata);
		lock.unlock();
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
						lock.lock();
						metaList.remove(aMeta);
						lock.unlock();
					}
				}
			}
			super.run();
			thread = null;	//quit the thread
		}
	}
	
	public String getAbsoluteFilename(String baseFilename){
		String rootDir = ClientSettings.getInstance().getRootDir();
		return Metadata.mixRootAndFilename(rootDir, baseFilename);
	}
	
	public String getAbsoluteFilename(Metadata metadata){
		return getAbsoluteFilename(metadata.filename);
	}
	
	public String getBaseFilename(String absoluteFilename){
		String rootDir = ClientSettings.getInstance().getRootDir();;
		if(rootDir==null || rootDir.length()==0)
			return null;	//error
		
		if(!rootDir.endsWith(File.separator)){
			rootDir += File.separator;
		}
		
		if(absoluteFilename.startsWith(rootDir)){
			return absoluteFilename.substring(rootDir.length());
		}else{
			return null;	//the file is NOT in root directory
		}
	}
}
