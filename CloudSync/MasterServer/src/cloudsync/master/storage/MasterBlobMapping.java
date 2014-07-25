package cloudsync.master.storage;

import cloudsync.sharedInterface.AzureConnection;
import cloudsync.sharedInterface.ServerLocation;

import com.microsoft.azure.storage.table.TableServiceEntity;

public class MasterBlobMapping extends TableServiceEntity {

    // partition key is master address main
    // row key is master address backup

    private int masterPortMain;
    private int masterPortBackup;

    private String blobAccountMain;
    private String blobAccountBackup;

    public MasterBlobMapping(){}
    
    public MasterBlobMapping(String masterAddrMain, String masterAddrBackup,
            String blobAccountMain, String blobAccountBackup) {
        super(masterAddrMain, masterAddrBackup);
        this.blobAccountBackup = blobAccountBackup;
        this.blobAccountMain = blobAccountMain;
        
        masterPortMain = 0;
        masterPortBackup = 0;
    }

    public ServerLocation getMasterMain() {
        return new ServerLocation(getMasterAddressMain(), masterPortMain);
    }

    public ServerLocation getMasterBackup() {
        return new ServerLocation(getMasterAddressBackup(), masterPortBackup);
    }

    public AzureConnection getBlobMain() {
        return new AzureConnection(blobAccountMain);
    }

    public AzureConnection getBlobBackup() {
        return new AzureConnection(blobAccountBackup);
    }

    public String getMasterAddressMain() {
        return partitionKey;
    }

    public String getMasterAddressBackup() {
        return rowKey;
    }

    public int getMasterPortMain() {
        return masterPortMain;
    }

    public void setMasterPortMain(int masterPortMain) {
        this.masterPortMain = masterPortMain;
    }

    public int getMasterPortBackup() {
        return masterPortBackup;
    }

    public void setMasterPortBackup(int masterPortBackup) {
        this.masterPortBackup = masterPortBackup;
    }

    public String getBlobAccountMain() {
        return blobAccountMain;
    }

    public void setBlobAccountMain(String blobAccountMain) {
        this.blobAccountMain = blobAccountMain;
    }

    public String getBlobAccountBackup() {
        return blobAccountBackup;
    }

    public void setBlobAccountBackup(String blobAccountBackup) {
        this.blobAccountBackup = blobAccountBackup;
    }

}
