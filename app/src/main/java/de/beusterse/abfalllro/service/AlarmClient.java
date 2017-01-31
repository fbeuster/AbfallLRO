package de.beusterse.abfalllro.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.Calendar;

/**
 * Creates and binds the alarm service.
 *
 * Created by Felix Beuster
 */
public class AlarmClient {

    private AlarmService boundService;
    private Context context;
    private boolean isBound;

    public AlarmClient(Context context) {
        this.context = context;
    }

    public void doBindService() {
        context.bindService(new Intent(context, AlarmService.class), connection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundService = ((AlarmService.ServiceBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundService = null;
        }
    };

    public void cancelAlarmForNotification(int can) {
        boundService.cancelAlarm(can);
    }

    public boolean isBound() {
        return isBound && boundService != null;
    }

    public boolean hasAlarmForNotification(int can) {
        return boundService.hasAlarm(can);
    }

    public void setAlarmForNotification(Calendar cal, int can) {
        boundService.setAlarm(cal, can);
    }

    public void doUnbindService() {
        if (isBound) {
            context.unbindService(connection);
            isBound = false;
        }
    }
}
