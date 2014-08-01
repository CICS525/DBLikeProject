package cloudsync.client;

/**
 * Class that represents a file system monitor.
 * @author Aaron Cheng
 * 
 */

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;

import cloudsync.client.FileSysMonitorCallback.Action;
import cloudsync.client.FileSysMonitorCallback.Operation;

import com.sun.nio.file.SensitivityWatchEventModifier;

public class FileSysMonitor {
	
	private class MFile{
		private File f;
		public MFile(String filename) {
			f = new File(filename);
			if(f.exists())
				return;
			
			//if(filename.startsWith(rootFolder.toString())){
			if(filename.toLowerCase().startsWith(rootFolder.toString().toLowerCase())){
				f = new File(filename);
			} else {
				FileSysPerformer fPer = FileSysPerformer.getInstance();
				f = new File(fPer.getAbsoluteFilename(filename));
			}
		}
		
		@Override
		public boolean equals(Object paramObject) {
			//return super.equals(paramObject);
			boolean ans = false;
			if(paramObject instanceof MFile ) {
				ans = f.equals(((MFile) paramObject).f);
			} else if(paramObject instanceof File) {
				ans = f.equals(paramObject);
			} else if(paramObject instanceof String) {
				ans = f.equals(new File((String) paramObject));
			}
			return ans;
		}

		@Override
		public String toString() {
			//return super.toString();
			return "File:[" + f.getAbsolutePath() + "]";
		}
	}
	
	private static boolean isListening = true;
	private ArrayList<MFile> ignoreList = new ArrayList<MFile>();
	private ArrayList<String> pathNames = new ArrayList<String>();
	private Thread watcherThread;
	private WatchService watcher;
	private Path rootFolder;
	private final HashMap<WatchKey, Path> keys = new HashMap<WatchKey, Path>();
	
	private final static Kind[] WATCHEVENTS = new Kind[]{
		//StandardWatchEventKinds.ENTRY_CREATE,
		StandardWatchEventKinds.ENTRY_DELETE,
		StandardWatchEventKinds.ENTRY_MODIFY };
	
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
	public boolean startListen(final FileSysMonitorCallback callback) {
		try {
			synchronized (watcherLock)  { watcher = FileSystems.getDefault().newWatchService(); } //lock the watcher
			WatchKey temp = rootFolder.register(watcher, WATCHEVENTS, SensitivityWatchEventModifier.HIGH);
			
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
								newTimeStamp = currFile.lastModified();	//0L if the file does not exist or if an I/O error occurs
								
								if(type!=StandardWatchEventKinds.ENTRY_DELETE && newTimeStamp==0)
									continue;
								if(type==StandardWatchEventKinds.ENTRY_MODIFY && currFile.length()==0)
									continue;
								
								String filename = child.toAbsolutePath().toString();
								//System.out.println("FileSysMonitor: WatchEvent " + type + " # " + filename + " Len:" + currFile.length() + " @ " + newTimeStamp);
								if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
									registerSubfolders(child);
									break; // ignore because it is a folder.
								}
								synchronized(ignoreList)
								{
									MFile temp = new MFile(filename);
									if ( !ignoreList.contains(temp) )
									{
										System.out.println("FileSysMonitor: NOT IN ignore_list # " + type + " # " + filename + " Len:" + currFile.length() + " @ " + oldTimeStamp + "~" + newTimeStamp);
									
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
										
										//callback.Callback(child.toAbsolutePath().toString(), action);
										callback.Callback( new Operation(child.toAbsolutePath().toString(), action) );
									}else{
										System.out.println("FileSysMonitor: IN ignore_list # " + type + " # " + filename + " Len:" + currFile.length() + " @ " + oldTimeStamp + "~" + newTimeStamp);
										ignoreList.remove( temp );
									}
								}
								
								//for(MFile ignore: ignoreList){
								//	System.out.println( "FileSysMonitor: ignore list~" + ignore.toString() );
								//}
								
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
						WatchKey temp = dir.register(watcher, WATCHEVENTS, SensitivityWatchEventModifier.HIGH);
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
	public boolean stopListen(){
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
	public boolean startIgnoreFile(String filename){
		//FileSysPerformer.java may need to update files. These action should be ignored
		boolean contains = false;
		synchronized (ignoreList)
		{
			System.out.println("FileSysMonitor: startIgnoreFile # " + filename);
			MFile temp = new MFile(filename);
			ignoreList.add(temp);
			contains = ignoreList.contains(temp);
		}
		return contains;
	}
	
	/**
	 * Allows a file to be uploaded to the cloud service.
	 * @param filenames can be absolute, relative or relative with a slash in front.
	 * @return true if the file is REMOVED from the ignore list, false otherwise
	 */
	public boolean stopIgnoreFile(String filename){
		boolean removed = false;
		synchronized (ignoreList)
		{
			System.out.println("FileSysMonitor: stopIgnoreFile #" + filename);
			ignoreList.remove( new MFile(filename) );
		}
		return removed; 
	}

	private boolean isLocked(String filename){
		boolean isLocked = true;
		
		try {
			FileInputStream is = new FileInputStream(filename);
			byte[] buff = new byte[1024];
			int len = is.read(buff);
			is.close();
			System.out.println("File len:" + len + " bin:" + buff);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		FileChannel fileChannel = null;
		try {
			fileChannel = new RandomAccessFile(filename, "rw").getChannel();
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			return false;
		}
		try {
			FileLock lock = fileChannel.tryLock();
			if (lock != null) {
				lock.close(); // file isn't locked. we close because tryLock will actually return a lock.
				isLocked = false;
			}
			fileChannel.close();
		} catch (OverlappingFileLockException | IOException e) {
			isLocked = true;
		}
		return isLocked;
	}
}
