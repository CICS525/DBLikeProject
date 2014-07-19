package cloudsync.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
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
	
	public boolean setGlobalWriteCounter(long newValue) {
		GlobalWriteCounter = newValue;
		return (GlobalWriteCounter == newValue);
	}

	public ArrayList<Metadata> getLocalMetadata() {
		return LocalMetadata;
	}

	/**
	 * Reads the local metadata information from the file
	 * Also loads the GlobalWriteCounter
	 * @return
	 */
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
		}
		
	}
	
	/**
	 * Saves the list of metadata to the file.
	 * Also saves the GlobalWriteCounter.
	 * @return
	 */
	public boolean saveLocalMetadata() {
		try {
			FileOutputStream fo = new FileOutputStream(META_FILENAME);
			ObjectOutputStream objStream = new ObjectOutputStream(fo);
			objStream.writeLong(GlobalWriteCounter);
			objStream.writeObject(LocalMetadata);
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
