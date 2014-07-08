using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

using System.Security.Cryptography;

namespace SharedLibrary
{
    [Serializable]
    public class FileMetadata
    {
        public enum STATUS : byte {ERROR, NEW, LAST, HISTORY, DELETE };
        public string filename;     //with path, up to sync root
        public ulong parent;
        public ulong globalCounter;

        public STATUS status;
        public DateTime timestamp;  //this is the master server time on updating or client local time for file modification. Only the master server time is used to calculate SHA

        public string blobKey;
        public ServerLocation blobServer;

        public static SHA256 GetSHA(FileMetadata metadata, FileBlobdata blobdata)
        {
            //The SHA will be used to be the blobKey
            //Only some parts of metadata should be used in calculating SHA: filename, parent, globalCounter, timestamp, fileData.
            //The other fileds of the metadata maybe updated in future.
            //Reference http://msdn.microsoft.com/en-us/library/system.security.cryptography.sha256%28v=vs.110%29.aspx
            return null;
        }
    }

    public class FileBlobdata
    {
        public byte[] binary;
        public int length;
    }

    [Serializable]
    public class FileOperation
    {
        enum COMMAND{
            FILE_CREATE,
            FILE_UPDATE,
            FILE_DELETE
        };

    }

    [Serializable]
    public class ServerLocation
    {
    }
}
