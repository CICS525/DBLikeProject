package cloudsync.client;

/**
 * Class that represents a file system monitor.
 * @author Aaron Cheng
 *
 */

import java.io.IOException;
import java.util.ArrayList;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class FileSysMonitor {

	private static boolean isListening = true;
	private ArrayList<String> ignoreList = new ArrayList<String>();
	
	/**
	 * Watches a directory for changes, sends to FileSysMontiorCallback
	 * when event is fired, and file is available (not locked).
	 * 
	 * Watches directory only, is NOT recursive (does not handle multiple
	 * directories)
	 * @param directory
	 * @param callback
	 * @throws IOException should an IOException occur
	 * @throws InterruptedException should an InterrupedException (thread terminated, etc...) occurs.
	 */
	public void StartListen(String directory, FileSysMonitorCallback callback) throws IOException, InterruptedException{
		WatchService watcher = FileSystems.getDefault().newWatchService();
		Path filePath = Paths.get(directory);
		WatchKey key;
		filePath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);
		
		while (isListening) {
			key = watcher.take();
			for (WatchEvent<?> event: key.pollEvents()) {
				callback.Callback(event.context().toString());			
			}
			boolean valid = key.reset();
			if (!valid) {
				break;
			}
		}
	}
	
	/**
	 * Stops the watcher.
	 * @return
	 */
	public boolean StopListen(){
		isListening = false;
		return false;
	}
	
	/**
	 * Checks if the file is currently locked.
	 * Default value is true; do not assumed file is not locked.
	 * @param filename
	 * @return
	 */
	public boolean isLocked(String filename) throws IOException {
		boolean isLocked = true;
		FileChannel fileChannel = new RandomAccessFile(filename, "rw").getChannel();
		try 
		{
			FileLock lock = fileChannel.tryLock();
			if (lock != null) {
				lock.close(); //file isn't locked. we clsoe because trylock will actually return a lock.
				isLocked = true;
			}
		} catch (OverlappingFileLockException e) {
			isLocked = true;
		}
		fileChannel.close();
		return isLocked;		
	}
	
	/**
	 * Prevents a file from being uploaded to the cloud service
	 * @param filename
	 * @return
	 */
	public boolean startIgnoreFile(String filename){
		//FileSysPerformer.java may need to update files. These action should be ignored.
		ignoreList.add(filename);
		return false;
	}
	
	/**
	 * Allows a file to be uploaded to the cloud service.
	 * @param filename
	 * @return
	 */
	public boolean stopIgnoreFile(String filename){
		ignoreList.remove(filename);
		return false;
	}
}
