package cloudsync.master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import cloudsync.master.storage.AccountDatabase;
import cloudsync.sharedInterface.AccountInfo;
import cloudsync.sharedInterface.FileReceiver;
import cloudsync.sharedInterface.RemoteInterface;
import cloudsync.sharedInterface.ServerLocation;
import cloudsync.sharedInterface.SocketStream;

public class MasterMain {

	public static void main(String[] args) {
		System.out.println("MasterMain starts ...");
		
		//if(System.getSecurityManager()==null){
		//    System.setProperty("java.security.policy", "C:\\Users\\cloudsync\\Documents\\server.policy");
		//    System.setSecurityManager(new RMISecurityManager());
		//}
		
		MasterSettings settings = MasterSettings.getInstance();
		settings.loadSettings();

		SessionManager sessionManager = SessionManager.getInstance();
		
		//---RMI---
		try {
			System.setProperty("java.rmi.server.hostname", ServerLocation.getExternalIp());
			System.out.println("java.rmi.server.hostname=" + System.getProperty("java.rmi.server.hostname"));
			int rmiPort = settings.getLocalRmiPort();
			RemoteMethodProvider remoteMethodProvider = new RemoteMethodProvider();
			RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(remoteMethodProvider, rmiPort);
			Registry registry = LocateRegistry.createRegistry(rmiPort);
			registry.bind(RemoteInterface.RMI_ID, remoteMethodProvider);
			System.out.println("MasterMain Server: RIM wating ...@" + rmiPort);
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
			return;
		}

		//---Upload File---
		int uplPort = settings.getLocalUploadPort();
		FileReceiver.initialize(uplPort);
		System.out.println("MasterMain Server: Upload wating ...@" + uplPort);
		
		//---Command Message---
		ServerSocket serverSocket = null;
		try {
			int msgPort = settings.getLocalMessagePort();
			serverSocket = new ServerSocket(msgPort);
			System.out.println("MasterMain Server: Command Message waiting ...@" + msgPort);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		boolean loop = true;
		while(loop)
		{
			Socket socket = null;
			
			AccountInfo account = null;
			try {
				socket = serverSocket.accept();
				System.out.println("MasterMain Server: Client socket coming. " + socket.getRemoteSocketAddress().toString());
				SocketStream socketStream = new SocketStream();
				socketStream.initStream(socket);

				account = (AccountInfo)(socketStream.readObject());
				if(account==null)
					continue;
				if(account.getUsername()==null || account.getPassword()==null)
					continue;
				
				AccountDatabase accountDB = AccountDatabase.getInstance();
				boolean suc = accountDB.login(account.getUsername(), account.getPassword());
				System.out.println("login@MasterMain: " + account.toString() + " -> " + suc);
				if(suc){
					//login success, then let session manager to take over
					sessionManager.acceptClient(account.getUsername(), socketStream);
				}else{
					//login fail, then close the connection at once
					socketStream.deinitStream();
					socket = null;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		try {
			serverSocket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

	}
}
