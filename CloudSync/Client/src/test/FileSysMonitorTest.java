package test;

import static org.junit.Assert.*;
import cloudsync.client.*;
import org.junit.Test;

public class FileSysMonitorTest {

	@Test
	public void test() {
		FileSysMonitor monitor = new FileSysMonitor("C:\\Users\\docaholic\\Documents\\GitHub\\DBLikeProject\\CloudSync");
		monitor.startIgnoreFile("New Text Document.txt");
		for(String temp: monitor.ignoreList) {
			System.out.println(temp);
		}
		
		assertEquals(true, monitor.ignoreList.contains("C:\\Users\\docaholic\\Documents\\GitHub\\DBLikeProject\\CloudSync\\New Document.txt"));
	}

}
