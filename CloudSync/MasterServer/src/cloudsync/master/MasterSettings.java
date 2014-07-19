package cloudsync.master;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import cloudsync.sharedInterface.AzureConnection;
import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.ServerLocation;

public class MasterSettings implements Serializable{      //serialization initialized 
	
	//Master Settings should be singleton design paten
	private static final long serialVersionUID = -1026245079078573719L;
	
	private static MasterSettings that = null;
	
	private int LocalPort = 0; 
	private ServerLocation MasterBackup = null;
	private AzureConnection BlobFirst = null;
	private AzureConnection BlobSecond = null;
	
	private final String fSettingsFileName = ".MasterSettings.settings";

	public int getLocalPort() {
		if(LocalPort!=0){
			return LocalPort;
		}else{
			return DefaultSetting.DEFAULT_MASTER_SERVEL_PORT;
		}
	}
	public void setLocalPort(int localPort) {
		LocalPort = localPort;
	}
	public ServerLocation getMasterBackup() {
		return MasterBackup;
	}
	public void setMasterBackup(ServerLocation masterBackup) {
		MasterBackup = masterBackup;
	}
	public AzureConnection getBlobFirst() {
		return BlobFirst;
	}
	public void setBlobFirst(AzureConnection blobFirst) {
		BlobFirst = blobFirst;
	}
	public AzureConnection getBlobSecond() {
		return BlobSecond;
	}
	public void setBlobSecond(AzureConnection blobSecond) {
		BlobSecond = blobSecond;
	}
	
	private MasterSettings(){
		loadSettings();
	}
	
	public static MasterSettings getInstance(){
		if(that==null){
			that = new MasterSettings();
		}
		return that;
	}

	public boolean loadSettings(){
		boolean ans = false;
		//Read all settings from file SettingsFileName
		ObjectInputStream is = null;
		try {
			is = new ObjectInputStream(new FileInputStream(fSettingsFileName));
			
			MasterSettings temp = (MasterSettings)is.readObject();
			setLocalPort(temp.getLocalPort());
			setMasterBackup(temp.getMasterBackup());
			setBlobFirst(temp.getBlobFirst());
			setBlobSecond(temp.getBlobSecond());
			
			ans = true;
		} catch (Exception e) {
			System.out.println("ERROR"+e);
		}
		finally{
			try {
				if(is!=null)
					is.close();
			} catch (IOException e) {
				System.out.println("ERROR"+e);
				e.printStackTrace();
			}
		}
		return ans;
	}
	
	public boolean saveSettings(){
		boolean ans = false;
		//Write all settings into file SettingsFileName
		ObjectOutputStream os = null;
		try {
			os=new ObjectOutputStream(new FileOutputStream(fSettingsFileName));
			os.writeObject(this);
			ans = true;
		} catch (FileNotFoundException e) {
			System.out.println("ERROR"+e);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ERROR"+e);
			e.printStackTrace();
		}
		finally{
			try {
				if(os!=null)
					os.close();
			} catch (IOException e) {
				System.out.println("ERROR"+e);
				e.printStackTrace();
			}
		}
		return ans;
	}
}
