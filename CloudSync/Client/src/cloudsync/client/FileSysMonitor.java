package cloudsync.client;

/**
 * Class that represents a file system monitor.
 * @author Aaron Cheng
 * 
 */

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.io.File;
import java.io.IOException;
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
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;

import cloudsync.client.FileSysMonitorCallback.Action;

import com.sun.nio.file.SensitivityWatchEventModifier;

public class FileSysMonitor {

	private static boolean isListening = true;
	private ArrayList<String> ignoreList = new ArrayList<String>();
	private ArrayList<String> pathNames = new ArrayList<String>();
	private Thread watcherThread;
	private WatchService watcher;
	private Path rootFolder;
	private final HashMap<WatchKey, Path> keys = new HashMap<WatchKey, Path>();
	
	private Object watcherLock = new Object();
	
	
	public FileSysMonitor(String directory) {
		rootFolder = Paths.get(directory);
	}
	
	/**
	 * Watches a directory for changes, sends to FileSysMontiorCallback
	 * when event is fired, and file is available (not locked).
	 * 
	 * @param directory name of the directory to watch.
	 * @param callback the callback object
	 */
	public boolean StartListen(final FileSysMonitorCallback callback) {
		try {
			synchronized (watcherLock)  { watcher = FileSystems.getDefault().newWatchService(); } //lock the watcher
			WatchKey temp = rootFolder.register(watcher, 
					new WatchEvent.Kind[]{
						StandardWatchEventKinds.ENTRY_CREATE,
						StandardWatchEventKinds.ENTRY_DELETE,
						StandardWatchEventKinds.ENTRY_MODIFY },
					SensitivityWatchEventModifier.HIGH);
			
			keys.put(temp, rootFolder); //put into map for monitoring
			registerSubfolders(rootFolder);
			watcherThread = new Thread(new Runnable() {
				
				public void run() {
					try
					{
						WatchKey key;
						long oldTimeStamp = 0;
						long newTimeStamp = 0;
						while (isListening)
						{
							key = watcher.take();
							Path dir = keys.get(key);
							for (WatchEvent<?> event: key.pollEvents()) {
								Kind<?> type = event.kind();
								Path child = dir.resolve((Path) event.context());
								File currFile = child.toFile();
								newTimeStamp = currFile.lastModified();
								String filename = child.toAbsolutePath().toString();
								System.out.println(filename + "@" + newTimeStamp);
								if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
									registerSubfolders(child);
									break; // ignore because it is a folder.
								}
								if (!ignoreList.contains(filename))
								{
									System.out.println("FileSysMonitor: not in ignore list #" + filename);
									
									Action action = FileSysMonitorCallback.Action.ERROR;
									
									if (type == StandardWatchEventKinds.ENTRY_CREATE) {
										action = FileSysMonitorCallback.Action.MODIFY;
									} else if ((type == StandardWatchEventKinds.ENTRY_MODIFY) && (newTimeStamp > oldTimeStamp)) {
										action = FileSysMonitorCallback.Action.MODIFY;
									} else if (type == StandardWatchEventKinds.ENTRY_MODIFY && (newTimeStamp <= oldTimeStamp)) {
										continue;
									} else if (type == StandardWatchEventKinds.ENTRY_DELETE) {
										action = FileSysMonitorCallback.Action.DELETE;
									}
									callback.Callback(child.toAbsolutePath().toString(), action);
								}else{
									System.out.println("FileSysMonitor: in ignore list #" + filename);
								}
								
								for(String ign: ignoreList){
									System.out.println("FileSysMonitor: ignore list~" + ign);
								}
								
								oldTimeStamp = newTimeStamp;
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
						WatchKey temp = dir.register(watcher, 
								new WatchEvent.Kind[]{
									StandardWatchEventKinds.ENTRY_CREATE, 
									StandardWatchEventKinds.ENTRY_MODIFY, 
									StandardWatchEventKinds.ENTRY_DELETE}, 
								SensitivityWatchEventModifier.HIGH);
						System.out.println("FileSystemMonitor: Registered folder#" + dir);
						keys.put(temp, dir);
						pathNames.add(dir.toString());
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
	 * Prevents a file from being uploaded to the cloud service.
	 * 
	 * @param filenames can be absolute, relative or relative with slash in front.
	 * @return
	 */
	public synchronized boolean startIgnoreFile(String filename){
		//FileSysPerformer.java may need to update files. These action should be ignored
		String name = convertToAbsolute(filename);
		System.out.println("FileSysMonitor: startIgnoreFile #" + filename);
		ignoreList.add(name);
		return ignoreList.contains(name);
	}
	
	/**
	 * Allows a file to be uploaded to the cloud service.
	 * @param filenames can be absolute, relative or relative with a slash in front.
	 * @return true if the file is REMOVED from the ignorelist, false otherwise
	 */
	public synchronized boolean stopIgnoreFile(String filename){
		String name = convertToAbsolute(filename);
		System.out.println("FileSysMonitor: stopIgnoreFile #" + filename);
		ignoreList.remove(name);
		return !ignoreList.contains(name); 
	}
	
	/**
	 * Converts a filename to absolute (if it isn't already)
	 * @param filename
	 * @return
	 */
	public String convertToAbsolute(String filename) {
		String file = filename;
		if (filename.charAt(0) == '\\')
		{	
			file = filename.substring(1);
		}
		Path filePath = Paths.get(file);
		if (filePath.isAbsolute()) {
			return file;
		}
		return rootFolder.toString() + "\\" + file;
	}
}
