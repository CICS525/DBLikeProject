package cloudsync.master.storage;

import java.util.Date;

import cloudsync.sharedInterface.AzureConnection;
import cloudsync.sharedInterface.Metadata;

import com.microsoft.azure.storage.table.*;

public class MetadataDBRow extends TableServiceEntity {

    private String filename;
    private long globalCounter;
    private long parent;
    private String status;
    private Date time = null; // this maps to timestamp in Metadata, because
                              // table already have timestamp
    private String blobKey = null;
    private String blobServer;
    private String blobBackup;

    public MetadataDBRow() {
    }

    public MetadataDBRow(String username, Metadata meta) {
        this.partitionKey = username;
        this.rowKey = String.valueOf(meta.globalCounter);
        this.globalCounter = meta.globalCounter;

        this.filename = meta.basename;
        this.setParent(meta.parent);
        this.status = meta.status.toString();
        this.setTime(meta.timestamp);
        this.setBlobKey(meta.blobKey);
        this.setBlobServer(meta.blobServer.toString());
        this.setBlobBackup(meta.blobBackup.toString());

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getParent() {
        return parent;
    }

    public void setParent(long parent) {
        this.parent = parent;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getBlobKey() {
        return blobKey;
    }

    public void setBlobKey(String blobKey) {
        this.blobKey = blobKey;
    }

    public String getBlobServer() {
        return blobServer;
    }

    public void setBlobServer(String blobServer) {
        this.blobServer = blobServer;
    }

    public String getBlobBackup() {
        return blobBackup;
    }

    public void setBlobBackup(String blobBackup) {
        this.blobBackup = blobBackup;
    }

    public String getUsername() {
        return partitionKey;
    }

    public void setUsername(String username) {
        this.partitionKey = username;
    }

    public long getGlobalCounter() {

        return this.globalCounter;
    }

    public void setGlobalCounter(long counter) {
        this.rowKey = String.valueOf(counter);
        this.globalCounter = counter;
    }

    public Metadata toMetadata() {
        // get correspond metadata
        Metadata meta = new Metadata();
        meta.basename = this.getFilename();
        meta.globalCounter = this.getGlobalCounter();
        meta.parent = this.getParent();
        meta.status = Metadata.STATUS.valueOf(this.getStatus());
        meta.timestamp = this.getTime();
        meta.blobKey = this.getBlobKey();
        meta.blobServer = new AzureConnection(this.getBlobServer());
        meta.blobBackup = new AzureConnection(this.getBlobBackup());

        return meta;
    }

}
