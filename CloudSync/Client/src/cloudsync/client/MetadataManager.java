package cloudsync.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.Metadata.STATUS;

/**
 * Class that manages the all the metadata for
 * a particular client.
 * @author Aaron Cheng
 *
 */
public class MetadataManager {

	private static Long GlobalWriteCounter = (long) 0;
	private ArrayList<Metadata> LocalMetadata = new ArrayList<Metadata>();
	private static MetadataManager that = null;
	private static final String META_FILENAME = "metadata.dat";
	
	private MetadataManager(){
		//private constructor to secure singleton
	}
	
	public static MetadataManager getInstance(){
		if(that==null){
			that = new MetadataManager();
		}
		return that;
	}
	
	public long getSyncedGlobalWriteCounter() {
		synchronized (GlobalWriteCounter){
			return GlobalWriteCounter;
		}
	}
	public boolean setSyncedGlobalWriteCounter(long counter) {
		synchronized (GlobalWriteCounter){
			if( GlobalWriteCounter >= counter ){
				return false;
			} else {
				GlobalWriteCounter = counter;
				saveLocalMetadata();
				return true;
			}
		}
	}

	public long getSyncingGlobalWriteCounter() {
		long ans = 0;
		synchronized(LocalMetadata){
			for( Metadata meta : LocalMetadata ){
				if(meta.globalCounter>ans)
					ans = meta.globalCounter;
			}
		}
		return ans;
	}

	//public ArrayList<Metadata> getLocalMetadata() {
	//	return LocalMetadata;
	//}

	/**
	 * Reads the local metadata information from the file
	 * Also loads the GlobalWriteCounter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean readLocalMetadata() {
		try {
			FileInputStream fs = new FileInputStream(META_FILENAME);
			ObjectInputStream objInput = new ObjectInputStream(fs);
			synchronized(GlobalWriteCounter){
				GlobalWriteCounter = objInput.readLong();
			}
			synchronized(LocalMetadata){
				LocalMetadata = (ArrayList<Metadata>) objInput.readObject();
			}
			objInput.close();
			return true;
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.out.println("MetadataManager: Metadata file not found! " + META_FILENAME);
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Saves the list of metadata to the file.
	 * Also saves the GlobalWriteCounter.
	 * @return
	 */
	private boolean saveLocalMetadata() {
		try {
			FileOutputStream fo = new FileOutputStream(META_FILENAME);
			ObjectOutputStream objStream = new ObjectOutputStream(fo);
			synchronized (GlobalWriteCounter){
				objStream.writeLong(GlobalWriteCounter);
			}
			synchronized(LocalMetadata){
				objStream.writeObject(LocalMetadata);
			}
			objStream.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} 
	}

	/**
	 * Update a metadata object.
	 * @param aMetadata
	 * @return
	 */
	public boolean updateLocalMetadata(Metadata aMetadata){

		//if(aMetadata.status!=STATUS.DELETE){
		//}
		synchronized(LocalMetadata){
			if (LocalMetadata.contains(aMetadata)) {
				LocalMetadata.remove(aMetadata); //remove the old version
			}

			LocalMetadata.add(aMetadata); // add the new version
		}
		
//		synchronized (GlobalWriteCounter){
//			while(aMetadata.globalCounter>GlobalWriteCounter) {	//update GlobalWriteCounter
//				//GlobalWriteCounter = aMetadata.globalCounter;
//				if( findByGlobalCounter(GlobalWriteCounter+1) !=null ){
//
//					synchronized (LocalMetadata) {	//clear previous metadata for the same basename
//						int i = LocalMetadata.size()-2;
//						for(; i>=0; i--){
//							Metadata p = LocalMetadata.get(i);
//							if( p.basename.compareTo(aMetadata.basename)== 0 ){
//								LocalMetadata.remove(i);
//							}
//						}
//					}
//					GlobalWriteCounter++;
//
//				}else{
//					break;
//				}
//			}
//			System.out.println("updateLocalMetadata@MetadataManager: GlobalWriteCounter=" + GlobalWriteCounter);
//		}
		
		return saveLocalMetadata(); //maybe save at once
	}
	
	/**
	 * Delete a metadata object
	 * @param aMetadata
	 * @return
	 */
	public boolean deleteLocalMetadata(Metadata aMetadata){
		boolean ans = false;
		synchronized(LocalMetadata){
			while(true){	//delete all metadata with same base name
				boolean get = LocalMetadata.remove(aMetadata);
				if(get==true)
					ans = true;
				else
					break;
			}
		}
		saveLocalMetadata();	//maybe save at once
		return ans;
	}
	
	public Metadata findByBasename(String basename){
		Metadata lastMatch = null;
		synchronized(LocalMetadata){
			for( Metadata meta : LocalMetadata ){
				if( meta.basename.compareTo(basename)==0 ){
					//return meta;
					if(lastMatch==null){
						lastMatch = meta;
					}else{
						if(meta.globalCounter > lastMatch.globalCounter)
							lastMatch = meta;
					}
				}
			}
		}
		return lastMatch;
	}
	
	/**
	 * Get metadata in a folder whose status is LAST
	 * @param folderName The folder to search
	 * @return ArrayList of matching metadata
	 */
	public ArrayList<Metadata> findByFolder(String folderName) {
	    ArrayList<Metadata> result = new ArrayList<>();
	    String filePrefix = folderName + File.separator;
	    synchronized(LocalMetadata){
            for( Metadata meta : LocalMetadata ){
                if( meta.basename.startsWith(filePrefix) && meta.status == STATUS.LAST) {
                    result.add(meta);
                }
            }
        }
	    return result;
	}
	
	public Metadata findByGlobalCounter(long globalCounter){
		synchronized(LocalMetadata){
			for( Metadata meta : LocalMetadata ){
				if( meta.globalCounter==globalCounter ){
					return meta;
				}
			}
		}
		return null;
	}
	
	public boolean includeNewerMetadata(Metadata aMetadata){
		synchronized(LocalMetadata){
			for( Metadata meta : LocalMetadata ){
				if( meta.globalCounter>= aMetadata.globalCounter && 
					meta.equals(aMetadata) ){
					return true;
				}
			}
		}
		return false;
	}
	
	private void clearMetadata(){
		synchronized(LocalMetadata){
		}
	}
	
	
	public boolean deleteMetaData()
	{
		File metafile = new File(META_FILENAME);
		if(metafile.delete())
		{	
			System.out.println("Deleting the Metadata file");
			return true;
		}
		
		return false;
	}
}
