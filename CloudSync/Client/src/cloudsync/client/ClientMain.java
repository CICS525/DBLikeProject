package cloudsync.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cloudsync.client.FileSysMonitorCallback.Action;
import cloudsync.sharedInterface.FileSender;
import cloudsync.sharedInterface.FileSysCallback;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.Metadata.STATUS;
import cloudsync.sharedInterface.ServerLocation;
import cloudsync.sharedInterface.SocketMessage;
import cloudsync.sharedInterface.SocketMessage.COMMAND;

public class ClientMain {

	private static ClientSettings				settings		= null;
	private static ServerLocation				masterLocation	= null;
	private static SessionMaster				masterSession	= null;
	private static ArrayList<FileSysMonitor>	allFileMonitors	= new ArrayList<FileSysMonitor>();
	private static MetadataManager				metadataManager	= null;

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

	public static boolean initClientMain() {
		System.out.println("ClientMain starts ...");

		settings = ClientSettings.getInstance();
		settings.loadSettings();

		// Client should do upload first & do download.
		// This is in order to handle the file could be modified when the client
		// is not running.
		// Here should scan all local file time stamps to compare with the one
		// in local metadata.

		// masterLocation = settings.getRecentMaster();
		SessionEntry entry = SessionEntry.getInstance();
		masterLocation = entry.getMasterServerLocation(settings.getUsername(), settings.getPassword());
		if (masterLocation == null) {
			System.out.println("Can not locate Master Server");
			return false;
		}

		masterSession = SessionMaster.getInstance();
		masterSession.setMasterServerLocation(masterLocation);

		metadataManager = MetadataManager.getInstance();
		metadataManager.readLocalMetadata();

		FileSysMonitor fileMonitor = new FileSysMonitor(settings.getRootDir());
		boolean bMnt = fileMonitor.startListen(new FileSysMonitorCallback() {
			@Override
			public void Callback(String filename, Action action) {
				final String absoluteFilename = FileSysPerformer.getInstance().getAbsoluteFilename(filename);
				if (Action.MODIFY == action) {
					System.out.println("ClientMain: FileSysMonitor~Callback: Upload File:" + absoluteFilename);

					FileSender sender = new FileSender(masterLocation.url, absoluteFilename, new FileSysCallback() {

						@Override
						public void onFinish(boolean success, String tempFileOnServer) {
							if (success) {
								commitFileUpdate(Action.MODIFY, absoluteFilename, tempFileOnServer);
							}
						}

					});
					sender.startFileTransfer();

				} else if (Action.DELETE == action) {
					System.out.println("ClientMain: FileSysMonitor~Callback: Delete File:" + absoluteFilename);
					commitFileUpdate(Action.DELETE, absoluteFilename, null);
				}
			}
		});
		if (bMnt) {
			System.out.println("initClientMain@ClientMain: fileMonitor.StartListen#" + settings.getRootDir() + "->" + bMnt);
			allFileMonitors.add(fileMonitor);
		}

		System.out.println("initClientMain@ClientMain: Connecint to Master Server: " + settings.getUsername() + "#" + settings.getPassword());
		boolean bCnt = masterSession.connect(settings.getUsername(), settings.getPassword());
		return bCnt;
	}

	public static synchronized boolean commitFileUpdate(Action action, String absoluteFilename, String tempFileOnServer) {
		// metadataManager.findByBasename(FileSysPerformer.getInstance().getBaseFilename(absoluteFilename));
		Metadata incomplete = new Metadata();
		SessionMaster masterSession = SessionMaster.getInstance();

		// --- [should not change, unless conflict] ---
		incomplete.basename = FileSysPerformer.getInstance().getBaseFilename(absoluteFilename);
		Metadata parentMeta = metadataManager.findByBasename(incomplete.basename);
		if (parentMeta == null || parentMeta.status!=STATUS.LAST)
			incomplete.parent = 0;
		else
			incomplete.parent = parentMeta.globalCounter;
		// --- [to be over written by Master Server] ---
		incomplete.globalCounter = metadataManager.getGlobalWriteCounter();
		incomplete.status = Action.MODIFY == action ? STATUS.LAST : STATUS.DELETE;
		// incomplete.timestamp = new Date();
		// --- [to be set by Master Server] ---
		// incomplete.blobKey
		// incomplete.blobServer
		// incomplete.blobBackup

		while (true) {
			Metadata complete = masterSession.rmiCommitFileUpdate(incomplete, tempFileOnServer);

			if (complete != null) {
				System.out.println("commitFileUpdate@ClientMain:" + "basename=" + complete.basename + " parent=" + complete.parent + " globalCounter=" + complete.globalCounter + " status=" + complete.status);

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
					if(get)
						System.out.println("commitFileUpdate@ClientMain: CONFLICT~RENAME:" + absoluteFilename + " -> " + temp);

//					for (FileSysMonitor aMonitor : ClientMain.getAllFileMonitors()) {
//						aMonitor.stopIgnoreFile(absoluteFilename);
//					}
					return false;
				} else if (complete.status == STATUS.ERROR) {
					return false;
				} else {
					boolean suc = metadataManager.updateLocalMetadata(complete);
					System.out.println("commitFileUpdate@ClientMain: updateLocalMetadata(" + "basename=" + complete.basename + " parent=" + complete.parent + " globalCounter=" + complete.globalCounter + " status=" + complete.status + ") => " + suc);
					if(suc){
						SocketMessage message = new SocketMessage(COMMAND.UPDATE);
						message.infoLong = MetadataManager.getInstance().getGlobalWriteCounter();
						int num = masterSession.rmiBroadcastMessage(message);
						System.out.println("commitFileUpdate@ClientMain: rmiBroadcastMessage=" + num);
					}
					return suc;
				}
			}else{
				System.out.println("commitFileUpdate@ClientMain-ERROR-NULL:" + "basename=" + incomplete.basename + " parent=" + incomplete.parent + " globalCounter=" + incomplete.globalCounter + " status=" + incomplete.status);
				return false;
			}
		}
	}

	public static void main(String[] args) {
		System.out.println("java.rmi.server.hostname=" + System.getProperty("java.rmi.server.hostname"));
		System.setProperty("java.rmi.server.hostname", "127.0.0.1");
		boolean suc = initClientMain();
		System.out.println("main@ClientMain=>" + suc);
	}
}