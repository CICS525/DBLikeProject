package cloudsync.client;

import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import cloudsync.client.FileSysMonitorCallback.Action;
import cloudsync.client.FileSysMonitorCallback.Operation;
import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.FileSender;
import cloudsync.sharedInterface.FileSenderClient;
import cloudsync.sharedInterface.FileSysCallback;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.Metadata.STATUS;
import cloudsync.sharedInterface.ServerLocation;
import cloudsync.sharedInterface.SocketMessage;
import cloudsync.sharedInterface.SocketMessage.COMMAND;

public class ClientMain {

	private static final int					RETRY_INTERVAL	= (5 * 1000);
	private static ClientSettings				settings		= null;
	private static ServerLocation				masterLocation	= null;
	private static SessionMaster				masterSession	= null;
	private static ArrayList<FileSysMonitor>	allFileMonitors	= new ArrayList<FileSysMonitor>();
	private static MetadataManager				metadataManager	= null;
	private static ArrayList<Operation>			delayOperations	= new ArrayList<Operation>();
	private static RetryThread					commitRetry		= null;

	private static FileSysMonitorCallback		fileSysAnswer	= new FileSysMonitorCallback() {
		
			@Override
			public void Callback(final Operation operation) {

				final FileSysPerformer fp = FileSysPerformer.getInstance();
				final String absoluteFilename = fp.getAbsoluteFilename(operation.filename);
				final String basename = fp.getBaseFilename(operation.filename);
				if (Action.MODIFY == operation.action) {
					System.out.println("ClientMain: FileSysMonitor~Callback: Upload File:" + absoluteFilename);
					// LoggerClass.writeLog("ClientMain: FileSysMonitor~Callback: Upload File:" + absoluteFilename);

					if (masterLocation == null) {
						synchronized (delayOperations) {
							delayOperations.add(operation); // save for next try
						}
						return;
					}

					fp.addDelayPerformFile(basename);

					FileSender sender = new FileSender(masterLocation.url, absoluteFilename,
							new FileSysCallback() {

								@Override
								public void onFinish(boolean success, String tempFileOnServer) {
									boolean done = false;
									if (success) {
										done = commitFileUpdate(Action.MODIFY, absoluteFilename, tempFileOnServer);
									}
									if (!done) {
										synchronized (delayOperations) {
											delayOperations.add(operation); // save for next try
										}
									} else {
									}
									fp.removeDelayPerformFile(basename);
								}

							});
					sender.startFileTransfer();

				} else if (Action.DELETE == operation.action) {
					Metadata parent = MetadataManager.getInstance().findByBasename(basename);
					if(parent==null || parent.status!=STATUS.LAST){
						System.out.println("ClientMain: FileSysMonitor~Callback: Delete File SKIP:" + absoluteFilename);
					}else{
						System.out.println("ClientMain: FileSysMonitor~Callback: Delete File:" + absoluteFilename);
						boolean done = false;
						done = commitFileUpdate(Action.DELETE, absoluteFilename, null);
						if (!done) {
							synchronized (delayOperations) {
								delayOperations.add(operation); // save for next try
							}
						}						
					}
				}
			}
		};

	private static class RetryThread extends Thread {

		@Override
		public void run() {
			while (delayOperations != null) {
				try {
					Thread.sleep(RETRY_INTERVAL);
				} catch (InterruptedException e) {
					//e.printStackTrace();
					System.out.println("RetryThread: InterruptedException break.");
					break;
				}

				if (!SessionMaster.getInstance().checkSocketStreamMessageActive())
					initClientMain();

				synchronized (delayOperations) {
					int len = delayOperations.size();
					for (int i = 0; i < len; i++) {
						Operation op = delayOperations.remove(0);
						System.out.println("RetryThread:" + op.filename + "#" + op.action);
						fileSysAnswer.Callback(op);
					}
				}
			}
			super.run();
		}

	}

	public static ArrayList<FileSysMonitor> getAllFileMonitors() {
		return allFileMonitors;
	}

	public static ClientSettings getSettings() {
		return settings;
	}

	public static void setSettings(ClientSettings settings) {
		ClientMain.settings = settings;
	}

	public static SessionMaster getMasterSession() {
		return masterSession;
	}

