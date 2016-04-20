package de.beusterse.abfalllro.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import java.util.Calendar;

import de.beusterse.abfalllro.Can;
import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.TimePreference;

/**
 * Manages connections to services and service classes.
 *
 * Created by Felix Beuster
 */
public class ServiceManager {

    private Context context;
    public static final int SCHEDULE_IDLE   = 50;
    private int[] canAlarmTimes             = null;
    private int[] canAlarmTypes             = { Can.BLACK, Can.BLUE,
                                                Can.GREEN, Can.YELLOW};
    private ScheduleClient scheduleClient;
    private SharedPreferences pref;

    public ServiceManager(Context context, SharedPreferences pref) {
        this.context    = context;
        this.pref       = pref;
        scheduleClient  = new ScheduleClient(context);
    }

    public void bind() {
        scheduleClient.doBindService();
    }

    private void cancelScheduledAlarms() {
        for (int i = 0; i < canAlarmTypes.length; i++) {
            if (scheduleClient.hasAlarmForNotification(canAlarmTypes[i])) {
                scheduleClient.cancelAlarmForNotification(canAlarmTypes[i]);
            }
        }
    }

    public void run() {
        if (canAlarmTimes != null) {
            scheduleAlarms();
        }
    }

    private void scheduleAlarms() {
        if (scheduleClient.isBound()) {
            if (pref.getBoolean(context.getString(R.string.pref_key_notifications_active),
                                context.getResources().getBoolean(R.bool.notifications_active_default))) {
                setScheduledAlarms();
            } else {
                cancelScheduledAlarms();
            }
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scheduleAlarms();
                }
            }, SCHEDULE_IDLE);
        }
    }

    public void setAlarmTimes(int[] daysUntil) {
        canAlarmTimes = daysUntil;
    }

    private void setScheduledAlarms() {
        String alarmTime    = pref.getString(   context.getString(R.string.pref_key_notifications_time),
                                                context.getString(R.string.pref_notifications_default_time));
        int alarmHour       = TimePreference.getHour(alarmTime);
        int alarmMinute     = TimePreference.getMinute(alarmTime);

        for (int i = 0; i < canAlarmTypes.length; i++) {

            /* cancel alarm to make sure all current settings are applied */
            if (scheduleClient.hasAlarmForNotification(canAlarmTypes[i])) {
                scheduleClient.cancelAlarmForNotification(canAlarmTypes[i]);
            }

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, canAlarmTimes[i] - 1);
            cal.set(Calendar.HOUR_OF_DAY, alarmHour);
            cal.set(Calendar.MINUTE, alarmMinute);

            scheduleClient.setAlarmForNotification(cal, canAlarmTypes[i]);
        }
    }

    public void unbind() {
        if (scheduleClient != null) {
            scheduleClient.doUnbindService();
        }
    }
}
