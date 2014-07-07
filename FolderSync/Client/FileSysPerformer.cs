using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

using SharedLibrary;

namespace Client
{
    class FileSysPerformer
    {
        public bool UpdateFile(string filename, FileMetadata blobInfo)
        {
            //write file from cloud (specifed by BlobInfo) to local disk
            return false;
        }
        public bool DeleteFile(string filename)
        {
            //the file name should be based on path to root sync directory
            return false;
        }
    }
}
