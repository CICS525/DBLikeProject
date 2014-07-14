package cloudsync.master;

public class SessionAccount {
	private String Username = null;
	private AccountThread thread = null;

	public String getUsername() {
		return Username;
	}
	public AccountThread getThread() {
		return thread;
	}
	
	SessionAccount(String Username){
		this.Username = Username;
	}

	private class AccountThread extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
		}
		
	}
}
