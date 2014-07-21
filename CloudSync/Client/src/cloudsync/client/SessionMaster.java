package cloudsync.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import cloudsync.sharedInterface.AccountInfo;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.RemoteInterface;
import cloudsync.sharedInterface.ServerLocation;
import cloudsync.sharedInterface.SocketMessage;
import cloudsync.sharedInterface.SocketStream;

public class SessionMaster {
	//SessionMaster should be singleton design pattern
	private static SessionMaster that = null;
	
	private ServerLocation masterLocation = null;
	private SocketStream socketStream = null;
	private SocketThread thread = null;
	private RemoteInterface rmi = null;
	private String username = null;
	
	private SessionMaster(){
		//private constructor to secure singleton
	}
	
	public static SessionMaster getInstance(){
		if( that==null ){
			that = new SessionMaster();
		}
		return that;
	}

	public ServerLocation getMasterServerLocation() {
		return masterLocation;
	}

	public boolean setMasterServerLocation(ServerLocation masterServerLocation) {
		if(socketStream==null){	//master server location can only be set when there is socket is not active
			masterLocation = masterServerLocation;
			return true;
		}else{
			return false;
		}
	}

	public boolean connect(String username, String password) {
		//for the entry server and master server at separated, so here may need to check username & password again
		this.username = username;
		
		//initialize the RMI interface.
		try {
			Registry registry = LocateRegistry.getRegistry(masterLocation.url, RemoteInterface.RMI_PORT);
			rmi = (RemoteInterface) registry.lookup(RemoteInterface.RMI_ID);
			System.out.println("Connected to MasterServer - RIM: " + rmi.toString());
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		} catch (NotBoundException e) {
			e.printStackTrace();
			return false;
		}
		
		//initialize the stock long link for message pushing.
		Socket socket = null;
		try {
			socket = new Socket(masterLocation.url, masterLocation.port);
			System.out.println("Connected to MasterServer - Command Message: " + socket.getRemoteSocketAddress().toString());
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		AccountInfo account = new AccountInfo(username, password);
		//Write account into socket. If server respond, means login OK.
		
		socketStream = new SocketStream();
		socketStream.initStream(socket);
		socketStream.writeObject(account);
		
		System.out.println("Connected to MasterServer: Stream ready. " + socketStream.getStreamIn() + ";" + socketStream.getStreamOut());
		
		//Then, create a new thread to wait in-coming message for master server
		thread = new SocketThread();
		thread.start();
		
		return true;
	}
	
	public boolean disconnect(){
		boolean suc = socketStream.deinitStream();
		socketStream = null;
		rmi = null;
		username = null;
		return suc;
	}
	
	public boolean rmiCheckUsernamePassword(String username, String password){
		boolean ans = false;
		if(rmi!=null){
			try {
				ans = rmi.RmiCheckUsernamePassword(username, password);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ans;
	}
	
	public long rmiGetMasterServerGlobalCounter(){
		long ans = -1;
		if(rmi!=null){
			try {
				ans = rmi.RmiGetMasterServerGlobalCounter(username);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ans;
	}

	public ArrayList<Metadata> rmiGetCompleteMetadata(long sinceCounter){
		ArrayList<Metadata> ans = null;
		if(rmi!=null){
			try {
				ans = rmi.RmiGetCompleteMetadata(username, sinceCounter);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ans;
	}
	
	public Metadata rmiCommitFileUpdate(Metadata incompleteMetadata, String fileInfo){
		//Metadata incompleteMetadata: client should only fill some part of the metadata, the remains will be filled by master server
		//String fileInfo: should be the return from method uploadFile(String filename); fileInfo = "" for deleting a file
		Metadata ans = null;
		if(rmi!=null){
			try {
				ans = rmi.RmiCommitFileUpdate(username, incompleteMetadata, fileInfo);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ans;
	}
	
	public String uploadFile(String filename){
		//Update a file to Master Server, the file should be temporarily saved in file system on master server
		//the Master server may return the path & filename on server file system
		return null;
	}
	
	
	private class SocketThread extends Thread {

		@Override
		public void run() {
			while( SessionMaster.this.socketStream!=null ) {
				Socket socket = SessionMaster.this.socketStream.getSocket();
				if(socket==null)
					break;

				//Waiting incoming command
				SocketMessage message = (SocketMessage)socketStream.readObject();
				if(message==null){
					System.out.println("SocketThread@SessionMaster: message null error, connection lost!");
					break;
				}else{
					System.out.println("SocketThread@SessionMaster: message=" + message.command);
				}
				
				if(message.command==SocketMessage.COMMAND.EMPTY){
					//do nothing
				}else if(message.command==SocketMessage.COMMAND.UPDATE){
					//global write counter increased
					MetadataManager metadataManage = MetadataManager.getInstance();
					long globalCounter = metadataManage.getGlobalWriteCounter();
					System.out.println("SocketThread@SessionMaster: globalCounter="+globalCounter);
					ArrayList<Metadata> newMetaList = rmiGetCompleteMetadata( globalCounter );
					for(Metadata aMeta: newMetaList){
						metadataManage.updateLocalMetadata(aMeta);	//update local metadata info
						
						FileSysPerformer performer = FileSysPerformer.getInstance(); 
						performer.addUpdateLocalTask(aMeta);					
					}
				}else if(message.command==SocketMessage.COMMAND.DISCONNECT){
					break;	//do disconnect
				}
			}
			System.out.println("SocketThread@SessionMaster: thread finished.");
			super.run();
		}
		
	}
}
