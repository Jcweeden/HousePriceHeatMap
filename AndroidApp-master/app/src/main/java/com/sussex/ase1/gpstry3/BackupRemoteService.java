package com.sussex.ase1.gpstry3;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

/**
 * Created by User on 15/10/2016.
 */

public class BackupRemoteService extends Service
{

    final Messenger myMessenger = new Messenger(new IncomingHandler());

    public BackupRemoteService() {

    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg)
        {
            Bundle data = msg.getData();
            int gpsUpdateSec = data.getInt("gpsUpdateSec");
            int cloudUpdateSec = data.getInt("cloudUpdateSec");

            Log.i("AAAAAA","RemoteService gpsUpdateSec = " + gpsUpdateSec);
            Log.i("AAAAAA","RemoteService cloudUpdateSec = " + cloudUpdateSec);
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.i("AAAAAA","RemoteService IBinder sent");
        return myMessenger.getBinder();
    }
}
