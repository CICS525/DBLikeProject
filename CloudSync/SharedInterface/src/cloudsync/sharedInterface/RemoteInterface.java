package cloudsync.sharedInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RemoteInterface extends Remote{
	public final String RMI_ID = "CloudSync_RMI";

	public boolean RmiCheckUsernamePassword(String username, String password) throws RemoteException;
	
	public long RmiGetMasterServerGlobalCounter(String username) throws RemoteException;

	public ArrayList<Metadata> RmiGetCompleteMetadata(String username, long sinceCounter) throws RemoteException;
	
	public Metadata RmiCommitFileUpdate(String username, Metadata incompleteMetadata, String fileInfo) throws RemoteException;
	
}
