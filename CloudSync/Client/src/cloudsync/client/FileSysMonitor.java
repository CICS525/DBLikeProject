package cloudsync.client;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import cloudsync.client.FileSysMonitorCallback.Action;
import cloudsync.client.FileSysMonitorCallback.Operation;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

public class FileSysMonitor {
    
    private static final int zeroLenFileDelay = 10000;

    private String rootFolder;
    private FileSysMonitorCallback callback;

    private int watchID;

    //private ArrayList<String> ignoreList = new ArrayList<>();
    private HashMap<String, Integer> ignoreList = new HashMap<>();
    private ArrayList<String> pendingList = new ArrayList<>();
    private HashMap<String, Long> lastModify = new HashMap<>();

    public FileSysMonitor(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public boolean startListen(final FileSysMonitorCallback callback) {
        this.callback = callback;

        int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED | JNotify.FILE_RENAMED | JNotify.FILE_MODIFIED;

        boolean watchSubtree = true;

        try {
            watchID = JNotify.addWatch(rootFolder, mask, watchSubtree, new Listener());
        } catch (JNotifyException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean stopListen() {
        boolean result = false;
        try {
            result = JNotify.removeWatch(watchID);
        } catch (JNotifyException e) {
            e.printStackTrace();
            return false;
        }

        return result;
    }

    /**
     * Ignore event from a file for one time
     * @param filename The absolute path of the file to ignore
     * @return true on success
     */
    public boolean startIgnoreFile(String filename) {
        synchronized (ignoreList) {
            if (ignoreList.containsKey(filename)) {
                ignoreList.put(filename, (ignoreList.get(filename) + 1));
            } else {
                ignoreList.put(filename, 1);
            }
            System.out.println("FileSysMonitor: startIgnoreFile # " + filename + " count: " + ignoreList.get(filename));
        }
        return true;
    }

    /**
     * Stop ignore event from a file
     * @param filename The absolute path of the file to ignore
     * @return true if the file is in ignore list and is removed
     */
    public boolean stopIgnoreFile(String filename) {
        boolean result = decreaseIgnoreCounter(filename);
        System.out.println("FileSysMonitor: stopIgnoreFile #" + filename + " -> " + result + " Count: " + ignoreList.get(filename));
        return result;
    }
    
    /**
     * Decrease ignore counter of a file by 1 if the file is in ignore map and has value larger than 1
     * @param filename the file to decrease counter
     * @return true on success, false otherwise
     */
    private boolean decreaseIgnoreCounter(String filename) {
        synchronized (ignoreList) {
            if (ignoreList.containsKey(filename)) {
                Integer count = ignoreList.get(filename) - 1;
                if (count < 0) {
                    return false;
                } else {
                    ignoreList.put(filename, count);
                    return true;
                }
            }
            return false;
        }
    }

    class Listener implements JNotifyListener {

        @Override
        public void fileCreated(int wd, String rootPath, String name) {
            Path path = Paths.get(rootPath, name);

            File file = path.toFile();
            
            // ignore creation of folder
            if (file.isDirectory()) {
                return;
            }

            handleZeroLen(file);
        }

        @Override
        public void fileDeleted(int wd, String rootPath, String name) {
            Path path = Paths.get(rootPath, name);
            String absPath = path.toAbsolutePath().toString();

            File file = path.toFile();
            
            // temperory ignore delete directory
            // TODO cannot detect delete folder
            if (file.isDirectory()) {
                return;
            }

            // remove lastModify info
            lastModify.remove(absPath);

            // do not proceed if file in ignore list
            if (decreaseIgnoreCounter(absPath)) {
                System.out.println("FileSysMonitor IGNORE: Delete File: " + file.getPath());
                System.out.println("FileSysMonitor: decrease ignore counter" + absPath + " Count: " + ignoreList.get(absPath));
                return;
            }

            System.out.println("FileSysMonitor: Deleted: " + absPath + " Len: " + file.length() + " Time: " + file.lastModified());
            callback.Callback(new Operation(path.toAbsolutePath().toString(), Action.DELETE));

        }

        @Override
        public void fileModified(int wd, String rootPath, String name) {
            Path path = Paths.get(rootPath, name);
            File file = path.toFile();

            // ignore modification on folder
            if (file.isDirectory()) {
                return;
            }

            handleZeroLen(file);
        }

        @Override
        public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
            Path path = Paths.get(rootPath, newName);
            File file = path.toFile();
            
            // ignore modification on folder
            if (file.isDirectory()) {
                return;
            }

            //Path oldPath = Paths.get(rootPath, oldName);            
            System.out.println("FileSysMonitor: Renamed " + path + " : " + oldName + " -> " + newName + " Len: " + file.length() + " time: " + file.lastModified());
            fileDeleted(wd, rootPath, oldName);
            fileModified(wd, rootPath, newName);
            //callback.Callback(new Operation(oldPath.toAbsolutePath().toString(), Action.DELETE));
            //callback.Callback(new Operation(path.toAbsolutePath().toString(), Action.MODIFY));

        }

        private void handleZeroLen(final File file) {
            final String pathName = file.getPath().toString();
            if (file.length() == 0) {
                pendingList.add(pathName);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(zeroLenFileDelay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        synchronized (pendingList) {
                            if (pendingList.contains(pathName)) {
                                System.out.println("FileSysMonitor: Uploading zero length file: " + file.getPath() + " time: " + file.lastModified());
                                pendingList.remove(pathName);
                                doModifyCallback(file);
                            }
                        }
                    }
                }).run();
            } else {
                synchronized (pendingList) {
                    pendingList.remove(pathName);
                }
                doModifyCallback(file);
            }
        }

        private void doModifyCallback(File file) {
            String absPath = file.getAbsolutePath();
            // do not proceed if file in ignore list
            if (decreaseIgnoreCounter(absPath)) {
                System.out.println("FileSysMonitor doModifyCallback IGNORE: Modify File: " + file.getPath() + " Len: " + file.length() + " Time: " + file.lastModified());
                
                // add ignored file to lastmodified
                lastModify.put(absPath, file.lastModified());
                
                System.out.println("FileSysMonitor: decrease ignore counter" + absPath + " Count: " + ignoreList.get(absPath));
                return;
            }

            // remove duplicate modification notice
            Long lastModifyTime = lastModify.get(absPath);
            if (lastModifyTime != null && lastModifyTime >= file.lastModified()) {
                return;
            }

            // update lastModify
            lastModify.put(absPath, file.lastModified());

            System.out.println("FileSysMonitor: Modified: " + absPath + " Len: " + file.length() + " Time: " + file.lastModified());
            callback.Callback(new Operation(absPath, Action.MODIFY));
        }

    }
}