package cloudsync.master;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import cloudsync.master.storage.AccountDatabase;
import cloudsync.master.storage.Metadatabase;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.RemoteInterface;
import cloudsync.sharedInterface.ServerLocation;
import cloudsync.sharedInterface.SocketMessage;

public class RemoteMethodProvider extends UnicastRemoteObject implements RemoteInterface {

	private static final long	serialVersionUID	= -3466483622817116103L;

	protected RemoteMethodProvider() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean RmiCheckUsernamePassword(String username, String password) throws RemoteException {
		System.out.println("RmiCheckUsernamePassword@RemoteMethodProvider[" + username + "]:" + password);
		boolean result = AccountDatabase.getInstance().login(username, password);
		return result;
	}

	@Override
	public long RmiGetMasterServerGlobalCounter(String username) throws RemoteException {
		System.out.println("RmiGetMasterServerGlobalCounter" + "@RemoteMethodProvider[" + username + "]");
		long counter = AccountDatabase.getInstance().getAccount(username).getGlobalCounter();
		return counter;
	}

	@Override
	public ArrayList<Metadata> RmiGetCompleteMetadata(String username, long sinceCounter) throws RemoteException {
		System.out.println("RmiGetCompleteMetadata@RemoteMethodProvider[" + username + "]: since=" + sinceCounter);
		ArrayList<Metadata> result = Metadatabase.getCompleteMetadata(username, sinceCounter);
		return result;
	}

	@Override
	public Metadata RmiCommitFileUpdate(String username, Metadata incompleteMetadata, String fileInfo) throws RemoteException {
		System.out.println("RmiCommitFileUpdate@RemoteMethodProvider[" + username + "]:" + "fileInfo=" + fileInfo);
		Metadata result = Metadatabase.acceptFileUpdate(username, incompleteMetadata, fileInfo);
		return result;
	}

	@Override
	public boolean RmiCreateAccount(String username, String password) throws RemoteException {
		System.out.println("RmiCreateAccount@RemoteMethodProvider[" + username + "]:" + password);
		boolean result = AccountDatabase.getInstance().createAccount(username, password);
		return result;
	}

	@Override
	public ServerLocation RmiGetMasterServerAddress(String username) throws RemoteException {
		ServerLocation loc = AccountDatabase.getInstance().getServerLocation(username);
		System.out.println("RmiGetMasterServerAddress@RemoteMethodProvider[" + username + "]:" + loc.url);
		return loc;
	}

	@Override
	public int RmiBroadcastMessage(String username, SocketMessage message) throws RemoteException {
		System.out.println("RmiBroadcastMessage@RemoteMethodProvider[" + username + "]:" + message.command);
		int ret = SessionManager.getInstance().broadcastSocketMessage(username, message);
		return ret;
	}

}
