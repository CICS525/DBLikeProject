package cloudsync.client;

import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import cloudsync.sharedInterface.AccountInfo;
import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.FileReceiverClient;
import cloudsync.sharedInterface.FileSysCallback;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.RemoteInterface;
import cloudsync.sharedInterface.ServerLocation;
import cloudsync.sharedInterface.SocketMessage;
import cloudsync.sharedInterface.SocketStream;

public class SessionMaster {
	// SessionMaster should be singleton design pattern
	private static SessionMaster	that			= null;

	private ServerLocation			masterLocation	= null;
	private SocketStream			socketStream	= null;
	private SocketThread			threadS			= null;
	private ActiveThread			threadA			= null;
	private RemoteInterface			rmi				= null;
	private String					username		= null;
	
	private static final long		ACTIVE_MESSAGE_INTERVAL = (60*1000);

	private SessionMaster() {
		// private constructor to secure singleton
	}

	public static SessionMaster getInstance() {
		if (that == null) {
			that = new SessionMaster();
		}
		return that;
	}

	public ServerLocation getMasterServerLocation() {
		return masterLocation;
	}

	public boolean setMasterServerLocation(ServerLocation masterServerLocation) {
		if (socketStream == null) { // master server location can only be set when there is socket is not active
			masterLocation = masterServerLocation;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean checkSocketStreamMessageActive(){
		return (socketStream!=null);
	}

	public boolean connect(String username, String password) {
		// for the entry server and master server at separated, so here may need
		// to check username & password again
		this.username = username;

		MetadataManager metadataManager = MetadataManager.getInstance();

		// initialize the RMI interface.
		try {
			//System.setProperty("java.rmi.server.hostname", "cloudsync");
			System.out.println("Connecting to MasterServer - RIM: " + masterLocation.url + "@" + DefaultSetting.DEFAULT_MASTER_RMI_PORT);
			Registry registry = LocateRegistry.getRegistry(masterLocation.url, DefaultSetting.DEFAULT_MASTER_RMI_PORT);
			rmi = (RemoteInterface) registry.lookup(RemoteInterface.RMI_ID);
			System.out.println("~Connected to MasterServer - RIM: " + rmi.toString());
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		} catch (NotBoundException e) {
			e.printStackTrace();
			return false;
		}

		try {
			boolean suc = rmi.RmiCheckUsernamePassword(username, password);
			if (!suc) {
				suc = rmi.RmiCreateAccount(username, password);
				if (!suc)
					return false;
			}

			if (suc) { // fetch metadata from Master Server
				long serverCounter = rmi.RmiGetMasterServerGlobalCounter(username);
				long localCounter = metadataManager.getSyncedGlobalWriteCounter();
				System.out.println("SessionMaster:GlobalCounter-Server:" + serverCounter);
				System.out.println("SessionMaster:GlobalCounter-Local :" + localCounter);

				if (serverCounter > localCounter) {
					int toGet = getMetadataAndBlob(localCounter, null);
					if(toGet==0){	//nothing to retrieve, local is updated
						metadataManager.setSyncedGlobalWriteCounter(serverCounter);
					}
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}

		// initialize the socket long link for message pushing.
		Socket socket = null;
		try {
			System.out.println("Connecting to MasterServer - Command Message: " + masterLocation.url + "@" + DefaultSetting.DEFAULT_MASTER_MESSAGE_PORT);
			socket = new Socket(masterLocation.url, DefaultSetting.DEFAULT_MASTER_MESSAGE_PORT);
			System.out.println("~Connected to MasterServer - Command Message: " + socket.getRemoteSocketAddress().toString());
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		if(socketStream==null){
			socketStream = new SocketStream();
			socketStream.initStream(socket);

			System.out.println("Connected to MasterServer: Stream ready. " + socketStream.getStreamIn() + ";" + socketStream.getStreamOut());

			// Write account into socket. If server respond, means login OK.
			AccountInfo account = new AccountInfo(username, password);
			socketStream.writeObject(account);
		}else{
			// the socket is useless, for these is already a socket long link for message pushing.
			try {
				socket.close();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}


		// Then, create a new thread to wait in-coming message for master server
		if(threadS==null){
			threadS = new SocketThread();
			threadS.start();
		}
		
		if(threadA==null){
			threadA = new ActiveThread();
			threadA.start();
		}

		return true;
	}

	public boolean disconnect() {
		boolean suc = socketStream.deinitStream();
		socketStream = null;
		rmi = null;
		username = null;
		return suc;
	}

	public boolean rmiCheckUsernamePassword(String username, String password) {
		boolean ans = false;
		if (rmi != null) {
			try {
				ans = rmi.RmiCheckUsernamePassword(username, password);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ans;
	}

	public long rmiGetMasterServerGlobalCounter() {
		long ans = -1;
		if (rmi != null) {
			try {
				ans = rmi.RmiGetMasterServerGlobalCounter(username);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ans;
	}

	public ArrayList<Metadata> rmiGetCompleteMetadata(long sinceCounter) {
		ArrayList<Metadata> ans = null;
		if (rmi != null) {
			try {
				ans = rmi.RmiGetCompleteMetadata(username, sinceCounter);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ans;
	}

	public Metadata rmiCommitFileUpdate(Metadata incompleteMetadata, String fileInfo) {
		// Metadata incompleteMetadata: client should only fill some part of the
		// metadata, the remains will be filled by master server
		// String fileInfo: should be the return from method uploadFile(String
		// filename); fileInfo = "" for deleting a file
		Metadata ans = null;
		if (rmi != null) {
			try {
				ans = rmi.RmiCommitFileUpdate(username, incompleteMetadata, fileInfo);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ans;
	}

	public int rmiBroadcastMessage(SocketMessage message) {
		int ans = -1;
		if (rmi != null) {
			try {
				ans = rmi.RmiBroadcastMessage(username, message);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ans;
	}

	public String uploadFile(String filename) {
		// Update a file to Master Server, the file should be temporarily saved
		// in file system on master server
		// the Master server may return the path & filename on server file
		// system
		return null;
	}
	
	private class ActiveThread extends Thread {

		@Override
		public void run() {
			while( SessionMaster.this.socketStream != null ){
				Socket socket = SessionMaster.this.socketStream.getSocket();
				if( socket == null)
					break;
				
				try {
					Thread.sleep(ACTIVE_MESSAGE_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(SessionMaster.this.socketStream!=null){
					SocketMessage message = new SocketMessage(SocketMessage.COMMAND.EMPTY);
					boolean suc = SessionMaster.this.socketStream.writeObject(message);
					if( suc ) {
						//System.out.println("ActiveThread@SessionMaster: message EMPTY");
					} else {
						break;
					}
				}
			}
			
			SessionMaster.this.threadA = null;
			super.run();
		}
		
	}

	private class SocketThread extends Thread {

		@Override
		public void run() {
			while (SessionMaster.this.socketStream != null) {
				Socket socket = SessionMaster.this.socketStream.getSocket();
				if (socket == null)
					break;

				// Waiting incoming command
				SocketMessage message = (SocketMessage) socketStream.readObject();
				if (message == null) {
					System.out.println("SocketThread@SessionMaster: message null error, connection lost!");
					break;
				} else {
					System.out.println("SocketThread@SessionMaster: message=" + message.command);
				}

				if (message.command == SocketMessage.COMMAND.EMPTY) {
					// do nothing
				} else if (message.command == SocketMessage.COMMAND.UPDATE) {
					// global write counter increased
					MetadataManager metadataManager = MetadataManager.getInstance();
					long localCounter = metadataManager.getSyncedGlobalWriteCounter();
					long serverCounter = message.infoLong;
					String lanHostname = message.infoString;
					System.out.println("SocketThread@SessionMaster: localGlobalCounter=" + localCounter + ", serverGlobalCounter=" + serverCounter + ", lanHostname=" + lanHostname);

					if (localCounter < serverCounter) {
						int toGet = getMetadataAndBlob(localCounter, lanHostname);
						if(toGet==0){	//nothing to retrieve, local is updated
							metadataManager.setSyncedGlobalWriteCounter(serverCounter);
						}

					}
				} else if (message.command == SocketMessage.COMMAND.DISCONNECT) {
					break; // do disconnect
				}
			}
			System.out.println("SocketThread@SessionMaster: thread finished.");
			
			if(SessionMaster.this.threadA!=null){
				//SessionMaster.this.threadA.stop();
				SessionMaster.this.threadA = null;
			}
			
			SessionMaster.this.socketStream = null;
			
			super.run();
		}

	}
	
	private void increaseCounter(final ArrayList<Metadata> metaList, final Metadata aMeta ){
		final MetadataManager mm = MetadataManager.getInstance();
		for(Metadata one : metaList){
			long currentCnt = mm.getSyncedGlobalWriteCounter();
			
			if(one.globalCounter<=currentCnt)
				continue;	//nothing to do with increase global counter
			
			if(mm.includeNewerMetadata(aMeta)){
				boolean b = mm.setSyncedGlobalWriteCounter(aMeta.globalCounter);
				if(b)
					System.out.println("getMetadataAndBlob@SessionMaster: localGlobalCounter=" + mm.getSyncedGlobalWriteCounter());
			}
		}
	}
	
	private interface FileSysCallbackEx extends FileSysCallback{
		void setExFlag(boolean flag);
	}
	
	private int getMetadataAndBlob(long since, String lanHostname){
		
		final ArrayList<Metadata> newMetaList = rmiGetCompleteMetadata(since);
		final MetadataManager mm = MetadataManager.getInstance();
		
		for (final Metadata aMeta : newMetaList) {
			if(mm.includeNewerMetadata(aMeta)) {
				System.out.println("getMetadataAndBlob@SessionMaster: OLD metadata #" + " basename=" + aMeta.basename + " status=" + aMeta.status + " globalCounter=" + aMeta.globalCounter + " hostname=" + lanHostname);
				increaseCounter(newMetaList, aMeta);
				continue;	//skip
			} else {
				System.out.println("getMetadataAndBlob@SessionMaster: NEW metadata #" + " basename=" + aMeta.basename + " status=" + aMeta.status + " globalCounter=" + aMeta.globalCounter + " hostname=" + lanHostname);
			}
			
			final FileSysCallbackEx wanCallback = new FileSysCallbackEx(){
				private boolean bFlag = true;	//set default as true, to display message

				@Override
				public void onFinish(boolean success, String filename) {
					if(success){
						// update local metadata database
						boolean u = mm.updateLocalMetadata(aMeta);
						System.out.println("getMetadataAndBlob@SessionMaster: updateLocalMetadata:" + filename + "->" + u);
						
						//for(Metadata bMeta : newMetaList){
						//	long currentCnt = mm.getSyncedGlobalWriteCounter();
						//	
						//	if(bMeta.globalCounter<=currentCnt)
						//		continue;	//nothing to do with increase global counter
						//	
						//	if(mm.includeNewerMetadata(aMeta)){
						//		boolean b = mm.setSyncedGlobalWriteCounter(aMeta.globalCounter);
						//		if(b)
						//			System.out.println("getMetadataAndBlob@SessionMaster: localGlobalCounter=" + mm.getSyncedGlobalWriteCounter());
						//	}
						//}
						increaseCounter(newMetaList, aMeta);
						
						if(bFlag)
							ClientMain.messageSystemTray("Downloaded", aMeta.basename, MessageType.NONE);
					}
				}

				@Override
				public void setExFlag(boolean flag) {
					bFlag = flag;
				}
			};
			final FileSysCallback lanCallback = new FileSysCallback(){

				@Override
				public void onFinish(boolean success, String filename) {
					if(success){
						wanCallback.setExFlag(false);
						wanCallback.onFinish(success, filename);
						ClientMain.messageSystemTray("Downloaded via LAN", aMeta.basename, MessageType.NONE);
					} else {
						FileSysPerformer performer = FileSysPerformer.getInstance();
						performer.addUpdateLocalTask(aMeta, wanCallback);
					}
				}
			};
			
			if(lanHostname==null || lanHostname.length()==0){
				FileSysPerformer performer = FileSysPerformer.getInstance();
				performer.addUpdateLocalTask(aMeta, wanCallback);
			}else{
				for (FileSysMonitor aMonitor : ClientMain.getAllFileMonitors()) {
					aMonitor.startIgnoreFile( FileSysPerformer.getInstance().getAbsoluteFilename(aMeta.basename) );
				}
				new FileReceiverClient(lanHostname, DefaultSetting.DEFAULT_CLIENT_DOWNLOAD_PORT, ClientSettings.getInstance().getRootDir(), aMeta, lanCallback); 
			}
		}
		
		return newMetaList.size();
	}
}
