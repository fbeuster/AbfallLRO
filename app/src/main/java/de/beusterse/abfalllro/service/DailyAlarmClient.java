package de.beusterse.abfalllro.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Creates and binds the alarm service.
 *
 * Created by Felix Beuster
 */
public class DailyAlarmClient {

    private DailyAlarmService boundService;
    private Context context;
    private boolean isBound;

    public DailyAlarmClient(Context context) {
        this.context = context;
    }

    public void doBindService() {
        context.bindService(new Intent(context, DailyAlarmService.class), connection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundService = ((DailyAlarmService.ServiceBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundService = null;
        }
    };

    public void cancelDailyAlarm() { boundService.cancelDailyAlarm(); }

    public boolean isBound() {
        return isBound && boundService != null;
    }

    public void setDailyAlarm() {
        boundService.setDailyAlarm();
    }

    public void doUnbindService() {
        if (isBound) {
            context.unbindService(connection);
            isBound = false;
        }
    }
}
