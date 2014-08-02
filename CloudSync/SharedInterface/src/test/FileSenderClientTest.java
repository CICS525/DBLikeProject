package test;

import cloudsync.sharedInterface.FileSenderClient;

public class FileSenderClientTest {
	public static void main(String[] args) throws InterruptedException{
		System.out.println("This is Sender");
		if(FileSenderClient.initialize(200, "C:\\Users\\Tianlai Dong"))
			System.out.println("The fileSenderClient is initialized");
		FileSenderClient fsc = FileSenderClient.getInstance();
		while(true){
			Thread.sleep(1000);
		}
	}
}
