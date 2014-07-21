package cloudsync.sharedInterface;

public class DefaultSetting {
	public final static String	DEFAULT_MASTER_SERVER_URL = "127.0.0.1";
	public final static int		DEFAULT_MASTER_SERVEL_PORT = 389;	//use LDAP default port number to facilitate Azure Virtual Machine configure
	
	public final static String eli_storageConnectionString = 
		    "DefaultEndpointsProtocol=http;" + 
		    "AccountName=portalvhdsql3h2lbtq12d7;" + 
		    "AccountKey=uC6oYc8BafbOaFme6dZp5MKgZUQrDk+wAz0vCf7ISC1JHDolgwIYxlHuKgAXWseRxMNlHpqjNRgtw90qE7wvzA==";

	public final static String chris_storageConnectionString = "DefaultEndpointsProtocol=http;"
            + "AccountName=portalvhds96n2s1jyj5b5k;"
            + "AccountKey=vzJ56owCpSgvpfToqBEx2cUy6slkT7eUtWCUATe6OLWDo/GBXkbup3x8kkIHpNRdva7syOruyMq9mJfez1ZvOA==";
    
	public final static String sky_storageConnectionString = "DefaultEndpointsProtocol=http;"
            + "AccountName=portalvhds0c37fqp3tw964;"
            + "AccountKey=n8uEGZrIUoMcD4J7WgbcZyk6gMZ0hV9mtn83jXtMpWwLjFAWlPSZizDdZiWmeLjJMetOvrMko1dwoQnaUQTSLQ==";
	
	public final static String metadatabase_table_name = "meta";
	
	public final static String account_table_name = "account";
}
