using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.IO;

namespace Client
{
    class FileSysWatcher
    {
        FileSysWatcher(string rootDir, FileSystemEventHandler onChanged)
        {
            //the watcher here only care about fclose() after fwrite(), then the file is read to be uploaded to cloud
        }
    }
}
