using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

using SharedLibrary;

namespace Client
{
    class EntrySession
    {
        public ServerLocation Login(string usrname, string password)
        {
            return null;
        }
    }
    class MasterSession
    {
        MasterSession(ServerLocation masterServer)
        {
            //the parameter specifies which master server to connect
        }
        public bool Connect(string username, string password)
        {
            //for the entry server and master server at seperated, so here may need to check username & password again
            return false;
        }
        public void Disconnect()
        {
        }
        public bool RegisterCallback(/*here should be a callback function*/)
        {
            //the callback function should be triggered on file updating from other device during the session
            return false;
        }
        public bool UploadFile()
        {
            return false;
        }
        public Queue<FileMetadata> GetCompleteMetadata(ulong sinceCounter)
        {
            //this method only get the complete metadata from master server, then client can retrieve every file from blob server
            //this could be used to initialize a new blank device (download everything from cloud), when set sinceCounter = 0;
            return null;
        }
    }
    class BlobSession
    {
        public bool DownloadFile(FileMetadata blobInfo)
        {
            return true;
        }
    }
}
