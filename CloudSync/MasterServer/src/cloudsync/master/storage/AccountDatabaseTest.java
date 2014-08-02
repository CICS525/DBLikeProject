package cloudsync.master.storage;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import cloudsync.master.MasterSettings;
import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.ServerLocation;

public class AccountDatabaseTest {
    private static String storageConnectionString = DefaultSetting.chris_storageConnectionString;

    private static String tableName = "accountTest";

    private static AccountDatabase db;

    private static String username = "testUser";
    private static String password = "testPass";

    @BeforeClass
    public static void setUpTable() {
        db = new AccountDatabase(storageConnectionString, tableName);
        db.createAccount(username, password);
    }

    @AfterClass
    public static void deleteTable() {
        db.deleteTable();
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
        System.out.println(server.url);
        assertEquals(server.url, DefaultSetting.VM_ADDR_CHRIS);
        
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
