package cloudsync.client;


public interface FileSysMonitorCallback {
	public enum Action {MODIFY, DELETE, ERROR};

	public void Callback(String filename, Action action);
}
