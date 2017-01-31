package de.beusterse.abfalllro.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.Calendar;

/**
 * Service that creates tasks for the alarm manager in AlarmTask.
 *
 * Created by Felix Beuster
 */
public class AlarmService extends Service {

    public class ServiceBinder extends Binder {
        AlarmService getService() {
            return AlarmService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final IBinder binder = new ServiceBinder();

    public void cancelAlarm(int can) {
        new AlarmTask(this, Calendar.getInstance(), can).cancel();
    }

    public boolean hasAlarm(int can) {
        return new AlarmTask(this, Calendar.getInstance(), can).exists();
    }

    public void setAlarm(Calendar cal, int can) {
        new AlarmTask(this, cal, can).run();
    }
}
