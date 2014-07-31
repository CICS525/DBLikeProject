package cloudsync.client;


public interface FileSysMonitorCallback {
	public enum Action {MODIFY, DELETE, ERROR};

	//public void Callback(String filename, Action action);
	public void Callback(Operation operation);
	
	public class Operation{
		public String filename;
		public Action action;
		
		public Operation(String filename, Action action) {
			super();
			this.filename = filename;
			this.action = action;
		}
	}
}
