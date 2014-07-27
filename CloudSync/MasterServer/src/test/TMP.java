package test;

import static org.junit.Assert.*;

import org.junit.Test;

import cloudsync.master.MasterSettings;
import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.Metadata.STATUS;

public class TMP {

    @Test
    public void test() {
        MasterSettings ms = MasterSettings.getInstance();
        
        ms.setMasterAddrMain(DefaultSetting.chris_storageConnectionString);
        ms.setMasterAddrBackup(DefaultSetting.sky_storageConnectionString);
        
        ms.setMasterAddrBackup(DefaultSetting.SKY_VM_ADDR);
        ms.setMasterAddrMain(DefaultSetting.CHRIS_VM_ADDR);
        
        ms.saveSettings();
    }

}
