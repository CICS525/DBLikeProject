package cloudsync.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import cloudsync.sharedInterface.AccountInfo;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.ServerLocation;

public class SessionMaster {
	//SessionMaster should be singleton design pattern
	private static SessionMaster that = null;
	
	private ServerLocation masterLocation = null;
	private Socket socket = null;
	private SocketThread thread = null;
	
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
		if(socket==null){	//master server location can only be set when there is socket is not active
			masterLocation = masterServerLocation;
			return true;
		}else{
			return false;
		}
	}

	public boolean connect(String username, String password) {
		//for the entry server and master server at separated, so here may need to check username & password again
		try {
			socket = new Socket(masterLocation.url, masterLocation.port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		AccountInfo account = new AccountInfo(username, password);
		//Write account into socket. If server respond, means login OK.
		
		//Then, create a new thread to wait in-coming message for master server
		thread = new SocketThread();
		thread.start();
		
		return false;
	}
	
	public boolean disconnect(){
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		socket = null;
		return false;
	}
	
	public ArrayList<Metadata> getCompleteMetadata(long sinceCounter){
		return null;
	}
	
	public String uploadFile(String filename){
		//upate a file to Master Server, the file should be temporarily saved in file system on master server
		//the Master server may return the path & filename on server file system
		return null;
	}
	
	public Metadata commitFileUpdate(Metadata incompleteMetadata, String fileInfo){
		//Metadata incompleteMetadata: client should only fill some part of the metadata, the remains will be filled by master server
		//String fileInfo: should be the return from method uploadFile(String filename); fileInfo = "" for deleting a file
		return null;
	}
	
	
	private class SocketThread extends Thread {

		@Override
		public void run() {
			while( socket.isConnected() ){
				//wating incoming command
				
				MetadataManager metadataManage = MetadataManager.getInstance();
				ArrayList<Metadata> newMetaList = getCompleteMetadata( metadataManage.getGlobalWriteCounter() );
				for(Metadata aMeta: newMetaList){
					metadataManage.updateLocalMetadate(aMeta);	//update local metadata info
					
					FileSysPerformer performer = FileSysPerformer.getInstance(); 
					performer.addUpdateLocalTask(aMeta);					
				}
			}
			super.run();
		}
		
	}
}
