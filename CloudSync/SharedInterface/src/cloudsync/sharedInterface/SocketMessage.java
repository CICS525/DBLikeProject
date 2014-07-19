package cloudsync.sharedInterface;

import java.io.Serializable;

public class SocketMessage implements Serializable{

	private static final long serialVersionUID = -9069178478026619154L;

	public enum COMMAND {EMPTY, UPDATE, DISCONNECT};
	
	public COMMAND command = COMMAND.EMPTY;
}
