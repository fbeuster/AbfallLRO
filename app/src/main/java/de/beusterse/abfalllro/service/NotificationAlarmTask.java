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
        PendingIntent pendingIntent = getPendingIntent(getPendingIntentFlagForVersionS(PendingIntent.FLAG_UPDATE_CURRENT));
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public boolean exists() {
        return getPendingIntent(getPendingIntentFlagForVersionS(PendingIntent.FLAG_NO_CREATE)) != null;
    }

    private PendingIntent getPendingIntent(int flags) {
        Intent intent = new Intent(context, NotificationAlarmReceiver.class);
        intent.putExtra(NotificationAlarmReceiver.EXTRA_NOTIFY_CAN, can);

        return PendingIntent.getBroadcast(context, can, intent, flags);
    }

    @Override
    public void run() {
        int flag = getPendingIntentFlagForVersionS(0);
        int type = AlarmManager.RTC_WAKEUP;
        long time = date.getTimeInMillis();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(type, time, getPendingIntent(flag));
            } else {
                alarmManager.setAndAllowWhileIdle(type, time, getPendingIntent(flag));
            }

        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            try {
                alarmManager.setExactAndAllowWhileIdle(type, time, getPendingIntent(flag));
            } catch (SecurityException e) {
                alarmManager.setAndAllowWhileIdle(type, time, getPendingIntent(flag));
            }

        } else {
            try {
                alarmManager.setExact(type, time, getPendingIntent(0));
            } catch (SecurityException e) {
                alarmManager.set(type, time, getPendingIntent(0));
            }
        }
    }

    private int getPendingIntentFlagForVersionS(int flags) {
        // as per https://stackoverflow.com/a/72079329/4151333
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.FLAG_IMMUTABLE | flags;
        }

        return flags;
    }
}
