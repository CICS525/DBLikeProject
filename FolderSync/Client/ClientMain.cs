using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Forms;

using System.Configuration;
using System.Collections;
using SharedLibrary;

namespace Client
{
    static class ClientMain
    {
        private static string sUsername = null;
        private static string sPassword = null;
        private static string sDeviceName = null;
        private static string sRootDir = null;

        private static Queue<FileOperation> sDownloadQue = null;
        private static Queue<FileOperation> sUploadQue = null;

        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            bool settingCompelete = LoadSetting();
            if (!settingCompelete)
            {
                Application.Run(new Setting());
            }

            //load system tray

            sDownloadQue = new Queue<FileOperation>();
            sUploadQue = new Queue<FileOperation>();
        }

        static bool LoadSetting()
        {
            sUsername = ConfigurationManager.AppSettings.Get("Username");
            sPassword = ConfigurationManager.AppSettings.Get("Password");
            sDeviceName = ConfigurationManager.AppSettings.Get("DeviceName");
            sRootDir = ConfigurationManager.AppSettings.Get("RootDir");

            if( sUsername!=null && sUsername.Length>0 &&
                sPassword!=null && sPassword.Length>0 &&
                sDeviceName!=null && sDeviceName.Length>0 &&
                sRootDir!=null && sRootDir.Length>0 )
            {
                return true;    //the setting is complete, but still may need to be valided
            }
            else
            {
                return false;   //the setting is not complete, show the setting dialogue for user to complete
            }
        }

        static bool SaveSetting()
        {
            return false;
        }
    }
}
