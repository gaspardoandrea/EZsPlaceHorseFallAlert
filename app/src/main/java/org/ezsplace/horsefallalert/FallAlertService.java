package org.ezsplace.horsefallalert;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;


public class FallAlertService extends Service {
    private static FallAlert serviceImpl;

    private BroadcastReceiver mAngleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            serviceImpl.updateAngle(intent.getIntExtra(Broadcaster.DEBUG_ANGLE_VALUE, 0));
        }
    };

    public FallAlertService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAngleReceiver);
        AlertLogManager.register(getApplicationContext(), "Service stopped");
        serviceImpl.stop();
        serviceImpl = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (serviceImpl == null) {
            AlertLogManager.register(getApplicationContext(), "Service started");
            serviceImpl = new FallAlert(getApplicationContext());
            registerReceiver(mAngleReceiver, new IntentFilter(Broadcaster.DEBUG_ANGLE));
            return Service.START_REDELIVER_INTENT;
        } else {
            AlertLogManager.register(getApplicationContext(), "Reusing existing service");
            serviceImpl.setContext(getApplicationContext());
            registerReceiver(mAngleReceiver, new IntentFilter(Broadcaster.DEBUG_ANGLE));
            return Service.START_REDELIVER_INTENT;
        }
    }
}
