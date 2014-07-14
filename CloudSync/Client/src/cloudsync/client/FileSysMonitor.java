package cloudsync.client;

public class FileSysMonitor {

	public boolean StartListen(String directory, FileSysMonitorCallback callback){
		return false;
	}
	
	public boolean StopListen(){
		return false;
	}
	
	public boolean isLocked(String filename){
		//When a file is opened, this file should be locked.
		//Even the file is updated in cloud, it can not be overwritten on local disk
		return true; 
	}
}
