package cloudsync.client;

import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.SessionBlob;

public class SessionBlobClient extends SessionBlob{
	public boolean downloadFile(Metadata metadata){
		String absoluteFilename = FileSysPerformer.getInstance().getAbsoluteFilename(metadata);
		boolean suc = downloadFile(metadata, absoluteFilename);
		return suc;
	}
}
