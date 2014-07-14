package cloudsync.client;

import java.util.ArrayList;

import cloudsync.sharedInterface.Metadata;

public class MetadataManager {

	private long GlobalWriteCounter;
	private ArrayList<Metadata> LocalMetadata;
	private static MetadataManager that = null;
	
	private MetadataManager(){
		//private constructor to secure singleton
	}
	
	public static MetadataManager getInstance(){
		if(that==null){
			that = new MetadataManager();
		}
		return that;
	}
	
	public long getGlobalWriteCounter() {
		return GlobalWriteCounter;
	}

	public ArrayList<Metadata> getLocalMetadata() {
		return LocalMetadata;
	}

	public boolean readLocalMetadata(){
		GlobalWriteCounter = 1; //should be read from local file
		LocalMetadata = new ArrayList<Metadata>();
		return false;
	}
	
	public boolean saveLocalMetadate(){
		return false;
	}

	public boolean updateLocalMetadate(Metadata aMetadata){
		saveLocalMetadate();	//maybe save at once
		return false;
	}
	
	public boolean delteLocalMetadata(Metadata aMetadata){
		saveLocalMetadate();	//maybe save at once
		return false;
	}
}
