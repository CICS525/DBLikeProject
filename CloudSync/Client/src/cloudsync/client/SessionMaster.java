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
import cloudsync.sharedInterface.DefaultSetting;
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
	
	private static final long		ACTIVE_MESSAGE_INTERVAL = (100*1000);

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
		if (socketStream == null) { // master server location can only be set
									// when there is socket is not active
			masterLocation = masterServerLocation;
			return true;
		} else {
			return false;
		}
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
				long localCounter = metadataManager.getGlobalWriteCounter();
				System.out.println("SessionMaster:GlobalCounter-Server:" + serverCounter);
				System.out.println("SessionMaster:GlobalCounter-Local :" + localCounter);

				if (serverCounter > localCounter) {
					ArrayList<Metadata> metadataArray = rmi.RmiGetCompleteMetadata(username, metadataManager.getGlobalWriteCounter());
					for (final Metadata one : metadataArray) {
						System.out.println("SessionMaster: new metadata #" + " basename=" + one.basename + " status=" + one.status + " globalCounter=" + one.globalCounter);
						FileSysPerformer performer = FileSysPerformer.getInstance();
						performer.addUpdateLocalTask(one, new FileSysCallback() {

							@Override
							public void onFinish(boolean success, String filename) {
								// save the single metadata item into local
								// Metadata database and save
								MetadataManager metaDB = MetadataManager.getInstance();
								metaDB.updateLocalMetadata(one);
							}

						});
					}
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}

		// initialize the stock long link for message pushing.
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

		AccountInfo account = new AccountInfo(username, password);
		// Write account into socket. If server respond, means login OK.

		socketStream = new SocketStream();
		socketStream.initStream(socket);

		System.out.println("Connected to MasterServer: Stream ready. " + socketStream.getStreamIn() + ";" + socketStream.getStreamOut());

		socketStream.writeObject(account);

		// Then, create a new thread to wait in-coming message for master server
		threadS = new SocketThread();
		threadS.start();
		
		threadA = new ActiveThread();
		threadA.start();

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
				
				SocketMessage message = new SocketMessage(SocketMessage.COMMAND.EMPTY);
				boolean suc = SessionMaster.this.socketStream.writeObject(message);
				if(suc)
					System.out.println("ActiveThread@SessionMaster: message EMPTY");
				else
					break;
			}
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
					MetadataManager metadataManage = MetadataManager.getInstance();
					long localGlobalCounter = metadataManage.getGlobalWriteCounter();
					long serverGlobalCounter = message.infoLong;
					System.out.println("SocketThread@SessionMaster: localGlobalCounter=" + localGlobalCounter + ", serverGlobalCounter=" + serverGlobalCounter);

					if (localGlobalCounter < serverGlobalCounter) {
						ArrayList<Metadata> newMetaList = rmiGetCompleteMetadata(localGlobalCounter);
						for (final Metadata aMeta : newMetaList) {

							FileSysPerformer performer = FileSysPerformer.getInstance();
							performer.addUpdateLocalTask(aMeta, new FileSysCallback(){

								@Override
								public void onFinish(boolean success, String filename) {
									// update local metadata database
									MetadataManager.getInstance().updateLocalMetadata(aMeta);
								}
								
							});
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
			super.run();
		}

	}
}
