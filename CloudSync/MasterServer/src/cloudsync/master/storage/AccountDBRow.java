package cloudsync.master.storage;

import cloudsync.master.MasterSettings;
import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.ServerLocation;

import com.microsoft.azure.storage.table.*;

public class AccountDBRow extends TableServiceEntity {
    
    public static final int USING_NONE = 0;
    public static final int USING_MAIN = 1;
    public static final int USING_BACKUP = 2;

    private String password;
    private long globalCounter;
    private String masterAddrMain;
    private int masterPortMain;
    private String masterAddrBackup;
    private int masterPortBackup;
    
    private int connectionCount;

    private int serverflag;

    public AccountDBRow() {
    }

    public AccountDBRow(String username, String password) {
        super("account", username);
        this.password = password;
        
        this.setGlobalCounter(0);
        this.setServerflag(AccountDBRow.USING_NONE);
        this.setConnectionCount(0);
        
        this.setMasterAddrMain  (getPrimaryMasterServerAddressByUsername(username));
        this.setMasterAddrBackup(getBackupMasterServerAddressByUsername (username));
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    String getUsername() {
        return rowKey;
    }

    void setUsername(String username) {
        this.rowKey = username;
    }

    public long getGlobalCounter() {
        return globalCounter;
    }

    public void setGlobalCounter(long globalCounter) {
        this.globalCounter = globalCounter;
    }

    public String getMasterAddrMain() {
        return masterAddrMain;
    }

    public void setMasterAddrMain(String masterAddrMain) {
        this.masterAddrMain = masterAddrMain;
    }

    public String getMasterAddrBackup() {
        return masterAddrBackup;
    }

    public void setMasterAddrBackup(String masterAddrBackup) {
        this.masterAddrBackup = masterAddrBackup;
    }

    public int getServerflag() {
        return serverflag;
    }

    public void setServerflag(int serverflag) {
        this.serverflag = serverflag;
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
    
    public ServerLocation getMainServer() {
        return new ServerLocation(masterAddrMain, masterPortMain);
    }
    
    public ServerLocation getBackupServer() {
        return new ServerLocation(masterAddrBackup, masterPortBackup);
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    private String getPrimaryMasterServerAddressByUsername(String username){
        //MasterSettings settings = MasterSettings.getInstance();
    	//String address = settings.getMasterAddrMain();
    	return DefaultSetting.VM_ADDR_CHRIS;
    }

    private String getBackupMasterServerAddressByUsername(String username){
        //MasterSettings settings = MasterSettings.getInstance();
    	//String address = settings.getMasterAddrBackup();
    	return DefaultSetting.VM_ADDR_SKY;
    }
}
