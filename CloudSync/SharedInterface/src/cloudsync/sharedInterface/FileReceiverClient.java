package cloudsync.sharedInterface;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class FileReceiverClient {
	
	/**
	 * 	This is a p2p file receiver for client. It initializes request 
	 * 	for a file by passing the metadata of the file and the destination
	 *  hostname
	 */
	private int portNum = 0;
	private String hostname = null;
	private Metadata metadata = null;
	private Socket clientSocket = null;
	
	public FileReceiverClient(int portNum, String hostname, Metadata metadata){
		this.portNum = portNum;
		this.hostname = hostname;
		this.metadata = metadata;
		FileRequesterThread thread = new FileRequesterThread();
		thread.start();
	}
	
	private class FileRequesterThread extends Thread{

		private SocketStream streams = null;
		private Long length = null;
		private int tempLen = 0;
		private final int BUFF_SIZE = 1024; 
		private FileOutputStream os = null;
		
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
					System.out.println("FileReceiverClient: file length to receive" + length);
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
			try {
				os = new FileOutputStream(metadata.basename);
			} catch (FileNotFoundException e1) {
				System.out.println("FileReceiverClient: File not found" + metadata.basename);
			}
			while(true){
				if( tempLen >= length){
					System.out.println("FileReceiverClient: " + metadata.basename + "is successfully received");
					break;
				}else {
					byte[] buff = new byte[BUFF_SIZE];
					int readCount = 0;
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
