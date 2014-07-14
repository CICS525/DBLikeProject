package cloudsync.sharedInterface;

public class ServerLocation {
	public String url = null;
	public int port = 0;
	
	public ServerLocation(String url, int port) {
		super();
		this.url = url;
		this.port = port;
	}
}
