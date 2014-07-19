package cloudsync.master;

import java.util.ArrayList;

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
		for(int i=0; i<10; i++){
			System.out.println("test" + i);
			socketStream.writeObject(new SocketMessage());
		}

		return false;
	}
}
