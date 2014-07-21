package cloudsync.sharedInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class FileReceiver {
	//File Receiver should be singleton design pattern
	
	private final int FILE_RECEVER_PORT = 234;
	private ServerSocket serverSocket = null;
	private static FileReceiver that = null;
	private BackgroundThread thread = null;
	
	private FileReceiver(){
		//private constructor to secure singleton
		ServerSocket serverSocket = null;
		thread = new BackgroundThread();
		thread.start();
	}
	
	public static FileReceiver getInstance(){
		if(that==null){
			that = new FileReceiver();
		}
		return that; 
	}
	
	private String getTempFilename(){
		Date now = new Date();
		return "receive_" + now.getTime() + ".tmp";
	}
	
	private class BackgroundThread extends Thread{

		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(FILE_RECEVER_PORT);
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
			
			boolean loop = true;
			while(loop)
			{
				Socket socket = null;
				
				try {
					socket = serverSocket.accept();
					System.out.println("BackgroundThread@FileReceiver: Client socket coming. " + socket.getRemoteSocketAddress().toString());
					FileReceiverThread fileThread = new FileReceiverThread(socket, getTempFilename());
					fileThread.start();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
			super.run();
		}
	}
	
	private class FileReceiverThread extends Thread{
		private SocketStream streams = null;
		private String filename = null;
		private FileOutputStream os=null;

		public FileReceiverThread(Socket socket, String filename) {
			super();
			this.filename = filename;
			this.streams = new SocketStream();
			this.streams.initStream(socket);
		}


		@Override
		public void run() {
			boolean finish = false;
			Integer length = (Integer) streams.readObject();
			int len = 0;
			if(length!=null){
				try {
					os = new FileOutputStream(filename);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
					return;
				}
				
				while(true){
					if(len>=length){
						finish = true;
						break;
					}else{
						try {
							byte[] buff = null;
							len += streams.getStreamIn().read(buff);
							os.write(buff);
						} catch (IOException e) {
							e.printStackTrace();
							break;
						}
					}
				}
				
				try {
					streams.getStreamOut().writeObject(filename);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				streams.deinitStream();
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if(!finish){
					//delete file, for it is not complete
					File file = new File(filename);
					if(file!=null)
						file.delete();
				}
			}
			super.run();
		}
		
	}
}
