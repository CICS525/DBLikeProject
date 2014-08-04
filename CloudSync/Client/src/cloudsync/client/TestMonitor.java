package cloudsync.client;

import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Test;

import cloudsync.client.FileSysMonitorCallback.Operation;

public class TestMonitor {

    
    @Test
    public void test() throws InterruptedException, IOException {
        //FileSysMonitor2 monitor = new FileSysMonitor2("D:\\test");
        FileSysMonitor monitor = new FileSysMonitor("D:\\test2");
        monitor.startListen(new FileSysMonitorCallback() {
            
            @Override
            public void Callback(Operation operation) {
                File file = new File(operation.filename);
                System.err.println("OUTPUT______Filename: " + operation.filename + " Action:" + operation.action.toString() + " Time: " + file.lastModified() + " Len: " + file.length());
            }
        });
        
        Thread.sleep(1000);
        
        System.out.println("---------Test Start -----------");
        
        int count;
        byte[] buffer = new byte[10000];
        
        FileInputStream fis = new FileInputStream("D:\\a.mkv");
        FileOutputStream fos = new FileOutputStream("D:\\test2\\b.mkv");
        
        while (true) {
            count = fis.read(buffer);
            if (count == -1) {
                break;
            }
            
            fos.write(buffer, 0, count);
        }
        
        fis.close();
        fos.close();
        
        System.out.println("finished");
        
        while (true) {
            Thread.sleep(1000);
        }

    }
    

}
