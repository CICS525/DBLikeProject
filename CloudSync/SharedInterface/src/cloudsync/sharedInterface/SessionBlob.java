package cloudsync.sharedInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import cloudsync.sharedInterface.Metadata;

public class SessionBlob {
	final private static String DEFAULTCONTAINER = "bolbpool".toLowerCase();
	
	public void blobTest(){
		// Define the connection-string with your values
		final String storageConnectionString = 
			    "DefaultEndpointsProtocol=http;" + 
			    "AccountName=portalvhdsql3h2lbtq12d7;" + 
			    "AccountKey=uC6oYc8BafbOaFme6dZp5MKgZUQrDk+wAz0vCf7ISC1JHDolgwIYxlHuKgAXWseRxMNlHpqjNRgtw90qE7wvzA==";
		
		String containerName = "MyTestContainerInJava".toLowerCase();
		
		
		// How to: Create a container
		try
		{
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			
			// Create the blob client.
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			
			// Get a reference to a container.
			// The container name must be lower case
			CloudBlobContainer container = blobClient.getContainerReference(containerName);
			
			// Create the container if it does not exist.
			container.createIfNotExists();
			
			//------ Optional: Configure a container for public access ------
			// Create a permissions object.
			BlobContainerPermissions containerPermissions = new BlobContainerPermissions();

			// Include public access in the permissions object.
			containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);

			// Set the permissions on the container.
			container.uploadPermissions(containerPermissions);
		}
		catch (Exception e)
		{
			// Output the stack trace.
			e.printStackTrace();
		}
		
		
		// How to: Upload a blob into a container
		try
		{
		    // Retrieve storage account from connection-string.
		    CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

		    // Create the blob client.
		    CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

		    // Retrieve reference to a previously created container.
		    CloudBlobContainer container = blobClient.getContainerReference(containerName);

		    // Define the path to a local file.
		    final String filePath = "C:\\Users\\Elitward\\Pictures\\Capture\\Capture.JPG";

		    // Create or overwrite the "myimage.jpg" blob with contents from a local file.
		    CloudBlockBlob blob = container.getBlockBlobReference("myimage.jpg");
		    File source = new File(filePath);
		    blob.upload(new FileInputStream(source), source.length());
		}
		catch (Exception e)
		{
		    // Output the stack trace.
		    e.printStackTrace();
		}
		
		
		// How to: List the blobs in a container
		try
		{
		    // Retrieve storage account from connection-string.
		    CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

		    // Create the blob client.
		    CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

		    // Retrieve reference to a previously created container.
		    CloudBlobContainer container = blobClient.getContainerReference(containerName);

		    // Loop over blobs within the container and output the URI to each of them.
		    for (ListBlobItem blobItem : container.listBlobs()) {
		       System.out.println(blobItem.getUri());
		   }
		}
		catch (Exception e)
		{
		    // Output the stack trace.
		    e.printStackTrace();
		}
		
		
		//How to: Download a blob
		try
		{
		    // Retrieve storage account from connection-string.
		   CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

		   // Create the blob client.
		   CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

		   // Retrieve reference to a previously created container.
		   CloudBlobContainer container = blobClient.getContainerReference(containerName);

		   // Loop through each blob item in the container.
		   for (ListBlobItem blobItem : container.listBlobs()) {
		       // If the item is a blob, not a virtual directory.
		       if (blobItem instanceof CloudBlob) {
		           // Download the item and save it to a file with the same name.
		            CloudBlob blob = (CloudBlob) blobItem;
		            blob.download(new FileOutputStream("C:\\mydownloads\\" + blob.getName()));
		        }
		    }
		}
		catch (Exception e)
		{
		    // Output the stack trace.
		    e.printStackTrace();
		}
		
		
		// How to: Delete a blob container
		try
		{
		   // Retrieve storage account from connection-string.
		   CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

		   // Create the blob client.
		   CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

		   // Retrieve reference to a previously created container.
		   CloudBlobContainer container = blobClient.getContainerReference(containerName);

		   // Delete the blob container.
		   container.deleteIfExists();
		}
		catch (Exception e)
		{
		    // Output the stack trace.
		    e.printStackTrace();
		}
	}
	
	private boolean uploadLocalFileToAzureStorageBlob(String filename, String azureConnection, String containerName, String blobName){
		try
		{
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(azureConnection);
			
			// Create the blob client.
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			
			// Get a reference to a container.
			// The container name must be lower case
			CloudBlobContainer container = blobClient.getContainerReference(containerName);
			
			// Create the container if it does not exist.
			container.createIfNotExists();
			
			//------ Optional: Configure a container for public access ------
			// Create a permissions object.
			BlobContainerPermissions containerPermissions = new BlobContainerPermissions();

			// Include public access in the permissions object.
			containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);

			// Set the permissions on the container.
			container.uploadPermissions(containerPermissions);
			
		    // Create or overwrite the blob with contents from a local file.
		    CloudBlockBlob blob = container.getBlockBlobReference(blobName);
		    File source = new File(filename);
		    FileInputStream iStream = new FileInputStream(source);
		    blob.upload(iStream, source.length());
		    iStream.close();
		}
		catch (Exception e)
		{
			// Output the stack trace.
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean downloadAzureStorageBlobToLocalFile(String azureConnection, String containerName, String blobName, String filename) {
		try
		{
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(azureConnection);
			
			// Create the blob client.
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			
			// Retrieve reference to a previously created container.
			CloudBlobContainer container = blobClient.getContainerReference(containerName);
			
			CloudBlob blob = container.getBlockBlobReference(blobName);
			
			FileOutputStream oStream = new FileOutputStream(filename);
			blob.download(oStream);
			oStream.close();
		}
		catch (Exception e)
		{
		    // Output the stack trace.
		    e.printStackTrace();
		    return false;
		}
		return true;
	}
	
	private boolean deleteAzureStorageBlob(String azureConnection, String containerName, String blobName){
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(azureConnection);
			
			// Create the blob client.
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			
			// Retrieve reference to a previously created container.
			CloudBlobContainer container = blobClient.getContainerReference(containerName);
			
			CloudBlob blob = container.getBlockBlobReference(blobName);
			
			// Delete the blob
			return blob.deleteIfExists();
		} catch (Exception e) {
			// Output the stack trace.
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean uploadFile(String filename, Metadata metadata){
		//update file, specified by filename to Blob server
		//Blob server and Blob name are specified in metadata
		//If there are Backup blob server, they should all be updated in parallel thread

		boolean suc = uploadLocalFileToAzureStorageBlob(filename, metadata.blobServer.toString(), DEFAULTCONTAINER, metadata.blobKey);
		return suc;
	}
	
	public boolean downloadFile(Metadata metadata, String filename){
		boolean suc = downloadAzureStorageBlobToLocalFile(metadata.blobServer.toString(), DEFAULTCONTAINER, metadata.blobKey, filename);
		return suc;
	}
	
	public boolean deleteFile(Metadata metadata){
		boolean suc = deleteAzureStorageBlob(metadata.blobServer.toString(), DEFAULTCONTAINER, metadata.blobKey);
		return suc;
	}
}
