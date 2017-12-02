package de.beusterse.abfalllro.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Calendar;

import de.beusterse.abfalllro.capsules.Can;

/**
 * Sets or cancels the daily alarm for notifications checks.
 *
 * Created by Felix Beuster
 */
public class DailyAlarmTask implements Runnable {

    private final Calendar date;
    private final AlarmManager alarmManager;
    private final Context context;

    public DailyAlarmTask(Context context) {
        this.context        = context;
        this.alarmManager   = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.date           = Calendar.getInstance();

        date.setTimeInMillis(System.currentTimeMillis());
    }

    public void cancel() {
        PendingIntent pendingIntent = getPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        cancelNotificationAlarms();
    }

    private void cancelNotificationAlarms() {
        int[] canAlarmTypes = { Can.BLACK, Can.BLUE,
                Can.GREEN, Can.YELLOW};

        for (int i = 0; i < canAlarmTypes.length; i++) {
            new NotificationAlarmTask(context, Calendar.getInstance(), canAlarmTypes[i]).cancel();
        }
    }

    private PendingIntent getPendingIntent(int flags) {
            Intent intent = new Intent(context, DailyCheckReceiver.class);

            return PendingIntent.getBroadcast(context, 0, intent, flags);
    }

    @Override
    public void run() {
        if (Build.VERSION.SDK_INT < 26) {
            /**
             * WakefulBroadcastReceiver is deprecated since Android 8.0
             * Thus the daily check/notification scheduling is temporarily disabled.
             *
             * TODO implement newer version
             */
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    date.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    getPendingIntent(0));
        }

        ComponentName receiver = new ComponentName(context, BootCompletedReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
}
