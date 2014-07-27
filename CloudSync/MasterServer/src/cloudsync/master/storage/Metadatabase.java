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

	private CloudTable	table;

	public static Metadata acceptFileUpdate(String username, Metadata incompleteMetadata, String fileToUpload) {
		// if NO conflict, save the Metadata in local database & save Blobdata
		// in Blob Server, then update FileMetadata and return
		// if conflict, reject this update and return error reason in
		// FileMetadata. Then let client to rename the file and upate again
		// Future Plan: Maybe the Blobdata can be saved to avoid upload again,
		// but need a mechanism to collect garbage if client does not update
		// again.

		if (incompleteMetadata.status != STATUS.LAST && incompleteMetadata.status != STATUS.DELETE) {
			incompleteMetadata.status = STATUS.ERROR;
			return incompleteMetadata;
		}

		// get account
		AccountDBRow account = AccountDatabase.getInstance().getAccount(username);
		Metadatabase db = getServer();

		// verify
		MetadataDBRow last = db.getLast(incompleteMetadata.basename, username);
		if (db.hasConflict(last, incompleteMetadata)) {
			incompleteMetadata.status = STATUS.CONFLICT;
			return incompleteMetadata;
		}

		// generate complete metadata
		incompleteMetadata.globalCounter = account.getGlobalCounter() + 1;
		incompleteMetadata.timestamp = new Date();

		String strToSHA = incompleteMetadata.basename + incompleteMetadata.timestamp.toString();
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
		// TODO retrieve master servers by querying table
		Metadatabase main = new Metadatabase(settings.getMasterFirst().toString(), DefaultSetting.METADATA_TABLE_NAME);
		Metadatabase backup = new Metadatabase(settings.getMasterSecond().toString(), DefaultSetting.METADATA_TABLE_NAME);

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

	public static ArrayList<Metadata> getCompleteMetadata(String username, long sinceCounter) {

		Metadatabase db = getServer();

		Iterable<MetadataDBRow> rows = db.retrieveRecordSince(username, sinceCounter);
		ArrayList<Metadata> result = new ArrayList<Metadata>();
		for (MetadataDBRow row : rows) {
			Metadata m = row.toMetadata();
			if(result.contains(m))
				result.remove(m);	//remove old metadata
			result.add(m);			//only keep the last metadata for a file
		}
		return result;
	}

	private Metadatabase(String connString, String tableName) {
		table = AzureStorageConnection.connectToTable(connString, tableName);
	}

	private Metadatabase() {
	}

	private static Metadatabase getServer() {
		Metadatabase server = new Metadatabase(MasterSettings.getInstance().getMasterFirst().toString(), DefaultSetting.METADATA_TABLE_NAME);
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

	private Iterable<MetadataDBRow> retrieveRecordSince(String username, long counter) {
		String partitionFilter = TableQuery.generateFilterCondition("PartitionKey", QueryComparisons.EQUAL, username);

		String rowFilter = TableQuery.generateFilterCondition("GlobalCounter", QueryComparisons.GREATER_THAN_OR_EQUAL, counter);

		String combinedFilter = TableQuery.combineFilters(partitionFilter, Operators.AND, rowFilter);

		TableQuery<MetadataDBRow> rangeQuery = TableQuery.from(MetadataDBRow.class).where(combinedFilter);

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
		String partitionFilter = TableQuery.generateFilterCondition("PartitionKey", QueryComparisons.EQUAL, username);
		String fileFilter = TableQuery.generateFilterCondition("Filename", QueryComparisons.EQUAL, filename);
		String statusFilterString = TableQuery.generateFilterCondition("Status", QueryComparisons.EQUAL, "LAST");

		String combinedFilter = TableQuery.combineFilters(partitionFilter, Operators.AND, fileFilter);
		combinedFilter = TableQuery.combineFilters(combinedFilter, Operators.AND, statusFilterString);
		System.out.println(combinedFilter);

		TableQuery<MetadataDBRow> rangeQuery = TableQuery.from(MetadataDBRow.class).where(combinedFilter);

		Iterable<MetadataDBRow> result = table.execute(rangeQuery);

		for (MetadataDBRow tmp : result) {
			// if exist return it
			return tmp;
		}

		return null; // not exist
	}

}
