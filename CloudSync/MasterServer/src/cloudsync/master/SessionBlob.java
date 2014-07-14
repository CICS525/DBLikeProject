package cloudsync.master;

import cloudsync.sharedInterface.Metadata;

public class SessionBlob {
	public boolean uploadFile(String filename, Metadata metadata){
		//update file, specified by filename to Blob server
		//Blob server and Blob name are specified in metadata
		//If there are Backup blob server, they should all be updated in parallel thread
		return false;
	}
}
