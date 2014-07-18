package cloudsync.sharedInterface;

public class AzureConnection {
	final private String accountInfo;
	
	public AzureConnection(){
		final String defaultStorageConnectionString = 
			    "DefaultEndpointsProtocol=http;" + 
			    "AccountName=portalvhdsql3h2lbtq12d7;" + 
			    "AccountKey=uC6oYc8BafbOaFme6dZp5MKgZUQrDk+wAz0vCf7ISC1JHDolgwIYxlHuKgAXWseRxMNlHpqjNRgtw90qE7wvzA==";
		accountInfo = defaultStorageConnectionString; 
	}
	
	public AzureConnection(String accountInfo){
		this.accountInfo = accountInfo;
	}
	
	public AzureConnection(String accountName, String accountKey){
		this.accountInfo = 
			    "DefaultEndpointsProtocol=http;" + 
			    "AccountName=" + accountName +";" +
			    "AccountKey="  + accountKey;
	}

	@Override
	public String toString() {
		return accountInfo;	//super.toString();
	}
	
}
