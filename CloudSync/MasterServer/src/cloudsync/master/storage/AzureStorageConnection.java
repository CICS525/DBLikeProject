package cloudsync.master.storage;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.table.*;

public class AzureStorageConnection {
    public static CloudTable connectToTable(String connStr, String tableName) {
        CloudTable table;
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(connStr);
            CloudTableClient tableClient = account.createCloudTableClient();
            table = new CloudTable(tableName, tableClient);
            table.createIfNotExists();
            return table;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
