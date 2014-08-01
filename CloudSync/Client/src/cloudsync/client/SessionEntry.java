package cloudsync.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.RemoteInterface;
import cloudsync.sharedInterface.ServerLocation;

public class SessionEntry {
	// SessionEntry should be singleton design pattern
	private static SessionEntry that = null;
	private ServerLocation entryLocation = null;
	private RemoteInterface rmi = null;

	private SessionEntry() {
		// private constructor to secure singleton
	}

	public static SessionEntry getInstance() {
		if (that == null) {
			that = new SessionEntry();
			that.entryLocation = new ServerLocation(DefaultSetting.DEFAULT_ENTRY_SERVER_URL, DefaultSetting.DEFAULT_MASTER_RMI_PORT);
		}
		return that;
	}

	public ServerLocation getEntryLocation() {
		return entryLocation;
	}

	public void setEntryLocation(ServerLocation entryLocation) {
		this.entryLocation = entryLocation;
	}

	public ServerLocation getMasterServerLocation(String username, String password) {
		Registry registry;
		// get master server location
		try {
			// initialize RMI interface for entry server
			System.out.println("Connecting to EntryServer - RIM: " + entryLocation.url + "@" + DefaultSetting.DEFAULT_MASTER_RMI_PORT);
			registry = LocateRegistry.getRegistry(entryLocation.url, DefaultSetting.DEFAULT_MASTER_RMI_PORT);
			rmi = (RemoteInterface) registry.lookup(RemoteInterface.RMI_ID);
			System.out.println("~Connected to EntryServer - RIM: " + rmi.toString());
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		} catch (NotBoundException e) {
			e.printStackTrace();
			return null;
		}
		
        // check password at entry server
        try {
            boolean suc = rmi.RmiCheckUsernamePassword(username, password);
            if(!suc){
                suc = rmi.RmiCreateAccount(username, password);
                if(!suc)
                	return null;
            }
            
            // get master location from entry server
            ServerLocation master = rmi.RmiGetMasterServerAddress(username);
            return master;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
	}
}
