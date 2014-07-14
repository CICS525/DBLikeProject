package cloudsync.sharedInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote{

	public boolean CheckUsernamePassword(String username, String password) throws RemoteException;
}
