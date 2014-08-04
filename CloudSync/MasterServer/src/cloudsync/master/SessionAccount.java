package cloudsync.master;

import java.util.ArrayList;

import cloudsync.master.storage.AccountDatabase;
import cloudsync.sharedInterface.SocketMessage;
import cloudsync.sharedInterface.SocketStream;

public class SessionAccount {
	private String username = null;
	private ArrayList<SocketStream> socketList = null;

	public String getUsername() {
		return username;
	}
	public boolean isSocketStreamEmpty(){
		synchronized(socketList){
			return socketList.isEmpty();
		}
	}

	SessionAccount(String username) {
		this.username = username;
		socketList = new ArrayList<SocketStream>();
	}

	public boolean addSocketStream(SocketStream socketStream) {
		if (this.username == null && this.username.length() == 0)
			return false;
		
		SocketStreamThread thread = new SocketStreamThread(socketStream);
		thread.start();
		boolean suc = false;
		synchronized(socketList){
			int orilen = socketList.size();
			suc = socketList.add(socketStream);
			System.out.println("addSocketStream@SessionAccount: socketList.size=" + orilen + "=>" + socketList.size());
		}
		return suc;
	}
	
	public boolean removeSocketStream(SocketStream socketStream){
		boolean suc;
		synchronized(socketList){
			suc = socketList.remove(socketStream);
		}
		if(!suc)
			System.err.println("removeSocketStream@SessionAccount: remove failed! ERROR!!!");
		else {
		    // logout, decrement account's counter
            AccountDatabase.getInstance().logout(username);
        }
		if(isSocketStreamEmpty()){
			SessionManager sessionManager = SessionManager.getInstance();
			sessionManager.clearDeactiveAccount();
		}
		return suc;
	}
	
	public int broadcastSocketMessage(SocketMessage message){
		int counter = 0;
		synchronized(socketList){
			System.out.println("broadcastSocketMessage@SessionAccount: socketList.size=" + socketList.size());
			for(SocketStream socket: socketList){
				boolean suc = socket.writeObject(message);
				if(suc)
					counter++;
			}
		}
		return counter;
	}

	private class SocketStreamThread extends Thread {
		SocketStream socketStream = null;

		public SocketStreamThread(SocketStream socketStream) {
			super();
			this.socketStream = socketStream;
		}

		@Override
		public void run() {
			while( socketStream!=null && socketStream.getSocket()!=null ){
				SocketMessage message = (SocketMessage)socketStream.readObject();
				if(message==null){
					System.out.println("SocketStreamThread@SessionAccount: message null error, connection lost!");
					break;
				}else{
					System.out.println("SocketStreamThread@SessionAccount: message=" + message.command);
				}
				
				if(message.command==SocketMessage.COMMAND.EMPTY){
					//do nothing
				}else if(message.command==SocketMessage.COMMAND.UPDATE){
					//global write counter increased (should be from Master -> Client, not available here)
				}else if(message.command==SocketMessage.COMMAND.DISCONNECT){
					break;	//do disconnect
				}
			}
			super.run();
			
			//the current socketStream is down
			SessionAccount.this.removeSocketStream(socketStream);
			socketStream.deinitStream();
		}

	}
}