	public static void setMasterSession(SessionMaster masterSession) {
		ClientMain.masterSession = masterSession;
	}

	private static String getHostname() {

		InetAddress inetAddr = null;
		try {
			inetAddr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		String hostname = inetAddr.getHostName();
		return hostname;
	}

	public static synchronized boolean initClientMain() {
		System.out.println("ClientMain starts ...");
		// LoggerClass.writeLog("ClientMain starts ...");
		
		settings = ClientSettings.getInstance();

		// Client should do upload first & do download.
		// This is in order to handle the file could be modified when the client
		// is not running.
		// Here should scan all local file time stamps to compare with the one
		// in local metadata.

		// masterLocation = settings.getRecentMaster();
		if (settings.loadSettings()) {
			
			FileSenderClient.deinitialize();
			boolean bLan = FileSenderClient.initialize(DefaultSetting.DEFAULT_CLIENT_DOWNLOAD_PORT, settings.getRootDir());
			if (bLan){
				System.out.println("initClientMain@ClientMain: FileSenderClient Success.");
			}else{
				System.out.println("initClientMain@ClientMain: FileSenderClient Failure.");
				return false;
			}
			
			SessionEntry entry = SessionEntry.getInstance();
			masterLocation = entry.getMasterServerLocation(settings.getUsername(), settings.getPassword());
			if (masterLocation == null) {
				System.out.println("Can not locate Master Server");
				// LoggerClass.writeLog("Can not locate Master Server");

				return false;
			}

			masterSession = SessionMaster.getInstance();
			masterSession.setMasterServerLocation(masterLocation);

			metadataManager = MetadataManager.getInstance();
			metadataManager.readLocalMetadata();

			if (allFileMonitors.size() == 0) { // no monitor set yet
				FileSysMonitor fileMonitor = new FileSysMonitor(settings.getRootDir());
				if (fileMonitor != null) {
					boolean bMnt = fileMonitor.startListen(fileSysAnswer);
					if (bMnt) {
						System.out.println("initClientMain@ClientMain: fileMonitor.StartListen#" + settings.getRootDir() + "->" + bMnt);
						// LoggerClass.writeLog("initClientMain@ClientMain: fileMonitor.StartListen#"
						// + settings.getRootDir() + "->" + bMnt);

						allFileMonitors.add(fileMonitor);
					}
				}
			}

			System.out.println("initClientMain@ClientMain: Connecint to Master Server: " + settings.getUsername() + "#" + settings.getPassword());
			// LoggerClass.writeLog("initClientMain@ClientMain: Connecint to Master Server: "
			// + settings.getUsername() + "#" + settings.getPassword());

			boolean bCnt = masterSession.connect(settings.getUsername(), settings.getPassword());

			if (bCnt && commitRetry == null) { // start retry thread
				commitRetry = new RetryThread();
				commitRetry.start();
			}

			return bCnt;
		} else {
			return false;
		}

	}

	public static MetadataManager getMetadataManager() {
		return metadataManager;
	}

	public static void setMetadataManager(MetadataManager metadataManager) {
		ClientMain.metadataManager = metadataManager;
	}

	public static synchronized boolean deinitClientMain() {

		// disable file system monitor
		for (FileSysMonitor monitor : allFileMonitors) {
			monitor.stopListen();
		}
		allFileMonitors.clear();

		// disable master session
		if(masterSession!=null)
			masterSession.disconnect();

		// disable LAN sync listener
		FileSenderClient.deinitialize();
		
		// disable retry thread
		if(commitRetry!=null)
			commitRetry.interrupt();

		return true;
	}

	public static synchronized boolean commitFileUpdate(Action action, String absoluteFilename, String tempFileOnServer) {
		// metadataManager.findByBasename(FileSysPerformer.getInstance().getBaseFilename(absoluteFilename));
		Metadata incomplete = new Metadata();
		SessionMaster masterSession = SessionMaster.getInstance();

		// --- [should not change, unless conflict] ---
		incomplete.basename = FileSysPerformer.getInstance().getBaseFilename(absoluteFilename);
		Metadata parentMeta = metadataManager.findByBasename(incomplete.basename);
		if (parentMeta == null || parentMeta.status != STATUS.LAST)
			incomplete.parent = 0;
		else
			incomplete.parent = parentMeta.globalCounter;
		// --- [to be over written by Master Server] ---
		incomplete.globalCounter = 0; // metadataManager.getSyncedGlobalWriteCounter();
		incomplete.status = Action.MODIFY == action ? STATUS.LAST : STATUS.DELETE;
		// incomplete.timestamp = new Date();
		// --- [to be set by Master Server] ---
		// incomplete.blobKey
		// incomplete.blobServer
		// incomplete.blobBackup

		while (true) {
			Metadata complete = masterSession.rmiCommitFileUpdate(incomplete, tempFileOnServer);
			// SystemTrayImplementor.displayMessage("Syncing with Cloud Folder",
			// complete.basename, MessageType.INFO);

			if (complete != null) {
				System.out.println("commitFileUpdate@ClientMain:" + "basename=" + complete.basename + " parent=" + complete.parent + " globalCounter="
						+ complete.globalCounter + " status=" + complete.status);
				// LoggerClass.writeLog("commitFileUpdate@ClientMain:" +
				// "basename=" + complete.basename + " parent=" +
				// complete.parent + " globalCounter=" + complete.globalCounter
				// + " status=" + complete.status);

				if (complete.status == STATUS.CONFLICT) {
					// should rename & try again in next FileSysMonitor callback

					int pointIdx = absoluteFilename.lastIndexOf(".");
					if (pointIdx < 0)
						pointIdx = absoluteFilename.length();
					String head = absoluteFilename.substring(0, pointIdx);
					String tail = absoluteFilename.substring(pointIdx, absoluteFilename.length());

					String temp = head + "_" + ClientSettings.getInstance().getDeviceName() + tail;
					int retry = 0;
					while (true) {
						File check = new File(temp);
						if (check.exists())
							temp = head + "_" + ClientSettings.getInstance().getDeviceName() + "(" + (retry++) + ")" + tail;
						else
							break;
					}

					File fOld = new File(absoluteFilename);
					File fNew = new File(temp);

					for (FileSysMonitor aMonitor : ClientMain.getAllFileMonitors()) {
						aMonitor.startIgnoreFile(absoluteFilename);
					}

					boolean get = fOld.renameTo(fNew);
					if (get)
						System.out.println("commitFileUpdate@ClientMain: CONFLICT~RENAME:" + absoluteFilename + " -> " + temp);

					// for (FileSysMonitor aMonitor :
					// ClientMain.getAllFileMonitors()) {
					// aMonitor.stopIgnoreFile(absoluteFilename);
					// }
					return true;
				} else if (complete.status == STATUS.ERROR) {
					return false;
				} else {
					boolean suc = metadataManager.updateLocalMetadata(complete);
					System.out.println("commitFileUpdate@ClientMain: updateLocalMetadata(" + "basename=" + complete.basename + " parent=" + complete.parent + " globalCounter=" + complete.globalCounter + " status=" + complete.status + ") => " + suc);
					if (suc) {
						if (complete.globalCounter == 1 + metadataManager.getSyncedGlobalWriteCounter())
							metadataManager.setSyncedGlobalWriteCounter(complete.globalCounter);

						SocketMessage message = new SocketMessage(COMMAND.UPDATE);
						message.infoLong = complete.globalCounter;
						// Get the local IP address from this machine;
						message.infoString = getHostname();
						int num = masterSession.rmiBroadcastMessage(message);
						System.out.println("commitFileUpdate@ClientMain: rmiBroadcastMessage=" + num);
						
						if(complete.status == STATUS.DELETE){
							messageSystemTray("Delete Synced", complete.basename, MessageType.NONE);
						}else{
							messageSystemTray("Uploaded", complete.basename, MessageType.NONE);
						}
					}
					return suc;
				}
			} else {
				System.out.println("commitFileUpdate@ClientMain-ERROR-NULL:" + "basename=" + incomplete.basename + " parent=" + incomplete.parent + " globalCounter=" + incomplete.globalCounter + " status=" + incomplete.status);
				return false;
			}
		}
	}
	
	public static synchronized void messageSystemTray(String title, String boday, MessageType type){
		SystemTrayImplementor.displayMessage(title, boday, type);
	}

//	public static void main(String[] args) {
//
//		boolean suc = initClientMain();
//
//		System.out.println("main@ClientMain=>" + suc);
//	}

}