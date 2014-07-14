package cloudsync.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import cloudsync.sharedInterface.AccountInfo;

public class MasterMain {

	public static void main(String[] args) {
		MasterSettings settings = MasterSettings.getInstance();
		settings.loadSettings();
		SessionManager sessionManager = SessionManager.getInstance();
		
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(settings.getLocalPort());
			
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		boolean loop = true;
		while(loop)
		{
			Socket socket = null;
			ObjectInputStream  streamIn = null;
			ObjectOutputStream streamOut = null;
			
			AccountInfo account = null;
			try {
				socket = serverSocket.accept();
				streamIn  = new ObjectInputStream(socket.getInputStream());
				streamOut = new ObjectOutputStream(socket.getOutputStream());
				account = (AccountInfo)streamIn.readObject();
				
				AccountDatabase accountDB = AccountDatabase.getInstance();
				boolean suc = accountDB.login(account.getUsername(), account.getPassword());
				if(suc){
					//login success, then let session manager to take over
					SocketStream socketStream = new SocketStream(socket, streamIn, streamOut);
					sessionManager.acceptClient(account.getUsername(), socketStream);
				}else{
					//login fail, then close the connection at once
					streamIn.close();
					streamOut.close();
					socket.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
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
