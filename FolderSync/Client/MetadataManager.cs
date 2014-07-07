using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

using SharedLibrary;

namespace Client
{
    class MetadataManager
    {
        private static ulong sGlobalWriteCounter = 0;    //this counter recorders how many write recording has been added on master server. The value is up to master server. Client only save the last value to know how big is the gap since last sync.
        private static List<FileMetadata> sLocalMetadata = null;    //this list cache all metadata for every file under root dir
        private static MetadataManager that = null;

        private MetadataManager()
        {
            //singleton design pattern
            if(sLocalMetadata==null)
            {
                sLocalMetadata = ReadLocalMetadata();
            }
        }

        public List<FileMetadata> ReadLocalMetadata()
        {
            return null;
        }

        public void SaveLocalMetadate()
        {
        }

        public MetadataManager GetInstance()
        {
            if(that==null)
        }
    }
}
