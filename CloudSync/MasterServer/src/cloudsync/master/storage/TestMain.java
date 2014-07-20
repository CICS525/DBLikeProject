package cloudsync.master.storage;

import cloudsync.master.MasterSettings;
import cloudsync.sharedInterface.AzureConnection;

// test main function to initialize the config file

/*
public class TestMain {
    private static String conn1 = "DefaultEndpointsProtocol=http;"
            + "AccountName=portalvhds96n2s1jyj5b5k;"
            + "AccountKey=vzJ56owCpSgvpfToqBEx2cUy6slkT7eUtWCUATe6OLWDo/GBXkbup3x8kkIHpNRdva7syOruyMq9mJfez1ZvOA==";
    
    private static String conn2 = "DefaultEndpointsProtocol=http;"
            + "AccountName=portalvhds0c37fqp3tw964;"
            + "AccountKey=n8uEGZrIUoMcD4J7WgbcZyk6gMZ0hV9mtn83jXtMpWwLjFAWlPSZizDdZiWmeLjJMetOvrMko1dwoQnaUQTSLQ==";
    
    public static void main(String[] argv) {
        MasterSettings mSettings = MasterSettings.getInstance();
        
        AzureConnection connection1 = new AzureConnection(conn1);
        AzureConnection connection2 = new AzureConnection(conn2);
        
        mSettings.setBlobFirst(connection1);
        mSettings.setBlobSecond(connection2);
        mSettings.setEntryServer(connection1);
        mSettings.setMasterFirst(connection1);
        mSettings.setMasterSecond(connection2);
        
        mSettings.saveSettings();
        
        mSettings.loadSettings();
        
        System.out.println(mSettings.getBlobSecond().toString());
        
    }

}
*/

