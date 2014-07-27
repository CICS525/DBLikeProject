package test;

import org.junit.Test;

import cloudsync.master.MasterSettings;
import cloudsync.sharedInterface.DefaultSetting;

public class CreateMasterSettingsChris {

    @Test
    public void test() {
        MasterSettings ms = MasterSettings.getInstance();
        
        ms.setMasterAddrMain(DefaultSetting.chris_storageConnectionString);
        ms.setMasterAddrBackup(DefaultSetting.sky_storageConnectionString);
        
        ms.setMasterAddrBackup(DefaultSetting.VM_ADDR_SKY);
        ms.setMasterAddrMain(DefaultSetting.VM_ADDR_CHRIS);
        
        ms.saveSettings();
    }

}
