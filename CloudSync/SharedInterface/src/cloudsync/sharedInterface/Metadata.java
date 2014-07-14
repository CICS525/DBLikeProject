package cloudsync.sharedInterface;

import java.util.Date;

public class Metadata {
	public enum STATUS {INIT, NEW, LAST, HISTORY, DELETE, CONFLICT, ERROR};
	
	public String filename = null;
	public long globalCounter = 0;
	public long parent = 0;
	
	public STATUS status = STATUS.INIT;
	public Date timestamp = new Date();	//this is the master server time on updating or client local time for file modification. Only the master server time is used to calculate SHA
	
	public String blobKey = null;
	public ServerLocation blobServer;	//primary blob server
	public ServerLocation blobBackup;	//secondary blob server	
} 
