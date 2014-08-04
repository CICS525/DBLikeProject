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

public class MasterSettings implements Serializable { // serialization
	// initialized

	// Master Settings should be singleton design paten
	private static final long		serialVersionUID	= -1026245079078573719L;

	private static MasterSettings	that				= null;

	private int						LocalMessagePort	= 0;
	private int						LocalRmiPort		= 0;
	private int						LocalUploadPort		= 0;

	//
	private AzureConnection			StorageFirst		= null;
	private AzureConnection			StorageSecond		= null;

	//
	private AzureConnection			entryServer			= null;

	// addr that store in account db, used when create account
	private String					MasterAddrMain		= null;
	private String					MasterAddrBackup	= null;

	private final static String		SETTING_FILENAME	= ".MasterSettings.settings";

	public int getLocalMessagePort() {
		return LocalMessagePort;
	}

	public void setLocalMessagePort(int localPort) {
		LocalMessagePort = localPort;
	}

	public int getLocalRmiPort() {
		return LocalRmiPort;
	}

	public void setLocalRmiPort(int locaLRmiPort) {
		LocalRmiPort = locaLRmiPort;
	}

	public int getLocalUploadPort() {
		return LocalUploadPort;
	}

	public void setLocalUploadPort(int localUploadPort) {
		LocalUploadPort = localUploadPort;
	}

	public AzureConnection getStorageFirst() {
		return StorageFirst;
	}

	public void setStorageFirst(AzureConnection storageFirst) {
		StorageFirst = storageFirst;
	}

	public AzureConnection getStorageSecond() {
		return StorageSecond;
	}

	public void setStorageSecond(AzureConnection storageSecond) {
		StorageSecond = storageSecond;
	}

	public AzureConnection getEntryServer() {
		return entryServer;
	}

	public void setEntryServer(AzureConnection entryServer) {
		this.entryServer = entryServer;
	}

	public String getMasterAddrMain() {
		return MasterAddrMain;
	}

	public void setMasterAddrMain(String masterAddrMain) {
		MasterAddrMain = masterAddrMain;
	}

	public String getMasterAddrBackup() {
		return MasterAddrBackup;
	}

	public void setMasterAddrBackup(String masterAddrBackup) {
		MasterAddrBackup = masterAddrBackup;
	}

	private MasterSettings() {
		// set default value
		setLocalMessagePort(DefaultSetting.DEFAULT_MASTER_MESSAGE_PORT);
		setLocalRmiPort(DefaultSetting.DEFAULT_MASTER_RMI_PORT);
		setLocalUploadPort(DefaultSetting.DEFAULT_MASTER_UPLOAD_PORT);
		setEntryServer(new AzureConnection(DefaultSetting.eli_storageConnectionString));

		setStorageFirst(new AzureConnection(DefaultSetting.chris_storageConnectionString));
		setStorageSecond(new AzureConnection(DefaultSetting.sky_storageConnectionString));
		setMasterAddrMain(DefaultSetting.DEFAULT_MASTER_SERVER_URL);
		setMasterAddrBackup(DefaultSetting.DEFAULT_MASTER_SERVER_URL);
	}

	public static MasterSettings getInstance() {
		if (that == null) {
			that = new MasterSettings();
		}
		return that;
	}

	public boolean loadSettings() {
		boolean ans = false;
		// Read all settings from file SettingsFileName
		ObjectInputStream is = null;
		try {
			is = new ObjectInputStream(new FileInputStream(SETTING_FILENAME));

			MasterSettings temp = (MasterSettings) is.readObject();
			setLocalMessagePort(temp.getLocalMessagePort());
			setLocalRmiPort(temp.getLocalRmiPort());
			setLocalUploadPort(temp.getLocalUploadPort());
			setStorageFirst(temp.getStorageFirst());
			setStorageSecond(temp.getStorageSecond());
			setEntryServer(temp.getEntryServer());

			setMasterAddrMain(temp.getMasterAddrMain());
			setMasterAddrBackup(temp.getMasterAddrBackup());

			ans = true;
		} catch (FileNotFoundException e) {
			System.out.println("MasterSettings: Setting file not found! " + SETTING_FILENAME);
		} catch (Exception e) {
			System.err.println("ERROR:" + e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				System.out.println("ERROR" + e);
				e.printStackTrace();
			}
		}
		return ans;
	}

	public boolean saveSettings() {
		boolean ans = false;
		// Write all settings into file SettingsFileName
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(new FileOutputStream(SETTING_FILENAME));
			os.writeObject(this);
			ans = true;
		} catch (FileNotFoundException e) {
			System.out.println("ERROR" + e);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ERROR" + e);
			e.printStackTrace();
		} finally {
			try {
				if (os != null)
					os.close();
			} catch (IOException e) {
				System.out.println("ERROR" + e);
				e.printStackTrace();
			}
		}
		return ans;
	}

}
