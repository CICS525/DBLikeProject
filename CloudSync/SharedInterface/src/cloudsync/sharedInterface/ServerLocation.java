package cloudsync.sharedInterface;

import java.io.Serializable;

public class ServerLocation implements Serializable {

	private static final long serialVersionUID = -570150120132915738L;
	public String url = null;
	public int port = 0;
	
	public ServerLocation(String url, int port) {
		super();
		this.url = url;
		this.port = port;
	}
}
