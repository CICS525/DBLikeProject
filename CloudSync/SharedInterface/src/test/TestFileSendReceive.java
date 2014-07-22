package test;

import static org.junit.Assert.*;

import org.junit.Test;

import cloudsync.sharedInterface.FileReceiver;
import cloudsync.sharedInterface.FileSender;



public class TestFileSendReceive {

	@Test
	public void testFileSendReceive() {
		FileReceiver fileReceiver = FileReceiver.getInstance();
		FileSender fileSender = new FileSender(234,"localhost");
		while (true){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
