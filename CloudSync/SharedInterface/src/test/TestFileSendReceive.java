package test;

import static org.junit.Assert.*;

import org.junit.Test;

import cloudsync.sharedInterface.FileReceiver;
import cloudsync.sharedInterface.FileSender;



public class TestFileSendReceive {

	@Test
	public void testFileSendReceive() throws InterruptedException {
		FileReceiver fileReceiver = FileReceiver.getInstance();
		FileSender fileSender = new FileSender(234,"localhost","C:\\Users\\Tianlai Dong\\Desktop\\test.rmvb");
		Thread.sleep(1000);
		fileSender.stopFileTransfer("C:\\Users\\Tianlai Dong\\Desktop\\test.rmvb");
		while (true){
				Thread.sleep(1000);
		}
	}
}
