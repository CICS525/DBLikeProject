package cloudsync.sharedInterface;

import java.io.Serializable;

public class AzureConnection implements Serializable{

	private static final long serialVersionUID = -6481061029956180656L;
	final private String accountInfo;
	
	public AzureConnection(){
		accountInfo = DefaultSetting.eli_storageConnectionString;	//default setting 
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
