package de.beusterse.abfalllro.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

/**
 * Sets or cancels individual alarms for notifications.
 *
 * Created by Felix Beuster
 */
public class NotificationAlarmTask implements Runnable {

    private final Calendar date;
    private final AlarmManager alarmManager;
    private final Context context;
    private int can;

    public NotificationAlarmTask(Context context, Calendar date, int can) {
        this.context        = context;
        this.alarmManager   = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.date           = date;
        this.can            = can;
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
        Intent intent = new Intent(context, NotificationAlarmReceiver.class);
        intent.putExtra(NotificationService.EXTRA_INTENT_NOTIFY, true);
        intent.putExtra(NotificationService.EXTRA_NOTIFY_CAN, can);

        return PendingIntent.getBroadcast(context, can, intent, flags);
    }

    @Override
    public void run() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), getPendingIntent(0));
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), getPendingIntent(0));
        }
    }
}
