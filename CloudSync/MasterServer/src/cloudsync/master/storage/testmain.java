package cloudsync.master.storage;

import java.util.ArrayList;

import cloudsync.sharedInterface.AzureConnection;
import cloudsync.sharedInterface.Metadata;

public class testmain {
    public static void main(String[] argv) {
        
        Metadatabase db = Metadatabase.getInstance();
        
        Metadata meta = new Metadata();
        meta.filename = "testfile6";
        meta.globalCounter = 6;
        meta.parent = 0;
        meta.blobKey = "kekek";
        meta.blobServer = new AzureConnection("storage", "key");
        meta.blobBackup = new AzureConnection("storagebu", "key");
        
        MetadataDBRow row = new MetadataDBRow("user", meta);
        
        //db.addRecord(row);
        
        //Iterable<MetadataDBRow> rows = db.retrieveRecordSince("user", 1);
        ArrayList<Metadata> rows = db.getCompleteMetadata("user", 0);
        
        System.out.println("retrieved");
        
        for (Metadata arow : rows) {
            System.out.print(arow.filename);
            System.out.println(" " + arow.globalCounter);
        }

    }
}
