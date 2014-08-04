package test;

import static org.junit.Assert.*;

import org.junit.Test;

import cloudsync.sharedInterface.FileReceiver;
import cloudsync.sharedInterface.FileSender;



public class TestFileSendReceive {
	
	// When transferring a new file, a new instance of file Sender should be created
	// In this test case, the first file is terminated
	// We transfer three files and terminate one so that we should receive two files in the end
	@Test
	public void testFileSendReceive() throws InterruptedException {
		FileReceiver.initialize(234);
		//FileReceiver fileReceiver = FileReceiver.getInstance();
		FileSender fileSender = new FileSender(234,"localhost","C:\\Users\\Tianlai Dong\\Desktop\\test.rmvb");
		fileSender.startFileTransfer();
		Thread.sleep(1000);
		fileSender.stopFileTransfer("C:\\Users\\Tianlai Dong\\Desktop\\test.rmvb");
		//Start a new file transfer 
		FileSender fileSender2 = new FileSender(234,"localhost","C:\\Users\\Tianlai Dong\\Desktop\\test.docx");
		fileSender2.startFileTransfer();
		//FileSender fileSender3 = new FileSender(234,"localhost","C:\\Users\\Tianlai Dong\\Desktop\\test.rmvb");
		//fileSender3.startFileTransfer();
		//FileSender fileSender1 = new FileSender(234,"localhost","C:\\Users\\Tianlai Dong\\Desktop\\test.docx");
		while (true){
				Thread.sleep(1000);
		}
	}
}
