package cloudsync.master.storage;

import java.util.ArrayList;
import java.util.Date;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableQuery.Operators;
import com.microsoft.azure.storage.table.TableQuery.QueryComparisons;
import com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource_Xerces;

import cloudsync.sharedInterface.Blobdata;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.Metadata.STATUS;
import cloudsync.sharedInterface.AzureConnection;

public class Metadatabase {

    // TODO: replace default server with config
    private static String storageConnectionString = "DefaultEndpointsProtocol=http;"
            + "AccountName=portalvhds96n2s1jyj5b5k;"
            + "AccountKey=vzJ56owCpSgvpfToqBEx2cUy6slkT7eUtWCUATe6OLWDo/GBXkbup3x8kkIHpNRdva7syOruyMq9mJfez1ZvOA==";
    private static String tableName = "meta";

    private CloudTable table;

    public static Metadata acceptFileUpdate(String username,
            Metadata incompleteMetadata, Blobdata blobdata) {
        // if NO conflict, save the Metadata in local database & save Blobdata
        // in Blob Server, then update FileMetadata and return
        // if conflict, reject this update and return error reason in
        // FileMetadata. Then let client to rename the file and upate again
        // Future Plan: Maybe the Blobdata can be saved to avoid upload again,
        // but need a mechanism to collect garbage if client does not update
        // again.

        // get account
        AccountDBRow account = AccountDatabase.getInstance().getAccount(
                username);

        Metadatabase db = getServer(username);

        // verify
        if (db.hasConflict(incompleteMetadata, username)) {
            incompleteMetadata.status = STATUS.CONFLICT;
            return incompleteMetadata;
        }

        // generate complete metadata
        incompleteMetadata.globalCounter = account.getGlobalCounter() + 1;
        // TODO: generate key, read blob servers;
        incompleteMetadata.timestamp = new Date();
        incompleteMetadata.blobKey = "some key";
        incompleteMetadata.blobServer = new AzureConnection(
                storageConnectionString);
        incompleteMetadata.blobBackup = new AzureConnection(
                storageConnectionString);

        MetadataDBRow metaRow = new MetadataDBRow(username, incompleteMetadata);

        // update meta data
        Metadatabase main = new Metadatabase(account.getMainServer(), tableName);
        main.addRecord(metaRow);
        Metadatabase backup = new Metadatabase(account.getBackupServer(),
                tableName);
        backup.addRecord(metaRow);
        
        // update blob

        // update account
        account.setGlobalCounter(account.getGlobalCounter() + 1);
        AccountDatabase.getInstance().updateAccount(account);

        // fill metadata
        return incompleteMetadata;
    }

    public static ArrayList<Metadata> getCompleteMetadata(String username,
            long sinceCounter) {

        Metadatabase db = getServer(username);

        Iterable<MetadataDBRow> rows = db.retrieveRecordSince(username,
                sinceCounter);
        ArrayList<Metadata> result = new ArrayList<Metadata>();
        for (MetadataDBRow row : rows) {
            result.add(row.toMetadata());
        }
        return result;
    }

    private static Metadatabase getServer(String username) {
        AccountDBRow account = AccountDatabase.getInstance().getAccount(
                username);
        Metadatabase server = new Metadatabase(account.getMainServer(),
                tableName);
        return server;
    }

    public Metadatabase(String connString, String tableName) {
        table = AzureStorageConnection.connectToTable(connString, tableName);
    }

    public boolean addRecord(MetadataDBRow meta) {
        TableOperation insert = TableOperation.insert(meta);
        try {
            table.execute(insert);
            System.out.println("Insert complete");
            return true;
        } catch (StorageException e) {
            e.printStackTrace();
            return false;
        }
    }

    public MetadataDBRow retrieveRecord(String username, long counter) {
        TableOperation retrieveRecord = TableOperation.retrieve(username,
                String.valueOf(counter), MetadataDBRow.class);
        try {
            MetadataDBRow meta = table.execute(retrieveRecord)
                    .getResultAsType();
            return meta;
        } catch (StorageException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Iterable<MetadataDBRow> retrieveRecordSince(String username,
            long counter) {
        String partitionFilter = TableQuery.generateFilterCondition(
                "PartitionKey", QueryComparisons.EQUAL, username);

        String rowFilter = TableQuery.generateFilterCondition("GlobalCounter",
                QueryComparisons.GREATER_THAN_OR_EQUAL, counter);

        String combinedFilter = TableQuery.combineFilters(partitionFilter,
                Operators.AND, rowFilter);

        TableQuery<MetadataDBRow> rangeQuery = TableQuery.from(
                MetadataDBRow.class).where(combinedFilter);

        System.out.println(combinedFilter);

        Iterable<MetadataDBRow> result = table.execute(rangeQuery);
        return result;
    }

    public boolean hasConflict(Metadata meta, String username) {
        // return true if there is conflict
        MetadataDBRow last = getLast(meta, username);

        if (meta.parent == 0) { // new file
            if (last != null) { // conflict existing file
                return true;
            } else { // no conflict
                return false;
            }
        }

        return (last.getGlobalCounter() != meta.parent);
    }

    private MetadataDBRow getLast(Metadata meta, String username) {
        String partitionFilter = TableQuery.generateFilterCondition(
                "PartitionKey", QueryComparisons.EQUAL, username);
        String fileFilter = TableQuery.generateFilterCondition("Filename",
                QueryComparisons.EQUAL, meta.filename);
        String statusFilterString = TableQuery.generateFilterCondition(
                "Status", QueryComparisons.EQUAL, "LAST");

        String combinedFilter = TableQuery.combineFilters(partitionFilter,
                Operators.AND, fileFilter);
        combinedFilter = TableQuery.combineFilters(combinedFilter,
                Operators.AND, statusFilterString);
        System.out.println(combinedFilter);

        TableQuery<MetadataDBRow> rangeQuery = TableQuery.from(
                MetadataDBRow.class).where(combinedFilter);

        Iterable<MetadataDBRow> result = table.execute(rangeQuery);

        for (MetadataDBRow tmp : result) {
            // if exist return it
            return tmp;
        }

        return null; // not exist
    }

}
