package cloudsync.master.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Collections;

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

	public static synchronized Metadata acceptFileUpdate(String username, Metadata incompleteMetadata, String fileToUpload) {
		// if NO conflict, save the Metadata in local database & save Blobdata
		// in Blob Server, then update FileMetadata and return
		// if conflict, reject this update and return error reason in
		// FileMetadata. Then let client to rename the file and update again
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
		if (db.hasError(last, incompleteMetadata)) {
			incompleteMetadata.status = STATUS.ERROR;
			return incompleteMetadata;
		}
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
		incompleteMetadata.blobServer = settings.getStorageFirst();
		incompleteMetadata.blobBackup = settings.getStorageSecond();
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
		Metadatabase main = new Metadatabase(settings.getStorageFirst().toString(), DefaultSetting.METADATA_TABLE_NAME);
		Metadatabase backup = new Metadatabase(settings.getStorageSecond().toString(), DefaultSetting.METADATA_TABLE_NAME);

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
			System.out.println("acceptFileUpdate@Metadatabase: Update #" + fileToUpload + "->" + b);
			
			if(b){	//delete the file after successful upload
				File f = new File(fileToUpload);
				boolean d = f.delete();
				System.out.println("acceptFileUpdate@Metadatabase: Delete #" + fileToUpload + "->" + d);
			}
		}
		/*
		 * Do nothing when status is DELETE else {
		 * sb.deleteFile(completeMetadata); }
		 */

		return completeMetadata;
	}

	public static ArrayList<Metadata> getCompleteMetadata(String username, long sinceCounter) {

		Metadatabase db = getServer();

		ArrayList<MetadataDBRow> rows = db.retrieveRecordSince(username, sinceCounter);
		ArrayList<Metadata> result = new ArrayList<Metadata>();
		for (MetadataDBRow row : rows) {
			Metadata m = row.toMetadata();
			// rows is in descending order
			if(!result.contains(m))
			    result.add(m);		//only add record for a not recorded file
		}
		
		// make the result in descending order
		Collections.reverse(result);
		return result;
	}

	private Metadatabase(String connString, String tableName) {
		table = AzureStorageConnection.connectToTable(connString, tableName);
	}

	private Metadatabase() {
	}

	private static Metadatabase getServer() {
		Metadatabase server = new Metadatabase(MasterSettings.getInstance().getStorageFirst().toString(), DefaultSetting.METADATA_TABLE_NAME);
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
			System.out.println("updateRecord@Metadatabase: update complete");
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

	private ArrayList<MetadataDBRow> retrieveRecordSince(String username, long counter) {
		String partitionFilter = TableQuery.generateFilterCondition("PartitionKey", QueryComparisons.EQUAL, username);

		String rowFilter = TableQuery.generateFilterCondition("GlobalCounter", QueryComparisons.GREATER_THAN_OR_EQUAL, counter);

		String combinedFilter = TableQuery.combineFilters(partitionFilter, Operators.AND, rowFilter);

		TableQuery<MetadataDBRow> rangeQuery = TableQuery.from(MetadataDBRow.class).where(combinedFilter);

		System.out.println("retrieveRecordSince@Metadatabase: combinedFilter=" +combinedFilter);

		Iterable<MetadataDBRow> result = table.execute(rangeQuery);
		
		ArrayList<MetadataDBRow> sortedResult = new ArrayList<>();
		
		for (MetadataDBRow row : result) {
		    sortedResult.add(row);
		}
		
		Collections.sort(sortedResult, new Comparator<MetadataDBRow>() {
            @Override
            public int compare(MetadataDBRow o1, MetadataDBRow o2) {
                // sort in descending order
                return (int) (o2.getGlobalCounter() - o1.getGlobalCounter());
            }
		});
		
		return sortedResult;
	}

	private boolean hasError(MetadataDBRow last, Metadata meta) {
		if(last==null && meta.parent!=0){	//add by Eli
			return true;	//this is an error! meta data says there is a parent, but can not find the parent in database
		}
		return false;	//no error
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
		System.out.println("getLast@Metadatabase: combinedFilter=" + combinedFilter);

		TableQuery<MetadataDBRow> rangeQuery = TableQuery.from(MetadataDBRow.class).where(combinedFilter);

		Iterable<MetadataDBRow> result = table.execute(rangeQuery);

		for (MetadataDBRow tmp : result) {
			// if exist return it
			return tmp;
		}

		return null; // not exist
	}
}
