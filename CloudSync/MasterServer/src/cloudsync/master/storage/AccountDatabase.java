package cloudsync.master.storage;

import cloudsync.master.MasterSettings;
import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.ServerLocation;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.table.*;

public class AccountDatabase {
    // Singleton design pattern
    private static AccountDatabase that = null;

    private CloudTable table;

    private AccountDatabase() {
        String conn = MasterSettings.getInstance().getEntryServer().toString();
        table = AzureStorageConnection.connectToTable(conn,
                DefaultSetting.ACCOUNT_TABLE_NAME);
    }

    AccountDatabase(String connStr, String tableName) {
        // connect to a specific azure table
        // is used for test now
        table = AzureStorageConnection.connectToTable(connStr, tableName);
    }

    void deleteTable() {
        // delete table, for test use only
        try {
            table.delete();
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    public static AccountDatabase getInstance() {
        if (that == null) {
            that = new AccountDatabase();
        }
        return that;
    }

    public boolean login(String username, String password) {

        AccountDBRow acc = getAccount(username);
        if (acc == null) {
            return false;
        }
        
        boolean result = acc.getPassword().equals(password);
        if(result)
        	System.out.println("AccountDatabase:login(" + username + "," + password + ")=" + result);
        else
        	System.out.println("AccountDatabase:login(" + username + "," + acc.getPassword() + ")=" + result + " input password:" + password);
        
        return result;
    }
    
    public synchronized ServerLocation getServerLocation(String username) {
        // get current server flag, set server flag if current is USING_NONE
        AccountDBRow acc = getAccount(username);
        int flag = acc.getServerflag();
        if (flag == AccountDBRow.USING_NONE) {
            flag = selectServer();
            acc.setServerflag(flag);
            acc.setConnectionCount(1);
        } else {
            acc.setConnectionCount(acc.getConnectionCount() + 1);
        }
        
        updateAccount(acc);
        
        if (flag == AccountDBRow.USING_MAIN) {
            return acc.getMainServer();
        } else {
            return acc.getBackupServer();
        }
    }
    
    public synchronized void logout(String username) {
        // logout, if it is the last user, set flag to NONE
        AccountDBRow acc = getAccount(username);
        if( acc!=null ){
            int count = acc.getConnectionCount() - 1;
            acc.setConnectionCount(count);
            
            if (count == 0) {
                acc.setServerflag(AccountDBRow.USING_NONE);
            }
            
            updateAccount(acc);
        }
    }
    
    private int selectServer() {
        // select a server from main and backup, may be expanded
        return AccountDBRow.USING_MAIN;
    }

    public boolean createAccount(String username, String password)
    // this method should be triggered by entry server in final release
    {
        AccountDBRow acc = new AccountDBRow(username, password);
        boolean ans = addAccount(acc);
        System.out.println("AccountDatabase:createAccount(" + username + "," + password + ")=" + ans);
        return ans;
    }

    public boolean updateAccount(AccountDBRow acc) {
        AccountDBRow existing = getAccount(acc.getRowKey());
        if (existing == null) {
            // account not exist
            return false;
        }
        TableOperation insert = TableOperation.insertOrReplace(acc);
        try {
            table.execute(insert);
            return true;
        } catch (StorageException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean addAccount(AccountDBRow acc) {

        AccountDBRow existing = getAccount(acc.getRowKey());
        if (existing != null) {
            // account already exist
            return false;
        }

        TableOperation insert = TableOperation.insert(acc);
        try {
            table.execute(insert);
            System.out.println("Insert complete");
            return true;
        } catch (StorageException e) {
            e.printStackTrace();
            return false;
        }
    }

    public AccountDBRow getAccount(String username) {
        TableOperation retrieveUser = TableOperation.retrieve("account",
                username, AccountDBRow.class);

        try {
            AccountDBRow acc = table.execute(retrieveUser).getResultAsType();
            return acc;
        } catch (StorageException e) {
            e.printStackTrace();
            return null;
        }
    }

}
