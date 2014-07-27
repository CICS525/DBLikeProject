package test;

import org.junit.Test;

import cloudsync.master.MasterSettings;
import cloudsync.sharedInterface.DefaultSetting;

public class CreateMasterSettingsSky {

    @Test
    public void test() {
        MasterSettings ms = MasterSettings.getInstance();
        
        ms.setMasterAddrMain(DefaultSetting.sky_storageConnectionString);
        ms.setMasterAddrBackup(DefaultSetting.chris_storageConnectionString);
        
        ms.setMasterAddrBackup(DefaultSetting.VM_ADDR_CHRIS);
        ms.setMasterAddrMain(DefaultSetting.VM_ADDR_SKY);
        
        ms.saveSettings();
    }

}
