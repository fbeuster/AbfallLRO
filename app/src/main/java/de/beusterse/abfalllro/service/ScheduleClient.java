package de.beusterse.abfalllro.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.Calendar;

/**
 * Created by Felix Beuster
 */
public class ScheduleClient {

    private ScheduleService boundService;
    private Context context;
    private boolean isBound;

    public ScheduleClient(Context context) {
        this.context = context;
    }

    public void doBindService() {
        context.bindService(new Intent(context, ScheduleService.class), connection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundService = ((ScheduleService.ServiceBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundService = null;
        }
    };

    public boolean isBound() {
        return isBound && boundService != null;
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
