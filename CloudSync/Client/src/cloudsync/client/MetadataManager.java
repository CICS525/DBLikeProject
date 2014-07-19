package cloudsync.client;

<<<<<<< HEAD

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
=======
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
>>>>>>> 4ee1819492a8e0a17d070feaccd62f23f10ea0cb
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

	private static long GlobalWriteCounter = 1;
	private ArrayList<Metadata> LocalMetadata = new ArrayList<Metadata>();
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
<<<<<<< HEAD
	 * Also loads the GlobalWriteCounter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean readLocalMetadata() {
		try {
			FileInputStream fs = new FileInputStream(META_FILENAME);
			ObjectInputStream objInput = new ObjectInputStream(fs);
			GlobalWriteCounter = objInput.readLong();
			LocalMetadata = (ArrayList<Metadata>) objInput.readObject();
			objInput.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
=======
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
>>>>>>> 4ee1819492a8e0a17d070feaccd62f23f10ea0cb
		}
		
	}
	
	/**
	 * Saves the list of metadata to the file.
<<<<<<< HEAD
	 * Also saves the GlobalWriteCounter.
=======
>>>>>>> 4ee1819492a8e0a17d070feaccd62f23f10ea0cb
	 * @return
	 */
	public boolean saveLocalMetadata() {
		try {
			FileOutputStream fo = new FileOutputStream(META_FILENAME);
			ObjectOutputStream objStream = new ObjectOutputStream(fo);
<<<<<<< HEAD
			objStream.writeLong(GlobalWriteCounter);
			objStream.writeObject(LocalMetadata);
			objStream.close();
=======
			objStream.writeObject(LocalMetadata);
>>>>>>> 4ee1819492a8e0a17d070feaccd62f23f10ea0cb
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
<<<<<<< HEAD
	 * Update a metadata object.
	 * TODO: Currently fails as there is no way to tell if
	 * two metadata objects are equal or not.
	 * @param aMetadata
	 * @return
	 */
	public boolean updateLocalMetadata(Metadata aMetadata){
=======
	 * Update a metadata object
	 * @param aMetadata
	 * @return
	 */
	public boolean updateLocalMetadate(Metadata aMetadata){
>>>>>>> 4ee1819492a8e0a17d070feaccd62f23f10ea0cb
		if (LocalMetadata.contains(aMetadata)) {
			LocalMetadata.remove(aMetadata); //remove the old version
		}
		LocalMetadata.add(aMetadata); // add the new version
<<<<<<< HEAD
		return saveLocalMetadata(); //maybe save at once
=======
		saveLocalMetadata(); //maybe save at once
		return false;
>>>>>>> 4ee1819492a8e0a17d070feaccd62f23f10ea0cb
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
