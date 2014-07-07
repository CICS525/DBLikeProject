using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

using SharedLibrary;

namespace MasterServer
{
    //-------------------------------
    // Database Tables:
    // 1: account
    //  Username, Password, GlobalWriteCounter, ...
    // 2: metadata
    //  See Metadata define
    //-------------------------------
    class MasterMain
    {
        private bool Login(string username, string password)
        {
            return false;
        }
        private bool CreateAccount(string username, string password)    //this method should be triggered by entry server in final release
        {
            return false;
        }
        static void Main(string[] args)
        {
        }
    }
}
