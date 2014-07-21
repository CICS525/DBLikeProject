package cloudsync.sharedInterface;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class FileSender {
	private int portNum = 234;
	private String hostname = "localhost";
	private FileSenderThread thread = null;
	private String filePath = "C:\\Users\\Tianlai Dong\\Desktop\\test.docx";
	private File file = null;
	private FileInputStream fis = null;
	private DataOutputStream dos = null;
	private final int BUFF_SIZE = 8192;
	private int read = 0;
	private byte[] buff = null;
	private boolean finished = false; 
	private Socket clientSocket = null;
	
	
	public FileSender(int portNum, String hostname){
		this.portNum = portNum;
		this.hostname = hostname;
		thread = new FileSenderThread();
		thread.start();
	}
	
	private class FileSenderThread extends Thread{

		@Override
		public void run() {
			try {
				clientSocket = new Socket(hostname, portNum);
				fis = new FileInputStream(getFilePath());
				dos = new DataOutputStream(clientSocket.getOutputStream());
				file = new File(getFilePath());
				// dos.writeLong((long)file.length());
				// dos.flush();
				buff = new byte[BUFF_SIZE];
				sendMessage();
			} catch (UnknownHostException e) {
				System.err.println("Client: Don't know about host " + hostname);
	            System.exit(1);
			} catch (IOException e) {
				System.err.println("Client: Couldn't get I/O for the connection to " + hostname);
		        System.exit(1);
			} 
			super.run();
		}	
	}
	
	private String getFilePath(){
		return filePath;
	}
	
	public void setFilePath(String filePath){
		this.filePath = filePath;
	}
	
	private boolean continueSendFile(){
		return true;
	}
	
	private void sendMessage(){
		finished = false;
		while (continueSendFile()){
			try {
				if(fis != null)
					read = fis.read(buff);
				if(read == -1){
					finished = true;
					break;
				}
				dos.write(buff,0,read);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			if(finished){
				System.out.println("FileSender: File Send Completed...");
			}
			try {
				clientSocket.close();
				dos.close();
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
