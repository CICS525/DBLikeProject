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
	private FileSysCallback callback = null;
	//private static boolean stop = false;
	
	
	public FileSender(int portNum, String hostname, String filePath){
		this.portNum = portNum;
		this.hostname = hostname;
		this.filePath = filePath;
		// thread = new FileSenderThread();
		// thread.start();
	}
	
	public FileSender(String hostname, String filePath, FileSysCallback callback){
		this(DefaultSetting.DEFAULT_MASTER_UPLOAD_PORT, hostname, filePath);
		this.callback = callback; 
	}

	public FileSender(String hostname, String filePath){
		this(hostname, filePath, null);
	}

	public synchronized void startFileTransfer(){
		if(thread == null || thread.isInterrupted()){
			thread = new FileSenderThread();
			thread.start();
		}
	}
	
	public synchronized void stopFileTransfer(String filePath){
		if(this.filePath.equals(filePath)){
			thread.interrupt();
			//stop = true;
		}
		/*
		else {
			stop = false;
		}*/
	}
	
	private class FileSenderThread extends Thread{

		@Override
		public void run() {
			boolean suc = false;
			//stop = false;
			try {
				clientSocket = new Socket(hostname, portNum);
				fis = new FileInputStream(getFilePath());
				dos = new ObjectOutputStream(clientSocket.getOutputStream());
				dis = new ObjectInputStream(clientSocket.getInputStream());
				file = new File(getFilePath());
				dos.writeObject((Long)file.length());
				dos.flush();
				buff = new byte[BUFF_SIZE];
				suc = sendFile();
			} catch (UnknownHostException e) {
				System.err.println("FileSender: UnknownHostException #" + hostname);
			} catch (IOException e) {
				System.err.println("FileSender: IOException in connecting to #" + hostname);
			} 
			//super.run();
			if(callback!=null){
				callback.onFinish(suc, filePath);
			}
		}	
	}
	
	private String getFilePath(){
		return filePath;
	}
	
	public void setFilePath(String filePath){
		this.filePath = filePath;
	}
	/*
	private boolean continueSendFile(){
		boolean status = true;
		if(status)
			return true;
		else {
			thread = null;
			return false;
		}
	}*/
	
	private boolean sendFile(){
		boolean ans = false;
		finished = false;
		while (true){
			if(thread.isInterrupted()){
				break;
			}
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
					ans = true;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("FileSender: File Send Terminated...");
				thread = null;
			}
			try {
				clientSocket.close();
				dos.close();
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ans;
	}
}
