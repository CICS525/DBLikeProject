package cloudsync.client;

/**
 * Class that represents a file system monitor.
 * @author Aaron Cheng
 * 
 * TODO: Only catch the SECOND modify event by comparing modify event times.
 *
 */

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.attribute.*;

import static java.nio.file.LinkOption.*;
import cloudsync.client.FileSysMonitorCallback.Action;

public class FileSysMonitor {

	private static boolean isListening = true;
	private ArrayList<String> ignoreList = new ArrayList<String>();
	private ArrayList<String> pathNames = new ArrayList<String>();
	private Thread watcherThread;
	private WatchService watcher;
	private Path rootFolder;
	private final HashMap<WatchKey, Path> keys = new HashMap<WatchKey, Path>();
	
	private Object watcherLock = new Object();
	private Object eventLock = new Object();
	
	
	/**
	 * Watches a directory for changes, sends to FileSysMontiorCallback
	 * when event is fired, and file is available (not locked).
	 * 
	 * @param directory name of the directory to watch.
	 * @param callback the callback object
	 */
	public boolean StartListen(String directory, final FileSysMonitorCallback callback) {
		try {
			synchronized (watcherLock)  { watcher = FileSystems.getDefault().newWatchService(); } //lock the watcher
			rootFolder = Paths.get(directory);
			WatchKey temp = rootFolder.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			
			keys.put(temp, rootFolder); //put into map for monitoring
			watcherThread = new Thread(new Runnable() {
				
				public void run() {
					try
					{
						WatchKey key;
						while (isListening)
						{
							key = watcher.take();
							for (WatchEvent<?> event: key.pollEvents()) {
								Path child = (Path) event.context();
								String filename = child.toAbsolutePath().toString();
								if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
									registerSubfolders(child);
									break; // ignore because it is a folder.
								}
								if (!ignoreList.contains(filename))
								{
									Action action = FileSysMonitorCallback.Action.ERROR;
									Kind<?> type = event.kind();
									if(type==StandardWatchEventKinds.ENTRY_CREATE) { action = FileSysMonitorCallback.Action.MODIFY; } else 
									if(type==StandardWatchEventKinds.ENTRY_MODIFY) { action = FileSysMonitorCallback.Action.MODIFY; } else 
									if(type==StandardWatchEventKinds.ENTRY_DELETE) { action = FileSysMonitorCallback.Action.DELETE; }
									callback.Callback(child.toAbsolutePath().toString(), action);
								}
							}
							boolean valid = key.reset();
							if (!valid) {
								keys.remove(key);
								if (keys.isEmpty()) { break; }
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			watcherThread.start(); //start the watcher service thread
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Register the folder along with any child directories for monitoring.
	 * @param root folder
	 * @throws IOException
	 */
	private void registerSubfolders(final Path root) throws IOException {
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override 
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) throws IOException
			{
				synchronized (watcherLock)
				{
					if (!pathNames.contains(dir.toString()))
					{
						WatchKey temp = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, 
							StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
						System.out.println("Registered folder!");
						pathNames.add(dir.toString());
						keys.put(temp, dir);
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	/**
	 * Stops the watcher.
	 * Synchronized to prevent multiple access to the the isListening variable.
	 * @return true if isListening has been stopped, false otherwise.
	 */
	public boolean StopListen(){
		synchronized (this)
		{
			isListening = false;
		}
		return !isListening ? true : false;
	}
	
	/**
	 * Checks if the file is currently locked.
	 * Default value is true; do not assumed file is not locked.
	 * @param filename
	 * @return
	 */
	private boolean isLocked(String filename) throws IOException {
		boolean isLocked = true;
		FileChannel fileChannel = new RandomAccessFile(filename, "rw").getChannel();
		FileLock lock = null;
		try 
		{
			lock = fileChannel.tryLock();
			if (lock != null) {
				lock.close(); //file isn't locked. we clsoe because trylock will actually return a lock.
				isLocked = false;
			}
		} catch (Exception e) {
			isLocked = true;
		} finally {
			lock.release();
			lock.close();
		}
		fileChannel.close();
		return isLocked;		
	}
	
	/**
	 * Prevents a file from being uploaded to the cloud service.
	 * 
	 * @param filename absolute path of the file to be ignored.
	 * @return
	 */
	public synchronized boolean startIgnoreFile(String filename){
		//FileSysPerformer.java may need to update files. These action should be ignored.
		ignoreList.add(filename);
		return false;
	}
	
	/**
	 * Allows a file to be uploaded to the cloud service.
	 * @param filename absolute path of the file to be ignored.
	 * @return
	 */
	public synchronized boolean stopIgnoreFile(String filename){
		ignoreList.remove(filename);
		return false;
	}
}
