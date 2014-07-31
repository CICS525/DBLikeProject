package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import cloudsync.client.FileSysMonitor;
import cloudsync.client.FileSysMonitorCallback;
import cloudsync.client.FileSysPerformer;
import cloudsync.client.MetadataManager;
import cloudsync.sharedInterface.FileSysCallback;
import cloudsync.sharedInterface.Metadata;

public class FileSysMonitorTest {

	@Test
	public void test() {
		String userHome = System.getProperty( "user.home" );
		String rootDir  = userHome + File.separator + "CloudSync" + File.separator;
		String filename = "abc.txt";
		FileSysMonitor monitor = new FileSysMonitor(rootDir);
		monitor.startListen(new FileSysMonitorCallback(){

			@Override
			//public void Callback(String filename, Action action) {
			public void Callback(Operation o) {
				System.out.println("onCallback:" + o.filename + o.action);
			}
			
		});
		//monitor.startIgnoreFile(filename);
		FileSysPerformer fPerf = FileSysPerformer.getInstance();
		MetadataManager mm = MetadataManager.getInstance();
		mm.readLocalMetadata();
		ArrayList<Metadata> ml = null;	//mm.getLocalMetadata();
		Metadata meta = ml.get(0);
		fPerf.addUpdateLocalTask(meta, new FileSysCallback(){

			@Override
			public void onFinish(boolean success, String filename) {
				System.out.println("onFinish:" + filename);
			}
			
		});
		//assertEquals(true, monitor.ignoreList.contains("C:\\Users\\docaholic\\Documents\\GitHub\\DBLikeProject\\CloudSync\\New Text Document.txt"));
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
