package cloudsync.sharedInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class FileReceiverClient {
	
	/**
	 * 	This is a p2p file receiver for client. It initializes request 
	 * 	for a file by passing the metadata of the file. 
	 *  It defines following parameters:
	 *  1. The hostname of the machine that it tries to connect to 
	 *  2. The port number of that is running on that machine
	 *  3. Its own root directory to save the file
	 *  
	 */
	private int portNum = 0;
	private String hostname = null;
	private Metadata metadata = null;
	private Socket clientSocket = null;
	private String rootDir = null;
	private FileSysCallback callback = null;		
	
	public FileReceiverClient(String hostname, int portNum, String rootDir, Metadata metadata, FileSysCallback callback){
		this.portNum = portNum;
		this.hostname = hostname;
		this.metadata = metadata;
		this.rootDir = rootDir;
		this.callback = callback;
		FileRequesterThread thread = new FileRequesterThread();
		thread.start();
	}
	
	private class FileRequesterThread extends Thread{

		private SocketStream streams = null;
		private String absFilePath = null;
		private Long length = null;
		private final int BUFF_SIZE = 1024; 
		
		@Override
		public void run() {
			boolean suc = false;
			try {
				int getLen = 0;
				clientSocket = new Socket(hostname, portNum);
				streams = new SocketStream();
				
				streams.initStream(clientSocket);
				SocketAddress localIP = streams.getSocket().getLocalSocketAddress();
				SocketAddress remotIP = streams.getSocket().getRemoteSocketAddress();
				System.out.println("FileReceiverClient: SocketStream # " + "local:" + localIP + " remote:" + remotIP);
				

				// Pass the metadata to FileSenderClient 
				streams.writeObject(metadata);
				length = (Long) streams.readObject();
				if(length == -1){
					System.out.println("FileReceiverClient: file doesn't exist");
				}else {
					System.out.println("FileReceiverClient: file length to receive is:" + length);
					getLen = receiveFile();
					System.out.println("FileReceiverClient: file length receiveed is :" + getLen);
					if(getLen>=length){
						suc = true;
					}
				}
			} catch (UnknownHostException e) {
				System.err.println("FileReceiverClient: Don't know about host " + hostname);
			} catch (IOException e) {
				System.err.println("FileReceiverClient: Can't get I/O for the connection to " + hostname);
			}
			
			if(callback!=null){
				callback.onFinish(suc, metadata.basename);
			}
			super.run();
		}
		
		private int receiveFile(){
			
			absFilePath = Metadata.mixRootAndFilename(rootDir, metadata.basename);
			int tempLen = 0;
			int readCount = 0;
			System.out.println("FileReceiverClient: The absolute name is "+ absFilePath);
			FileOutputStream os = null;
			
			prepareFolder();
			while(true){
				//System.out.println("FileReceiverClient: tempLen is " + tempLen);
				if( tempLen >= length){
					System.out.print("->" + tempLen);
					break;
				}else {
					byte[] buff = new byte[BUFF_SIZE];
					try {
						readCount = streams.getStreamIn().read(buff);
					} catch (IOException e) {
						System.out.println("FileReceiverClient: Can't read from inputstream");
					}
					if(readCount<0){
						System.out.println("FileReceiverClient: read from the inputstream = " + readCount);
					}else{
						try {
							if(os==null){
								try {
									File file = new File(absFilePath);
									os = new FileOutputStream(file);
								} catch (FileNotFoundException e1) {
									System.out.println("FileReceiverClient: File not found "+ absFilePath);
								}
							}
							os.write(buff, 0, readCount);
						} catch (IOException e) {
							System.out.println("FileReceiverClient: Can't write into FileOutputStream");
						}
						tempLen += readCount;
					}
				}
			}
			streams.deinitStream();
			try {
				if(os!=null)
					os.close();
			} catch (IOException e) {
				System.out.println("FileReceiverClient: Can't close FileOutputStream");
			}
			try {
				clientSocket.close();
			} catch (IOException e) {
				System.out.println("FileReceiverClient: Can't close ClientSocket");
			}
			
			return tempLen;
		}
		
		
		private boolean prepareFolder(){
			// If the file isn't located in root directory, create folders for this file if the folders don't exist
			String folder = absFilePath.substring(0, absFilePath.lastIndexOf(File.separator));
			File directory = new File(folder);
			if(!directory.exists()){
				System.out.println("Creating a directory " + folder);
				try {
					directory.mkdirs();
					return true;
				} catch (SecurityException se) {
					System.out.println(se.getMessage());
					return false;
				}
			}
			else
				return false;
		}
	}
}
