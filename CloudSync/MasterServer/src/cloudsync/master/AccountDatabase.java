package cloudsync.master;

public class AccountDatabase {
	//Singleton design pattern
	private static AccountDatabase that = null;
	
	private AccountDatabase(){
		//private constructor to secure singleton
	}
	
	public static AccountDatabase getInstance(){
		if(that==null){
			that = new AccountDatabase();
		}
		return that;
	}
	
    public boolean login(String username, String password)
    {
    	if( "Tom".compareTo(username)==0 && "123456".compareTo(password)==0 ){
    		return true;
    	}else{
    		return false;
    	}
    }
    public boolean createAccount(String username, String password)    //this method should be triggered by entry server in final release
    {
        return false;
    }
}
