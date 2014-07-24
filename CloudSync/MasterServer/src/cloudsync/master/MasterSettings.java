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
    private static final long serialVersionUID = -1026245079078573719L;

    private static MasterSettings that = null;

    private int LocalMessagePort = 0;
    private int LocalRmiPort = 0;
    private int LocalUploadPort = 0;
    private AzureConnection MasterFirst = null;
    private AzureConnection MasterSecond = null;
    private AzureConnection BlobFirst = null;
    private AzureConnection BlobSecond = null;
    private AzureConnection entryServer = null;

    private final String fSettingsFileName = ".MasterSettings.settings";

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

	public AzureConnection getBlobFirst() {
        return BlobFirst;
    }

    public AzureConnection getMasterFirst() {
        return MasterFirst;
    }

    public void setMasterFirst(AzureConnection masterFirst) {
        MasterFirst = masterFirst;
    }

    public AzureConnection getMasterSecond() {
        return MasterSecond;
    }

    public void setMasterSecond(AzureConnection masterSecond) {
        MasterSecond = masterSecond;
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

    public AzureConnection getEntryServer() {
        return entryServer;
    }

    public void setEntryServer(AzureConnection entryServer) {
        this.entryServer = entryServer;
    }

    private MasterSettings() {
    	//set default value
    	setLocalMessagePort( DefaultSetting.DEFAULT_MASTER_MESSAGE_PORT );
    	setLocalRmiPort( DefaultSetting.DEFAULT_MASTER_RMI_PORT );
    	setLocalUploadPort( DefaultSetting.DEFAULT_MASTER_UPLOAD_PORT );
        setBlobFirst( new AzureConnection(DefaultSetting.chris_storageConnectionString) );
        setBlobSecond( new AzureConnection(DefaultSetting.sky_storageConnectionString) );
        setMasterFirst( new AzureConnection(DefaultSetting.chris_storageConnectionString) );
        setMasterSecond( new AzureConnection(DefaultSetting.sky_storageConnectionString) );
        setEntryServer( new AzureConnection(DefaultSetting.chris_storageConnectionString) );
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
            is = new ObjectInputStream(new FileInputStream(fSettingsFileName));

            MasterSettings temp = (MasterSettings) is.readObject();
            setLocalMessagePort(temp.getLocalMessagePort());
        	setLocalRmiPort( temp.getLocalRmiPort() );
        	setLocalUploadPort( temp.getLocalUploadPort() );
            setMasterFirst(temp.getMasterFirst());
            setMasterSecond(temp.getMasterSecond());
            setBlobFirst(temp.getBlobFirst());
            setBlobSecond(temp.getBlobSecond());
            setEntryServer(temp.getEntryServer());

            ans = true;
        } catch (Exception e) {
            System.out.println("ERROR" + e);
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
            os = new ObjectOutputStream(new FileOutputStream(fSettingsFileName));
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
