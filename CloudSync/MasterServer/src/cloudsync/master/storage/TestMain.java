package cloudsync.master.storage;

import cloudsync.master.MasterSettings;
import cloudsync.sharedInterface.AzureConnection;

public class TestMain {
    private static String storageConnectionString = "DefaultEndpointsProtocol=http;"
            + "AccountName=portalvhds96n2s1jyj5b5k;"
            + "AccountKey=vzJ56owCpSgvpfToqBEx2cUy6slkT7eUtWCUATe6OLWDo/GBXkbup3x8kkIHpNRdva7syOruyMq9mJfez1ZvOA==";
    
    public static void main(String[] argv) {
        MasterSettings mSettings = MasterSettings.getInstance();
        
        AzureConnection connection = new AzureConnection(storageConnectionString);
        
        mSettings.setBlobFirst(connection);
        mSettings.setBlobSecond(connection);
        mSettings.setEntryServer(connection);
        mSettings.setMasterFirst(connection);
        mSettings.setMasterSecond(connection);
        
        mSettings.saveSettings();
    }

}
