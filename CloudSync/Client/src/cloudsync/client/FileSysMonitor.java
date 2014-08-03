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

    private String rootFolder;
    private FileSysMonitorCallback callback;

    private int watchID;

    private ArrayList<String> ignoreList = new ArrayList<>();
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

    public boolean startIgnoreFile(String filename) {
        boolean contains = false;
        synchronized (ignoreList) {
            System.out.println("FileSysMonitor: startIgnoreFile # " + filename);
            ignoreList.add(filename);
            contains = ignoreList.contains(filename);
        }
        return contains;
    }

    public boolean stopIgnoreFile(String filename) {
        boolean removed = false;
        synchronized (ignoreList) {
            System.out.println("FileSysMonitor: stopIgnoreFile #" + filename);
            removed = ignoreList.remove(filename);
        }
        return removed;
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
            synchronized (ignoreList) {
                
                if (ignoreList.contains(absPath)) {
                    System.out.println("FileSysMonitor IGNORE: Delete File: " + file.getPath());
                    // remove entry after one use
                    ignoreList.remove(absPath);
                    return;
                }
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
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        synchronized (pendingList) {
                            if (pendingList.contains(pathName)) {
                                System.out.println("FileSysMonitor: Uploading zero length file.");
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
            synchronized (ignoreList) {
                
                if (ignoreList.contains(absPath)) {
                    System.out.println("FileSysMonitor IGNORE: Modify File: " + file.getPath() + " Len: " + file.length() + " Time: " + file.lastModified());
                    
                    lastModify.put(absPath, file.lastModified());
                    
                    // remove entry after one use
                    ignoreList.remove(absPath);
                    return;
                }
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