package cloudsync.master.storage;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.table.*;
import com.microsoft.azure.storage.table.TableQuery.*;

public class AccountDatabase {
    // Singleton design pattern
    private static AccountDatabase that = null;

    // temperal storage key
    // TODO: read from config
    private String storageConnectionString = "DefaultEndpointsProtocol=http;"
            + "AccountName=portalvhds96n2s1jyj5b5k;"
            + "AccountKey=vzJ56owCpSgvpfToqBEx2cUy6slkT7eUtWCUATe6OLWDo/GBXkbup3x8kkIHpNRdva7syOruyMq9mJfez1ZvOA==";
    private String tableName = "account";

    private CloudTable table;

    private AccountDatabase() {
        table = AzureStorageConnection.connectToTable(storageConnectionString,
                tableName);
    }

    public AccountDatabase(String connStr, String tableName) {
        // connect to a specific azure table
        // is used for test now
        table = AzureStorageConnection.connectToTable(connStr, tableName);
    }

    public void deleteTable() {
        // delete table, for test use only
        try {
            table.delete();
        } catch (StorageException e) {
            // TODO Auto-generated catch block
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

        return acc.getPassword().equals(password);
    }

    public boolean createAccount(String username, String password)
    // this method should be triggered by entry server in final release
    {
        AccountDBRow acc = new AccountDBRow(username, password);
        acc.setGlobalCounter(0);
        
        // TODO: read server config
        String testConn = "DefaultEndpointsProtocol=http;"
                + "AccountName=portalvhds96n2s1jyj5b5k;"
                + "AccountKey=vzJ56owCpSgvpfToqBEx2cUy6slkT7eUtWCUATe6OLWDo/GBXkbup3x8kkIHpNRdva7syOruyMq9mJfez1ZvOA==";

        acc.setMainServer(testConn);
        acc.setBackupServer(testConn);
        
        return addAccount(acc);
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

    public boolean addAccount(AccountDBRow acc) {

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
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

}
