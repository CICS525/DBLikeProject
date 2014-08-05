package cloudsync.master.storage;

import java.io.IOException;
import java.net.Socket;

import cloudsync.master.MasterSettings;
import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.ServerLocation;
import cloudsync.sharedInterface.SocketStream;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableOperation;

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
        
        boolean mainResult = checkMasterServer(acc.getMasterAddrMain());
        boolean backupResult = checkMasterServer(acc.getMasterAddrMain());
        
        // both server down
        if (!mainResult && !backupResult) {
            return null;
        }
        
        // both server working
        if (mainResult && backupResult ) {
            int flag = acc.getServerflag();
            if (flag == AccountDBRow.USING_NONE) {
                flag = selectServer();
                acc.setServerflag(flag);
                acc.setConnectionCount(1);
            } else {
                acc.setConnectionCount(acc.getConnectionCount() + 1);
            }
        } else {
            // only main server working
            if (mainResult) {
                int flag = acc.getServerflag();
                if (flag != AccountDBRow.USING_MAIN) {
                    flag = AccountDBRow.USING_MAIN;
                    acc.setServerflag(flag);
                    acc.setConnectionCount(1);
                } else {
                    acc.setConnectionCount(acc.getConnectionCount() + 1);
                }
            }
            
            // only backup server working
            if (backupResult) {
                int flag = acc.getServerflag();
                if (flag != AccountDBRow.USING_BACKUP) {
                    flag = AccountDBRow.USING_BACKUP;
                    acc.setServerflag(flag);
                    acc.setConnectionCount(1);
                } else {
                    acc.setConnectionCount(acc.getConnectionCount() + 1);
                }
            }        	
        }

        updateAccount(acc);
        
        // return based on flag
        if (acc.getServerflag() == AccountDBRow.USING_MAIN) {
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
            if (count < 0) {
                count = 0;
            }
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

	public boolean checkMasterServer(String masterAddress){
		Socket socket = null;
		try {
			socket = new Socket(masterAddress, DefaultSetting.DEFAULT_MASTER_MESSAGE_PORT);
			socket.setSoTimeout(500);
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("Can not connect to: " + masterAddress);
			return false;
		}
		SocketStream socketStream = new SocketStream();
		if(socket!=null){
			socketStream.initStream(socket);
			socketStream.deinitStream();
			return true;
		}else{
			return false;
		}
	}
}
