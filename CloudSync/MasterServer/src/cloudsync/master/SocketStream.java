package cloudsync.master;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketStream {
	private Socket socket = null;
	private ObjectInputStream  streamIn = null;
	private ObjectOutputStream streamOut = null;
	
	public SocketStream(Socket socket, ObjectInputStream streamIn, ObjectOutputStream streamOut) {
		super();
		this.socket = socket;
		this.streamIn = streamIn;
		this.streamOut = streamOut;
	}

	public Socket getSocket() {
		return socket;
	}
	public ObjectInputStream getStreamIn() {
		return streamIn;
	}
	public ObjectOutputStream getStreamOut() {
		return streamOut;
	}
}
