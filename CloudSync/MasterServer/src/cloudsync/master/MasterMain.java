package cloudsync.master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import cloudsync.master.storage.AccountDatabase;
import cloudsync.sharedInterface.AccountInfo;
import cloudsync.sharedInterface.RemoteInterface;
import cloudsync.sharedInterface.SocketStream;

public class MasterMain {

	public static void main(String[] args) {
		System.out.println("MasterMain starts ...");
		
		MasterSettings settings = MasterSettings.getInstance();
		settings.loadSettings();

		SessionManager sessionManager = SessionManager.getInstance();
		
		try {
			RemoteMethodProvider remoteMethodProvider = new RemoteMethodProvider();
			Registry registry = LocateRegistry.createRegistry(RemoteInterface.RMI_PORT);
			registry.bind(RemoteInterface.RMI_ID, remoteMethodProvider);
			System.out.println("MasterMain RIM wating ...");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(settings.getLocalMessagePort());
			System.out.println("MasterMain Server Socket ready ...");
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		boolean loop = true;
		while(loop)
		{
			Socket socket = null;
			
			AccountInfo account = null;
			try {
				socket = serverSocket.accept();
				System.out.println("MasterMain: Client socket coming. " + socket.getRemoteSocketAddress().toString());
				SocketStream socketStream = new SocketStream();
				socketStream.initStream(socket);

				account = (AccountInfo)(socketStream.readObject());
				
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
		}

		//--- wait here forever ---
		try {
			byte[] buff = new byte[128];
			System.in.read(buff);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
