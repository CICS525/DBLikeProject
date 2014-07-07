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
        private static ulong sGlobalWriteCounter = 0;               //this counter recorders how many write recording has been added on master server. The value is up to master server. Client only save the last value to know how big is the gap since last sync.
        private static List<FileMetadata> sLocalMetadata = null;    //this list cache all metadata for every file under root dir
        private static MetadataManager that = null;

        private MetadataManager()
        {
            //singleton design pattern
            if(sLocalMetadata==null)
            {
                ReadLocalMetadata();
            }
        }

        public bool ReadLocalMetadata()
        {
            sLocalMetadata = new List<FileMetadata>();  //update it
            return false;
        }

        public void SaveLocalMetadate()
        {
        }

        public MetadataManager GetInstance()
        {
            if(that==null)
            {
                that = new MetadataManager();
            }
            return that;
        }

        public List<FileMetadata> GetLocalMetadata()
        {
            return sLocalMetadata;
        }

        public bool UpdateMetadata(FileMetadata aFileMetadata)  // local metadata should be udate on create / update a file. There metadata is from master server, for the SHA is calculated on master server.
        {
            //overwrite into sLocalMetadata
            SaveLocalMetadate();
            return false;
        }

        public bool DeleteMetadate(FileMetadata aFileMetadata)  // local metadata should be udate on delete a file
        {
            //delete from sLocalMetadata
            SaveLocalMetadate();
            return false;
        }
    }
}
