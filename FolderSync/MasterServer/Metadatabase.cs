using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

using SharedLibrary;

namespace MasterServer
{
    class Metadatabase  //manipulate metadata into database
    {
        public FileMetadata UpdateMetadata(string username, FileMetadata aFileMetadata, FileBlobdata aFileBlobdata)
        {
            //if NO conflict, save the Metadata in local database & save Blobdata in Blob Server, then update FileMetadata and return
            //if conflict, reject this update and return error reason in FileMetadata. Then let client to rename the file and upate again
            //Future Plan: Maybe the Blobdata can be saved to avoid upload again, but need a mechanism to collect garbage if client does not update again.
            return null;
        }

        public bool DeleteMetadate(string username, FileMetadata aFileMetadata)  // local metadata should be udate on delete a file
        {
            return false;
        }

        public List<FileMetadata> GetCompleteMetadata(ulong sinceCounter)
        {
            return null;
        }
    }
}
