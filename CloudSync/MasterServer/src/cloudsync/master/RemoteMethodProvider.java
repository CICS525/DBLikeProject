package cloudsync.master;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import cloudsync.master.storage.AccountDatabase;
import cloudsync.master.storage.Metadatabase;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.RemoteInterface;

public class RemoteMethodProvider extends UnicastRemoteObject implements
        RemoteInterface {

    private static final long serialVersionUID = -3466483622817116103L;

    protected RemoteMethodProvider() throws RemoteException {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean RmiCheckUsernamePassword(String username, String password)
            throws RemoteException {
        System.out.println("RmiCheckUsernamePassword@RemoteMethodProvider["
                + username + "]:" + password);
        boolean result = AccountDatabase.getInstance()
                .login(username, password);
        return result;
    }

    @Override
    public long RmiGetMasterServerGlobalCounter(String username)
            throws RemoteException {
        System.out.println("RmiGetMasterServerGlobalCounter"
                + "@RemoteMethodProvider[" + username + "]");
        long counter = AccountDatabase.getInstance().getAccount(username)
                .getGlobalCounter();
        return counter;
    }

    @Override
    public ArrayList<Metadata> RmiGetCompleteMetadata(String username,
            long sinceCounter) throws RemoteException {
        System.out.println("RmiGetCompleteMetadata@RemoteMethodProvider["
                + username + "]: since=" + sinceCounter);
        ArrayList<Metadata> result = Metadatabase.getCompleteMetadata(username,
                sinceCounter);

        return result;
    }

    @Override
    public Metadata RmiCommitFileUpdate(String username,
            Metadata incompleteMetadata, String fileInfo)
            throws RemoteException {
        System.out.println("RmiCommitFileUpdate@RemoteMethodProvider["
                + username + "]:" + "fileInfo=" + fileInfo);
        Metadata result = Metadatabase.acceptFileUpdate(username,
                incompleteMetadata, fileInfo);
        return result;
    }

}
