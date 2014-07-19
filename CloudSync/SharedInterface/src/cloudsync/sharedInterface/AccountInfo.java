package cloudsync.sharedInterface;

import java.io.Serializable;

public class AccountInfo implements Serializable{

	private static final long serialVersionUID = 8152704311863891154L;
	
	private String username;
	private String password;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public AccountInfo(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	@Override
	public String toString() {
		return "AccountInfo:[" + username + "#" + password + "]";
	}
}
