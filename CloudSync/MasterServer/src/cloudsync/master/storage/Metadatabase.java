package cloudsync.master.storage;

import java.util.ArrayList;
import java.util.Date;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableQuery.Operators;
import com.microsoft.azure.storage.table.TableQuery.QueryComparisons;

import cloudsync.master.MasterSettings;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.Metadata.STATUS;
import cloudsync.sharedInterface.SessionBlob;
import cloudsync.sharedInterface.DefaultSetting;

public class Metadatabase {

    private CloudTable table;

    public static Metadata acceptFileUpdate(String username,
            Metadata incompleteMetadata, String fileToUpload) {
        // if NO conflict, save the Metadata in local database & save Blobdata
        // in Blob Server, then update FileMetadata and return
        // if conflict, reject this update and return error reason in
        // FileMetadata. Then let client to rename the file and upate again
        // Future Plan: Maybe the Blobdata can be saved to avoid upload again,
        // but need a mechanism to collect garbage if client does not update
        // again.

        if (incompleteMetadata.status != STATUS.LAST
                && incompleteMetadata.status != STATUS.DELETE) {
            incompleteMetadata.status = STATUS.ERROR;
            return incompleteMetadata;
        }

        // get account
        AccountDBRow account = AccountDatabase.getInstance().getAccount(
                username);
        Metadatabase db = getServer();

        // verify
        MetadataDBRow last = db.getLast(incompleteMetadata.filename, username);
        if (db.hasConflict(last, incompleteMetadata)) {
            incompleteMetadata.status = STATUS.CONFLICT;
            return incompleteMetadata;
        }

        // generate complete metadata
        incompleteMetadata.globalCounter = account.getGlobalCounter() + 1;
        incompleteMetadata.timestamp = new Date();

        String strToSHA = incompleteMetadata.filename
                + incompleteMetadata.timestamp.toString();
        incompleteMetadata.blobKey = SHA.getSha1(strToSHA);

        MasterSettings settings = MasterSettings.getInstance();
        incompleteMetadata.blobServer = settings.getBlobFirst();
        incompleteMetadata.blobBackup = settings.getBlobSecond();
        Metadata completeMetadata = incompleteMetadata;

        MetadataDBRow metaRow = new MetadataDBRow(username, completeMetadata);

        /*
         * Do tables update: add new metadata, update last metadata, add blob,
         * update account's global count
         */

        // update account
        account.setGlobalCounter(account.getGlobalCounter() + 1);
        AccountDatabase.getInstance().updateAccount(account);

        // update meta data
        Metadatabase main = new Metadatabase(settings.getMasterAddrMain(),
                DefaultSetting.metadatabase_table_name);
        Metadatabase backup = new Metadatabase(settings.getMasterAddrBackup(),
                DefaultSetting.metadatabase_table_name);

        main.addRecord(metaRow);
        backup.addRecord(metaRow);

        // update previous last
        if (last != null) {
            last.setStatus(STATUS.HISTORY.toString());
            main.updateRecord(last);
            backup.updateRecord(last);
        }

        // update blob
        SessionBlob sb = new SessionBlob();
        if (completeMetadata.status == STATUS.LAST) {
            boolean b = sb.uploadFile(fileToUpload, completeMetadata);
            System.out.println("Update " + b);
        }
        /*
         * Do nothing when status is DELETE else {
         * sb.deleteFile(completeMetadata); }
         */

        return completeMetadata;
    }

    public static ArrayList<Metadata> getCompleteMetadata(String username,
            long sinceCounter) {

        Metadatabase db = getServer();

        Iterable<MetadataDBRow> rows = db.retrieveRecordSince(username,
                sinceCounter);
        ArrayList<Metadata> result = new ArrayList<Metadata>();
        for (MetadataDBRow row : rows) {
            result.add(row.toMetadata());
        }
        return result;
    }

    private Metadatabase(String connString, String tableName) {
        table = AzureStorageConnection.connectToTable(connString, tableName);
    }

    private Metadatabase() {
    }

    private static Metadatabase getServer() {
        Metadatabase server = new Metadatabase(MasterSettings.getInstance()
                .getMasterFirst().toString(),
                DefaultSetting.metadatabase_table_name);
        return server;
    }

    private boolean addRecord(MetadataDBRow meta) {
        TableOperation insert = TableOperation.insert(meta);
        try {
            table.execute(insert);
            return true;
        } catch (StorageException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateRecord(MetadataDBRow meta) {
        TableOperation update = TableOperation.insertOrReplace(meta);
        try {
            table.execute(update);
            System.out.println("update complete");
            return true;
        } catch (StorageException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * private MetadataDBRow retrieveRecord(String username, long counter) {
     * TableOperation retrieveRecord = TableOperation.retrieve(username,
     * String.valueOf(counter), MetadataDBRow.class); try { MetadataDBRow meta =
     * table.execute(retrieveRecord) .getResultAsType(); return meta; } catch
     * (StorageException e) { e.printStackTrace(); return null; } }
     */

    private Iterable<MetadataDBRow> retrieveRecordSince(String username,
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

    private boolean hasConflict(MetadataDBRow last, Metadata meta) {
        if (meta.parent == 0) { // new file
            if (last != null) { // conflict existing file
                return true;
            } else { // no conflict
                return false;
            }
        }
        return (last.getGlobalCounter() != meta.parent);
    }

    private MetadataDBRow getLast(String filename, String username) {
        String partitionFilter = TableQuery.generateFilterCondition(
                "PartitionKey", QueryComparisons.EQUAL, username);
        String fileFilter = TableQuery.generateFilterCondition("Filename",
                QueryComparisons.EQUAL, filename);
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

    static boolean testRecord() {
        Metadatabase db = new Metadatabase();
        db.table = AzureStorageConnection.connectToTable(MasterSettings
                .getInstance().getMasterFirst().toString(), "testmeta");

        Metadata meta = new Metadata();

        meta.parent = 0;
        meta.status = STATUS.HISTORY;
        meta.blobServer = MasterSettings.getInstance().getBlobFirst();
        meta.blobBackup = MasterSettings.getInstance().getBlobSecond();

        for (int i = 0; i < 5; i++) {
            meta.filename = "testfile" + String.valueOf(i);
            meta.globalCounter = 1 + i;

            meta.timestamp = new Date();
            meta.blobKey = SHA.getSha1(meta.filename
                    + meta.timestamp.toString());
            MetadataDBRow metadb = new MetadataDBRow("testuser", meta);

            db.addRecord(metadb);
        }

        meta.parent = 5;
        meta.globalCounter = 6;
        meta.status = STATUS.LAST;
        meta.blobServer = MasterSettings.getInstance().getBlobFirst();
        meta.blobBackup = MasterSettings.getInstance().getBlobSecond();

        MetadataDBRow metadb = new MetadataDBRow("testuser", meta);

        db.addRecord(metadb);

        MetadataDBRow last = db.getLast("testfile4", "testuser");

        if (last == null) {
            try {
                db.table.delete();
            } catch (StorageException e) {
                e.printStackTrace();
            }
            return false;
        }

        meta.parent = 5;
        meta.globalCounter = 7;

        last.setStatus(STATUS.HISTORY.toString());
        db.updateRecord(last);

        Iterable<MetadataDBRow> ma = db.retrieveRecordSince("testuser", 1);
        for (MetadataDBRow row : ma) {
            System.out.println(row.getFilename() + row.getRowKey());
        }

        try {
            db.table.delete();
        } catch (StorageException e) {
            e.printStackTrace();
        }
        return true;
    }

}
