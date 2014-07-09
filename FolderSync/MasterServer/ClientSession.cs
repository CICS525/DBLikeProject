using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

using SharedLibrary;
using System.Net.Sockets;
using System.Threading;

namespace MasterServer
{
    class DeviceSession
    {
        //This class maintain the information about a connected device.
        Socket socket = null;   //the socket is alway active when the Client is connected to Master Server
    }
    class AccountSession
    {
        //This class maintains the session to communicate with an active account
        //an account may have several devices, they all handled here in a single object
        //Master server should create a thread to handel all requests from multiple devices for same account, the thread may exit when all devices are disconnected.

        private List<DeviceSession> deviceList = null;
        public bool Takeover()
        {
            Thread accountThread = null;
            accountThread = new Thread(new ThreadStart(ThreadCallback));
            accountThread.Start();

            return false;
        }

        private static void ThreadCallback()
        {
        }
    }

    class ClientSessionManager
    {
        //singleton design pattern
        private static List<AccountSession> sAccountSessionPool = null;
        private static ClientSessionManager that = null;

        private ClientSessionManager()
        {
            sAccountSessionPool = new List<AccountSession>();
        }

        public ClientSessionManager GetInstance()
        {
            if(that==null)
            {
                that = new ClientSessionManager();
            }
            return that;
        }

        public bool AcceptClient(/*parameter need be defined*/)
        {
            //the coming client should be dispatched into a new / existing AccountSession to handle.
            return false;
        }
    }
}
