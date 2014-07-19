package cloudsync.master.storage;

import com.microsoft.azure.storage.table.*;

public class AccountDBRow extends TableServiceEntity{
    
    private String password;
    private long globalCounter;
    private String mainServer;
    private String backupServer;
    
    public AccountDBRow() {}
    
    public AccountDBRow(String username, String password) {
        super("account", username);
        this.password = password;
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

    public String getBackupServer() {
        return backupServer;
    }

    public void setBackupServer(String backupServer) {
        this.backupServer = backupServer;
    }

    public String getMainServer() {
        return mainServer;
    }

    public void setMainServer(String mainServer) {
        this.mainServer = mainServer;
    }


}
