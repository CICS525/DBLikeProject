package cloudsync.master;


import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.ServerLocation;

import java.io.*;
public class MasterSettings implements Serializable{      //serialization intialized 
	

	//Master Settings should be singleton design patten
	
	private static MasterSettings that = null;
	private int LocalPort = 0; 
	private ServerLocation MasterBackup = null;
	private ServerLocation BlobFirst = null;
	private ServerLocation BlobSecond = null;

MasterSettings mss=new MasterSettings();

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
	public ServerLocation getBlobFirst() {
		return BlobFirst;
	}
	public void setBlobFirst(ServerLocation blobFirst) {
		BlobFirst = blobFirst;
	}
	public ServerLocation getBlobSecond() {
		return BlobSecond;
	}
	public void setBlobSecond(ServerLocation blobSecond) {
		BlobSecond = blobSecond;
	}
	
	private MasterSettings(){
		//private constructor to secure singleton
		
			
	}
	
	public static MasterSettings getInstance(){
		if(that==null){
			that = new MasterSettings();
		}
		return that;
	}

	public boolean loadSettings(){
		//Read all settings from file SettingsFileName
		ObjectInputStream ip = null;
		try {
			
			ip = new ObjectInputStream(new FileInputStream("mastersettings.dat"));
			ip.readObject();
			
		} catch (Exception e) {
			System.out.println("ERROR"+e);
		}
		finally{
			try {
				ip.close();
			} catch (IOException e) {
				System.out.println("ERROR"+e);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean saveSettings(){
		//Write all settings into file SettingsFileName

		mss.setLocalPort(LocalPort);
		mss.setMasterBackup(MasterBackup);
		mss.setBlobFirst(BlobFirst);
		mss.setBlobSecond(BlobSecond);
		ObjectOutputStream os = null;
		try {
			os=new ObjectOutputStream(new FileOutputStream("mastersettings.dat"));
			os.writeObject(mss);
		} catch (FileNotFoundException e) {
			System.out.println("ERROR"+e);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ERROR"+e);
			e.printStackTrace();
		}
		finally{
			try {
				os.close();
			} catch (IOException e) {
				System.out.println("ERROR"+e);
				e.printStackTrace();
			}
		}
		
		
		return false;
	}
}
