package cloudsync.master;

import java.util.ArrayList;

import cloudsync.master.storage.AccountDatabase;
import cloudsync.sharedInterface.SocketMessage;
import cloudsync.sharedInterface.SocketStream;

public class SessionManager {
	//singleton design pattern
	private static SessionManager that = null;
	private ArrayList<SessionAccount> accountPool = null;
	
	private SessionManager(){
		//private constructor to secure singleton
		accountPool = new ArrayList<SessionAccount>();
	}
	
	public static SessionManager getInstance(){
		if(that==null){
			that = new SessionManager();
		}
		return that;
	}
	
	public boolean acceptClient(String username, SocketStream socketStream ){	//parameter may need be modified
		//the coming client should be dispatched into a new / existing AccountSession to handle.
		
		synchronized(accountPool){
			for(SessionAccount account: accountPool){
				if(account.getUsername().compareTo(username)==0 ){
					//match an existing account session, and it's thread is active
					return account.addSocketStream(socketStream);
				}
			}
			
			//no existing account session match, then create a new one
			SessionAccount account = new SessionAccount(username);
			account.addSocketStream(socketStream);
			accountPool.add(account);
		}

		return false;
	}
	
	public int clearDeactiveAccount() {
		int counter = 0;
		synchronized(accountPool){
			ArrayList<SessionAccount> delList = null;
			for(SessionAccount account: accountPool){
				if( account.isSocketStreamEmpty() ){
					counter++;
				    
				    // change entry server flag
				    AccountDatabase.getInstance().logout(account.getUsername());
				    
					if(delList==null)
						delList = new ArrayList<SessionAccount>();
					delList.add(account);
				}
			}
			if(counter>0){
				accountPool.removeAll(delList);
			}
		}
		return counter;
	}
	
	public int broadcastSocketMessage(String username, SocketMessage message){
		int counter = 0;
		synchronized(accountPool){
			for(SessionAccount account: accountPool){
				if(account.getUsername().compareTo(username)==0 ){
					int get = account.broadcastSocketMessage(message);
					if(get>0)
						counter++;
				}
			}
		}
		return counter;
	}
}
