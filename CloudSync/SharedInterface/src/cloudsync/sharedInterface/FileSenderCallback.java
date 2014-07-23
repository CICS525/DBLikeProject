package cloudsync.sharedInterface;

public interface FileSenderCallback {
	public void showSenderStatus();   
}

class showSuccessCallback implements FileSenderCallback {

	@Override
	public void showSenderStatus() {
		// TODO Auto-generated method stub
		System.out.println("Callback: File Sending Completed...");
	}
	
}

class showFailureCallback implements FileSenderCallback {

	@Override
	public void showSenderStatus() {
		// TODO Auto-generated method stub
		System.out.println("Callback: File Sending Terminated...");
	}
	
}
