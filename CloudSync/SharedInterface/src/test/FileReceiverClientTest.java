package test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import cloudsync.sharedInterface.FileReceiverClient;
import cloudsync.sharedInterface.Metadata;

public class FileReceiverClientTest {
	
	public static void main(String [] args) throws InterruptedException{
		int portNum = 200;
		Metadata metadata = new Metadata("Desktop\\123.txt");
		String hostname = getHostname();
		FileReceiverClient frc = new FileReceiverClient(portNum, hostname, "C:\\Users\\Tianlai Dong\\Test", metadata); 
		while(true){
			Thread.sleep(1000);
		}
	}
	
	public static String getHostname(){
		
		InetAddress inetAddr = null;
		try {
			inetAddr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		String hostname = inetAddr.getHostName();
		System.out.println("Hostname is "+hostname);
		return hostname;
	}
}
