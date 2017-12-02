package de.beusterse.abfalllro.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

/**
 * Receiver for the BOOT_COMPLETE event.
 *
 * Created by Felix Beuster
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            if (Build.VERSION.SDK_INT < 26) {
                /**
                 * WakefulBroadcastReceiver is deprecated since Android 8.0
                 * Thus the daily check/notification scheduling is temporarily disabled.
                 *
                 * TODO implement newer version
                 */
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Calendar date = Calendar.getInstance();
                Intent alarmIntent = new Intent(context, DailyCheckReceiver.class);

                date.setTimeInMillis(System.currentTimeMillis());

                alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        date.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        PendingIntent.getBroadcast(context, 0, alarmIntent, 0));
            }
        }
    }
}
