package cloudsync.sharedInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class ServerLocation implements Serializable {

	private static final long serialVersionUID = -570150120132915738L;
	public String url = null;
	public int port = 0;
	
	public ServerLocation(String url, int port) {
		super();
		this.url = url;
		this.port = port;
	}
	
	public static String getExternalIp() {
        URL whatismyip = null;
        BufferedReader in = null;
        try {
        	whatismyip = new URL("http://checkip.amazonaws.com");
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            return ip;
        } catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
		return null;
    }
}
