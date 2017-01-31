package de.beusterse.abfalllro.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Service that creates tasks for the alarm manager in AlarmTask.
 *
 * Created by Felix Beuster
 */
public class DailyAlarmService extends Service {

    public class ServiceBinder extends Binder {
        DailyAlarmService getService() {
            return DailyAlarmService.this;
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

    public void cancelDailyAlarm() {
        new DailyAlarmTask(this).cancel();
    }

    public void setDailyAlarm() {
        new DailyAlarmTask(this).run();
    }
}
