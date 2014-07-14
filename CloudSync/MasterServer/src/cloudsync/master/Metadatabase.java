package cloudsync.master;

import java.util.ArrayList;

import cloudsync.sharedInterface.Blobdata;
import cloudsync.sharedInterface.Metadata;

public class Metadatabase {
	
    public Metadata acceptFileUpdate(String username, Metadata incompleteMetadata, Blobdata blobdata)
    {
        //if NO conflict, save the Metadata in local database & save Blobdata in Blob Server, then update FileMetadata and return
        //if conflict, reject this update and return error reason in FileMetadata. Then let client to rename the file and upate again
        //Future Plan: Maybe the Blobdata can be saved to avoid upload again, but need a mechanism to collect garbage if client does not update again.
        return null;
    }
    
    public ArrayList<Metadata> getCompleteMetadata(String username, long sinceCounter){
    	return null;
    }
}
