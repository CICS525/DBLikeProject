package test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import cloudsync.master.MasterSettings;
import cloudsync.master.storage.AccountDBRow;
import cloudsync.master.storage.AccountDatabase;
import cloudsync.sharedInterface.ServerLocation;

public class AccountDatabaseTest {
    private static String storageConnectionString = "DefaultEndpointsProtocol=http;"
            + "AccountName=portalvhds96n2s1jyj5b5k;"
            + "AccountKey=vzJ56owCpSgvpfToqBEx2cUy6slkT7eUtWCUATe6OLWDo/GBXkbup3x8kkIHpNRdva7syOruyMq9mJfez1ZvOA==";

    private static String tableName = "accountTest";

    private static AccountDatabase db;

    private static String username = "testUser";
    private static String password = "testPass";

    @BeforeClass
    public static void setUpTable() {
        //db = new AccountDatabase(storageConnectionString, tableName);
        db.createAccount(username, password);
    }

    @AfterClass
    public static void deleteTable() {
        //db.deleteTable();
    }

    @Test
    public void testGetAccount() {
        AccountDBRow row = db.getAccount(username);

        assertNotNull(row);
        assertEquals(row.getPassword(), password);
        assertEquals(row.getGlobalCounter(), 0);
    }

    @Test
    public void testLogin() {
        assertTrue(db.login(username, password));
        
        assertFalse(db.login(username, "wrongpass"));
    }

    @Test
    public void testUpdate() {
        AccountDBRow row = db.getAccount(username);
        assertNotNull(row);
        
        row.setGlobalCounter(10);
        assertTrue(db.updateAccount(row));
        
        AccountDBRow newRow = db.getAccount(username);
        assertNotNull(newRow);
        assertEquals(newRow.getGlobalCounter(), 10);
        
        row.setGlobalCounter(0);
        db.updateAccount(row);
    }
    
    @Test
    public void testGetServerAndLogOut() {
        ServerLocation server = db.getServerLocation(username);
        assertEquals(server.url, MasterSettings.getInstance().getMasterAddrMain());
        
        AccountDBRow row = db.getAccount(username);
        assertEquals(row.getConnectionCount(), 1);
        assertEquals(row.getServerflag(), AccountDBRow.USING_MAIN);
        
        db.getServerLocation(username);
        row = db.getAccount(username);
        assertEquals(row.getConnectionCount(), 2);
        assertEquals(row.getServerflag(), AccountDBRow.USING_MAIN);
        
        db.logout(username);
        row = db.getAccount(username);
        assertEquals(row.getConnectionCount(), 1);
        assertEquals(row.getServerflag(), AccountDBRow.USING_MAIN);
        
        db.logout(username);
        row = db.getAccount(username);
        assertEquals(row.getConnectionCount(), 0);
        assertEquals(row.getServerflag(), AccountDBRow.USING_NONE);
        
        System.out.println("test finished");
        
    }

}
