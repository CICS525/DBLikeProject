package cloudsync.sharedInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
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
	
	public FileReceiverClient(int portNum, String hostname, String rootDir, Metadata metadata){
		this.portNum = portNum;
		this.hostname = hostname;
		this.metadata = metadata;
		this.rootDir = rootDir;
		FileRequesterThread thread = new FileRequesterThread();
		thread.start();
	}
	
	private class FileRequesterThread extends Thread{

		private SocketStream streams = null;
		private String absFilePath = null;
		private Long length = null;
		private int tempLen = 0;
		private final int BUFF_SIZE = 1024; 
		
		
		@Override
		public void run() {
			try {
				clientSocket = new Socket(hostname, portNum);
				streams = new SocketStream();
				streams.initStream(clientSocket);
				// Pass the metadata to FileSenderClient 
				streams.writeObject(metadata);
				length = (Long) streams.readObject();
				if(length == -1){
					System.out.println("FileReceiverClient: file doesn't exist");
				}else {
					System.out.println("FileReceiverClient: file length to receive is " + length);
					receiveFile();
				}
			} catch (UnknownHostException e) {
				System.err.println("FileReceiverClient: Don't know about host " + hostname);
				System.exit(1);
			} catch (IOException e) {
				System.err.println("FileReceiverClient: Can't get I/O for the connection to " + hostname);
		        System.exit(1);
			}
			super.run();
		}
		
		private void receiveFile(){
			
			absFilePath = Metadata.mixRootAndFilename(rootDir, metadata.basename);
			int readCount = 0;
			System.out.println("The absolute name is "+ absFilePath);
			File file = new File(absFilePath);
			FileOutputStream os = null;
			
			try {
				os = new FileOutputStream(file);
			} catch (FileNotFoundException e1) {
				System.out.println("FileReceiverClient: File not found "+ absFilePath);
			}
			while(true){
				System.out.println("tempLen is " + tempLen);
				if( tempLen >= length){
					System.out.println("FileReceiverClient: " + absFilePath + " is successfully received");
					break;
				}else {
					byte[] buff = new byte[BUFF_SIZE];
					try {
						readCount = streams.getStreamIn().read(buff);
					} catch (IOException e) {
						System.out.println("FileReceiverClient: Can't read from the inputstream");
					}
					tempLen += readCount;
					try {
						os.write(buff, 0, readCount);
					} catch (IOException e) {
						System.out.println("FileReceiverClient: Can't read from the FileOutputStream");
					}	
				}
			}
			streams.deinitStream();
			try {
				os.close();
			} catch (IOException e) {
				System.out.println("FileReceiverClient: Can't close FileOutputStream");
			}
			try {
				clientSocket.close();
			} catch (IOException e) {
				System.out.println("FileReceiverClient: Can't close ClientSocket");
			}
		}
	}
}
