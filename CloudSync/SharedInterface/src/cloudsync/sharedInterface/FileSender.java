package cloudsync.sharedInterface;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class FileSender {
	private int portNum = 234;
	private String hostname = "localhost";
	private FileSenderThread thread = null;
	private String filePath = "C:\\Users\\Tianlai Dong\\Desktop\\test.docx";
	private File file = null;
	private FileInputStream fis = null;
	private ObjectOutputStream dos = null;
	private ObjectInputStream dis = null;
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
				dos = new ObjectOutputStream(clientSocket.getOutputStream());
				dis = new ObjectInputStream(clientSocket.getInputStream());
				file = new File(getFilePath());
				dos.writeObject((Long)file.length());
				dos.flush();
				buff = new byte[BUFF_SIZE];
				sendFile();
			} catch (UnknownHostException e) {
				System.err.println("Client: Don't know about host " + hostname);
	            System.exit(1);
			} catch (IOException e) {
				System.err.println("Client: Couldn't get I/O for the connection to " + hostname);
		        System.exit(1);
			} 
			//super.run();
		}	
	}
	
	private String getFilePath(){
		return filePath;
	}
	
	public void setFilePath(String filePath){
		this.filePath = filePath;
	}
	
	private boolean continueSendFile(){
		boolean status = true;
		if(status)
			return true;
		else {
			thread = null;
			return false;
		}
	}
	
	private void sendFile(){
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
				try {
					String filename = (String) dis.readObject();
					System.out.println("Filename from Server: "+ filename);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
