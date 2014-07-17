package cloudsync.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import cloudsync.sharedInterface.Metadata;

/**
 * Class that manages the all the metadata for
 * a particular client.
 * @author Aaron Cheng
 *
 */
public class MetadataManager {

	private long GlobalWriteCounter;
	private ArrayList<Metadata> LocalMetadata;
	private static MetadataManager that = null;
	private static String META_FILENAME = "metadata.dat";
	
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

	/**
	 * Reads the local metadata information from the file
	 * @return
	 */
	public boolean readLocalMetadata() throws FileNotFoundException, IOException {
		GlobalWriteCounter = 1; //should be read from local file
		Scanner scan = new Scanner(META_FILENAME);
		
		LocalMetadata = new ArrayList<Metadata>();
		return false;
	}
	
	/**
	 * Saves the list of metadata to the file.
	 * @return
	 */
	public boolean saveLocalMetadate() throws FileNotFoundException, IOException {
		File file = new File(META_FILENAME);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(String.valueOf(GlobalWriteCounter) + "\n");
		for (Metadata m: LocalMetadata) {
			writer.write(String.valueOf(m.status));
			writer.write(String.valueOf(m.parent));
			writer.write(m.blobKey);
		}
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
