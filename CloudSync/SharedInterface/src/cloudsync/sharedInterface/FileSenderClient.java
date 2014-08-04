package cloudsync.sharedInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileSenderClient {
	
	/**
	 * 	This is a p2p file sender for client. It waits for connection from other 
	 * 	client to request a file transfer and provides with the file accordingly
	 * 	It defines following parameters:
	 * 	1. The port number that is running
	 * 	2. The rootDir in this machine 
	 */
	
	private ServerSocket serverSocket = null;
	private Socket socket = null;
	private static FileSenderClient that = null;
	private String rootDir = null;
	private BackgroundTread backgroundTread = null;
	private FileSenderThread fileSenderThread = null;

	private FileSenderClient(){
		serverSocket = null;
		backgroundTread = new BackgroundTread();
		backgroundTread.start();
	}
	
	public static boolean initialize(int port, String rootDir){
		if(that==null){
			ServerSocket tempServerSocket = null;
			try {
				tempServerSocket = new ServerSocket(port);
			} catch (IOException e) {
				System.out.println("FileSenderClient: Can't create server socket");
				return false;
			}
			that = new FileSenderClient();
			that.rootDir = rootDir;
			that.serverSocket = tempServerSocket;
			return true;
		} else 
			return false;
	}
	
	public boolean deinitalize(){
		if(that == null){
			System.out.println("FileSenderClient: file sender client hasn't been initialized");
			return false;
		}
		else {
			boolean isDeintialized = true;
			if(that.fileSenderThread != null || that.backgroundTread != null){
				if(that.fileSenderThread != null && that.fileSenderThread.isAlive()){
					that.fileSenderThread.interrupt();
					if(!that.fileSenderThread.isInterrupted())
						isDeintialized = false;
				}
				if(that.fileSenderThread != null && that.backgroundTread.isAlive()){
					that.backgroundTread.interrupt();
					if(!that.backgroundTread.isInterrupted())
						isDeintialized = false;
				} 
			}
			if(isDeintialized){
				deinitalizeSocket();
				that = null;
				System.out.println("FileSenderClient: fileSenderClient is deinitialized.");
				return true;
			}
			else{
				System.out.println("FileSenderClient: fileSenderClient can't be deinitialized.");
				return false;
			}
		}
	}
	
	public boolean deinitalizeSocket(){
		
		try {
			if(socket != null)
				socket.close();
		} catch (IOException e) {
			System.out.println("FileSenderClient: Can't deinitialize the client socket");
			return false;
		}
		
		try {
			if(serverSocket != null){
				serverSocket.close();
				return true;
			}
		} catch (IOException e) {
			System.out.println("FileSenderClient: Can't deinitialize the server socket");
			return false;
		}
		return false;
		
	}
	
	public static boolean deinitialize(){
		if(that!=null){
			that = null;
			return true;
		}else{
			return false;
		}
	}
	
	public static FileSenderClient getInstance(){
		return that;
	}
	
	private class BackgroundTread extends Thread{

		@Override
		public void run() {
			
			while(true){
				
				try {
					socket = serverSocket.accept();
				} catch (IOException e) {
					System.out.println("FileSenderClient: Can't accept client socket");
				}
				System.out.println("FileSenderClient: Client socket coming. " + socket.getRemoteSocketAddress().toString());
				fileSenderThread = new FileSenderThread(socket);
				fileSenderThread.start();
			}
		}
	}
	
	private class FileSenderThread extends Thread{
		
		private SocketStream streams = null;
		private Metadata metadata = null;
		private String absFilePath = null;
		private final int BUFF_SIZE = 1024;
		
		public FileSenderThread(Socket socket){
			super();
			streams = new SocketStream();
			streams.initStream(socket);
			metadata = (Metadata) streams.readObject();
		}
		
		@Override
		public void run() {
			
			Long length = null;
			absFilePath = Metadata.mixRootAndFilename(rootDir, metadata.basename);
			File file = new File(absFilePath);
			
			if(file.isFile()){
				length = (Long)file.length();
				System.out.println("FileSenderClient: the file exists: " + metadata.basename + " length is " + length);
				streams.writeObject(length);
				sendFile();
			}else{
				System.out.println("FileSenderClient: the file doesn't exist: " + metadata.basename);
				length = (long) -1;
				streams.writeObject(length);
			}
			
			super.run();
		}
		
		private void sendFile(){
			
			FileInputStream fis = null;
			byte[] buff = new byte[BUFF_SIZE];
			int readCount = 0;
			
			try {
				fis = new FileInputStream(absFilePath);
			} catch (FileNotFoundException e) {
				System.out.println("FileSenderClient: the file doesn't exist and can't create FileInputStream");
				System.exit(1);
			}
			while(true){
				try {
					readCount = fis.read(buff);
				} catch (IOException e) {
					System.out.println("FileSenderClient: Can't read file.");
					System.exit(1);
				}
				if (readCount == -1)
					break;
				try {
					streams.getStreamOut().write(buff,0,readCount);
				} catch (IOException e) {
					System.out.println("FileSenderClient: Can't write buff to socket");
					System.exit(1);
				}
			}
			try {
				fis.close();
			} catch (IOException e) {
				System.out.println("FileSenderClient: Can't close FileInputStream");
			}
			streams.deinitStream();
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("FileSenderClient: Can't close socket for file transfer");
			}
			System.out.println("FileSenderClient: File Send Completed...");
			
		}	
	}
	
}
