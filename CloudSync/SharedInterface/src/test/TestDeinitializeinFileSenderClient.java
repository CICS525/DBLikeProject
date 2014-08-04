package test;

import static org.junit.Assert.*;

import org.junit.Test;

import cloudsync.sharedInterface.FileReceiverClient;
import cloudsync.sharedInterface.FileSenderClient;

public class TestDeinitializeinFileSenderClient {

	FileSenderClient fsc = null;
	@Test
	public void test() throws InterruptedException {
		System.out.println("This is Sender");
		if(FileSenderClient.initialize(200, "C:\\Users\\Tianlai Dong")){
			fsc = FileSenderClient.getInstance();
			System.out.println("The fileSenderClient is initialized");
		}
		else 
			System.out.println("Fail to initialize fileSenderClient");
		
		Thread.sleep(5000);
		FileSenderClient.deinitalize();
		Thread.sleep(5000);
		System.out.println("This is Sender");
		if(FileSenderClient.initialize(200, "C:\\Users\\Tianlai Dong"))
			System.out.println("The fileSenderClient is initialized");
		else 
			System.out.println("Fail to initialize fileSenderClient");
		
		while(true){
			Thread.sleep(1000);
		}
	}

}
