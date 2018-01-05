package de.beusterse.abfalllro.service.legacy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Calendar;

import de.beusterse.abfalllro.BuildConfig;
import de.beusterse.abfalllro.capsules.Can;
import de.beusterse.abfalllro.service.DailyCheck;
import de.beusterse.abfalllro.service.NotificationAlarmTask;

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
        /*
            The daily check and sync will use the JobScheduler for
            Android L and higher, and falls back to the AlarmTask
            for older versions.
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            long interval               = DailyCheck.INTERVAL;

            if (BuildConfig.DEBUG) {
                interval = DailyCheck.INTERVAL_DEBUG;
            }

            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    date.getTimeInMillis(),
                    interval,
                    getPendingIntent(0));
        }

        ComponentName receiver = new ComponentName(context, BootCompletedReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
}
