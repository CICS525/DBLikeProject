package cloudsync.master.storage;

import cloudsync.master.MasterSettings;
import cloudsync.sharedInterface.DefaultSetting;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableQuery.Operators;
import com.microsoft.azure.storage.table.TableQuery.QueryComparisons;

public class MasterBlobMapDB {
    private static MasterBlobMapDB that;

    private CloudTable table;

    private MasterBlobMapDB() {
        String conn = MasterSettings.getInstance().getEntryServer().toString();
        table = AzureStorageConnection.connectToTable(conn,
                DefaultSetting.MASTER_BLOB_TABLE_NAME);
    }

    public static MasterBlobMapDB getInstance() {
        if (that == null) {
            that = new MasterBlobMapDB();
        }

        return that;
    }

    public boolean addRow(MasterBlobMapping mapping) {
        TableOperation insert = TableOperation.insertOrReplace(mapping);
        try {
            table.execute(insert);
            return true;
        } catch (StorageException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addRow(String masterAddrMain, String masterAddrBackup,
            String blobAccountMain, String blobAccountBackup) {
        MasterBlobMapping mapping = new MasterBlobMapping(masterAddrMain,
                masterAddrBackup, blobAccountMain, blobAccountBackup);
        return addRow(mapping);
    }
    
    public MasterBlobMapping query(String serverAddress) {
        String partitionFilter = TableQuery.generateFilterCondition(
                "PartitionKey", QueryComparisons.EQUAL, serverAddress);
        String rowFilter = TableQuery.generateFilterCondition("RowKey",
                QueryComparisons.EQUAL, serverAddress);

        String combinedFilter = TableQuery.combineFilters(partitionFilter,
                Operators.OR, rowFilter);
        System.out.println(combinedFilter);

        TableQuery<MasterBlobMapping> rangeQuery = TableQuery.from(
                MasterBlobMapping.class).where(combinedFilter);

        Iterable<MasterBlobMapping> result = table.execute(rangeQuery);

        for (MasterBlobMapping tmp : result) {
            // if exist return it
            return tmp;
        }

        return null; // not exist
    }
}
