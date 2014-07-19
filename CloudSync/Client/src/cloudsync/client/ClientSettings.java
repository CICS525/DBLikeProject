package cloudsync.client;



import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import cloudsync.sharedInterface.DefaultSetting;
import cloudsync.sharedInterface.ServerLocation;

public class ClientSettings implements Serializable {	// Serialization initialized 
	//Client Settings should be singleton design pattern
	private static final long serialVersionUID = -3650388503954215299L;

	private static ClientSettings that = null;
	private final String fSettingsFileName = ".ClientSettings.settings";		//auto hidden file on Linux & MacOS
	private String Username = null;
	private String Password = null;
	private String DeviceName = null;				//a account many have several devices, each device should have a unique name
	private String RootDir = null;					//the root (base) directory to sync
	private ServerLocation RecentMaster = null;		//backup the recent Master Server location
	
	public String getUsername() {
		return Username;
	}
	public void setUsername(String username) {
		this.Username = username;
	}
	public String getPassword() {
		return Password;
	}
	public void setPassword(String password) {
		this.Password = password;
	}
	public String getDeviceName() {
		return DeviceName;
	}
	public void setDeviceName(String deviceName) {
		this.DeviceName = deviceName;
	}
	public String getRootDir() {
		return RootDir;
	}
	public void setRootDir(String rootDir) {
		this.RootDir = rootDir;
	}
	public ServerLocation getRecentMaster() {
		if(RecentMaster!=null){
			return RecentMaster;
		}else{
			return new ServerLocation(DefaultSetting.DEFAULT_MASTER_SERVER_URL, DefaultSetting.DEFAULT_MASTER_SERVEL_PORT);
		}
	}
	public void setRecentMaster(ServerLocation recentMaster) {
		RecentMaster = recentMaster;
	}

	
	private ClientSettings(){
		//private constructor to secure singleton
		
		//set default value, to be over written in loading setting file
		String userHome = System.getProperty( "user.home" );
		setRootDir(userHome);
		setUsername("Tom");
		setPassword("123456x");
	}
	
	public static ClientSettings getInstance(){
		if(that==null){
			that = new ClientSettings();
		}
		return that;
	}
	
	public boolean loadSettings(){

		//Read all settings from file SettingsFileName
		boolean ans = false;
		ObjectInputStream is=null;
		try {
			
			is=new ObjectInputStream(new FileInputStream(fSettingsFileName));
			
			ClientSettings temp = (ClientSettings)is.readObject();
			setUsername(temp.getUsername());
			setPassword(temp.getPassword());
			setDeviceName(temp.getDeviceName());
			setRootDir(temp.getRootDir());
			setRecentMaster(temp.getRecentMaster());
			
			ans = true;
		} catch (Exception e) {
            System.out.println("ERROR"+e);
		}
		finally{
			try {
				if(is!=null)
					is.close();
			} catch (IOException e) {
				System.out.println("ERROR"+e);
				e.printStackTrace();
			}
		}
		return ans;
	}
	
	public boolean saveSettings(){

		//Write all settings into file SettingsFileName
		boolean ans = false;
		ObjectOutputStream os=null;
		try {
			os=new ObjectOutputStream(new FileOutputStream(fSettingsFileName));
			os.writeObject(this);
			ans = true;
		} catch (Exception e) {
			System.out.println("ERROR"+e);
		}
		finally{
			try {
				if(os!=null)
					os.close();
			} catch (IOException e) {
				System.out.println("ERROR"+e);
				e.printStackTrace();
			}
		}
		return ans;
	}
}
