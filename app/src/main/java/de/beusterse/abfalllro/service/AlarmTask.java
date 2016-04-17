package de.beusterse.abfalllro.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
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

    @Override
    public void run() {
        Intent intent = new Intent(context, NotifyService.class);
        intent.putExtra(NotifyService.INTENT_NOTIFY, true);
        intent.putExtra(NotifyService.NOTIFY_CAN, can);
        PendingIntent pendingIntent = PendingIntent.getService(context, can, intent, 0);

        alarmManager.set(AlarmManager.RTC, date.getTimeInMillis(), pendingIntent);
    }
}
