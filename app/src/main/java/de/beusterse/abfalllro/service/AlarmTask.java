package de.beusterse.abfalllro.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Sets or cancels induvidual alarms for notifications.
 *
 * Created by Felix Beuster
 */
public class AlarmTask implements Runnable {

    private final Calendar date;
    private final AlarmManager alarmManager;
    private final Context context;
    private int can;

    public AlarmTask(Context context, Calendar date, int can) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.date = date;
        this.can = can;
    }

    public void cancel() {
        PendingIntent pendingIntent = getPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public boolean exists() {
        return getPendingIntent(PendingIntent.FLAG_NO_CREATE) != null;
    }

    private PendingIntent getPendingIntent(int flags) {
        Intent intent = new Intent(context, NotifyService.class);
        intent.putExtra(NotifyService.INTENT_NOTIFY, true);
        intent.putExtra(NotifyService.NOTIFY_CAN, can);
        return PendingIntent.getService(context, can, intent, flags);
    }

    @Override
    public void run() {
        alarmManager.set(AlarmManager.RTC, date.getTimeInMillis(), getPendingIntent(0));
    }
}
