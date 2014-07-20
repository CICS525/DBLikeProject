package cloudsync.master.storage;

import cloudsync.master.MasterSettings;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.table.*;

public class AccountDatabase {
    // Singleton design pattern
    private static AccountDatabase that = null;

    private final String tableName = "account";

    private CloudTable table;

    private AccountDatabase() {
        String conn = MasterSettings.getInstance().getEntryServer().toString();
        table = AzureStorageConnection.connectToTable(conn, tableName);
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

        return acc.getPassword().equals(password);
    }

    public boolean createAccount(String username, String password)
    // this method should be triggered by entry server in final release
    {
        AccountDBRow acc = new AccountDBRow(username, password);
        acc.setGlobalCounter(0);

        MasterSettings settings = MasterSettings.getInstance();

        acc.setMainServer(settings.getMasterFirst().toString());
        acc.setBackupServer(settings.getMasterSecond().toString());

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
            e.printStackTrace();
            return null;
        }
    }

}
