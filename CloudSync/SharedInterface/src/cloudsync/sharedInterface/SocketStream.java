package cloudsync.sharedInterface;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketStream {
	private Socket socket = null;
	private ObjectOutputStream streamOut = null;
	private ObjectInputStream  streamIn = null;
	
	public boolean initStream(Socket socket) {
		this.socket = socket;
		try {
			streamOut = new ObjectOutputStream(socket.getOutputStream());;
			streamIn = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if(socket!=null && streamIn!=null && streamOut!=null)
			return true;
		else
			return false;
	}
	
	public boolean deinitStream(){
		try {
			if(streamIn!=null)
				streamIn.close();
			if(streamOut!=null)
				streamOut.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
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
	public Object readObject(){
		Object object = null;
		try {
			object = streamIn.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return object;
	}
	public boolean writeObject(Object object){
		try {
			streamOut.writeObject(object);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
