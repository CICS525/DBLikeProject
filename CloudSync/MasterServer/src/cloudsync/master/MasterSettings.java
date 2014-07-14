package cloudsync.master;

import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.ServerLocation;

public class MasterSettings {

	//Master Settings should be singleton design patten
	private static MasterSettings that = null;
	
	private int LocalPort = 0; 
	private ServerLocation MasterBackup = null;
	private ServerLocation BlobFirst = null;
	private ServerLocation BlobSecond = null;

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
		return false;
	}
	
	public boolean saveSettings(){
		//Write all settings into file SettingsFileName
		return false;
	}
}
