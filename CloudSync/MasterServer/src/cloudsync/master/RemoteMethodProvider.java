package cloudsync.master;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.RemoteInterface;

public class RemoteMethodProvider extends UnicastRemoteObject implements RemoteInterface{

	private static final long serialVersionUID = -3466483622817116103L;

	protected RemoteMethodProvider() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean RmiCheckUsernamePassword(String username, String password)
			throws RemoteException {
		System.out.println("RmiCheckUsernamePassword@RemoteMethodProvider[" + username + "]:" + password);
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long RmiGetMasterServerGlobalCounter(String username)
			throws RemoteException {
		System.out.println("RmiGetMasterServerGlobalCounter@RemoteMethodProvider[" + username + "]" );
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<Metadata> RmiGetCompleteMetadata(String username,
			long sinceCounter) throws RemoteException {
		System.out.println("RmiGetCompleteMetadata@RemoteMethodProvider[" + username + "]: since=" + sinceCounter);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Metadata RmiCommitFileUpdate(String username,
			Metadata incompleteMetadata, String fileInfo)
			throws RemoteException {
		System.out.println("RmiCommitFileUpdate@RemoteMethodProvider[" + username + "]:" + "fileInfo=" + fileInfo);
		// TODO Auto-generated method stub
		return null;
	}

}
