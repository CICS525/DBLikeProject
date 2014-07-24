package cloudsync.sharedInterface;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

public class Metadata implements Serializable{

	/**
	 * Metadata should be serializable to facilitate local data storage
	 */
	private static final long serialVersionUID = 2951772298425485024L;

	public enum STATUS {INIT, /*NEW,*/ LAST, HISTORY, DELETE, CONFLICT, ERROR};
	
	public String filename = null;
	public long globalCounter = 0;
	public long parent = 0;
	
	public STATUS status = STATUS.ERROR;
	public Date timestamp = null;	//this is the master server time on updating or client local time for file modification. Only the master server time is used to calculate SHA
	
	public String blobKey = null;
	public AzureConnection blobServer;	//primary blob server
	public AzureConnection blobBackup;	//secondary blob server
	
	public Metadata(){
		status = STATUS.INIT;
		timestamp = new Date();
	}	
	public Metadata(String baseFilname){
		this();
		filename = baseFilname;
	}	
	
	public static String mixRootAndFilename(String rootDir, String filename){
		if( rootDir.endsWith(File.separator) && filename.startsWith(File.separator) ){
			return rootDir + filename.substring(1);	//skip the first character in baseFilename, e.g. "c:/abc/" + "/def.txt" = "c:/adc/def.txt" 
		} else if ( !rootDir.endsWith(File.separator) && !filename.startsWith(File.separator) ){
			return rootDir + File.separator + filename;
		} else {
			return rootDir + filename;
		}
	}
	
	public String getAbsoluteFilename(String rootDir){
		return mixRootAndFilename(rootDir, filename);
	}

	/**
	 * Overriding the equals method for metadata objects.
	 * Takes only the filename into account.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Metadata other = (Metadata) obj;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		return true;
	}
} 
