package cloudsync.sharedInterface;

public class DefaultSetting {
	//TCP: IMAP:143	/ LDAP:389 / POP3:110
	
	public final static String	DEFAULT_ENTRY_SERVER_URL  = "127.0.0.1";
	public final static String	DEFAULT_MASTER_SERVER_URL = "127.0.0.1";
	
	public final static int		DEFAULT_MASTER_MESSAGE_PORT = 2001;
	public final static int		DEFAULT_MASTER_RMI_PORT     = 2002;
	public final static int		DEFAULT_MASTER_UPLOAD_PORT  = 2003;

	public final static String	ELI_AZURE_SERVER_PUBLIC_IP = "137.135.59.120";
	
	public final static String ELI_VM_ADDR = "cloudsync.cloudapp.net";
	public final static String CHRIS_VM_ADDR = "cloudsync2.cloudapp.net";
	public final static String SKY_VM_ADDR = "cloudsync3.cloudapp.net";

	public final static String eli_storageConnectionString = 
		    "DefaultEndpointsProtocol=http;" + 
		    "AccountName=cloudsync;" + 
		    "AccountKey=JAOg0vyETxNJDgadihZCLDJzjqgJ79eGWbgbX6pIkAnXTIVqc1oxd+cH6caU9kB5lLhwrXKRaaAD/ak+raq4tg==";

	public final static String chris_storageConnectionString = "DefaultEndpointsProtocol=http;"
            + "AccountName=portalvhds049kfr2ss7hpd;"
            + "AccountKey=w9StAFFrwJ7kFkOmiWLB/nH/rR1HUIVJhan4N5H6YZEgl9BnBtF8BRK5xMo6KLZ+UavOoAza7bzkfjSziSQcWw==";
    
	public final static String sky_storageConnectionString = "DefaultEndpointsProtocol=http;"
            + "AccountName=portalvhds98y1bsjj9fbb7;"
            + "AccountKey=qfs8hait+aDHDFyNG/I4GzWeVjsmYp7aI2H1G4yCE67YY2XFSp6F1P/OO/qq7IIqBnHRgwbOSjre/xPbTmzT9Q==";
	
	public final static String METADATA_TABLE_NAME = "meta";
	
	public final static String ACCOUNT_TABLE_NAME = "account";
	
	public final static String MASTER_BLOB_TABLE_NAME = "masterblob";
	
	public final static String DEFAULT_SYNC_DIR_NAME = "CloudSync";
}
