package cloudsync.sharedInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RemoteInterface extends Remote{
	public final String RMI_ID = "CloudSync_RMI";
	
	public boolean RmiCreateAccount(String username, String password) throws RemoteException;

	public boolean RmiCheckUsernamePassword(String username, String password) throws RemoteException;
	
	public ServerLocation RmiGetMasterServerAddress(String username) throws RemoteException;
	
	public long RmiGetMasterServerGlobalCounter(String username) throws RemoteException;

	public ArrayList<Metadata> RmiGetCompleteMetadata(String username, long sinceCounter) throws RemoteException;
	
	public Metadata RmiCommitFileUpdate(String username, Metadata incompleteMetadata, String fileInfo) throws RemoteException;
	
	public int RmiBroadcastMessage(String username, SocketMessage message) throws RemoteException;
	
	public int RmiHello() throws RemoteException;
}
