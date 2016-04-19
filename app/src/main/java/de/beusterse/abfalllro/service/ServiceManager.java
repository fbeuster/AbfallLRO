package de.beusterse.abfalllro.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import java.util.Calendar;

import de.beusterse.abfalllro.RawNotification;
import de.beusterse.abfalllro.TimePreference;

/**
 * Manages connections to services and service classes.
 *
 * Created by Felix Beuster
 */
public class ServiceManager {

    private Context context;
    private int[] canAlarmTimes = null;
    private int[] canAlarmTypes = { RawNotification.BLACK_CAN, RawNotification.BLUE_CAN,
                                    RawNotification.GREEN_CAN, RawNotification.YELLOW_CAN};
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
            if (pref.getBoolean("pref_notifications_active", false)) {
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
            }, 50);
        }
    }

    public void setAlarmTimes(int[] daysUntil) {
        canAlarmTimes = daysUntil;
    }

    private void setScheduledAlarms() {
        String alarmTime    = pref.getString("pref_notifications_time", "18:00");
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
