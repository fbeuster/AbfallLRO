package de.beusterse.abfalllro.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

/**
 * Service that creates tasks for the alarm manager in AlarmTask.
 *
 * Created by Felix Beuster
 */
public class ScheduleService extends Service {

    public class ServiceBinder extends Binder {
        ScheduleService getService() {
            return ScheduleService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ScheduleService", "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final IBinder binder = new ServiceBinder();

    public void cancelAlarm(Calendar cal, int can) {
        new AlarmTask(this, cal, can).cancel();
    }

    public boolean hasAlarm(Calendar cal, int can) {
        return new AlarmTask(this, cal, can).exists();
    }

    public void setAlarm(Calendar cal, int can) {
        new AlarmTask(this, cal, can).run();
    }
}
