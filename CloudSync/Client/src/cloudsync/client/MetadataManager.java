package cloudsync.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.ServerLocation;

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
	public boolean readLocalMetadata() {
		Scanner scan = null; 
		try {
			scan = new Scanner(META_FILENAME);
			LocalMetadata = new ArrayList<Metadata>();
			LocalMetadata.clear(); // clear completely before adding
			String url;
			int port;
			String backupUrl;
			int backupPort;

			GlobalWriteCounter = scan.nextInt();
			while (scan.hasNext()) {
				Metadata temp = new Metadata();
				temp.status = Metadata.STATUS.valueOf(scan.next());
				temp.parent = scan.nextLong();
				temp.blobKey = scan.next();
				url = scan.next();
				port = scan.nextInt();
				backupUrl = scan.next();
				backupPort = scan.nextInt();
				temp.blobServer = new ServerLocation(url, port);
				temp.blobBackup = new ServerLocation(backupUrl, backupPort);
				LocalMetadata.add(temp);
			}
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			scan.close();
		}
	}
	
	/**
	 * Saves the list of metadata to the file.
	 * @return
	 */
	public boolean saveLocalMetadata() {
		try {
			FileOutputStream fo = new FileOutputStream(META_FILENAME);
			ObjectOutputStream objStream = new ObjectOutputStream(fo);
			objStream.writeObject(LocalMetadata);
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
	 * Update a metadata object
	 * @param aMetadata
	 * @return
	 */
	public boolean updateLocalMetadate(Metadata aMetadata){
		if (LocalMetadata.contains(aMetadata)) {
			LocalMetadata.remove(aMetadata); //remove the old version
		}
		LocalMetadata.add(aMetadata); // add the new version
		saveLocalMetadata(); //maybe save at once
		return false;
	}
	
	/**
	 * Delete a metadata object
	 * @param aMetadata
	 * @return
	 */
	public boolean deleteLocalMetadata(Metadata aMetadata){
		LocalMetadata.remove(aMetadata);
		saveLocalMetadata();	//maybe save at once
		return false;
	}
}
